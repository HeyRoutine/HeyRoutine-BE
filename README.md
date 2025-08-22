# HeyRoutine-BE
HeyRoutine 백엔드 Repository
<br><br>

## Git Flow
- main
- release
- develop
- feat/기능  (ex) (`feat/login-#이슈번호`, `feat/user-login`)
- hotfix/이슈
<br><br>

## git Message
(깃이모지) Feat[#이슈번호]: 커밋 메시지
<br>\- 상세설명1
<br>\- 상세설명2
<br><br>
```
예시)
✨ Feat[#119]: 공인중개사 조회 실패 시 exception 추가
🐛 Fix[#95]: InfoDto 더미 값 수정
🔧 Chore[#74]: 충돌 해결
♻️ Refactor[#72]: MissingReviewDetailException 예외 클래스 제거
✅ Test[#51] 테스트 코드 추가
```
## 설계원칙
**도메인 중심 아키텍처** 설계
`global`에는 공통 인프라·설정·보안·예외를,
`domain`에는 실제 비즈니스 로직을 둠.

- **Domain First**: 도메인별로 Controller/Service/Repository/Entity/DTO를 내부에 캡슐화
- **Separation of Concerns**:  
  - `global` → 공통 인프라/설정/보안/예외/외부연동  
  - `domain` → 비즈니스 규칙, 상태, 유스케이스
- **External Adapters**: 외부 API/메시징/영속 보조는 `global.infra` 아래 어댑터로 분리
- **Naming**: 패키지 전부 **소문자/단수형**, 표준 약어만 사용(DTO, API, JWT 등)
<br><br>

## 📁 디렉토리 트리

```
📦 com.heyroutine
├─ 📂 global                  # 공통 인프라/설정/보안/예외
│  ├─ 📂 config               # Spring/외부 라이브러리 설정
│  ├─ 📂 common               # 전역 공통 파일
│  │  ├─ 📂 dto               # 공통 DTO
│  │  └─ 📂 enum              # 공통 Enum
│  ├─ 📂 error                # 예외 처리
│  │  ├─ 📂 exception         # 예외 클래스, 에러 코드
│  │  └─ 📂 handler           # 전역 예외 핸들러
│  ├─ 📂 security             # 인증·인가 인프라
│  │  └─ 📂 jwt               # JWT 발급/검증/필터
│  ├─ 📂 web                  # 전역 Web 계층 유틸
│  │  └─ 📂 response          # API 응답 래퍼
│  └─ 📂 infra                # 외부 연동/메시징/영속성
│     ├─ 📂 http              # 외부 API 클라이언트
│     │  ├─ 📂 oauth          # OAuth 어댑터
│     │  └─ 📂 bank           # 금융 API 어댑터
│     ├─ 📂 messaging         # Kafka, FCM, 메일, SMS
│     └─ 📂 persistence       # BaseEntity, Auditing
│
└─ 📂 domain                  # 비즈니스 로직
   ├─ 📂 auth                 # 인증 흐름/동의/토큰 정책
   │  ├─ 📂 controller
   │  ├─ 📂 service
   │  ├─ 📂 entity
   │  ├─ 📂 repository
   │  └─ 📂 dto
   ├─ 📂 user                 # 유저 관리
   ├─ 📂 routine              # 루틴 관리
   │  ├─ 📂 group             # 그룹 루틴
   │  └─ 📂 personal          # 개인 루틴
   ├─ 📂 finance              # 금융 도메인 로직
   └─ 📂 analysis             # 추천/스코어링, AI 분석
```