# Buttie Monitoring

Prometheus and Grafana run on a dedicated `buttie-monitoring` EC2 instance. The
API stays on `buttie-spring`; Prometheus scrapes the API JVM metrics over the
VPC private network.

## Network and EC2 prerequisites

1. Create `buttie-monitoring` as at least `t3.small` in the same VPC as
   `buttie-spring`.
2. Give the monitoring instance a security group that permits administrator SSH
   access. Keep ports `9090` and `3000` closed to the public Internet.
3. Add an inbound rule to the Spring instance security group:
   `TCP 9404` from the **monitoring security group only**.
4. Add `METRICS_BIND_ADDRESS=<buttie-spring private IPv4>` to the Jenkins
   `spring-runtime-env` credential. Until this is set, the deployment binds
   metrics to `127.0.0.1`, which is intentionally not reachable from another
   instance.
5. Add the Spring private IP to
   `monitoring/prometheus/targets/api.yml` on the monitoring instance. Start
   from `api.yml.example`; do not commit the real target file.

Example `api.yml`:

```yaml
- targets:
    - "10.0.0.10:9404"
  labels:
    service: buttie-api
    environment: production
```

## Monitoring EC2 installation

Copy the `monitoring/` directory to the monitoring EC2, create a local
`.env`, then start the stack.

```dotenv
GRAFANA_ADMIN_USER=admin
GRAFANA_ADMIN_PASSWORD=replace-with-a-long-unique-password
GRAFANA_ROOT_URL=https://grafana.example.com
PROMETHEUS_RETENTION_TIME=15d
```

```sh
docker compose --env-file .env -f docker-compose.monitoring.yml up -d
```

Grafana is deliberately bound to `127.0.0.1:3000`. Use an SSH tunnel during
initial setup, or add an authenticated HTTPS reverse-proxy route on
`buttie-nginx` before exposing it to users.

## Verification

On the Spring EC2 after an API deployment:

```sh
curl -fsS http://127.0.0.1:9404/metrics | grep -E 'jvm_threads_live|hikari_connections_active'
```

On the monitoring EC2, verify that Prometheus can reach the private endpoint:

```sh
curl -fsS http://<buttie-spring-private-ip>:9404/metrics | head
```

Then open Grafana through the SSH tunnel or reverse proxy. The provisioned
`Buttie API Runtime` dashboard shows JVM, Tomcat, Hikari, and GC metrics.

## Spring JVM memory and GC logs

The Spring deployment limits the API container to `640m` by default; the JVM
can use up to 60% of that limit for heap. GC logs are retained on the Spring
EC2 at `/home/ubuntu/deploy/logs/gc.log` (five files of up to 10MB).

The Jenkins `spring-runtime-env` credential may contain the following values
to tune a later deployment without changing source code:

```dotenv
APP_CONTAINER_MEMORY=640m
APP_CONTAINER_MEMORY_RESERVATION=512m
JVM_MAX_RAM_PERCENTAGE=60.0
```

Tune these only after checking heap usage, GC pause, and Hikari wait metrics.
For a `t3.micro`, prefer an instance size increase over raising this limit.

## Scope

This first version intentionally does not run MySQL exporter, cAdvisor, or
node exporter. Add them after the JVM/Tomcat/Hikari baseline is stable.
