pipeline {
    agent any

    environment {
        SPRING_HOST = credentials('spring-ec2-host')
        SPRING_USER = 'ubuntu'
        DEPLOY_DIR = '/home/ubuntu/deploy'
        WAR_FILE = 'build/libs/backend-1.0-SNAPSHOT.war'
        MOCK_MYDATA_DIR = 'mock-mydata'
        JMX_EXPORTER_CONFIG = 'monitoring/jmx-exporter/config.yml'
    }

    stages {
        stage('Build') {
            steps {
                sh '''
                    chmod +x gradlew
                    ./gradlew clean war --no-daemon
                '''
            }
        }

        stage('Deploy') {
            steps {
                withCredentials([
                    file(credentialsId: 'spring-runtime-env', variable: 'ENV_FILE')
                ]) {
                    sshagent(credentials: ['spring-ec2-ssh']) {
                        sh '''
                            set -eu

                            ssh -o StrictHostKeyChecking=accept-new \
                              ${SPRING_USER}@${SPRING_HOST} \
                              "mkdir -p ${DEPLOY_DIR}/mock-mydata ${DEPLOY_DIR}/monitoring/jmx-exporter"

                            scp -o StrictHostKeyChecking=accept-new \
                              "${WAR_FILE}" \
                              ${SPRING_USER}@${SPRING_HOST}:${DEPLOY_DIR}/backend.war

                            scp -o StrictHostKeyChecking=accept-new \
                              "${ENV_FILE}" \
                              ${SPRING_USER}@${SPRING_HOST}:${DEPLOY_DIR}/.env.next

                            scp -o StrictHostKeyChecking=accept-new \
                              ${MOCK_MYDATA_DIR}/package.json \
                              ${MOCK_MYDATA_DIR}/package-lock.json \
                              ${MOCK_MYDATA_DIR}/db.json \
                              ${MOCK_MYDATA_DIR}/server.js \
                              ${SPRING_USER}@${SPRING_HOST}:${DEPLOY_DIR}/mock-mydata/

                            scp -o StrictHostKeyChecking=accept-new \
                              "${JMX_EXPORTER_CONFIG}" \
                              ${SPRING_USER}@${SPRING_HOST}:${DEPLOY_DIR}/monitoring/jmx-exporter/

                            ssh -o StrictHostKeyChecking=accept-new \
                              ${SPRING_USER}@${SPRING_HOST} '
                                set -eu
                                mv /home/ubuntu/deploy/.env.next /home/ubuntu/deploy/.env

                                docker network inspect buttie-network > /dev/null 2>&1 \
                                  || docker network create buttie-network

                                jmx_agent_path=/home/ubuntu/deploy/monitoring/jmx-exporter/jmx_prometheus_javaagent-1.5.0.jar
                                jmx_agent_url=https://github.com/prometheus/jmx_exporter/releases/download/1.5.0/jmx_prometheus_javaagent-1.5.0.jar
                                jmx_agent_sha256=0315f3f657876302c6205a98d4036ec775dca529c5d0419ca60ee669c688239f
                                curl --fail --location --proto "=https" --tlsv1.2 \
                                  --output "$jmx_agent_path.next" "$jmx_agent_url"
                                printf "%s  %s\\n" "$jmx_agent_sha256" "$jmx_agent_path.next" \
                                  | sha256sum -c -
                                mv "$jmx_agent_path.next" "$jmx_agent_path"

                                docker rm -f buttie-mydata-mock || true

                                docker run --rm \
                                  -v /home/ubuntu/deploy/mock-mydata/package.json:/app/package.json:ro \
                                  -v /home/ubuntu/deploy/mock-mydata/package-lock.json:/app/package-lock.json:ro \
                                  -v buttie-mydata-node-modules:/app/node_modules \
                                  -w /app \
                                  node:20-alpine \
                                  npm ci --omit=dev

                                docker run -d \
                                  --name buttie-mydata-mock \
                                  --network buttie-network \
                                  --restart unless-stopped \
                                  -v /home/ubuntu/deploy/mock-mydata/package.json:/app/package.json:ro \
                                  -v /home/ubuntu/deploy/mock-mydata/package-lock.json:/app/package-lock.json:ro \
                                  -v /home/ubuntu/deploy/mock-mydata/db.json:/app/db.json:ro \
                                  -v /home/ubuntu/deploy/mock-mydata/server.js:/app/server.js:ro \
                                  -v buttie-mydata-node-modules:/app/node_modules \
                                  -w /app \
                                  node:20-alpine \
                                  npm start

                                docker rm -f buttie-api || true

                                METRICS_BIND_ADDRESS=$(sed -n "s/^METRICS_BIND_ADDRESS=//p" \
                                  /home/ubuntu/deploy/.env | tail -n 1)
                                METRICS_BIND_ADDRESS=${METRICS_BIND_ADDRESS:-127.0.0.1}

                                # t3.micro(1 GiB)에서 OS와 mock 서버가 사용할 여유를 남긴다.
                                # 운영 수치가 쌓이면 .env 값으로만 조정할 수 있다.
                                APP_CONTAINER_MEMORY=$(sed -n "s/^APP_CONTAINER_MEMORY=//p" \
                                  /home/ubuntu/deploy/.env | tail -n 1)
                                APP_CONTAINER_MEMORY=${APP_CONTAINER_MEMORY:-640m}
                                APP_CONTAINER_MEMORY_RESERVATION=$(sed -n \
                                  "s/^APP_CONTAINER_MEMORY_RESERVATION=//p" \
                                  /home/ubuntu/deploy/.env | tail -n 1)
                                APP_CONTAINER_MEMORY_RESERVATION=${APP_CONTAINER_MEMORY_RESERVATION:-512m}
                                JVM_MAX_RAM_PERCENTAGE=$(sed -n \
                                  "s/^JVM_MAX_RAM_PERCENTAGE=//p" \
                                  /home/ubuntu/deploy/.env | tail -n 1)
                                JVM_MAX_RAM_PERCENTAGE=${JVM_MAX_RAM_PERCENTAGE:-60.0}
                                mkdir -p /home/ubuntu/deploy/logs

                                docker run -d \
                                  --name buttie-api \
                                  --restart unless-stopped \
                                  --network buttie-network \
                                  --memory "$APP_CONTAINER_MEMORY" \
                                  --memory-reservation "$APP_CONTAINER_MEMORY_RESERVATION" \
                                  --env-file /home/ubuntu/deploy/.env \
                                  -e MYDATA_MOCK_BASE_URL=http://buttie-mydata-mock:3000 \
                                  -e CATALINA_OPTS="-XX:+UseG1GC -XX:InitialRAMPercentage=25.0 -XX:MaxRAMPercentage=$JVM_MAX_RAM_PERCENTAGE -XX:MaxGCPauseMillis=200 -Xlog:gc*,safepoint:file=/usr/local/tomcat/logs/gc.log:time,level,tags:filecount=5,filesize=10M -javaagent:/opt/jmx-exporter/jmx_prometheus_javaagent-1.5.0.jar=9404:/opt/jmx-exporter/config.yml" \
                                  -p 8080:8080 \
                                  -p "${METRICS_BIND_ADDRESS}:9404:9404" \
                                  -v /home/ubuntu/deploy/monitoring/jmx-exporter/jmx_prometheus_javaagent-1.5.0.jar:/opt/jmx-exporter/jmx_prometheus_javaagent-1.5.0.jar:ro \
                                  -v /home/ubuntu/deploy/monitoring/jmx-exporter/config.yml:/opt/jmx-exporter/config.yml:ro \
                                  -v /home/ubuntu/deploy/logs:/usr/local/tomcat/logs \
                                  -v /home/ubuntu/deploy/backend.war:/usr/local/tomcat/webapps/ROOT.war:ro \
                                  tomcat:9.0-jdk17-temurin
                              '
                        '''
                    }
                }
            }
        }

        stage('Verify') {
            steps {
                sshagent(credentials: ['spring-ec2-ssh']) {
                    sh '''
                        ssh ${SPRING_USER}@${SPRING_HOST} '
                            set -eu

                            docker inspect --format="{{.State.Running}}" buttie-mydata-mock \
                              | grep -qx true \
                              || {
                                docker logs buttie-mydata-mock || true
                                exit 1
                              }

                            mock_ready=false
                            for i in $(seq 1 60); do
                                if docker exec buttie-mydata-mock \
                                  wget -qO- http://127.0.0.1:3000/health > /dev/null; then
                                    mock_ready=true
                                    break
                                fi
                                sleep 2
                            done

                            if [ "$mock_ready" != true ]; then
                                docker inspect --format="status={{.State.Status}} exit={{.State.ExitCode}} error={{.State.Error}}" \
                                  buttie-mydata-mock || true
                                docker logs buttie-mydata-mock || true
                                exit 1
                            fi

                            metrics_address=$(docker port buttie-api 9404/tcp | head -n 1)
                            if [ -z "$metrics_address" ] \
                              || ! curl -fsS "http://$metrics_address/metrics" \
                                | grep -q "^jmx_scrape_error"; then
                                docker logs buttie-api || true
                                exit 1
                            fi

                            docker inspect --format="memory={{.HostConfig.Memory}} reservation={{.HostConfig.MemoryReservation}}" \
                              buttie-api

                            for i in $(seq 1 30); do
                                curl -fsS http://localhost:8080/swagger-ui.html > /dev/null && exit 0
                                sleep 2
                            done

                            docker logs buttie-api || true
                            exit 1
                        '
                    '''
                }
            }
        }
    }

    post {
    success {
        withCredentials([
            string(credentialsId: 'slack-webhook-url', variable: 'SLACK_WEBHOOK')
        ]) {
            sh """
                curl -sS -X POST \
                  -H 'Content-Type: application/json' \
                  --data '{"text":"✅ Buttie Backend 배포 성공\\n빌드: #${env.BUILD_NUMBER}\\n로그: ${env.BUILD_URL}console"}' \
                  "\$SLACK_WEBHOOK"
            """
        }
    }

    failure {
        withCredentials([
            string(credentialsId: 'slack-webhook-url', variable: 'SLACK_WEBHOOK')
        ]) {
            sh """
                curl -sS -X POST \
                  -H 'Content-Type: application/json' \
                  --data '{"text":"❌ Buttie Backend 배포 실패\\n빌드: #${env.BUILD_NUMBER}\\n로그: ${env.BUILD_URL}console"}' \
                  "\$SLACK_WEBHOOK"
            """
        }
    }
}
}
