pipeline {
    agent any

    environment {
        SPRING_HOST = credentials('spring-ec2-host')
        SPRING_USER = 'ubuntu'
        DEPLOY_DIR = '/home/ubuntu/deploy'
        WAR_FILE = 'build/libs/backend-1.0-SNAPSHOT.war'
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
                              "mkdir -p ${DEPLOY_DIR}"

                            scp -o StrictHostKeyChecking=accept-new \
                              "${WAR_FILE}" \
                              ${SPRING_USER}@${SPRING_HOST}:${DEPLOY_DIR}/backend.war

                            scp -o StrictHostKeyChecking=accept-new \
                              "${ENV_FILE}" \
                              ${SPRING_USER}@${SPRING_HOST}:${DEPLOY_DIR}/.env.next

                            ssh -o StrictHostKeyChecking=accept-new \
                              ${SPRING_USER}@${SPRING_HOST} '
                                mv /home/ubuntu/deploy/.env.next /home/ubuntu/deploy/.env
                                docker rm -f buttie-api || true

                                docker run -d \
                                  --name buttie-api \
                                  --restart unless-stopped \
                                  --env-file /home/ubuntu/deploy/.env \
                                  -p 8080:8080 \
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
                            for i in {1..30}; do
                                curl -fsS http://localhost:8080/swagger-ui.html > /dev/null && exit 0
                                sleep 2
                            done

                            docker logs buttie-api
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
                sh '''
                    curl -sS -X POST \
                      -H "Content-Type: application/json" \
                  --data '{"text":"✅ Buttie Backend 배포 성공\\n빌드: #${env.BUILD_NUMBER}\\n로그: ${env.BUILD_URL}console"}' \
                      "$SLACK_WEBHOOK"
                '''
            }
        }

        failure {
            withCredentials([
                string(credentialsId: 'slack-webhook-url', variable: 'SLACK_WEBHOOK')
            ]) {
                sh '''
                    curl -sS -X POST \
                      -H "Content-Type: application/json" \
                  --data '{"text":"❌ Buttie Backend 배포 실패\\n빌드: #${env.BUILD_NUMBER}\\n로그: ${env.BUILD_URL}console"}' \
                      "$SLACK_WEBHOOK"
                '''
            }
        }
    }
}
