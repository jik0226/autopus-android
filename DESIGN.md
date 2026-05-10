# 디자인: 모바일 RPA + AI Agent + 캐릭터 인터페이스 (Android)

생성일: 2026-05-06
업데이트: 2026-05-06 (v4 — 프로젝트 rebrand: Autopus → Gadi 통합)
상태: DRAFT v4
모드: 빌더 (재미 + 포트폴리오)
협업: Claude + Codex + 빌더(jik0226)
코드네임: **Gadi** (강아지 모티브, "guard"의 발음 — 위험한 자동화의 감시자)

---

## 변경 요약

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
5. **하이브리드 LLM 전략 (핵심)**:
   - v0.2 알림 분류 = **로컬 SLM 가능** (Gemma 3 1B / Llama 3.2 1B / Phi-3 mini / Gemini Nano via AICore)
   - v0.3 작업 자동화 = **클라우드 비전 LLM 필수** (Claude Sonnet / GPT-4o / Gemini)
   - 로컬 VLM은 "**실험 트랙**"으로 병렬 연구. 메인 데모는 클라우드.
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

## 5. 추천 접근법 — 빌드 단위 (v2 강화)

```
v0.1 — 캐릭터 + 채팅 + 폰 내부 상태 조회
v0.2 — 알림 분류 + 알림 → 원본 앱 열기
v0.3a — 알림 온 앱 열어서 메시지 보여주기 (탭만, 답장 X)
v0.3b — 자체 샌드박스 메신저 앱 자동 답장 (draft + confirm)
v0.3c — 카톡 테스트 연락처에 'OK' 답장 (draft + confirm)
v0.4+ — TTS, 시나리오 확장, 일정 관리, Live2D 검토
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

### v0.3a — 알림 온 앱 열어서 메시지 보여주기 (탭만, 답장 X)

**첫 자동화는 "쓰기"가 아니라 "읽기".** 사고 위험 0. 화면 캡처 + 비전 LLM + Action 루프가 동작하는지 검증.

- 명령 예: "방금 카톡 알림 와있는 거 보여줘"
- AccessibilityService로 화면 캡처 → Cloud VLM → "카톡 앱 아이콘 위치" 식별 → 탭
- 카톡 앱 열림 → 최신 채팅방 진입까지만
- **답장 X. 읽기 전용.**
- 이 단계에서 안전 가드레일 (Section 6) 전면 적용 필수

### v0.3b — 샌드박스 메신저 앱 자동 답장

- 자체 가짜 메신저 앱 (Compose로 빠르게)
- 동일 파이프라인 + 입력 액션
- 액션 실행 = **draft 입력 + 사용자 확인 후 전송**

### v0.3c — 카톡 테스트 연락처에 'OK' 답장

- 동일 파이프라인 + 카톡 Accessibility node 매핑
- **첫 진짜 데모 영상** (포트폴리오 표지급)
- 단일 연락처, 단일 채팅방, 고정 문구 ("OK"), draft + confirm

## 6. 안전 가드레일 (v0.3a 진입 전 필수)

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
| v0.3a | "방금 알림 보여줘" 명령 → AI가 카톡 앱 열고 최신 채팅방까지 자동 진입. 사고 0건. |
| v0.3b | 샌드박스 앱에서 "OK 답장해" → AI가 입력란에 'OK' draft + 사용자 confirm 후 전송 성공. |
| v0.3c | "엄마한테 OK 답장해" 명령 → 1분 안에 카톡으로 'OK' 전송 성공. **30초 데모 영상 1개 확보.** |
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

**Week 3 — LLM 연결**

9. Anthropic Claude API 통합 (cloud chat)
10. `ModelRouter` 인터페이스 정의, `CloudClaudeRouter` 구현, `LocalRouter` 빈 껍데기
11. 채팅 흐름 검증 (사용자 → 캐릭터 → API → 응답 → 말풍선)

**Week 4 — Tool Use (폰 상태 조회) + 데모 (신규)**

12. Tool Use 5종 구현: `getCurrentTime`, `getBatteryStatus`, `getNetworkStatus`, `getDeviceInfo`, `getVolumeLevel`
13. Claude API의 tool calling 통합 (Anthropic SDK의 `tools` 파라미터)
14. "지금 몇 시야?", "배터리 얼마야?" → 캐릭터 답변 흐름 검증
15. **v0.1 데모 영상 30~60초 촬영** ("폰 안에 사는 AI 비서" 컨셉)

이후 v0.2 진입 전에 한 번 점검 미팅 (Claude + Codex + 빌더).

## 10. 미해결 / 추후 결정

- 1차 LLM 공급자 — Anthropic Claude로 시작 권장, 비용/응답 속도 보고 GPT/Gemini 추가 라우팅
- 캐릭터 컨셉 — AI 생성 도구 (Stable Diffusion / NovelAI / Midjourney) 선택
- v0.4 TTS 공급자 — 한국어 자연스러움 기준 (ElevenLabs / Azure / OpenAI)
- v0.3a 첫 타깃 앱 — 카톡으로 갈지, 더 단순한 앱(메모/캘린더)부터 갈지

## 11. 관찰적 멘토링 피드백 (Claude의 시선)

세션을 통해 본 빌더의 사고 패턴 — 칭찬과 경고 둘 다.

1. **비전이 빠르게 커지는 경향.** 처음 답에서 "둘 다 + 더 많은 기능 + 캐릭터"로 한 번에 점프했다. 강점(야심)이지만 무덤이 되기도 쉽다. v0.1 → v0.3c 단계화가 안전장치. **본인이 의식적으로 "지금은 v0.X에만 집중"을 외칠 것.**

2. **"로컬"이라는 단어를 처음에 강하게 썼지만, 검증되자 빠르게 수정함.** 좋은 신호. 도전받았을 때 자기 가정을 고집하지 않는다. 빌더 모드에서 매우 중요한 자질.

3. **두 AI 협업을 먼저 제안한 건 영리.** 한 모델 의견에 매몰되지 않으려는 태도. 앞으로도 큰 결정마다 두 AI 시각 교차 검증 권장.

4. **정체성 reframing을 즉시 받아들임 (v1 → v2).** "펫 비서" → "모바일 RPA + 캐릭터 UI"라는 강한 reframing이 들어왔을 때 빌더가 "이제 정확히 잡혔어요"라며 즉시 받아들였다. 자기 표현에 매몰되지 않고 더 정확한 프레이밍으로 갈아타는 능력. **이건 시니어급 사고 패턴.**

5. **놓치기 쉬운 함정 — 안전 가드레일 미루기.** "v0.3 시작할 때 만들면 되지"라고 미루지 말 것. 코드 짜기 시작하면 무조건 가드레일 먼저. 한 번 잘못 보낸 메시지가 프로젝트 신뢰도를 통째로 날린다.

---

**다음 결정 시점**: v0.1 Week 1이 끝나는 시점. 캐릭터 sprite + 액티비티 안 표시까지 됐을 때, Week 2 진입 전에 한 번 점검.

— Claude (jik0226 + Codex 협업 v4)
