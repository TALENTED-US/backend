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

`db.json`에는 사용자 키 0~9별로 계좌 2~4개와 체크카드 2~4개가 서로 다르게 들어 있습니다.
실제 상품명 기반의 계좌·체크카드와 함께 쇼핑, 카페, 여행·여가, 배달·외식, 건강, 교통,
취업준비, 술·유흥에 치중된 소비 페르소나 8명, 균형 소비자 1명, 수입 우위·저축형 1명을 제공합니다.

## 지출 카테고리 분류

카드 승인내역은 다음 순서로 지출 카테고리를 분류합니다.

1. `merchant_regno`(가맹점 사업자등록번호)가 Mock 매핑에 있으면 해당 카테고리를 사용합니다.
2. 등록번호 매핑이 없으면 `merchant_category_code`(가맹점 업종 코드)로 분류합니다.
3. 두 정보로 분류할 수 없으면 `기타 금융`으로 처리합니다.

| 지출 카테고리 | Mock 가맹점 예시 |
| --- | --- |
| 식비 | 마켓컬리, 배달의민족, 성수다이닝, GS25 편의점 |
| 술, 유흥 | 을지로 포차 |
| 카페, 간식 | 스타벅스 강남역점 |
| 취준 비용 | 교보문고 광화문점, 해커스어학원 |
| 쇼핑 | 올리브영 홍대점, 무신사, 쿠팡, 준오헤어 강남점 |
| 취미, 여가 | 넷플릭스, NOL 인터파크, 대한항공 |
| 주거, 통신 | 계좌 거래의 월세 및 공과금 이체 |
| 교통, 유류비 | 서울교통공사, SK에너지 역삼주유소 |
| 의료, 건강, 피트니스 | 서울튼튼병원, 바디채널 피트니스, 종로약국 |
| 기타 금융 | 등록번호와 업종 코드가 모두 없는 미분류 거래 |

`merchantCategoryMappings`에는 Mock 가맹점의 등록번호·업종 코드·표시명을 함께 정의합니다.
백엔드 분류 규칙은 `MerchantCategoryClassifier`에서 관리합니다.
