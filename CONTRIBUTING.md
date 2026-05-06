# Contributing — Autopus Android

이 프로젝트는 **빌더 + Claude + Codex** 셋의 분업으로 진행된다. 두 AI 모두 매 세션 시작 시 이 문서를 읽고 룰을 따른다.

---

## Commit 룰 (Lore 형식, 필수)

모든 commit 메시지는 다음 형식을 따른다.

```
<type>(<scope>): <subject>

<body>

Constraint: <invariant or design boundary>
Confidence: <low|medium|high>
Scope-risk: <local|module|system>
Reversibility: <trivial|moderate|difficult>
Directive: <follow-up guidance>
Tested: <what was verified>
Not-tested: <what remains unverified>
Related: <SPEC-ID, issue, or related change>

🐙 Autopus <noreply@autopus.co>
```

### Type 목록

| Type | 의미 |
|---|---|
| `feat` | 새 기능 |
| `fix` | 버그 수정 |
| `refactor` | 동작 변화 없는 코드 개선 |
| `test` | 테스트 추가/수정 |
| `docs` | 문서 |
| `chore` | 빌드/설정 변경 |
| `perf` | 성능 개선 |

### 금지 사항

- **`Co-Authored-By:` 트레일러 사용 금지.** 두 AI 모두 적용.
- 서명은 `🐙 Autopus <noreply@autopus.co>` 단일 사용.

### Codex가 commit 만들 때

매 세션 시작 시 이 파일을 먼저 읽기 (`CONTRIBUTING.md`). Lore 형식 잊지 말 것. 메시지 작성이 헷갈리면 빌더에게 물어 Claude가 작성하게 핸드오프.

---

## 보안 룰 (commit 절대 금지)

- 실제 API 키 (Anthropic, OpenAI, Gemini 등)
- `.env` (실제 값) — 템플릿은 `.env.example`만
- `*.keystore`, `*.jks` (Android signing key)
- `local.properties`
- 사용자 카톡/문자/연락처/알림 캡처 (개발 디버깅용 데이터)

키 발견 시 즉시 `git filter-repo` 또는 BFG로 히스토리에서 제거 + 키 회전.

---

## 디자인 변경 룰

- **모든 디자인 결정은 [DESIGN.md](DESIGN.md)에 반영**
- Codex가 구현 중 디자인 이슈 발견 시 → 빌더에게 보고 → Claude가 DESIGN.md 업데이트 → Codex가 그에 맞춰 구현
- DESIGN.md를 우회한 임시 결정은 **다음 PR review에서 반려 사유**

---

## Branch / PR 룰

- 작업 단위: `feature/v0.X-{topic}` (예: `feature/v0.1-overlay`)
- 처음엔 **`main` + `feature/*`** 만. v0.3 + GitHub Actions CI 붙일 때 `dev` 분리 검토.
- PR 본문에 `Lead: Claude` 또는 `Lead: Codex` 표기 (트래킹용)
- PR review 분담:
  - **Claude 1차** — merge 전 설계/안전 적합도 + 디자인 일관성
  - **Codex self-check** — PR 만들기 전 lint/test 통과 확인
  - 같은 PR을 두 AI가 동시 깊이 보지 않기 (토큰 중복)

---

## 안전 가드레일 (v0.3a 진입 전 필수)

`AccessibilityService` 코드 작성 시작 *전*에 다음 모듈을 먼저 구현한다.

- `allowedPackages` / `blockedPackages` — 결제/은행/2FA/시스템 설정 화면 차단
- `maxSteps` — 한 명령 당 탭/입력 횟수 상한
- `confirmBeforeSend` — 보내기/결제/전송 액션은 사용자 확인 강제
- `sensitiveScreenDetector` — 비밀번호/카드번호 입력란 감지 시 즉시 중단
- `panicStop` — 캐릭터 길게 누르면 모든 자동화 즉시 중단 + Accessibility 비활성화
- 클라우드 VLM 전송 전 redaction — 이름/번호/계좌 마스킹 또는 명시 동의

자세한 설계: [DESIGN.md §6](DESIGN.md).

---

## 두 AI 협업 룰 (요약)

자세한 분업: [WORKFLOW.md](WORKFLOW.md).

- **같은 파일을 두 AI가 동시에 수정 X.** 한 task = 한 AI.
- **큰 결정 직면 시** Codex → 빌더 → Claude 순으로 핸드오프.
- **디자인 통합** — Codex가 발견한 구현 디테일은 Claude가 DESIGN.md에 통합.

---

🐙 Autopus
