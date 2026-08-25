# Buttie Backend

청년의 취업 준비와 자산 관리를 돕는 **Buttie** 서비스의 백엔드 API 서버입니다. 사용자의 소비·자산 정보를 기반으로 고정 지출을 관리하고, 취업 준비 기간별 자금 시뮬레이션과 청년 정책·금융상품 추천을 제공합니다.

> 서비스 도메인: [buttie.site](https://buttie.site)

---

## 목차

- [주요 기능](#주요-기능)
- [기술 스택](#기술-스택)
- [프로젝트 구조](#프로젝트-구조)
- [로컬 실행](#로컬-실행)
- [API 문서](#api-문서)
- [테스트](#테스트)
- [운영 및 성능 문서](#운영-및-성능-문서)
- [협업 규칙](#협업-규칙)

---

## 주요 기능

| 영역 | 기능 |
| --- | --- |
| 인증·회원 | 회원가입, 로그인·로그아웃, JWT Access/Refresh Token 재발급, 본인인증, 프로필·취업 준비 정보 관리, 회원 탈퇴 |
| 마이데이터 | 마이데이터 기관 연결, 계좌·카드 자산 등록 및 조회, 거래 내역 동기화, 고정 지출 후보 추출 |
| 가계부 | 거래 내역 조회·등록·수정·삭제, 지출 분류 및 메모 관리, 고정 지출 관리, 캘린더 조회 |
| 시뮬레이션 | 취업 준비 기간별 자금 시뮬레이션, 시뮬레이션 항목 관리, 지출·소득·정책·금융상품 추천, 결과 확정 및 되돌리기 |
| 정책·상품 | 청년 정책 목록·상세 검색, 외부 정책 데이터 수집 및 관리, 금융상품 관리 |
| 대시보드·성장 | 자산 현황 대시보드, 타임라인, 퀘스트 완료·되돌리기, 캐릭터 레벨 관리 |
| 알림 | 알림 목록·읽음 상태 관리, 전체 읽음 처리, 알림 설정 |
| 관리자 | 사용자·정책·금융상품·배치 작업 관리 |

---

## 기술 스택

| 구분 | 기술 |
| --- | --- |
| Language | Java 17 |
| Framework | Spring Framework 5.3, Spring MVC |
| Persistence | MyBatis 3, MySQL 8, HikariCP, Flyway |
| Authentication | JWT, BCrypt, Redis |
| API Documentation | Swagger 2 (Springfox) |
| Search & Cache | Elasticsearch 8, Caffeine |
| External Integration | Apache HttpClient, 온통청년·VWorld·공공데이터 API, PortOne |
| Build & Test | Gradle, JUnit 5, Mockito |
| Infrastructure | Docker Compose, Tomcat 9, Jenkins |
| Monitoring | Prometheus, Grafana, JMX Exporter |
| Performance Test | k6 |

---

## 프로젝트 구조

```text
.
├── src
│   ├── main
│   │   ├── java/com/talented/buttie
│   │   │   ├── common/          # 공통 설정, 보안, 예외, 응답, 암호화
│   │   │   ├── user/            # 인증 및 사용자
│   │   │   ├── mydata/          # 마이데이터 연동 및 자산 동기화
│   │   │   ├── ledger/          # 거래 내역과 가계부
│   │   │   ├── simulation/      # 취업 준비 자금 시뮬레이션
│   │   │   ├── catalog/         # 청년 정책 및 금융상품
│   │   │   ├── dashboard/       # 대시보드 및 타임라인
│   │   │   ├── quest/           # 퀘스트 및 캐릭터 성장
│   │   │   └── notification/    # 알림
│   │   └── resources
│   │       ├── db/migration/    # Flyway 마이그레이션
│   │       ├── mapper/          # MyBatis Mapper XML
│   │       └── application.properties
│   └── test/                    # 단위·통합 테스트
├── mock-mydata/                 # 로컬 마이데이터 Mock 서버
├── monitoring/                  # Prometheus·Grafana·JMX 설정
├── performance/                 # k6 부하 테스트와 결과 문서
├── docs/                        # ERD, 아키텍처, 테이블 명세
├── docker-compose.yml           # MySQL·Redis·Elasticsearch 개발 환경
└── Jenkinsfile                  # 빌드·배포 파이프라인
```

각 도메인은 `controller`, `service`, `domain`, `dto`, `mapper`, `exception`을 중심으로 구성합니다. 공통 기능은 `common` 패키지에서 관리합니다.

---

## 로컬 실행

### 1. 사전 요구 사항

- JDK 17
- Docker 및 Docker Compose
- 외부 Tomcat 9 이상 (WAR 배포 방식)

### 2. 환경 변수 설정

`application.properties`는 환경 변수로 민감한 설정을 주입합니다. 로컬 실행 전 셸 환경 또는 IDE 실행 구성에 아래 값을 설정하세요.

```bash
# MySQL
export MYSQL_ROOT_PASSWORD=local-root-password
export MYSQL_DATABASE=buttie
export MYSQL_USER=buttie
export MYSQL_PASSWORD=local-password

# 인증·암호화
export JWT_SECRET=replace-with-a-long-random-value
export JWT_ACCESS_TOKEN_EXPIRATION=3600000
export JWT_REFRESH_TOKEN_EXPIRATION=1209600000
export REDIS_HOST=localhost
export REDIS_PORT=6379
export CRYPTO_SECRET=replace-with-a-secure-secret

# 선택: 외부 연동 API
export PORTONE_API_KEY=
export OPENAI_API_KEY=
export YOUTH_CENTER_KEY=
export VWORLD_API_KEY=
export SEOUL_DATA_KEY=
export GYEONGGI_DATA_KEY=
export WORK_24_KEY=
```

`MYSQL_HOST`, `MYSQL_PORT`, `MYSQL_DRIVER`, `COOKIE_SECURE`, `MYDATA_MOCK_BASE_URL` 등은 필요에 따라 추가로 조정할 수 있습니다. 기본값은 [application.properties](src/main/resources/application.properties)를 참고하세요.

> API Key, 비밀번호, JWT Secret 등 민감 정보는 Git에 커밋하지 않습니다.

### 3. 의존 서비스 실행

```bash
docker compose up -d mysql redis
```

정책 검색 기능까지 확인하려면 Elasticsearch 프로필을 함께 실행합니다.

```bash
docker compose --profile search up -d
```

### 4. 빌드 및 배포

```bash
./gradlew clean war
```

빌드가 완료되면 `build/libs/backend-1.0-SNAPSHOT.war`가 생성됩니다. 생성된 WAR 파일을 Tomcat의 `webapps/ROOT.war`로 배포한 뒤 Tomcat을 실행하세요.

```bash
cp build/libs/backend-1.0-SNAPSHOT.war "$CATALINA_BASE/webapps/ROOT.war"
"$CATALINA_HOME/bin/catalina.sh" run
```

서버 실행 후 기본 주소는 `http://localhost:8080`입니다.

---

## API 문서

서버 실행 후 Swagger UI에서 API를 확인할 수 있습니다.

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- 정책 조회 예시: `GET /api/catalog/policy?page=1&size=10`

인증이 필요한 API는 다음 형식으로 `Authorization` 헤더를 전달합니다.

```http
Authorization: Bearer {accessToken}
```

---

## 테스트

전체 테스트는 다음 명령으로 실행합니다.

```bash
./gradlew test
```

테스트는 JUnit 5와 Mockito를 사용하며, 통합 테스트용 설정은 `src/test/resources/application-test.properties`에 있습니다.

---

## 운영 및 성능 문서

| 문서 | 설명 |
| --- | --- |
| [ERD](docs/erd.png) | 데이터베이스 ERD |
| [테이블 명세](docs/table-spec.md) | 테이블별 상세 명세 |
| [모니터링 가이드](monitoring/README.md) | Prometheus·Grafana 및 JMX 메트릭 구성·검증 방법 |
| [성능 테스트 결과](performance/README.md) | k6 부하 테스트 시나리오와 로컬 기준 결과 |
| [마이데이터 Mock 서버](mock-mydata/README.md) | 로컬 마이데이터 연동 테스트 방법 |

---

## 협업 규칙

### 브랜치 전략

| 브랜치 | 용도 |
| --- | --- |
| `main` | 배포 가능한 안정 버전 |
| `develop` | 기능 통합 및 개발 브랜치 |
| `feature/*` | 신규 기능 개발 |
| `fix/*` | 일반 오류 수정 |
| `hotfix/*` | 운영 환경 긴급 수정 |
| `refactor/*` | 기능 변경 없는 구조 개선 |

`main`과 `develop` 브랜치에는 직접 Push하지 않고 Pull Request를 통해 병합합니다.

### 커밋 메시지

```text
BUT-번호 타입: 작업 내용
```

예시:

```text
BUT-177 fix: 마이데이터 거래 동기화 안정성 개선
BUT-178 feat: 정책 추천 API 추가
BUT-179 test: 사용자 인증 서비스 테스트 추가
```

| 타입 | 설명 |
| --- | --- |
| `feat` | 새로운 기능 추가 |
| `fix` | 오류 수정 |
| `refactor` | 기능 변경 없는 코드 개선 |
| `test` | 테스트 코드 추가·수정 |
| `docs` | 문서 수정 |
| `chore` | 설정·의존성 등 기타 작업 |

### Pull Request 확인 사항

- 빌드와 관련 테스트가 통과하는지 확인합니다.
- API·DB·환경 변수 변경 사항을 PR 본문에 기록합니다.
- 비밀번호, 토큰, API Key 등 민감 정보가 포함되지 않았는지 확인합니다.
- 새 기능에는 성공·실패 시나리오 테스트를 함께 작성합니다.
