import http from 'k6/http';
import { check, sleep } from 'k6';
import { randomString } from 'https://jslib.k6.io/k6-utils/1.1.0/index.js';

export let options = {
    stages: [
        { duration: '30s', target: 10 },
        { duration: '1m', target: 50 },
        { duration: '30s', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<500'],
    },
};

export default function () {
    let randomUser = randomString(6);
    const password = 'password123';

    let registerPayload = JSON.stringify({
        username: `user_${randomUser}`,
        email: `user_${randomUser}@example.com`,
        password: password,
        role: 'USER'
    });

    let registerRes = http.post('http://localhost:8081/api/auth/register', registerPayload, {
        headers: { 'Content-Type': 'application/json' },
    });

    check(registerRes, {
        'register status 200 or 400': (r) => r.status === 200 || r.status === 400,
    });

    sleep(1);

    let loginPayload = JSON.stringify({
        email: `user_${randomUser}@example.com`,
        password: password,
    });

    let loginRes = http.post('http://localhost:8081/api/auth/login', loginPayload, {
        headers: { 'Content-Type': 'application/json' },
    });

    check(loginRes, {
        'login status 200': (r) => r.status === 200,
        'has token': (r) => r.json('token') !== undefined && r.json('token') !== '',
    });

    sleep(1);

    let token = loginRes.json('token');

    if (!token) return;

    let meRes = http.get('http://localhost:8081/api/auth/me', {
        headers: { Authorization: `Bearer ${token}` },
    });

    check(meRes, {
        'me status 200': (r) => r.status === 200,
    });

    sleep(1);
}