# Workflow — Claude + Codex + 빌더 분업 명세

## 한 줄 원칙

> **Claude = 문서/판단 owner**
> **Codex = repo/code owner**
> **GitHub 문서 = 둘 사이의 계약서**

---

## 분업 표

| 작업 | 담당 |
|---|---|
| 제품 방향 / v0.X 범위 결정 | **Claude** |
| 안전 철학 / 가드레일 설계 | **Claude** 초안 → Codex 구현 |
| `DESIGN.md` / `README.md` / `WORKFLOW.md` / `CONTRIBUTING.md` | **Claude** |
| 포트폴리오 문구 / 데모 영상 스크립트 | **Claude** |
| 권한/정책 리서치 | **Claude** 1차 → Codex 구현 검증 |
| Android 프로젝트 생성 / Gradle / Manifest | **Codex** |
| Kotlin / Compose 구현 | **Codex** |
| 단위 테스트 작성 | **Codex** |
| 빌드 에러 수정 / 단순 디버깅 | **Codex** |
| 멀티 파일 리팩터링 (실행) | **Codex** (설계는 Claude 먼저) |
| 큰 아키텍처 변경 전 판단 | **Claude** 먼저 |
| GitHub Issue 생성 | **Claude** 초안 → Codex 반영 |
| Branch / build / push / 워크스페이스 작업 | **Codex** |
| Commit 메시지 (Lore 형식) | **Claude** 또는 Codex (CONTRIBUTING.md 룰 따라) |
| PR 코드 리뷰 (merge 전) | **Claude** (설계/안전) |
| PR self-check (PR 만들기 전) | **Codex** (lint/test) |
| 복잡 디버깅 (멀티 파일 흐름 추적) | **Claude** |

---

## 토큰 가이드 (대략)

### Claude Max (~$100~200/월)

- 컨텍스트: **200K 토큰**
- 사용량: Pro 대비 5x ($100) 또는 20x ($200) — 5시간 단위 제한
- 강한 영역: 긴 토론, 멀티 파일 분석, 디자인 문서, 트레이드오프 정리
- 공식: [Anthropic Max plan](https://support.claude.com/en/articles/11049741-what-is-the-max-plan)

### Codex Plus (~$20/월)

- 2026년 4월부터 **토큰 기반 credit** (메시지 단위 X)
- **Output 토큰이 input의 6~8배 비쌈** → 짧은 명령형 작업으로 몰기
- 강한 영역: 단일 파일 패치, 빌드/테스트, 빠른 회전
- 공식: [Codex rate card](https://help.openai.com/en/articles/20001106-codex-rate-card)

### 작업 유형별 토큰 비교

| 작업 | Claude (Max) | Codex (Plus) |
|---|---|---|
| 단일 함수 구현 | 5~10k | 2~5k |
| 디자인 문서 1개 (DESIGN.md급) | 30~80k | (긴 컨텍스트 약함) |
| 멀티 파일 리팩터링 | 50~150k | 패치 단위 분할 권장 |
| Gradle 1개 파일 | 3~5k | 2~3k |
| PR 리뷰 (200줄 변경) | 20~50k | 10~30k (self-check 한정) |
| 단순 디버깅 | 10~30k | 5~15k |

### 운영 룰

**Claude에게 시킬 것:**
- "이 방향 맞아?"
- "v0.2 범위 줄여줘"
- "안전 정책 설계해줘"
- "이 PR을 면접관 시각으로 평가해줘"
- "README 다듬어줘"

**Codex에게 시킬 것:**
- "이 issue 구현해"
- "빌드 깨지는 거 고쳐"
- "테스트 추가해"
- "이 함수 짜줘"
- "GitHub push 준비해"

**피해야 할 낭비:**
- Claude에게 전체 코드베이스 매번 붙여넣기 X
- Codex에게 긴 철학 토론 / 문서 브레인스토밍 X — 짧은 명령형으로

---

## 주간 루틴

```
[월요일]
  Claude: 이번 주 목표 → GitHub Issue 3~5개로 쪼갬
  빌더: 우선순위 결정

[화~목]
  Codex: Issue 하나씩 구현 + self-check + PR 생성
  빌더: 필요 시 dogfooding 피드백

[금요일]
  Claude: PR 리뷰 (설계/안전 시각) → merge 또는 수정 요청
  Codex: 수정 + 최종 push
  빌더: 주간 점검 (DESIGN.md 갱신 필요한지)
```

---

## 충돌 방지 룰

1. **같은 파일을 두 AI가 동시에 수정 X.** 한 task = 한 AI.
2. **디자인 결정은 Claude가 `DESIGN.md`에 단일 출처로 반영.** Codex는 그에 맞춰 구현.
3. **Codex가 구현 중 큰 결정 직면 시** → 멈추고 빌더에게 보고 → 빌더가 Claude에 핸드오프 → Claude의 판단을 DESIGN.md에 박은 뒤 Codex가 진행.

---

## v0.X 진입 / 종료 룰

### 단계 시작 시
- Claude: `DESIGN.md §5` 해당 절 재확인 + 필요 시 갱신
- Claude: GitHub Issue 쪼개기 (3~5개)
- 빌더: 우선순위 결정

### 단계 종료 시
- Codex: 최종 빌드 검증 + 테스트 통과 확인
- 빌더: **데모 영상 30~60초 촬영** (포트폴리오용)
- Claude: `DESIGN.md §11` 멘토링 피드백 추가
- 모두: v0.(X+1) 진입 전 짧은 점검

---

## 핸드오프 노트 형식 (Claude → Codex)

큰 결정 / 컨텍스트 전달이 필요할 때 빌더가 두 AI 사이에서 전달하는 노트 형식.

```markdown
# 핸드오프 — Claude → Codex (또는 역방향)

## 컨텍스트
{무엇이 정해졌는지, 무엇이 미정인지}

## 합의된 것
- ...

## 묻고 싶은 것
1. {질문}
2. {질문}

## 발신자 입장 + 추천
{내 의견과 근거}

## 수신자에게 묻는 질문
- {답해야 할 핵심 질문}

— {Claude / Codex}
```

이 형식으로 전달하면 두 AI가 무엇을 결정하고 무엇을 묻는지 명확해지고, 빌더는 결정자 역할에 집중할 수 있다.

---

🐙 Autopus
