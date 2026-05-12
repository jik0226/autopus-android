# 디자인: 모바일 RPA + AI Agent + 캐릭터 인터페이스 (Android)

생성일: 2026-05-06
업데이트: 2026-05-06 (v5 — v0.3 reframing: 통합 폰 비서 + 온디바이스 only)
상태: DRAFT v5
모드: 빌더 (재미 + 포트폴리오)
협업: Claude + Codex + 빌더(jik0226)
코드네임: **Gadi** (강아지 모티브, "guard"의 발음 — 위험한 자동화의 감시자)

---

## 변경 요약

### v4 → v5 (v0.3 reframing: 통합 폰 비서 + 온디바이스 only)

- **v0.3 전체 재설계**: 카톡 자동화 (Accessibility) → **통합 폰 비서** (Calendar/SMS/파일/Proactive). Play Store 출시 가능 영역으로.
- 카톡 자동화 등 Accessibility 시나리오 → **v0.5+ 사이드로드 영역**으로 이동.
- **온디바이스 first 원칙 강화**: 기본 온디바이스 only, 사용자 명시 토글 시 cloud fallback (옵션 B).
- v0.3 sub-단계 6개 (a~f) 명시: Calendar → SMS/이메일 → 파일 → UsageStats → Proactive → 통합 데모.
- 빌더의 (b) 야심 선택 → (b') 단계화 수용 패턴 — §11 #6.

### v3 → v4 (프로젝트 전체 rebrand: Autopus → Gadi 통합)

- 도구/앱/repo/캐릭터 모두 **Gadi**로 통합. v3의 "도구 = Autopus 유지" 결정 번복.
- GitHub repo: `autopus-android` → **`gadi`**
- Android 패키지: `com.jik0226.autopus` → **`app.gadi`** (`gadi.app` reverse domain)
- App name: "Autopus Android" → **"Gadi"**
- Commit 서명 라인은 이 프로젝트만 생략 (빌더 글로벌 `CLAUDE.md` 룰의 명시 예외). 협업 정보는 README/WORKFLOW에서.

### v2 → v3 (마스코트 캐릭터 Gadi 확정)

- **마스코트 이름 = Gadi** (빌더의 강아지 이름 + "guard" 발음 — 위험한 자동화의 감시자)
- 컨셉 = 강아지 모티브 (시바/진도 라인, 크림/베이지 단모, 큰 선 귀)
- 도구/앱/repo 이름 = **Autopus** 그대로 (빌더 브랜드 유지) — 이후 v4에서 Gadi로 통합
- v0.1 Week 1 코드네임 결정 항목 완료

### v1 → v2 (정체성 reframing)

Codex의 정체성 reframing 반영:

- 제목/정체성: "펫 캐릭터형 AI 비서" → **"모바일 RPA + AI agent + 캐릭터 인터페이스"**
- 캐릭터 역할 재정의: 데코가 아니라 **"위험한 자동화를 감시/승인하는 UI"**
- v0.1에 **폰 내부 상태 조회 도구** (시간/배터리/네트워크) 추가 — 첫 버전부터 작은 "행동"
- v0.2에 **알림 누르면 원본 앱 열기** 추가 — 액션의 첫 발자국
- v0.3 a/b → **a/b/c 3단계 세분화**: 읽기(탭만) → 샌드박스 쓰기 → 실제 카톡 쓰기
- v0.1 액션 아이템 Week 3 → **Week 4 (Tool Use)** 추가

---

## 1. 정체성 (재정의)

이 프로젝트는 **모바일 RPA + AI agent + 캐릭터 인터페이스**다.

채팅만 하는 챗봇이 아니라, **폰 안의 앱/알림/화면을 보고 실제 모바일 업무를 대신 처리하는 비서.** Anthropic Computer Use(데스크톱 자동화)를 PC가 아닌 Android 폰 내부 업무에 적용한 형태. "AI that actually does things"라는 경험을 모바일에 구현하는 것이 본질.

### 동작 흐름

```
알림 수신 → 상황 판단 → 사용자에게 물어보기 → 앱 열기 → 탭/입력/스크롤 → 완료 보고
```

### 핵심 컴포넌트

```
[ Mascot Overlay UI ]    ← 위험한 자동화의 감시/승인 UI
        ↑↓
[ NotificationListener ] [ AccessibilityService ]
        ↑↓                       ↑↓
[ Mobile Action Loop ]   ← 화면 캡처 + Vision LLM + Action 실행
        ↑↓
[ ModelRouter ]
   ├─ LocalSLM (텍스트 분류, v0.2)
   └─ CloudVLM (화면 이해 + 액션 추론, v0.3)
```

### 캐릭터 = Gadi (이름 = 정체성 = 역할)

마스코트 이름 **Gadi**는 빌더의 강아지 이름이자 영어 "**guard**"의 발음. **예쁜 껍데기가 아니다.** Accessibility 자동화는 잘못 탭하면 바로 사고가 나는 영역이고, 그 위험을 사용자가 편하게 감시/승인하게 만드는 UI가 Gadi다. **이름과 역할이 우연히 일치 — 정체성 강화.**

- **펫 톤의 친근함** (강아지 모티브, 크림/베이지) = confirm 다이얼로그의 마찰을 낮춤
- **표정 변화** (idle / thinking / urgent) = 자동화 상태의 시각적 신호
- **Gadi 길게 누르기** = panic stop 액션
- **자율 실행이 아니라 감독 가능한 실행** = 이 프로젝트의 핵심 차별점
- **Gadi = guard** = 캐릭터의 본질적 역할 (감시자)

**레퍼런스**: Anthropic Computer Use 모바일판 + Vedal Neuro-sama류 마스코트. Mobile-Agent / AppAgent / Mobile-Agent-v2 학술 흐름.

## 2. 이게 멋진 이유

1. **"AI가 폰을 스스로 조작하는 영상"은 30초로 wow를 만든다.** 포트폴리오 표지급.
2. **캐릭터 = 안전 UI 라는 reframing.** 단순 데코가 아니라 위험한 RPA 액션의 감시/승인 인터페이스. 디자인 결정 하나하나에 합리적 근거가 생기고, 면접에서 "왜 캐릭터가 필요한가?"에 즉답 가능.
3. **자율 실행이 아니라 감독 가능한 실행.** "AI가 마음대로 카톡 보냄"이 아니라 "AI가 보내려고 하면 펫이 묻고 사용자가 OK 누르면 보냄." 학원/회사 면접관이 가장 좋아하는 안전성 프레임.
4. **매 버전이 데모 영상 1개.** v0.1만 만들어도 "폰 안에 사는 AI 비서"라는 작품이 손에 남는다.

## 3. 전제 (Claude + Codex 합의 확정)

1. **타깃 OS = Android only.** iOS 권한 제약으로 동등 구현 불가.
2. **빌더는 Android 서브폰으로 dogfooding.** 메인은 iPhone.
3. **느린 템포, 마감 없음.** 학습 + 포트폴리오 가치 우선.
4. **캐릭터 = 2D 펫 톤.** 음성/TTS는 v0.4 이후.
5. **온디바이스 first LLM 전략 (v5 강화)**:
   - **기본 = 온디바이스 only** (Gemma 3 1B / Llama 3.2 1B / Polyglot-ko 1.3B 검토). 모든 데이터 폰 내부 처리.
   - **사용자 명시 토글 시 cloud fallback** (옵션 B) — `ModelRouter`로 추상화. 복잡 명령 또는 한국어 자연스러움 보완용.
   - v0.3까지 모든 시나리오 = 표준 Android API 기반이라 온디바이스 1B로 가능.
   - v0.5+ Accessibility 영역 = 작은 모델 화면 이해 한계 → 사용자 토글로 cloud Vision LLM 또는 미래 모바일 Vision 모델 (Gemma 3n 등) 검토.
6. **`ModelRouter` 인터페이스를 처음부터 박는다.** 로컬/클라우드 추상화로 모델 교체 비용 최소화.
7. **포지셔닝 = Play Store 아님 / 사이드로드 + 포트폴리오 데모.** Google Play는 Accessibility로 자율 자동화하는 앱을 정책상 거부한다.
8. **Android 13+ Restricted Settings, 14+ Foreground Service Type, 15+ overlay 백그라운드 제약, 2026 Developer Verification 롤아웃** 모두 인지 후 사이드로드 경로(ADB / 제한 배포 계정)로 우회.

## 4. 검토한 접근법

### 4-1. v0.1 기술 스택

| 안 | 요약 | 결론 |
|---|---|---|
| **A. Kotlin Native + Jetpack Compose + Cloud LLM API** | 정통 | **채택** |
| B. Flutter + Kotlin 모듈 | UI 빠름, but v0.3에서 결국 Kotlin 깊이 필요 | 기각 |
| C. Android 얇은 클라이언트 + Python 백엔드 | 모델 교체 쉬움, 오프라인 X, 서버 비용 | **v0.3 이후 "연구/디버깅 harness"로 재포지셔닝** |

**A 채택 근거**: 이 프로젝트의 본질은 예쁜 채팅 UI가 아니라 **Android 권한 / Service lifecycle / Overlay / Notification / Accessibility 깊이**다. Native가 자연스럽고, Kotlin 학습 자체가 포트폴리오 자산. Flutter의 UI 우위는 v0.2/v0.3 들어가면 빠르게 사라진다.

**C는 v0.3 이후 Python 서버**로: prompt/eval 로깅, replay harness, 여러 cloud/local 모델 비교, 실패 케이스 데이터셋 관리. 즉 "제품 구조는 Native, 연구/디버깅 harness는 Python."

### 4-2. v0.3 첫 시나리오

| 후보 | 결론 |
|---|---|
| **카톡 'OK' 답장** (단일 연락처, 단일 채팅방, 고정 문구, draft + 사용자 확인) | **채택** (v0.3c 최종 단계) |
| 알람 7:30 만들기 | 기각 (OEM/앱 편차 큼) |

**좁히기 원칙**: 처음에는 자동 전송 X, **draft 입력 후 사용자 확인** 단계 강제. 그 전에 v0.3a로 **읽기만** 하는 단계 1개 더 둠.

## 5. 추천 접근법 — 빌드 단위 (v5 reframing)

```
v0.1 — 캐릭터 + 채팅 + 폰 내부 상태 조회
v0.2 — 알림 분류 + 알림 → 원본 앱 열기
v0.3 — "통합 폰 비서 (온디바이스 only)"
  v0.3a — Calendar 등록/조회 (Calendar Provider API)
  v0.3b — SMS + 이메일 prefilled
  v0.3c — 파일 정리 (반응형: "용량 부족해" 분석 + 제안 + 정리)
  v0.3d — UsageStatsManager 데이터 수집 (백그라운드 학습 기간)
  v0.3e — Proactive 룰 + 트리거
  v0.3f — 통합 데모 영상
v0.4+ — TTS, Live2D 검토
v0.5+ — 카톡 자동 답장 등 Accessibility 영역 (사이드로드 전용 데모)
```

### v0.1 — 캐릭터 + 채팅 + 폰 내부 상태 조회

**핵심 포인트**: floating pet + chat만으로는 약함. **첫 버전부터 작은 "행동"**이 있어야 "폰 안에 사는 비서" 느낌이 산다.

- `SYSTEM_ALERT_WINDOW` 권한 onboarding
- ForegroundService + Floating Window로 화면 위 떠다니는 마스코트
- 클릭 시 말풍선 + 간단 채팅 UI (Compose)
- LLM은 cloud chat API 한 곳만 (Anthropic Claude로 시작)
- 캐릭터 상태머신 6개: `idle` / `listening` / `thinking` / `happy` / `urgent` / `sleep|error`
- 표정은 눈+입 파츠 교체 방식 (스프라이트 atlas)
- `ModelRouter` 인터페이스 정의, `LocalRouter`는 빈 구현 껍데기만
- **LLM Tool Use로 폰 내부 상태 조회 (권한 X)**:
  - `getCurrentTime()` — 현재 시각, 요일
  - `getBatteryStatus()` — 잔량, 충전 상태
  - `getNetworkStatus()` — Wi-Fi / 모바일 데이터 / 오프라인
  - `getDeviceInfo()` — 모델, OS 버전
  - `getVolumeLevel()` — 미디어 음량
- **데모 시나리오 예**: "지금 몇 시야?" / "배터리 얼마야?" / "지금 와이파이야?" → 캐릭터가 자연스럽게 답변

### v0.2 — 알림 분류 + 알림 → 원본 앱 열기

- `NotificationListenerService` 권한 onboarding
- 1차 후보 모델: Gemma 3 1B (MediaPipe LLM Inference) 또는 Gemini Nano (AICore + ML Kit GenAI)
- 분류 결과 → 캐릭터 말풍선 ("이건 봐야 할 것 같아!")
- **사용자가 누르면 원본 앱 열기** (Notification PendingIntent — 아직 자동화 X)
- `allowedPackages` / `blockedPackages` 설정 화면

### v0.3a — Calendar 등록/조회 (Calendar Provider API)

- 명령 예: "내일 영희랑 7시 약속" / "이번 주 일정?"
- 자연어 입력 → LLM Tool Use → 일정 정보 추출 (제목, 시간, 참석자)
- `CalendarContract` API로 등록 / 조회 (사용자 폰에 동기화된 모든 캘린더)
- 사용자가 자주 쓰는 캘린더 자동 감지 (UsageStatsManager 연계, v0.3d 이후 정교화)
- **첫 진짜 데모** — 자연어 → 일정 등록 → 사용자 캘린더 앱에서 확인

### v0.3b — SMS + 이메일 prefilled

**SMS:**
- 명령 예: "엄마한테 늦는다고 문자"
- LLM Tool Use → `SmsManager.sendTextMessage()`
- 안전: 보내기 전 confirm 다이얼로그

**이메일 prefilled:**
- 명령 예: "OO한테 OO 제목으로 메일"
- `Intent.ACTION_SEND` + `EXTRA_EMAIL/SUBJECT/TEXT`
- 사용자 메일 앱 prefilled → 사용자가 마지막 "보내기" 한 번 (안전, 실수 방지)

### v0.3c — 파일 정리 (반응형 분석 + 제안)

- 명령 예: "용량 부족해" / "필요없는 파일 정리해"
- LLM Tool Use → StorageStatsManager + MediaStore 호출
- 90일 이상 안 본 파일 추출 (Downloads, 사진 등)
- 자연어 응답: "다운로드에 X개 YMB. 정리할까요?"
- 사용자 "정리해" → MediaStore API로 **휴지통 이동** (즉시 삭제 X, 안전)

### v0.3d — UsageStatsManager 데이터 수집 시작 (백그라운드 학습)

- `PACKAGE_USAGE_STATS` 권한 onboarding
- 앱 사용 패턴 + 시간대 + 알림 반응 빈도 수집
- Storage 통계, 파일 접근 빈도 수집
- **학습 기간 = 1~2주** (사용자에게 명시: "Gadi가 학습 중이에요")
- 데이터는 폰 내부 SQLite. 외부 전송 X.

### v0.3e — Proactive 룰 + 트리거 (수집 데이터 기반)

- 룰 예시:
  - "Downloads에 90일 안 본 파일 100MB+ 감지 시 알림"
  - "주말 빈 시간 + 자주 보는 사람 메시지 패턴 → 캘린더 제안"
  - "주 1회 자주 쓰는 앱 발견 → 즐겨찾기 제안"
- LLM은 자연어 응답만 생성. 분석/감지는 룰 기반.
- 사용자 동의 후 액션 실행 (자율 실행 X)

### v0.3f — 통합 데모 영상

- 5 시나리오 (Calendar, SMS, 이메일, 파일, Proactive) 한 영상에
- 30~60초 — **포트폴리오 표지급**
- 빌더 일상 시나리오로 시연 (실제 dogfooding 기반)

## 6. 안전 가드레일 (v0.5+ Accessibility 진입 전 필수)

> v0.3 (API 기반)에는 다른 가드레일 적용: 모든 외부 액션 (SMS 발송, 파일 삭제 등) **사용자 confirm 강제**, 휴지통 이동 (즉시 삭제 X), 권한 onboarding 명시 동의.

아래는 **v0.5+ Accessibility 영역** 진입 전 필수:

코드 짜기 시작하면 무조건 가드레일 먼저. 한 번이라도 잘못 보낸 메시지가 있으면 프로젝트 신뢰도가 날아간다.

- **`allowedPackages` / `blockedPackages`** — 결제/은행/2FA/시스템 설정 화면 차단
- **`maxSteps`** — 한 명령 당 탭/입력 횟수 상한
- **`confirmBeforeSend`** — 보내기/결제/전송 액션은 사용자 확인 강제
- **`sensitiveScreenDetector`** — 비밀번호/카드번호 입력란 감지 시 즉시 중단
- **`panicStop`** — 캐릭터 길게 누르면 모든 자동화 즉시 중단 + Accessibility 비활성화
- **클라우드 VLM 전송 전 redaction** — 이름/번호/계좌 마스킹 또는 명시 동의

## 7. 캐릭터 그래픽 파이프라인

1. AI 생성 또는 직접 낙서로 마스코트 컨셉 3안
2. 하나 골라 Krita / Clip Studio / Photopea에서 정리
3. 투명 PNG/WebP **sprite atlas**로 export
4. 6개 상태 sprite + 눈/입 파츠 교체 → Compose에서 frame animation + bounce physics
5. **Live2D는 v0.4 음성/TTS 붙일 때 검토.** 지금 들어가면 프로젝트가 캐릭터 제작기로 새는 함정.

## 8. 성공 기준

| 단계 | 성공 조건 |
|---|---|
| v0.1 | 캐릭터가 화면 위 떠 있고, 클릭하면 채팅. **"지금 몇 시야?", "배터리 얼마야?" 같은 폰 상태 질문에 정확히 답변.** 빌더 본인이 "매일 옆에 있으면 좋겠다" 정도의 감정. |
| v0.2 | 카톡 알림 10건 받았을 때 "엄마/중요/나머지"로 정확하게 분류. 누르면 원본 앱으로 이동. |
| v0.3a | "내일 영희랑 약속" → Calendar 등록 + 사용자 캘린더 앱에서 확인. "내일 일정?" → 자연어 답변. |
| v0.3b | "엄마한테 늦는다 문자" → SmsManager 발송. "OO한테 메일" → 메일 앱 prefilled. |
| v0.3c | "용량 부족해" → 분석 + 제안 + 휴지통 이동 흐름 동작. |
| v0.3d | UsageStatsManager 권한 + 데이터 수집 1주 안정 동작. |
| v0.3e | 1개 이상 Proactive 룰 ("Downloads 90일 안 본 파일") 정확 동작. |
| v0.3f | 5 시나리오 통합 30~60초 데모 영상. **포트폴리오 표지급.** |
| 종합 | 포트폴리오에 올렸을 때 "이거 어떻게 만들었어요?" 질문이 한 번이라도 들어오면 성공. |

## 9. 다음 단계 — v0.1 첫 4주 액션

**Week 1 — 프로젝트 셋업 + 캐릭터 자산 (Gadi)**

1. Android Studio 최신 + 새 Kotlin 프로젝트 (target SDK 36, min 28~30) ✅ 완료
2. 'Hello World' 액티비티 → Gadi sprite 1장 표시 (권한 X, 액티비티 안에서)
3. Gadi 컨셉 3안 AI 생성 (강아지 모티브, 크림/베이지 톤, 시바/진도 라인) → 1안 선택
4. 6개 상태 sprite + 눈/입 파츠 분리

**Week 2 — Floating Window**

5. `SYSTEM_ALERT_WINDOW` 권한 onboarding 화면
6. ForegroundService + Floating Window로 캐릭터 화면 위 띄우기
7. 캐릭터 상태머신 코드 (6 상태 전이)
8. 클릭 시 말풍선 + 채팅 입력 UI

**Week 3 — LLM 연결 (온디바이스 first, v5 반영)**

9. MediaPipe LLM Inference SDK 통합 (`com.google.mediapipe:tasks-genai`)
10. **Gemma 3 1B** 모델 통합 (assets 번들 또는 first-run download + 캐시)
11. `ModelRouter` 인터페이스 정의:
    - `LocalRouter` 구현 (Gemma 3 1B + MediaPipe) — 기본
    - `CloudRouter` 빈 껍데기 (옵션 B 사용자 토글 시 v0.4+에 구현)
12. 채팅 UI ↔ LocalRouter 연결 (사용자 → 캐릭터 → 온디바이스 LLM → 응답 → 말풍선)
13. 갤럭시 퀀텀 2에서 응답 속도 측정 (tokens/sec, 첫 토큰 latency)
14. 한국어 의도 파악 1차 테스트

**Week 4 — Tool Use (폰 상태 조회) + 한국어 검증 + 데모**

15. Tool Use 5종 구현: `getCurrentTime`, `getBatteryStatus`, `getNetworkStatus`, `getDeviceInfo`, `getVolumeLevel`
16. 온디바이스 LLM function calling / structured output 패턴:
    - Gemma 3 1B는 explicit tool calling API 약함 → **JSON 스키마 출력 + 파서** 패턴 권장
    - 안 되면 한국어 특화 모델 (Polyglot-ko 1.3B) fallback 검토
17. "지금 몇 시야?", "배터리 얼마야?" → 캐릭터 답변 흐름 검증
18. **한국어 의도 파악 정확도 측정** (10~20 한국어 명령 케이스)
19. **v0.1 데모 영상 30~60초 촬영** — 1B 한국어 성능에 따라:
    - 충분히 자연스러우면: 그대로 v0.1 데모
    - 부족하면: v0.4에서 Polyglot-ko 또는 fine-tuning 적용 후 데모

이후 v0.2 진입 전에 한 번 점검 미팅 (Claude + Codex + 빌더).

## 10. 미해결 / 추후 결정

- **1차 온디바이스 모델** — Gemma 3 1B 선택 완료. 한국어 정확도는 v0.1 Week 4에 검증.
- **MediaPipe LLM Inference → LiteRT-LM 마이그레이션** — Google AI Edge 공식 권장 (MediaPipe Android LLM Inference deprecated). v0.4 또는 v0.3 끝 시점 검토. Gemma 3 1B는 `.task` (MediaPipe) / `.litertlm` (LiteRT-LM) 둘 다 제공되므로 모델 자체는 그대로 사용 가능.
- **Cloud fallback UX** — v0.3 끝에 검토 (사용자 토글 위치, 안내 문구)
- ~~캐릭터 컨셉~~ ✅ 완료 (강아지 모티브 sprite 2종)
- **v0.4 TTS** — 한국어 온디바이스 (Android TTS 시스템 / Sherpa-ONNX 등 검토)

## 11. 관찰적 멘토링 피드백 (Claude의 시선)

세션을 통해 본 빌더의 사고 패턴 — 칭찬과 경고 둘 다.

1. **비전이 빠르게 커지는 경향.** 처음 답에서 "둘 다 + 더 많은 기능 + 캐릭터"로 한 번에 점프했다. 강점(야심)이지만 무덤이 되기도 쉽다. v0.1 → v0.3c 단계화가 안전장치. **본인이 의식적으로 "지금은 v0.X에만 집중"을 외칠 것.**

2. **"로컬"이라는 단어를 처음에 강하게 썼지만, 검증되자 빠르게 수정함.** 좋은 신호. 도전받았을 때 자기 가정을 고집하지 않는다. 빌더 모드에서 매우 중요한 자질.

3. **두 AI 협업을 먼저 제안한 건 영리.** 한 모델 의견에 매몰되지 않으려는 태도. 앞으로도 큰 결정마다 두 AI 시각 교차 검증 권장.

4. **정체성 reframing을 즉시 받아들임 (v1 → v2).** "펫 비서" → "모바일 RPA + 캐릭터 UI"라는 강한 reframing이 들어왔을 때 빌더가 "이제 정확히 잡혔어요"라며 즉시 받아들였다. 자기 표현에 매몰되지 않고 더 정확한 프레이밍으로 갈아타는 능력. **이건 시니어급 사고 패턴.**

5. **놓치기 쉬운 함정 — 안전 가드레일 미루기.** "v0.3 시작할 때 만들면 되지"라고 미루지 말 것. 코드 짜기 시작하면 무조건 가드레일 먼저. 한 번 잘못 보낸 메시지가 프로젝트 신뢰도를 통째로 날린다.

6. **무덤 패턴 (b → b') 단계화 수용.** v0.3에 5 도메인 묶기 (b) 선택 후, Claude의 "DESIGN.md §11 #1 같은 패턴 재발" 도전을 받아 (b') 단계화 받아들임. 빌더 야심 보존 + 무덤 회피. **미래 v0.4+에서도 같은 흐름 권장** — 큰 비전 던짐 → 한 번 더 도전 → sub-단계화 → 진행.

---

**다음 결정 시점**: v0.1 Week 1이 끝나는 시점. 캐릭터 sprite + 액티비티 안 표시까지 됐을 때, Week 2 진입 전에 한 번 점검.

— Claude (jik0226 + Codex 협업 v5)
