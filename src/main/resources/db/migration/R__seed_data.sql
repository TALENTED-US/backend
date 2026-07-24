-- =========================================================
-- Buttie 시드 데이터 (Flyway Repeatable) - 전 환경 실행
-- 기준: 테이블 명세서 v0.0.3 (개정) / V1__init_schema.sql
-- =========================================================
-- 메모:
--   - 마이데이터 실연동이 없는 데모 서비스라 mock 데이터를 모든 환경에 그대로 배포함.
--     (운영/개발 분리 없이 이 파일 하나로 기준 데이터 + 데모 데이터를 함께 넣음)
--   - TRUNCATE 없이 '고정 ID + ON DUPLICATE KEY UPDATE(upsert)'. 파일이 바뀌면 재적용됨.
--   - USE / SET NAMES 등은 Flyway가 연결을 관리하므로 넣지 않음.
--   - 부모 → 자식 순으로 INSERT (FK 충족). BUTTIE_LEVEL은 USER FK 대상이라 맨 먼저.
-- =========================================================

-- ---------------------------------------------------------
-- 0. 버티 레벨 (BUTTIE_LEVEL) - USER.BUTTIE_LEVEL FK 대상, 맨 먼저
-- ---------------------------------------------------------
INSERT INTO `BUTTIE_LEVEL`
(`LEVEL`, `STAGE_NAME`, `REQUIRED_EXP`, `DESCRIPTION`,
 `IMAGE_URL_STABLE`, `IMAGE_URL_CAUTION`, `IMAGE_URL_DANGER`) VALUES
(1, '새싹 버티',   0,    '이제 막 자산관리를 시작한 기본 버티',
 'https://cdn.buttie.com/buttie/lv1_stable.png',
 'https://cdn.buttie.com/buttie/lv1_caution.png',
 'https://cdn.buttie.com/buttie/lv1_danger.png'),
(2, '기사 버티',   300,  '왕관과 망토를 갖춘 재정 습관이 자라나는 버티',
 'https://cdn.buttie.com/buttie/lv2_stable.png',
 'https://cdn.buttie.com/buttie/lv2_caution.png',
 'https://cdn.buttie.com/buttie/lv2_danger.png'),
(3, '황금 버티',   800,  '자산을 반짝반짝 불려가는 황금빛 버티',
 'https://cdn.buttie.com/buttie/lv3_stable.png',
 'https://cdn.buttie.com/buttie/lv3_caution.png',
 'https://cdn.buttie.com/buttie/lv3_danger.png'),
(4, '천사 버티',   1500, '날개와 후광으로 자산을 든든히 지키는 버티',
 'https://cdn.buttie.com/buttie/lv4_stable.png',
 'https://cdn.buttie.com/buttie/lv4_caution.png',
 'https://cdn.buttie.com/buttie/lv4_danger.png'),
(5, '수호신 버티', 3000, '눈부시게 빛나며 재정을 완성한 최고 단계 버티',
 'https://cdn.buttie.com/buttie/lv5_stable.png',
 'https://cdn.buttie.com/buttie/lv5_caution.png',
 'https://cdn.buttie.com/buttie/lv5_danger.png')
ON DUPLICATE KEY UPDATE
    `STAGE_NAME`       = VALUES(`STAGE_NAME`),
    `REQUIRED_EXP`     = VALUES(`REQUIRED_EXP`),
    `DESCRIPTION`      = VALUES(`DESCRIPTION`),
    `IMAGE_URL_STABLE` = VALUES(`IMAGE_URL_STABLE`),
    `IMAGE_URL_CAUTION`= VALUES(`IMAGE_URL_CAUTION`),
    `IMAGE_URL_DANGER` = VALUES(`IMAGE_URL_DANGER`);

-- ---------------------------------------------------------
-- 1. 관리자 (ADMIN)  * 비밀번호는 bcrypt 예시 해시
-- ---------------------------------------------------------
INSERT INTO `ADMIN`
(`ADMIN_ID`, `EMAIL`, `PASSWORD`, `ADMIN_ROLE`, `STATUS`) VALUES
(1, 'admin@buttie.com',    '$2b$10$abcdefghijklmnopqrstuv1234567890ABCDEFGHIJKLMNOPQRS', 'SUPER_ADMIN', 'ACTIVE'),
(2, 'operator@buttie.com', '$2b$10$abcdefghijklmnopqrstuv1234567890ABCDEFGHIJKLMNOPQRS', 'OPERATOR',    'ACTIVE')
ON DUPLICATE KEY UPDATE
    `EMAIL`=VALUES(`EMAIL`), `PASSWORD`=VALUES(`PASSWORD`),
    `ADMIN_ROLE`=VALUES(`ADMIN_ROLE`), `STATUS`=VALUES(`STATUS`);

-- ---------------------------------------------------------
-- 2. 사용자 (USER)
-- ---------------------------------------------------------
INSERT INTO `USER`
(`USER_ID`, `USERNAME`, `EMAIL`, `PASSWORD`, `NICKNAME`, `PHONE_NUMBER`,
 `ONBOARDING_COMPLETED`, `STATUS`, `BUTTIE_TOTAL_EXP`, `BUTTIE_LEVEL`) VALUES
(1, '홍길동', 'gildong@example.com', '$2b$10$userhashgildong000000000000000000000000000000000000', 'gildong.hong', '010-1234-5678', TRUE,  'ACTIVE', 120, 1),
(2, '김철수', 'chulsoo@example.com', '$2b$10$userhashchulsoo000000000000000000000000000000000000', 'chulsoo.kim',  '010-2345-6789', FALSE, 'ACTIVE', 0,   1)
ON DUPLICATE KEY UPDATE
    `USERNAME`=VALUES(`USERNAME`), `EMAIL`=VALUES(`EMAIL`), `PASSWORD`=VALUES(`PASSWORD`),
    `NICKNAME`=VALUES(`NICKNAME`), `PHONE_NUMBER`=VALUES(`PHONE_NUMBER`),
    `ONBOARDING_COMPLETED`=VALUES(`ONBOARDING_COMPLETED`), `STATUS`=VALUES(`STATUS`),
    `BUTTIE_TOTAL_EXP`=VALUES(`BUTTIE_TOTAL_EXP`), `BUTTIE_LEVEL`=VALUES(`BUTTIE_LEVEL`);

-- ---------------------------------------------------------
-- 3. 취업 준비 정보 (EMPLOYMENT_PREPARATIONS) - USER와 1:1
-- ---------------------------------------------------------
INSERT INTO `EMPLOYMENT_PREPARATIONS`
(`USER_ID`, `BIRTH_DATE`, `REGION`, `FAMILY_COUNT`, `EMPLOYMENT_PREP_TYPE`,
 `PREP_START_DATE`, `TARGET_EMPLOYMENT_DATE`, `LIVING_FUND_THRESHOLD`) VALUES
(1, '1999-03-15', '서울특별시', 1, 'FIRST_JOB', '2026-06-01', '2026-12-31', 1000000)
ON DUPLICATE KEY UPDATE
    `BIRTH_DATE`=VALUES(`BIRTH_DATE`), `REGION`=VALUES(`REGION`),
    `FAMILY_COUNT`=VALUES(`FAMILY_COUNT`), `EMPLOYMENT_PREP_TYPE`=VALUES(`EMPLOYMENT_PREP_TYPE`),
    `PREP_START_DATE`=VALUES(`PREP_START_DATE`), `TARGET_EMPLOYMENT_DATE`=VALUES(`TARGET_EMPLOYMENT_DATE`),
    `LIVING_FUND_THRESHOLD`=VALUES(`LIVING_FUND_THRESHOLD`);

-- ---------------------------------------------------------
-- 4. 약관 동의 (USER_CONSENTS) - USER와 1:1
-- ---------------------------------------------------------
INSERT INTO `USER_CONSENTS`
(`USER_ID`, `TERMS_AGREED_AT`, `PRIVACY_AGREED_AT`, `FINANCIAL_INFO_AGREED_AT`) VALUES
(1, '2026-06-01 09:00:00', '2026-06-01 09:00:00', '2026-06-01 09:00:00'),
(2, '2026-07-20 14:30:00', '2026-07-20 14:30:00', '2026-07-20 14:30:00')
ON DUPLICATE KEY UPDATE
    `TERMS_AGREED_AT`=VALUES(`TERMS_AGREED_AT`),
    `PRIVACY_AGREED_AT`=VALUES(`PRIVACY_AGREED_AT`),
    `FINANCIAL_INFO_AGREED_AT`=VALUES(`FINANCIAL_INFO_AGREED_AT`);

-- ---------------------------------------------------------
-- 5. 마이데이터 연결 (MYDATA)
-- ---------------------------------------------------------
INSERT INTO `MYDATA`
(`MYDATA_ID`, `USER_ID`, `PROVIDER`, `PROVIDER_USER_ID`, `REFRESH_TOKEN_ENCRYPTED`,
 `ACCESS_TOKEN_EXPIRES_AT`, `REFRESH_TOKEN_EXPIRES_AT`, `SCOPE`,
 `STATUS`, `LAST_TOKEN_REFRESHED_AT`, `LAST_SYNCED_AT`) VALUES
(1, 1, 'KB_MYDATA', 'kb_user_0001', 'ENC::sample_refresh_token_value',
 '2026-07-23 12:00:00', '2026-08-22 12:00:00', 'account.read transaction.read',
 'CONNECTED', '2026-07-23 06:00:00', '2026-07-23 06:00:00')
ON DUPLICATE KEY UPDATE
    `PROVIDER`=VALUES(`PROVIDER`), `PROVIDER_USER_ID`=VALUES(`PROVIDER_USER_ID`),
    `REFRESH_TOKEN_ENCRYPTED`=VALUES(`REFRESH_TOKEN_ENCRYPTED`),
    `ACCESS_TOKEN_EXPIRES_AT`=VALUES(`ACCESS_TOKEN_EXPIRES_AT`),
    `REFRESH_TOKEN_EXPIRES_AT`=VALUES(`REFRESH_TOKEN_EXPIRES_AT`),
    `SCOPE`=VALUES(`SCOPE`), `STATUS`=VALUES(`STATUS`),
    `LAST_TOKEN_REFRESHED_AT`=VALUES(`LAST_TOKEN_REFRESHED_AT`),
    `LAST_SYNCED_AT`=VALUES(`LAST_SYNCED_AT`);

-- ---------------------------------------------------------
-- 6. 계좌 (ACCOUNT)  유동자산 500만(입출금+적금) / 금융상품 200만(예금)
-- ---------------------------------------------------------
INSERT INTO `ACCOUNT`
(`ACCOUNT_ID`, `USER_ID`, `MYDATA_ID`, `EXTERNAL_ACCOUNT_ID`, `INSTITUTION_NAME`,
 `ACCOUNT_NAME`, `ACCOUNT_TYPE`, `ACCOUNT_NUMBER_MASKED`, `BALANCE`,
 `IS_ACTIVE`, `SYNCED_AT`) VALUES
(1, 1, 1, 'EXT_ACC_001', 'KB국민은행', '주거래 입출금통장', 'CHECKING', '110-***-3456', 3200000, TRUE, '2026-07-23 06:00:00'),
(2, 1, 1, 'EXT_ACC_002', 'KB국민은행', 'KB 청년적금',      'SAVINGS',  '220-***-7890', 1800000, TRUE, '2026-07-23 06:00:00'),
(3, 1, 1, 'EXT_ACC_003', '카카오뱅크', '비상금 예금',      'DEPOSIT',  '333-***-1234', 2000000, TRUE, '2026-07-23 06:00:00')
ON DUPLICATE KEY UPDATE
    `INSTITUTION_NAME`=VALUES(`INSTITUTION_NAME`), `ACCOUNT_NAME`=VALUES(`ACCOUNT_NAME`),
    `ACCOUNT_TYPE`=VALUES(`ACCOUNT_TYPE`), `ACCOUNT_NUMBER_MASKED`=VALUES(`ACCOUNT_NUMBER_MASKED`),
    `BALANCE`=VALUES(`BALANCE`), `IS_ACTIVE`=VALUES(`IS_ACTIVE`), `SYNCED_AT`=VALUES(`SYNCED_AT`);

-- ---------------------------------------------------------
-- 7. 거래 (TRANSACTION)  TYPE: EXPENSE(지출)/INCOME(수입)/FIXED(고정지출)
-- ---------------------------------------------------------
INSERT INTO `TRANSACTION`
(`TRANSACTION_ID`, `USER_ID`, `ACCOUNT_ID`, `EXTERNAL_TRANSACTION_ID`, `CONTENT`,
 `TYPE`, `CATEGORY`, `AMOUNT`, `TRANSACTION_AT`, `MEMO`,
 `ANALYSIS_EXCLUDED`, `IS_DELETED`) VALUES
(1, 1, 1, 'EXT_TX_001', '스타벅스 강남점',        'EXPENSE', 'FOOD',          6300,   '2026-07-20 10:15:00', NULL, FALSE, FALSE),
(2, 1, 1, 'EXT_TX_002', '지하철 교통카드',        'EXPENSE', 'TRANSPORT',     1400,   '2026-07-20 08:40:00', NULL, FALSE, FALSE),
(3, 1, 1, 'EXT_TX_003', '월세 이체',              'FIXED',   'HOUSING',       500000, '2026-07-01 09:00:00', '원룸 월세(고정지출)', FALSE, FALSE),
(4, 1, 1, 'EXT_TX_004', '넷플릭스 정기결제',      'FIXED',   'SUBSCRIPTION',  9900,   '2026-07-05 00:10:00', '구독(고정지출)', FALSE, FALSE),
(5, 1, 1, 'EXT_TX_005', '토익 응시료',            'EXPENSE', 'EDUCATION',     48000,  '2026-07-12 13:00:00', NULL, FALSE, FALSE),
(6, 1, 1, 'EXT_TX_006', '편의점 아르바이트 급여', 'INCOME',  'ETC',           480000, '2026-07-10 18:00:00', '7월 알바 급여', FALSE, FALSE),
(7, 1, 1, 'EXT_TX_007', '통신비 자동이체',        'FIXED',   'COMMUNICATION', 55000,  '2026-07-05 09:00:00', '휴대폰 요금(고정지출)', FALSE, FALSE),
(8, 1, NULL, NULL, '현금 식비(수동 등록)', 'EXPENSE', 'FOOD', 8000, '2026-07-21 12:30:00', '학식', FALSE, FALSE)
ON DUPLICATE KEY UPDATE
    `CONTENT`=VALUES(`CONTENT`), `TYPE`=VALUES(`TYPE`), `CATEGORY`=VALUES(`CATEGORY`),
    `AMOUNT`=VALUES(`AMOUNT`), `TRANSACTION_AT`=VALUES(`TRANSACTION_AT`), `MEMO`=VALUES(`MEMO`),
    `ANALYSIS_EXCLUDED`=VALUES(`ANALYSIS_EXCLUDED`), `IS_DELETED`=VALUES(`IS_DELETED`);

-- ---------------------------------------------------------
-- 8. 재정 스냅샷 (SNAPSHOT)
--    유동자산 500만 / 월지출 140만 → 준비 가능 3.57개월, 위험도 CAUTION
-- ---------------------------------------------------------
INSERT INTO `SNAPSHOT`
(`SNAPSHOT_ID`, `USER_ID`, `BASE_DATE`, `CALCULATION_MONTHS`,
 `TOTAL_ASSETS`, `LIQUID_ASSETS`, `FINANCIAL_PRODUCT_ASSETS`, `TOTAL_DEBT`,
 `AVG_MONTHLY_INCOME`, `AVG_MONTHLY_EXPENSE`, `MONTHLY_NET_CASHFLOW`,
 `PREP_POSSIBLE_MONTHS`, `TARGET_BALANCE`, `ADDITIONAL_REQUIRED_AMOUNT`,
 `TARGET_ACHIEVEMENT_RATE`, `RISK_LEVEL`) VALUES
(1, 1, '2026-07-23', 3,
 7000000, 5000000, 2000000, 0,
 0, 1400000, -1400000,
 3.57, 0, 3400000,
 59.50, 'CAUTION')
ON DUPLICATE KEY UPDATE
    `CALCULATION_MONTHS`=VALUES(`CALCULATION_MONTHS`), `TOTAL_ASSETS`=VALUES(`TOTAL_ASSETS`),
    `LIQUID_ASSETS`=VALUES(`LIQUID_ASSETS`), `FINANCIAL_PRODUCT_ASSETS`=VALUES(`FINANCIAL_PRODUCT_ASSETS`),
    `TOTAL_DEBT`=VALUES(`TOTAL_DEBT`), `AVG_MONTHLY_INCOME`=VALUES(`AVG_MONTHLY_INCOME`),
    `AVG_MONTHLY_EXPENSE`=VALUES(`AVG_MONTHLY_EXPENSE`), `MONTHLY_NET_CASHFLOW`=VALUES(`MONTHLY_NET_CASHFLOW`),
    `PREP_POSSIBLE_MONTHS`=VALUES(`PREP_POSSIBLE_MONTHS`), `TARGET_BALANCE`=VALUES(`TARGET_BALANCE`),
    `ADDITIONAL_REQUIRED_AMOUNT`=VALUES(`ADDITIONAL_REQUIRED_AMOUNT`),
    `TARGET_ACHIEVEMENT_RATE`=VALUES(`TARGET_ACHIEVEMENT_RATE`), `RISK_LEVEL`=VALUES(`RISK_LEVEL`);

-- ---------------------------------------------------------
-- 9. 금융 상품 (FINANCE) - 추천용 참조 데이터(개발 seed)
-- ---------------------------------------------------------
INSERT INTO `FINANCE`
(`FINANCE_ID`, `COMPANY`, `NAME`, `TYPE`, `MIN_AGE`, `MAX_AGE`, `PERIOD`,
 `BASE_RATE`, `PREFERRED_RATE`, `MINIMUM_AMOUNT`,
 `REGISTER_CONDITION`, `WITHDRAW_CONDITION`, `URL`, `STATUS`) VALUES
(1, 'KB국민은행',  'KB Young Youth 적금', 'SAVINGS', 19, 34, 12, 3.50, 4.50, 10000,
 '만 19~34세 청년', '만기 전 해지 시 기본금리 미적용', 'https://obank.kbstar.com/youth-savings', 'AVAILABLE'),
(2, 'KB국민은행',  'KB국민 First 예금',   'DEPOSIT', NULL, NULL, 12, 3.20, 3.60, 100000,
 '제한 없음', '중도해지 시 약정금리의 50% 적용', 'https://obank.kbstar.com/first-deposit', 'AVAILABLE'),
(3, '카카오뱅크',  '26주 적금',           'SAVINGS', NULL, NULL, 6,  3.00, 4.00, 1000,
 '제한 없음', '중도해지 시 기본금리 적용', 'https://www.kakaobank.com/26weeks', 'AVAILABLE')
ON DUPLICATE KEY UPDATE
    `COMPANY`=VALUES(`COMPANY`), `NAME`=VALUES(`NAME`), `TYPE`=VALUES(`TYPE`),
    `MIN_AGE`=VALUES(`MIN_AGE`), `MAX_AGE`=VALUES(`MAX_AGE`), `PERIOD`=VALUES(`PERIOD`),
    `BASE_RATE`=VALUES(`BASE_RATE`), `PREFERRED_RATE`=VALUES(`PREFERRED_RATE`),
    `MINIMUM_AMOUNT`=VALUES(`MINIMUM_AMOUNT`), `REGISTER_CONDITION`=VALUES(`REGISTER_CONDITION`),
    `WITHDRAW_CONDITION`=VALUES(`WITHDRAW_CONDITION`), `URL`=VALUES(`URL`), `STATUS`=VALUES(`STATUS`);

-- ---------------------------------------------------------
-- 10. 정부 지원 정책 (POLICY) - 추천용 참조 데이터(개발 seed)
-- ---------------------------------------------------------
INSERT INTO `POLICY`
(`POLICY_ID`, `NAME`, `CATEGORY`, `MIN_AGE`, `MAX_AGE`, `REGION`, `AMOUNT`,
 `DUE_DATE`, `DOCUMENT`, `EMPLOYMENT_STATUS`, `FAMILY_COUNT`, `STATUS`, `URL`) VALUES
(1, '청년월세 특별지원', '주거', 19, 34, '전국', 200000,
 '2026-12-31 23:59:59', '주민등록등본, 임대차계약서, 통장사본', '미취업', 1, 'AVAILABLE',
 'https://www.gov.kr/youth-housing'),
(2, '국민취업지원제도',  '취업', 15, 69, '전국', 500000,
 '2026-11-30 23:59:59', '신분증, 구직신청서', '미취업', 1, 'AVAILABLE',
 'https://www.work24.go.kr/kua'),
(3, '서울 청년수당',     '취업', 19, 34, '서울특별시', 500000,
 '2026-10-15 23:59:59', '신분증, 졸업증명서', '미취업', 1, 'AVAILABLE',
 'https://youth.seoul.go.kr/allowance')
ON DUPLICATE KEY UPDATE
    `NAME`=VALUES(`NAME`), `CATEGORY`=VALUES(`CATEGORY`), `MIN_AGE`=VALUES(`MIN_AGE`),
    `MAX_AGE`=VALUES(`MAX_AGE`), `REGION`=VALUES(`REGION`), `AMOUNT`=VALUES(`AMOUNT`),
    `DUE_DATE`=VALUES(`DUE_DATE`), `DOCUMENT`=VALUES(`DOCUMENT`),
    `EMPLOYMENT_STATUS`=VALUES(`EMPLOYMENT_STATUS`), `FAMILY_COUNT`=VALUES(`FAMILY_COUNT`),
    `STATUS`=VALUES(`STATUS`), `URL`=VALUES(`URL`);

-- ---------------------------------------------------------
-- 11. 시뮬레이션 (SIMULATION) = 최종 재정 계획(확정)
--     알바 55만 + 월세지원 20만 + 구독해지 → 준비 가능 7.81개월로 개선
-- ---------------------------------------------------------
INSERT INTO `SIMULATION`
(`SIMULATION_ID`, `USER_ID`, `SNAPSHOT_ID`, `START_DATE`, `END_DATE`,
 `ENDING_BALANCE`, `TARGET_RATE`, `PREP_MONTHS`, `CONFIRMED_AT`, `IS_DELETED`) VALUES
(1, 1, 1, '2026-07-23 00:00:00', '2026-12-23 00:00:00',
 1799500, 100.00, 7.81, '2026-07-23 11:00:00', FALSE)
ON DUPLICATE KEY UPDATE
    `SNAPSHOT_ID`=VALUES(`SNAPSHOT_ID`), `START_DATE`=VALUES(`START_DATE`),
    `END_DATE`=VALUES(`END_DATE`), `ENDING_BALANCE`=VALUES(`ENDING_BALANCE`),
    `TARGET_RATE`=VALUES(`TARGET_RATE`), `PREP_MONTHS`=VALUES(`PREP_MONTHS`),
    `CONFIRMED_AT`=VALUES(`CONFIRMED_AT`), `IS_DELETED`=VALUES(`IS_DELETED`);

-- ---------------------------------------------------------
-- 12. 시뮬레이션 항목 (SIMULATION_ITEM)
-- ---------------------------------------------------------
INSERT INTO `SIMULATION_ITEM`
(`SIMULATION_ITEM_ID`, `SIMULATION_ID`, `CATEGORY`, `AMOUNT`, `START_DATE`, `END_DATE`,
 `POLICY_ID`, `FINANCE_ID`, `DETAIL_VALUE`, `RECURRENCE_TYPE`, `IS_DELETED`) VALUES
(1, 1, 'INCOME',  550000, '2026-08-01 00:00:00', '2026-12-23 00:00:00',
 NULL, NULL, '{"job":"편의점 알바","hourlyWage":11000,"hoursPerDay":5,"daysPerWeek":2}', 'MONTHLY', FALSE),
(2, 1, 'POLICY',  200000, '2026-08-01 00:00:00', '2026-12-23 00:00:00',
 1, NULL, '{"policyName":"청년월세 특별지원"}', 'MONTHLY', FALSE),
(3, 1, 'EXPENSE', 9900,   '2026-08-01 00:00:00', NULL,
 NULL, NULL, '{"action":"넷플릭스 구독 해지"}', 'MONTHLY', FALSE)
ON DUPLICATE KEY UPDATE
    `CATEGORY`=VALUES(`CATEGORY`), `AMOUNT`=VALUES(`AMOUNT`),
    `START_DATE`=VALUES(`START_DATE`), `END_DATE`=VALUES(`END_DATE`),
    `POLICY_ID`=VALUES(`POLICY_ID`), `FINANCE_ID`=VALUES(`FINANCE_ID`),
    `DETAIL_VALUE`=VALUES(`DETAIL_VALUE`), `RECURRENCE_TYPE`=VALUES(`RECURRENCE_TYPE`),
    `IS_DELETED`=VALUES(`IS_DELETED`);

-- ---------------------------------------------------------
-- 13. 월별 예상 (PROJECTION)
-- ---------------------------------------------------------
INSERT INTO `PROJECTION`
(`PROJECTION_ID`, `SIMULATION_ID`, `PROJECTION_MONTH`,
 `OPENING_BALANCE`, `EXPECTED_INCOME`, `EXPECTED_EXPENSE`, `CLOSING_BALANCE`,
 `ADJUSTMENT_REQUIRED`, `ADJUSTMENT_REASON`) VALUES
(1, 1, '2026-08-01', 5000000, 750000, 1390100, 4359900, FALSE, NULL),
(2, 1, '2026-09-01', 4359900, 750000, 1390100, 3719800, FALSE, NULL),
(3, 1, '2026-10-01', 3719800, 750000, 1390100, 3079700, FALSE, NULL),
(4, 1, '2026-11-01', 3079700, 750000, 1390100, 2439600, FALSE, NULL),
(5, 1, '2026-12-01', 2439600, 750000, 1390100, 1799500, FALSE, NULL)
ON DUPLICATE KEY UPDATE
    `OPENING_BALANCE`=VALUES(`OPENING_BALANCE`), `EXPECTED_INCOME`=VALUES(`EXPECTED_INCOME`),
    `EXPECTED_EXPENSE`=VALUES(`EXPECTED_EXPENSE`), `CLOSING_BALANCE`=VALUES(`CLOSING_BALANCE`),
    `ADJUSTMENT_REQUIRED`=VALUES(`ADJUSTMENT_REQUIRED`), `ADJUSTMENT_REASON`=VALUES(`ADJUSTMENT_REASON`);

-- ---------------------------------------------------------
-- 14. 알림 (NOTIFICATION)
-- ---------------------------------------------------------
INSERT INTO `NOTIFICATION`
(`NOTIFICATION_ID`, `USER_ID`, `TYPE`, `TITLE`, `CONTENT`, `URL`, `IS_READ`) VALUES
(1, 1, 'QUEST',            '행동 과제 마감 임박',    '‘청년월세 특별지원 신청’ 과제 마감이 3일 남았어요.', NULL, FALSE),
(2, 1, 'POLICY',           '청년월세 특별지원 D-30', '신청 마감일이 30일 남았습니다. 서류를 준비하세요.', 'https://www.gov.kr/youth-housing', FALSE),
(3, 1, 'FINANCIAL_CHANGE', '준비 가능 기간 증가',    '계획 적용으로 준비 가능 기간이 3.6개월 → 7.8개월로 늘었어요.', NULL, TRUE),
(4, 1, 'REWARD',           '경험치 획득',            '구독 해지 과제를 완료해 30 EXP를 획득했어요.', NULL, TRUE)
ON DUPLICATE KEY UPDATE
    `TYPE`=VALUES(`TYPE`), `TITLE`=VALUES(`TITLE`), `CONTENT`=VALUES(`CONTENT`),
    `URL`=VALUES(`URL`), `IS_READ`=VALUES(`IS_READ`);

-- ---------------------------------------------------------
-- 15. 퀘스트 (QUEST) - 최종 재정 계획 확정 시 항목 기반 생성
-- ---------------------------------------------------------
INSERT INTO `QUEST`
(`QUEST_ID`, `USER_ID`, `SIMULATION_ID`, `SIMULATION_ITEM_ID`, `TRANSACTION_ID`,
 `QUEST_TYPE`, `TITLE`, `DESCRIPTION`, `DEADLINE`, `STATUS`, `URL`,
 `EXP_REWARD`, `COMPLETED_AT`) VALUES
(1, 1, 1, 1, NULL, 'APPLY',  '아르바이트 지원하기',
 '편의점 아르바이트 공고를 확인하고 지원하세요.', '2026-08-01 23:59:59', 'PLANNED', NULL, 50, NULL),
(2, 1, 1, 2, NULL, 'SUBMIT', '청년월세 특별지원 신청',
 '자격 조건을 확인하고 필요 서류를 제출하세요.', '2026-08-10 23:59:59', 'PLANNED',
 'https://www.gov.kr/youth-housing', 100, NULL),
(3, 1, 1, 3, 4,    'CANCEL', '넷플릭스 구독 해지',
 '넷플릭스 정기결제를 해지해 월 9,900원을 절약하세요.', '2026-08-05 23:59:59', 'COMPLETED',
 NULL, 30, '2026-07-22 20:00:00')
ON DUPLICATE KEY UPDATE
    `SIMULATION_ID`=VALUES(`SIMULATION_ID`), `SIMULATION_ITEM_ID`=VALUES(`SIMULATION_ITEM_ID`),
    `TRANSACTION_ID`=VALUES(`TRANSACTION_ID`), `QUEST_TYPE`=VALUES(`QUEST_TYPE`),
    `TITLE`=VALUES(`TITLE`), `DESCRIPTION`=VALUES(`DESCRIPTION`), `DEADLINE`=VALUES(`DEADLINE`),
    `STATUS`=VALUES(`STATUS`), `URL`=VALUES(`URL`), `EXP_REWARD`=VALUES(`EXP_REWARD`),
    `COMPLETED_AT`=VALUES(`COMPLETED_AT`);
