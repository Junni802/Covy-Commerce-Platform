import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '10s', target: 50 },
    { duration: '30s', target: 50 },
    { duration: '10s', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<3000'], // 캐싱 효과 기대
    http_req_failed: ['rate<0.01'],
  },
};

export default function () {
  const url = 'http://127.0.0.1:8000/covy-goods/user/초코';
  const res = http.get(url);

  check(res, {
    'status is 200': (r) => r.status === 200,
    'response time < 5000ms': (r) => r.timings.duration < 200,
  });

  sleep(1);
}
