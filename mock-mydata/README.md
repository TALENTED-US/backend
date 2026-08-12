# Buttie MyData Mock Server

JSON Server를 이용해 마이데이터 v2 계좌·체크카드 API를 시연합니다.

## 실행

```bash
npm install
npm start
```

기본 포트는 `3000`입니다. 백엔드는 `MYDATA_MOCK_BASE_URL`로 주소를 변경할 수 있습니다.

## 사용자별 데이터

백엔드는 로그인 사용자의 `userId % 10` 값으로 Mock 접근 토큰을 만들어 전달합니다.

```http
Authorization: Bearer mock-access-token-user-1

GET /v2/bank/accounts
GET /v2/card/cards
GET /v2/card/cards/MOCK-CARD-01-01
GET /v2/card/cards/MOCK-CARD-01-01/approval-domestic
```

`db.json`에는 사용자 키 0~9 각각에 대해 계좌 3개와 체크카드 3개가 들어 있습니다.
