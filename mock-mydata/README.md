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

`db.json`에는 사용자 키 0~9별로 계좌 2~4개와 체크카드 2~4개가 서로 다르게 들어 있습니다. 실제 상품명 기반의 계좌·체크카드와 함께 쇼핑, 카페, 여행·여가,
배달·외식, 건강, 교통, 취업준비, 술·유흥에 치중된 소비 페르소나 8명, 균형 소비자 1명, 수입 우위·저축형 1명을 제공합니다.

## 지출 카테고리 분류

Mock 카드 승인내역은 다음 순서로 지출 카테고리를 분류합니다.

1. `merchant_regno`(가맹점 사업자등록번호)가 Mock 매핑에 있으면 해당 카테고리를 사용합니다.
2. 등록번호 매핑이 없으면 정규화한 `merchant_name`(가맹점명)으로 분류합니다.
3. 두 정보로 분류할 수 없으면 `기타 금융`으로 처리합니다.

| 지출 카테고리        | Mock 가맹점 예시                               |
|----------------------|------------------------------------------------|
| 식비                 | 마켓컬리, 배달의민족, 성수다이닝, GS25 편의점  |
| 술, 유흥             | 을지로 포차                                    |
| 카페, 간식           | 스타벅스 강남역점                              |
| 취준 비용            | 교보문고 광화문점, 해커스어학원                |
| 쇼핑                 | 올리브영 홍대점, 무신사, 쿠팡, 준오헤어 강남점 |
| 취미, 여가           | 넷플릭스, NOL 인터파크, 대한항공               |
| 주거, 통신           | 계좌 거래의 월세 및 공과금 이체                |
| 교통, 유류비         | 서울교통공사, SK에너지 역삼주유소              |
| 의료, 건강, 피트니스 | 서울튼튼병원, 바디채널 피트니스, 종로약국      |
| 기타 금융            | 등록번호와 가맹점명으로 분류할 수 없는 거래   |

`merchantCategoryMappings`에는 Mock 가맹점의 등록번호·표시명을 함께 정의합니다. 백엔드 분류 규칙은 `MerchantCategoryClassifier`에서
관리합니다.

## 실제 MyData 연동 시 분류 전략

실제 MyData에서는 하나의 필드만으로 모든 거래를 정확히 분류하기 어렵습니다. 제공 범위와 동의 여부에 따라 아래 정보를 우선순위로 조합하고, 정보가 부족한 거래는
`기타 금융`으로 처리합니다.

| 우선순위 | 활용 정보        | 분류 방법                                                                              | 적용 범위     |
|----------|------------------|----------------------------------------------------------------------------------------|---------------|
| 1        | 사용자 정정 이력 | 사용자가 직접 변경한 카테고리를 같은 가맹점의 최우선 규칙으로 저장                     | 전체          |
| 2        | `merchant_regno` | 사업자정보 API에서 업종·종목·KSIC 코드를 조회한 뒤 서비스 카테고리로 매핑              | 카드·전자금융 |
| 3        | `trans_category` | 제공되는 상품·구매 분류 코드를 서비스 카테고리로 직접 매핑                             | 전자금융 거래 |
| 4        | `merchant_name`  | 가맹점명을 정규화하여 가맹점명 사전 또는 키워드와 매칭                                 | 카드·전자금융 |
| 5        | `trans_memo`     | `월세`, `관리비`, 통신사명 등 이체 메모·상대방명 키워드로 분류                         | 계좌 거래     |
| 6        | 기타 정보        | `is_scheduled`, 거래일시, 금액, 승인 상태는 고정지출·취소 거래 판별의 보조 신호로 사용 | 제공되는 거래 |

카드 국내 승인내역 표준 API에서는 `merchant_name`, `merchant_regno`, 승인 일시·금액·상태·결제수단·할부 개월 수 등을 받을 수 있습니다. 가맹점명과
사업자등록번호는 사용자의 전송요구 동의 여부에 따라 누락될 수 있으므로, 가맹점명·메모 기반의 보조 규칙과 미분류 fallback이 필요합니다.

전자금융 거래는 `trans_category`, `trans_title`, `merchant_name`, `merchant_regno`, `is_scheduled` 등을 제공할 수
있어, `trans_category`가 존재할 때 가장 우선적인 자동 분류 신호로 활용합니다. 계좌 거래는 업종 코드 대신 `trans_memo`를 활용합니다.

- [카드 국내 승인내역 표준 API](https://developers.mydatakorea.org/mdtb/apg/mac/bas/FSAG0406?id=2)
- [전자금융 거래내역 표준 API](https://developers.mydatakorea.org/mdtb/apg/mac/bas/FSAG0201?id=5)
- [은행 거래내역 표준 API](https://developers.mydatakorea.org/mdtb/apg/mac/bas/FSAG0404?id=1)

### 향후 운영 분류 시퀀스

> 외부 사업자정보 API 조회와 매핑 upsert는 아직 구현하지 않은 확장 방향입니다.

```mermaid
sequenceDiagram
    autonumber
    participant S as MydataTransactionImportService
    participant A as MyData 카드 승인 API
    participant C as MerchantCategoryClassifier
    participant Cache as Caffeine Cache
    participant M as 가맹점 매핑 DB
    participant B as 외부 사업자정보 API
    participant T as TRANSACTION

    S->>A: 카드 승인내역 조회
    A-->>S: merchant_regno, merchant_name, 승인정보

    loop 승인 거래별 처리
        S->>C: classify(approval)
        alt merchant_regno 존재
            C->>Cache: 사업자번호 매핑 조회
            alt 사업자번호 매핑 성공
                Cache-->>C: ExpenseCategory
                C-->>S: 카테고리 + MERCHANT_REGNO
            else 사업자번호 매핑 없음
                C->>B: merchant_regno로 사업자정보 조회
                B-->>C: 업종·종목·KSIC 또는 조회 실패

                alt 업종 조회 성공
                    C->>C: 업종 코드 → ExpenseCategory 변환
                    C->>M: 사업자번호·가맹점명 매핑 upsert
                    C->>Cache: 새 매핑 캐시 저장
                    C-->>S: 카테고리 + MERCHANT_REGNO
                else 업종 조회 실패
                    C->>Cache: 정규화한 가맹점명 매핑 조회
                    alt 가맹점명 매핑 성공
                        Cache-->>C: ExpenseCategory
                        C-->>S: 카테고리 + MERCHANT_NAME
                    else 가맹점명 매핑 없음
                        C-->>S: OTHER_FINANCE + UNCLASSIFIED
                    end
                end
            end
        else merchant_regno 없음
            C->>Cache: 정규화한 가맹점명 매핑 조회
            alt 가맹점명 매핑 성공
                Cache-->>C: ExpenseCategory
                C-->>S: 카테고리 + MERCHANT_NAME
            else 가맹점명 매핑 없음
                C-->>S: OTHER_FINANCE + UNCLASSIFIED
            end
        end

        S->>T: MyData 원본값과 분류 결과 저장
    end
```
