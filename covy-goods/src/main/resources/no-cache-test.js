import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '10s', target: 50 },  // 10초 동안 50명으로 증가
    { duration: '30s', target: 50 },  // 30초 유지
    { duration: '10s', target: 0 },   // 종료
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95% 요청 500ms 미만
    http_req_failed: ['rate<0.01'],   // 실패율 1% 미만
  },
};

export default function () {
  const url = 'http://127.0.0.1:8000/covy-goods/user/초코'; // 실제 API 주소로 교체
  const res = http.get(url);

  check(res, {
    'status is 200': (r) => r.status === 200,
    'response time < 500ms': (r) => r.timings.duration < 500,
  });

  sleep(1);
}
