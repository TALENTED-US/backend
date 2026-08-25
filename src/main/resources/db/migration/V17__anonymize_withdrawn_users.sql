UPDATE `USER`
SET USER_NAME = CONCAT('withdrawn-', LEFT(REPLACE(UUID(), '-', ''), 20)),
    USER_EMAIL = CONCAT('withdrawn-', REPLACE(UUID(), '-', ''), '@deleted.local'),
    USER_PASSWORD_HASH = SHA2(UUID(), 256),
    USER_NICKNAME = CONCAT('withdrawn-', LEFT(REPLACE(UUID(), '-', ''), 20)),
    USER_PHONE_NUMBER = CONCAT('w', LEFT(REPLACE(UUID(), '-', ''), 19))
WHERE USER_STATUS = 'WITHDRAWN';
