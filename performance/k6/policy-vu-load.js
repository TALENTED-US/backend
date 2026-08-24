import http from 'k6/http';
import {check, sleep} from 'k6';

const baseUrl = __ENV.BASE_URL || 'http://127.0.0.1:8080';

export const options = {
  scenarios: {
    concurrent_policy_read: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        {duration: '30s', target: 500},
        {duration: '30s', target: 500},
        {duration: '30s', target: 1000},
        {duration: '30s', target: 1000},
        {duration: '45s', target: 2000},
        {duration: '30s', target: 2000},
        {duration: '60s', target: 3000},
        {duration: '60s', target: 3000},
        {duration: '30s', target: 0},
      ],
      gracefulRampDown: '15s',
      gracefulStop: '30s',
    },
  },
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

  // A 3-second think time makes 3,000 VUs roughly 1,000 RPS when responses are fast.
  sleep(3);
}
