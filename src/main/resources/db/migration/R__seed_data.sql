-- =========================================================
-- Buttie 시드 데이터 (Flyway Repeatable) - 전 환경 실행
-- 기준: 테이블 명세서 v1.0.0 (최종, (3)) / V1__init_schema.sql
-- =========================================================
-- 메모:
--   - 마이데이터 실연동이 없는 데모 서비스라 mock 데이터를 모든 환경에 그대로 배포함.
--   - TRUNCATE 없이 '고정 ID + ON DUPLICATE KEY UPDATE(upsert)'. 파일이 바뀌면 재적용됨.
--   - 부모 → 자식 순으로 INSERT (FK 충족). BUTTIE_LEVEL 먼저.
--   - 시뮬레이션 관련 날짜(적용일 포함)는 DATE(LocalDate)라 날짜 전용 값 사용.
-- =========================================================

-- ---------------------------------------------------------
-- 0. 버티 레벨 (BUTTIE_LEVEL)
-- ---------------------------------------------------------
INSERT INTO `BUTTIE_LEVEL`
(`LEVEL`, `STAGE_NAME`, `REQUIRED_EXP`, `LEVEL_DESCRIPTION`,
 `IMAGE_URL_STABLE`, `IMAGE_URL_CAUTION`, `IMAGE_URL_DANGER`) VALUES
(1, '새싹 버티',   0,    '이제 막 자산관리를 시작한 기본 버티',
 'https://cdn.buttie.com/buttie/lv1_stable.png', 'https://cdn.buttie.com/buttie/lv1_caution.png', 'https://cdn.buttie.com/buttie/lv1_danger.png'),
(2, '기사 버티',   300,  '재정 습관이 자라나는 버티',
 'https://cdn.buttie.com/buttie/lv2_stable.png', 'https://cdn.buttie.com/buttie/lv2_caution.png', 'https://cdn.buttie.com/buttie/lv2_danger.png'),
(3, '황금 버티',   800,  '자산을 불려가는 황금빛 버티',
 'https://cdn.buttie.com/buttie/lv3_stable.png', 'https://cdn.buttie.com/buttie/lv3_caution.png', 'https://cdn.buttie.com/buttie/lv3_danger.png'),
(4, '천사 버티',   1500, '자산을 든든히 지키는 버티',
 'https://cdn.buttie.com/buttie/lv4_stable.png', 'https://cdn.buttie.com/buttie/lv4_caution.png', 'https://cdn.buttie.com/buttie/lv4_danger.png'),
(5, '수호신 버티', 3000, '재정을 완성한 최고 단계 버티',
 'https://cdn.buttie.com/buttie/lv5_stable.png', 'https://cdn.buttie.com/buttie/lv5_caution.png', 'https://cdn.buttie.com/buttie/lv5_danger.png')
ON DUPLICATE KEY UPDATE
    `STAGE_NAME`=VALUES(`STAGE_NAME`), `REQUIRED_EXP`=VALUES(`REQUIRED_EXP`),
    `LEVEL_DESCRIPTION`=VALUES(`LEVEL_DESCRIPTION`), `IMAGE_URL_STABLE`=VALUES(`IMAGE_URL_STABLE`),
    `IMAGE_URL_CAUTION`=VALUES(`IMAGE_URL_CAUTION`), `IMAGE_URL_DANGER`=VALUES(`IMAGE_URL_DANGER`);

-- ---------------------------------------------------------
-- 1. 관리자 (ADMIN)
-- ---------------------------------------------------------
INSERT INTO `ADMIN`
(`ADMIN_ID`, `ADMIN_EMAIL`, `ADMIN_PASSWORD_HASH`, `ADMIN_ROLE`, `ADMIN_STATUS`) VALUES
(1, 'admin@buttie.com',    '$2b$10$abcdefghijklmnopqrstuv1234567890ABCDEFGHIJKLMNOPQRS', 'SUPER_ADMIN', 'ACTIVE'),
(2, 'operator@buttie.com', '$2b$10$abcdefghijklmnopqrstuv1234567890ABCDEFGHIJKLMNOPQRS', 'OPERATOR',    'ACTIVE')
ON DUPLICATE KEY UPDATE
    `ADMIN_EMAIL`=VALUES(`ADMIN_EMAIL`), `ADMIN_PASSWORD_HASH`=VALUES(`ADMIN_PASSWORD_HASH`),
    `ADMIN_ROLE`=VALUES(`ADMIN_ROLE`), `ADMIN_STATUS`=VALUES(`ADMIN_STATUS`);

-- ---------------------------------------------------------
-- 2. 사용자 (USER)
-- ---------------------------------------------------------
INSERT INTO `USER`
(`USER_ID`, `USER_NAME`, `USER_EMAIL`, `USER_PASSWORD_HASH`, `USER_NICKNAME`,
 `USER_PHONE_NUMBER`, `USER_ONBOARDING_COMPLETED`, `USER_STATUS`) VALUES
(1, '홍길동', 'gildong@example.com', '$2b$10$userhashgildong000000000000000000000000000000000000', 'gildong.hong', '010-1234-5678', TRUE,  'ACTIVE'),
(2, '김철수', 'chulsoo@example.com', '$2b$10$userhashchulsoo000000000000000000000000000000000000', 'chulsoo.kim',  '010-2345-6789', FALSE, 'ACTIVE')
ON DUPLICATE KEY UPDATE
    `USER_NAME`=VALUES(`USER_NAME`), `USER_EMAIL`=VALUES(`USER_EMAIL`),
    `USER_PASSWORD_HASH`=VALUES(`USER_PASSWORD_HASH`), `USER_NICKNAME`=VALUES(`USER_NICKNAME`),
    `USER_PHONE_NUMBER`=VALUES(`USER_PHONE_NUMBER`),
    `USER_ONBOARDING_COMPLETED`=VALUES(`USER_ONBOARDING_COMPLETED`), `USER_STATUS`=VALUES(`USER_STATUS`);

-- ---------------------------------------------------------
-- 3. 사용자 버티 정보 (USER_BUTTIE)
-- ---------------------------------------------------------
INSERT INTO `USER_BUTTIE`
(`USER_ID`, `BUTTIE_TOTAL_EXP`, `BUTTIE_LEVEL`) VALUES
(1, 120, 1),
(2, 0,   1)
ON DUPLICATE KEY UPDATE
    `BUTTIE_TOTAL_EXP`=VALUES(`BUTTIE_TOTAL_EXP`), `BUTTIE_LEVEL`=VALUES(`BUTTIE_LEVEL`);

-- ---------------------------------------------------------
-- 4. 취업 준비 정보 (EMPLOYMENT_PREPARATIONS)
-- ---------------------------------------------------------
INSERT INTO `EMPLOYMENT_PREPARATIONS`
(`USER_ID`, `BIRTH_DATE`, `EMPLOYMENT_PREP_REGION`, `FAMILY_COUNT`, `EMPLOYMENT_PREP_TYPE`,
 `PREP_START_DATE`, `TARGET_EMPLOYMENT_DATE`, `LIVING_FUND_THRESHOLD`) VALUES
(1, '1999-03-15', '서울특별시', 1, 'FIRST_JOB', '2026-06-01', '2026-12-31', 1000000)
ON DUPLICATE KEY UPDATE
    `BIRTH_DATE`=VALUES(`BIRTH_DATE`), `EMPLOYMENT_PREP_REGION`=VALUES(`EMPLOYMENT_PREP_REGION`),
    `FAMILY_COUNT`=VALUES(`FAMILY_COUNT`), `EMPLOYMENT_PREP_TYPE`=VALUES(`EMPLOYMENT_PREP_TYPE`),
    `PREP_START_DATE`=VALUES(`PREP_START_DATE`), `TARGET_EMPLOYMENT_DATE`=VALUES(`TARGET_EMPLOYMENT_DATE`),
    `LIVING_FUND_THRESHOLD`=VALUES(`LIVING_FUND_THRESHOLD`);

-- ---------------------------------------------------------
-- 5. 약관 동의 (USER_CONSENTS)
-- ---------------------------------------------------------
INSERT INTO `USER_CONSENTS`
(`USER_ID`, `TERMS_AGREED_AT`, `PRIVACY_AGREED_AT`, `FINANCIAL_INFO_AGREED_AT`) VALUES
(1, '2026-06-01 09:00:00', '2026-06-01 09:00:00', '2026-06-01 09:00:00'),
(2, '2026-07-20 14:30:00', '2026-07-20 14:30:00', '2026-07-20 14:30:00')
ON DUPLICATE KEY UPDATE
    `TERMS_AGREED_AT`=VALUES(`TERMS_AGREED_AT`), `PRIVACY_AGREED_AT`=VALUES(`PRIVACY_AGREED_AT`),
    `FINANCIAL_INFO_AGREED_AT`=VALUES(`FINANCIAL_INFO_AGREED_AT`);

-- ---------------------------------------------------------
-- 6. 마이데이터 연결 (MYDATA)
-- ---------------------------------------------------------
INSERT INTO `MYDATA`
(`MYDATA_ID`, `USER_ID`, `PROVIDER`, `REFRESH_TOKEN_ENCRYPTED`,
 `REFRESH_TOKEN_EXPIRES_AT`, `MYDATA_STATUS`, `LAST_SYNCED_AT`) VALUES
(1, 1, 'KB_MYDATA', 'ENC::sample_refresh_token_value',
 '2026-08-22 12:00:00', 'CONNECTED', '2026-07-23 06:00:00')
ON DUPLICATE KEY UPDATE
    `PROVIDER`=VALUES(`PROVIDER`), `REFRESH_TOKEN_ENCRYPTED`=VALUES(`REFRESH_TOKEN_ENCRYPTED`),
    `REFRESH_TOKEN_EXPIRES_AT`=VALUES(`REFRESH_TOKEN_EXPIRES_AT`),
    `MYDATA_STATUS`=VALUES(`MYDATA_STATUS`), `LAST_SYNCED_AT`=VALUES(`LAST_SYNCED_AT`);

-- ---------------------------------------------------------
-- 7. 계좌 (ACCOUNT)
-- ---------------------------------------------------------
INSERT INTO `ACCOUNT`
(`ACCOUNT_ID`, `USER_ID`, `EXTERNAL_ACCOUNT_ID`, `INSTITUTION_NAME`,
 `ACCOUNT_NAME`, `ACCOUNT_TYPE`, `ACCOUNT_NUMBER_MASKED`, `BALANCE`, `IS_ACTIVE`, `SYNCED_AT`) VALUES
(1, 1, 'EXT_ACC_001', 'KB국민은행', '주거래 입출금통장', 'CHECKING', '110-***-3456', 3200000, TRUE, '2026-07-23 06:00:00'),
(2, 1, 'EXT_ACC_002', 'KB국민은행', 'KB 청년적금',      'SAVINGS',  '220-***-7890', 1800000, TRUE, '2026-07-23 06:00:00'),
(3, 1, 'EXT_ACC_003', '카카오뱅크', '비상금 예금',      'DEPOSIT',  '333-***-1234', 2000000, TRUE, '2026-07-23 06:00:00')
ON DUPLICATE KEY UPDATE
    `INSTITUTION_NAME`=VALUES(`INSTITUTION_NAME`), `ACCOUNT_NAME`=VALUES(`ACCOUNT_NAME`),
    `ACCOUNT_TYPE`=VALUES(`ACCOUNT_TYPE`), `ACCOUNT_NUMBER_MASKED`=VALUES(`ACCOUNT_NUMBER_MASKED`),
    `BALANCE`=VALUES(`BALANCE`), `IS_ACTIVE`=VALUES(`IS_ACTIVE`), `SYNCED_AT`=VALUES(`SYNCED_AT`);

-- ---------------------------------------------------------
-- 8. 카드 (CARD)  CARD_TYPE: CREDIT(신용)/DEBIT(체크)/PREPAID(선불)
-- ---------------------------------------------------------
INSERT INTO `CARD`
(`CARD_ID`, `USER_ID`, `EXTERNAL_CARD_ID`, `CARD_INSTITUTION_NAME`,
 `CARD_NAME`, `CARD_TYPE`, `CARD_NUMBER_MASKED`, `CARD_BALANCE`, `CARD_IS_ACTIVE`, `CARD_SYNCED_AT`) VALUES
(1, 1, 'EXT_CARD_001', 'KB국민카드', 'KB국민 체크카드', 'DEBIT',  '5327-**-1234', 0, TRUE, '2026-07-23 06:00:00'),
(2, 1, 'EXT_CARD_002', 'KB국민카드', 'KB국민 신용카드', 'CREDIT', '5327-**-5678', 0, TRUE, '2026-07-23 06:00:00')
ON DUPLICATE KEY UPDATE
    `CARD_INSTITUTION_NAME`=VALUES(`CARD_INSTITUTION_NAME`), `CARD_NAME`=VALUES(`CARD_NAME`),
    `CARD_TYPE`=VALUES(`CARD_TYPE`), `CARD_NUMBER_MASKED`=VALUES(`CARD_NUMBER_MASKED`),
    `CARD_BALANCE`=VALUES(`CARD_BALANCE`), `CARD_IS_ACTIVE`=VALUES(`CARD_IS_ACTIVE`),
    `CARD_SYNCED_AT`=VALUES(`CARD_SYNCED_AT`);

-- ---------------------------------------------------------
-- 9. 거래 (TRANSACTION)  TYPE: EXPENSE/INCOME/FIXED
-- ---------------------------------------------------------
INSERT INTO `TRANSACTION`
(`TRANSACTION_ID`, `USER_ID`, `ACCOUNT_ID`, `EXTERNAL_TRANSACTION_ID`, `TRANSACTION_CONTENT`,
 `TRANSACTION_TYPE`, `EXPENSE_CATEGORY`, `TRANSACTION_AMOUNT`, `TRANSACTION_AT`, `TRANSACTION_MEMO`,
 `ANALYSIS_EXCLUDED`, `IS_DELETED`) VALUES
(1, 1, 1, 'EXT_TX_001', '스타벅스 강남점',        'EXPENSE', 'FOOD',          6300,   '2026-07-20 10:15:00', NULL, FALSE, FALSE),
(2, 1, 1, 'EXT_TX_002', '지하철 교통카드',        'EXPENSE', 'TRANSPORT',     1400,   '2026-07-20 08:40:00', NULL, FALSE, FALSE),
(3, 1, 1, 'EXT_TX_003', '월세 이체',              'FIXED',   'HOUSING',       500000, '2026-07-01 09:00:00', '원룸 월세(고정지출)', FALSE, FALSE),
(4, 1, 1, 'EXT_TX_004', '넷플릭스 정기결제',      'FIXED',   'SUBSCRIPTION',  9900,   '2026-07-05 00:10:00', '구독(고정지출)', FALSE, FALSE),
(5, 1, 1, 'EXT_TX_005', '토익 응시료',            'EXPENSE', 'EDUCATION',     48000,  '2026-07-12 13:00:00', NULL, FALSE, FALSE),
(6, 1, 1, 'EXT_TX_006', '편의점 아르바이트 급여', 'INCOME',  'ETC_EXPENSE',   480000, '2026-07-10 18:00:00', '7월 알바 급여', FALSE, FALSE),
(7, 1, 1, 'EXT_TX_007', '통신비 자동이체',        'FIXED',   'COMMUNICATION', 55000,  '2026-07-05 09:00:00', '휴대폰 요금(고정지출)', FALSE, FALSE),
(8, 1, NULL, NULL, '현금 식비(수동 등록)', 'EXPENSE', 'FOOD', 8000, '2026-07-21 12:30:00', '학식', FALSE, FALSE)
ON DUPLICATE KEY UPDATE
    `TRANSACTION_CONTENT`=VALUES(`TRANSACTION_CONTENT`), `TRANSACTION_TYPE`=VALUES(`TRANSACTION_TYPE`),
    `EXPENSE_CATEGORY`=VALUES(`EXPENSE_CATEGORY`), `TRANSACTION_AMOUNT`=VALUES(`TRANSACTION_AMOUNT`),
    `TRANSACTION_AT`=VALUES(`TRANSACTION_AT`), `TRANSACTION_MEMO`=VALUES(`TRANSACTION_MEMO`),
    `ANALYSIS_EXCLUDED`=VALUES(`ANALYSIS_EXCLUDED`), `IS_DELETED`=VALUES(`IS_DELETED`);

-- ---------------------------------------------------------
-- 10. 재정 스냅샷 (SNAPSHOT)
-- ---------------------------------------------------------
INSERT INTO `SNAPSHOT`
(`SNAPSHOT_ID`, `USER_ID`, `SNAPSHOT_BASE_DATE`, `LIQUID_ASSETS`, `MONTHLY_NET_CASHFLOW`,
 `PREP_POSSIBLE_MONTHS`, `AVG_WEEKEND_EXPENSE`, `AVG_WEEKEND_INCOME`,
 `AVG_WEEK_EXPENSE`, `AVG_WEEK_INCOME`, `RISK_LEVEL`) VALUES
(1, 1, '2026-07-23', 5000000, -1400000,
 3.57, 25000.00, 0.00, 18000.00, 16000.00, 'CAUTION')
ON DUPLICATE KEY UPDATE
    `LIQUID_ASSETS`=VALUES(`LIQUID_ASSETS`), `MONTHLY_NET_CASHFLOW`=VALUES(`MONTHLY_NET_CASHFLOW`),
    `PREP_POSSIBLE_MONTHS`=VALUES(`PREP_POSSIBLE_MONTHS`),
    `AVG_WEEKEND_EXPENSE`=VALUES(`AVG_WEEKEND_EXPENSE`), `AVG_WEEKEND_INCOME`=VALUES(`AVG_WEEKEND_INCOME`),
    `AVG_WEEK_EXPENSE`=VALUES(`AVG_WEEK_EXPENSE`), `AVG_WEEK_INCOME`=VALUES(`AVG_WEEK_INCOME`),
    `RISK_LEVEL`=VALUES(`RISK_LEVEL`);

-- ---------------------------------------------------------
-- 11. 정부 지원 정책 (POLICY)
-- ---------------------------------------------------------
INSERT INTO `POLICY`
(`POLICY_ID`, `POLICY_NAME`, `POLICY_CATEGORY`, `POLICY_MIN_AGE`, `POLICY_MAX_AGE`, `POLICY_REGION`,
 `POLICY_SUPPORT_AMOUNT`, `DUE_DATE`, `REQUIRED_DOCUMENT`, `EMPLOYMENT_PREP_STATUS`,
 `FAMILY_COUNT`, `POLICY_STATUS`, `POLICY_URL`) VALUES
(1, '청년월세 특별지원', '주거', 19, 34, '전국', 200000,
 '2026-12-31 23:59:59', '주민등록등본, 임대차계약서, 통장사본', '미취업', 1, 'AVAILABLE', 'https://www.gov.kr/youth-housing'),
(2, '국민취업지원제도',  '취업', 15, 69, '전국', 500000,
 '2026-11-30 23:59:59', '신분증, 구직신청서', '미취업', 1, 'AVAILABLE', 'https://www.work24.go.kr/kua'),
(3, '서울 청년수당',     '취업', 19, 34, '서울특별시', 500000,
 '2026-10-15 23:59:59', '신분증, 졸업증명서', '미취업', 1, 'AVAILABLE', 'https://youth.seoul.go.kr/allowance')
ON DUPLICATE KEY UPDATE
    `POLICY_NAME`=VALUES(`POLICY_NAME`), `POLICY_CATEGORY`=VALUES(`POLICY_CATEGORY`),
    `POLICY_MIN_AGE`=VALUES(`POLICY_MIN_AGE`), `POLICY_MAX_AGE`=VALUES(`POLICY_MAX_AGE`),
    `POLICY_REGION`=VALUES(`POLICY_REGION`), `POLICY_SUPPORT_AMOUNT`=VALUES(`POLICY_SUPPORT_AMOUNT`),
    `DUE_DATE`=VALUES(`DUE_DATE`), `REQUIRED_DOCUMENT`=VALUES(`REQUIRED_DOCUMENT`),
    `EMPLOYMENT_PREP_STATUS`=VALUES(`EMPLOYMENT_PREP_STATUS`), `FAMILY_COUNT`=VALUES(`FAMILY_COUNT`),
    `POLICY_STATUS`=VALUES(`POLICY_STATUS`), `POLICY_URL`=VALUES(`POLICY_URL`);

-- ---------------------------------------------------------
-- 12. 시뮬레이션 (SIMULATION)  * 날짜 DATE
-- ---------------------------------------------------------
INSERT INTO `SIMULATION`
(`SIMULATION_ID`, `USER_ID`, `SNAPSHOT_ID`, `SIMULATION_START_DATE`, `SIMULATION_DUE_DATE`,
 `SIMULATION_END_AMOUNT`, `PREP_MONTHS`, `CONFIRMED_AT`) VALUES
(1, 1, 1, '2026-07-23', '2026-12-23', 1799500, 7.81, '2026-07-23 11:00:00')
ON DUPLICATE KEY UPDATE
    `SNAPSHOT_ID`=VALUES(`SNAPSHOT_ID`), `SIMULATION_START_DATE`=VALUES(`SIMULATION_START_DATE`),
    `SIMULATION_DUE_DATE`=VALUES(`SIMULATION_DUE_DATE`), `SIMULATION_END_AMOUNT`=VALUES(`SIMULATION_END_AMOUNT`),
    `PREP_MONTHS`=VALUES(`PREP_MONTHS`), `CONFIRMED_AT`=VALUES(`CONFIRMED_AT`);

-- ---------------------------------------------------------
-- 13. 시뮬레이션 항목 (SIMULATION_ITEM)  * 적용일 DATE
-- ---------------------------------------------------------
INSERT INTO `SIMULATION_ITEM`
(`SIMULATION_ITEM_ID`, `SIMULATION_ID`, `SIMULATION_ITEM_CATEGORY`, `SIMULATION_ITEM_APPLY_AMOUNT`,
 `APPLY_START_DATE`, `APPLY_END_DATE`, `POLICY_ID`, `DETAIL_VALUE`, `RECURRENCE_TYPE`, `IS_DELETED`) VALUES
(1, 1, 'INCOME',  550000, '2026-08-01', '2026-12-23',
 NULL, '{"job":"편의점 알바","hourlyWage":11000,"hoursPerDay":5,"daysPerWeek":2}', 'MONTHLY', FALSE),
(2, 1, 'POLICY',  200000, '2026-08-01', '2026-12-23',
 1, '{"policyName":"청년월세 특별지원"}', 'MONTHLY', FALSE),
(3, 1, 'EXPENSE', 9900,   '2026-08-01', NULL,
 NULL, '{"action":"넷플릭스 구독 해지"}', 'MONTHLY', FALSE)
ON DUPLICATE KEY UPDATE
    `SIMULATION_ITEM_CATEGORY`=VALUES(`SIMULATION_ITEM_CATEGORY`),
    `SIMULATION_ITEM_APPLY_AMOUNT`=VALUES(`SIMULATION_ITEM_APPLY_AMOUNT`),
    `APPLY_START_DATE`=VALUES(`APPLY_START_DATE`), `APPLY_END_DATE`=VALUES(`APPLY_END_DATE`),
    `POLICY_ID`=VALUES(`POLICY_ID`), `DETAIL_VALUE`=VALUES(`DETAIL_VALUE`),
    `RECURRENCE_TYPE`=VALUES(`RECURRENCE_TYPE`), `IS_DELETED`=VALUES(`IS_DELETED`);

-- ---------------------------------------------------------
-- 14. 월별 예상 (PROJECTION)
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
-- 15. 알림 (NOTIFICATION)
-- ---------------------------------------------------------
INSERT INTO `NOTIFICATION`
(`NOTIFICATION_ID`, `USER_ID`, `NOTIFICATION_TYPE`, `NOTIFICATION_TITLE`,
 `NOTIFICATION_CONTENT`, `NOTIFICATION_URL`, `IS_READ`) VALUES
(1, 1, 'QUEST',            '행동 과제 마감 임박',    '‘청년월세 특별지원 신청’ 과제 마감이 3일 남았어요.', NULL, FALSE),
(2, 1, 'POLICY',           '청년월세 특별지원 D-30', '신청 마감일이 30일 남았습니다. 서류를 준비하세요.', 'https://www.gov.kr/youth-housing', FALSE),
(3, 1, 'FINANCIAL_CHANGE', '준비 가능 기간 증가',    '계획 적용으로 준비 가능 기간이 3.6개월 → 7.8개월로 늘었어요.', NULL, TRUE),
(4, 1, 'REWARD',           '경험치 획득',            '구독 해지 과제를 완료해 30 EXP를 획득했어요.', NULL, TRUE)
ON DUPLICATE KEY UPDATE
    `NOTIFICATION_TYPE`=VALUES(`NOTIFICATION_TYPE`), `NOTIFICATION_TITLE`=VALUES(`NOTIFICATION_TITLE`),
    `NOTIFICATION_CONTENT`=VALUES(`NOTIFICATION_CONTENT`), `NOTIFICATION_URL`=VALUES(`NOTIFICATION_URL`),
    `IS_READ`=VALUES(`IS_READ`);

-- ---------------------------------------------------------
-- 16. 퀘스트 (QUEST)
-- ---------------------------------------------------------
INSERT INTO `QUEST`
(`QUEST_ID`, `USER_ID`, `SIMULATION_ID`, `SIMULATION_ITEM_ID`, `TRANSACTION_ID`,
 `QUEST_TYPE`, `QUEST_TITLE`, `QUEST_DESCRIPTION`, `QUEST_DEADLINE`, `QUEST_STATUS`, `QUEST_URL`,
 `EXP_REWARD`, `QUEST_COMPLETED_AT`) VALUES
(1, 1, 1, 1, NULL, 'APPLY',  '아르바이트 지원하기',
 '편의점 아르바이트 공고를 확인하고 지원하세요.', '2026-08-01 23:59:59', 'NOT_COMPLETED', NULL, 50, NULL),
(2, 1, 1, 2, NULL, 'SUBMIT', '청년월세 특별지원 신청',
 '자격 조건을 확인하고 필요 서류를 제출하세요.', '2026-08-10 23:59:59', 'NOT_COMPLETED',
 'https://www.gov.kr/youth-housing', 100, NULL),
(3, 1, 1, 3, 4,    'CANCEL', '넷플릭스 구독 해지',
 '넷플릭스 정기결제를 해지해 월 9,900원을 절약하세요.', '2026-08-05 23:59:59', 'COMPLETED',
 NULL, 30, '2026-07-22 20:00:00')
ON DUPLICATE KEY UPDATE
    `SIMULATION_ID`=VALUES(`SIMULATION_ID`), `SIMULATION_ITEM_ID`=VALUES(`SIMULATION_ITEM_ID`),
    `TRANSACTION_ID`=VALUES(`TRANSACTION_ID`), `QUEST_TYPE`=VALUES(`QUEST_TYPE`),
    `QUEST_TITLE`=VALUES(`QUEST_TITLE`), `QUEST_DESCRIPTION`=VALUES(`QUEST_DESCRIPTION`),
    `QUEST_DEADLINE`=VALUES(`QUEST_DEADLINE`), `QUEST_STATUS`=VALUES(`QUEST_STATUS`),
    `QUEST_URL`=VALUES(`QUEST_URL`), `EXP_REWARD`=VALUES(`EXP_REWARD`),
    `QUEST_COMPLETED_AT`=VALUES(`QUEST_COMPLETED_AT`);

-- ---------------------------------------------------------
-- 17. 로그 (LOG) - 도메인 이벤트 이력 샘플
-- ---------------------------------------------------------
INSERT INTO `LOG`
(`LOG_ID`, `USER_ID`, `ENTITY_TYPE`, `ENTITY_ID`, `ACTION`, `LOG_DETAIL`) VALUES
(1, 1,    'AUTH',       NULL, 'LOGIN',         JSON_OBJECT('ip', '127.0.0.1')),
(2, 1,    'MYDATA',     1,    'SYNC_SUCCESS',  JSON_OBJECT('syncedCount', 12)),
(3, 1,    'MYDATA',     1,    'TOKEN_REFRESH', JSON_OBJECT('expiresAt', '2026-08-22 12:00:00')),
(4, 1,    'SIMULATION', 1,    'CONFIRM',       JSON_OBJECT('prepMonths', 7.81)),
(5, NULL, 'POLICY',     NULL, 'BATCH_SYNC',    JSON_OBJECT('collected', 3, 'source', '온통청년'))
ON DUPLICATE KEY UPDATE
    `USER_ID`=VALUES(`USER_ID`), `ENTITY_TYPE`=VALUES(`ENTITY_TYPE`), `ENTITY_ID`=VALUES(`ENTITY_ID`),
    `ACTION`=VALUES(`ACTION`), `LOG_DETAIL`=VALUES(`LOG_DETAIL`);
