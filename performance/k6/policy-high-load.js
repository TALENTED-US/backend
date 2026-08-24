import http from 'k6/http';
import {check} from 'k6';

const baseUrl = __ENV.BASE_URL || 'http://127.0.0.1:8080';

export const options = {
  scenarios: {
    policy_read: {
      executor: 'ramping-arrival-rate',
      startRate: 20,
      timeUnit: '1s',
      preAllocatedVUs: 30,
      maxVUs: 100,
      stages: [
        {duration: '30s', target: 100},
        {duration: '30s', target: 300},
        {duration: '45s', target: 500},
        {duration: '15s', target: 0},
      ],
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
}
