import http from 'k6/http';
import {check, sleep} from 'k6';

const baseUrl = __ENV.BASE_URL || 'http://127.0.0.1:8080';

export const options = {
  stages: [
    {duration: '10s', target: 1},
    {duration: '30s', target: 5},
    {duration: '30s', target: 10},
    {duration: '30s', target: 20},
    {duration: '10s', target: 0},
  ],
  thresholds: {
    checks: ['rate>0.99'],
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<1000'],
  },
};

export default function () {
  const response = http.get(
    `${baseUrl}/api/catalog/policy?page=1&size=10`,
    {tags: {name: 'GET /api/catalog/policy'}},
  );

  check(response, {
    'status is 200': (res) => res.status === 200,
    'successful application response': (res) => res.status === 200 && res.json('isSuccess') === true,
  });
  sleep(1);
}
