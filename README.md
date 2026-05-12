# Gadi

> 모바일 RPA + AI agent + 캐릭터 인터페이스 — Anthropic Computer Use의 Android 모바일 버전

화면 위에 떠다니는 펫 마스코트 **Gadi** (강아지 모티브, "guard" 발음)가 알림을 분류하고, 자연어 명령에 따라 폰 UI를 직접 조작하는 에이전트. "AI that actually does things"라는 경험을 모바일에 구현. **이름이 곧 역할 — 위험한 자동화의 감시자.**

**상태**: Pre-v0.1 (Week 1 셋업 단계)

---

## 핵심 컨셉

- **Notification Listener** — 알림을 LLM이 분류, 중요한 것만 Gadi가 알려줌 (v0.2)
- **Standard Android APIs** — Calendar / SMS / 파일 / UsageStats 기반 비서 (v0.3, 온디바이스 only)
- **Mascot Overlay** — Gadi = 위험한 자동화의 감시/승인 UI (단순 데코 아님)
- **On-device first LLM** — 기본 온디바이스 (Gemma/Llama 1B), 사용자 토글 시 cloud fallback
- **Accessibility Service** — 카톡/외부 앱 자동화 (v0.5+, 사이드로드 전용)

자세한 설계: [DESIGN.md](DESIGN.md)

## 기술 스택

- **Platform**: Android only (target SDK 36, min 28~30)
- **Language**: Kotlin + Jetpack Compose
- **권한**: `SYSTEM_ALERT_WINDOW`, `NotificationListenerService`, `AccessibilityService`
- **LLM**: Anthropic Claude (cloud chat, v0.1) → Cloud Vision LLM (v0.3) + 로컬 SLM (v0.2)
- **포지셔닝**: 사이드로드 / 포트폴리오 데모 (Play Store는 Accessibility 자동화 거부 정책)

## 빌드 단위

| 버전 | 내용 |
|---|---|
| v0.1 | 캐릭터 + 채팅 + 폰 내부 상태 조회 (Tool Use) |
| v0.2 | 알림 분류 + 알림 → 원본 앱 열기 |
| v0.3a | Calendar 등록/조회 (Calendar Provider API) |
| v0.3b | SMS + 이메일 prefilled |
| v0.3c | 파일 정리 (반응형: 용량 부족 분석 + 제안 + 정리) |
| v0.3d | UsageStatsManager 데이터 수집 (백그라운드 학습) |
| v0.3e | Proactive 룰 + 트리거 |
| v0.3f | 통합 데모 영상 (30~60초, 포트폴리오 표지) |
| v0.4+ | TTS, Live2D 검토 |
| v0.5+ | 카톡 자동 답장 (Accessibility, 사이드로드 전용 데모) |

각 단계가 독립 데모 영상 1개. 자세한 일정: [DESIGN.md §5](DESIGN.md).

## Quickstart

### Requirements

- Android Studio with JDK 17
- Android SDK Platform 36
- Android SDK Build Tools 36.0.0+

### Run

1. Open this repository in Android Studio.
2. Let Gradle sync the `:app` module.
3. Run `app` on an emulator or Android device.

## 협업 구조

빌더(`@jik0226`) + **Claude (Anthropic Max)** + **Codex (OpenAI Plus)** 셋의 분업.

- **Claude** — 문서/판단 owner: DESIGN.md, 아키텍처 결정, PR 리뷰, 포트폴리오 문구
- **Codex** — repo/code owner: Kotlin/Compose 구현, 빌드, 테스트, GitHub 작업
- **빌더** — 우선순위 + 최종 결정 + dogfooding (Android 서브폰)

분업 명세: [WORKFLOW.md](WORKFLOW.md)
기여 규칙: [CONTRIBUTING.md](CONTRIBUTING.md)

## License

MIT. See [LICENSE](LICENSE).

---

*"Gadi — AI that actually does things, but only when you say so."*
