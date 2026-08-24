# Local performance baseline

These measurements run against an isolated local MySQL/Redis stack. They are
useful for comparing a configuration on the same Mac, but are not production
capacity figures.

## Scripts

- `k6/policy-baseline.js`: 1 → 5 → 10 → 20 VU read-only policy query.
- `k6/policy-high-load.js`: ramping-arrival-rate at 100 → 300 → 500 RPS for
  the same endpoint.
- `k6/policy-vu-load.js`: 500 → 1,000 → 2,000 → 3,000 concurrent VUs,
  with a 3-second think time between read-only policy queries.

All JSON outputs are intentionally ignored by Git and are kept under
`performance/results/`.

## 2026-08-24 local results

Endpoint: `GET /api/catalog/policy?page=1&size=10`

| Scenario | GC | Requests | Error rate | p95 | Max | Dropped iterations |
| --- | --- | ---: | ---: | ---: | ---: | ---: |
| Low load (1 → 20 VU) | Default G1, no container limit | 833 | 0% | 17.81ms | 23.02ms | 0 |
| Low load (1 → 20 VU) | G1, 640MiB container / 384MiB heap | 833 | 0% | 19.12ms | 25.09ms | 0 |
| High load (100 → 500 RPS, 5-run average) | G1, 640MiB container / 384MiB heap | 29,540 | 0% | 8.93ms | 129.09ms* | 9.2* |
| High load (100 → 500 RPS, 5-run average) | Serial, 640MiB container / 384MiB heap | 29,536 | 0% | 9.11ms | 210.13ms* | 13.6* |
| VU load (500 → 3,000 VU, 3s think time) | G1, 640MiB container / 384MiB heap | 193,120 | 0% | 30.34ms | 950.18ms | 0 |

`*` Average of each run's value. G1 p95 values were 7.24ms, 9.34ms, 9.16ms,
10.45ms, and 8.46ms (median 9.16ms; range 7.24–10.45ms). Serial p95 values
were 7.46ms, 9.30ms, 11.12ms, 9.69ms, and 7.99ms (median 9.30ms; range
7.46–11.12ms).

Across five local runs, G1 reduced average high-load p95 by about 2.0% and
average dropped iterations by about 32%. The result supports G1 as the safer
explicit choice for this 640MiB container, but does not establish a large
latency improvement. Keep `-XX:+UseG1GC` explicit: otherwise Java 17
ergonomics selects Serial GC under this container limit.

## 3,000 VU local result

The VU scenario is a read-only request from up to 3,000 concurrent virtual
users. Each virtual user waits three seconds after a request, so the 3,000 VU
stage approximates up to 1,000 RPS when responses are fast; it is not 3,000
requests per second or 3,000 distinct authenticated users.

The API completed all requests successfully, but the Prometheus samples during
the test show the beginning of database-pool pressure: Tomcat busy threads
reached 147 of 200, Hikari active connections reached 10, and up to 121 threads
were awaiting a connection. JVM current/peak threads reached 222 and heap usage
reached about 215MiB. G1 Young Generation collection time increased by about
4.37 seconds, with no G1 Old Generation collection observed.

Treat this as a local stress-test boundary, not production capacity. k6 and the
API container share the same Mac, the endpoint is a single public read query,
and Prometheus has a 15-second scrape interval. The next improvement candidate
is therefore Hikari/DB-query capacity and connection-wait alerting, rather than
raising the advertised concurrent-user limit.
