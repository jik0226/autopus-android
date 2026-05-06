# Week 1 Builder Guide

Week 1의 빌더 역할은 코드를 많이 치는 것이 아니라, Autopus가 "내 폰 안에 사는 비서"처럼 느껴지는지 판단하고 실제 Android 기기에서 첫 화면을 검증하는 것이다.

## 목표

- Autopus 캐릭터 방향 1개 선택
- Android Studio에서 Gradle sync 통과
- Android 서브폰에서 placeholder 앱 실행
- AI가 작성한 첫 Compose 화면 코드를 직접 읽고 설명할 수 있기

## 빌더만 할 수 있는 일

| 카테고리 | 작업 |
|---|---|
| 의사결정 | 우선순위, 디자인 갈림길, 공개 시점 |
| 창의 | 캐릭터 컨셉, 이름, 말투, 데모 분위기 |
| 실기 검증 | Android 서브폰 dogfooding, 매일 쓰고 싶은지 판단 |
| 물리 작업 | API 키 발급, USB 연결, 개발자 모드, 데모 영상 촬영 |
| AI 협업 | Claude와 Codex 의견 비교 후 최종 결정 |

## Day 1: Repo 확인

- [ ] GitHub repo 열기: `https://github.com/jik0226/autopus-android`
- [ ] `README.md`, `DESIGN.md`, `WORKFLOW.md`, `CONTRIBUTING.md` 훑기
- [ ] 로컬 폴더명을 바꿀지 결정

권장 폴더명:

```text
/Users/j/Downloads/autopus-android
```

현재 Codex 세션은 `/Users/j/Downloads/solo AI`에 묶여 있으므로, rename은 Codex 작업이 끝난 뒤 Finder에서 해도 된다.

## Day 2-4: 캐릭터 디자인

Autopus는 단순한 장식이 아니라 위험한 자동화를 감시하고 승인하는 UI다. 그래서 귀엽기만 하면 안 되고, 상태가 잘 읽혀야 한다.

### 필요한 상태 6개

- [ ] `idle` — 평소 대기
- [ ] `listening` — 사용자의 말을 듣는 중
- [ ] `thinking` — LLM/API 응답 대기
- [ ] `happy` — 작업 성공
- [ ] `urgent` — 중요한 알림/확인 필요
- [ ] `sleep` 또는 `error` — 휴식/실패/중단

### 컨셉 체크 기준

- [ ] 작은 floating overlay에서도 실루엣이 읽히는가?
- [ ] 표정이 3초 안에 이해되는가?
- [ ] 매일 봐도 질리지 않는가?
- [ ] 안전 UI 역할과 어울리는가?
- [ ] 나중에 sprite atlas로 자르기 쉬운가?

### AI 이미지 생성 프롬프트 초안

```text
Cute 2D octopus mascot for an Android mobile AI assistant, small floating companion, soft rounded silhouette, expressive eyes, friendly but alert, simple sprite-ready design, clean transparent background, Korean app mascot style, idle pose, no text, no logo, flat colors, game UI asset
```

상태별 변형 프롬프트:

```text
Same cute 2D octopus mascot, {idle/listening/thinking/happy/urgent/sleep} expression, sprite sheet friendly, transparent background, consistent character design, flat colors, no text
```

### 결과물

- [ ] 컨셉 후보 3개
- [ ] 최종 1개 선택
- [ ] 상태 6개 이미지
- [ ] 가능하면 눈/입 파츠 분리

## Day 5: Android Studio 첫 실행

### 설치

- [ ] Android Studio 설치
- [ ] JDK 17 포함 여부 확인
- [ ] Android SDK Platform 35 설치
- [ ] Android SDK Build Tools 35.0.0+ 설치

### Gradle sync

- [ ] Android Studio에서 repo 열기
- [ ] Gradle sync 실행
- [ ] 에러가 나면 전체 에러 텍스트를 Codex에게 전달

### 서브폰 설정

- [ ] 개발자 옵션 켜기
- [ ] USB debugging 켜기
- [ ] USB로 Mac에 연결
- [ ] 폰에서 디버깅 허용
- [ ] Android Studio device selector에 폰 표시 확인

### 첫 Run

- [ ] `app` configuration 선택
- [ ] Run 실행
- [ ] 폰에 Autopus placeholder 화면이 뜨는지 확인
- [ ] 스크린샷 1장 저장

## 코드 읽기 루틴

v0.X마다 최소 30-60분은 AI가 쓴 코드를 직접 읽는다. 포트폴리오 가치는 "AI가 만들어줬다"가 아니라 "내가 이해하고 조율했다"에서 나온다.

Week 1에서 읽을 파일:

- [ ] `settings.gradle.kts` — 프로젝트와 `:app` 모듈 연결
- [ ] `build.gradle.kts` — 루트 플러그인 선언
- [ ] `gradle/libs.versions.toml` — Gradle/Compose 버전 관리
- [ ] `app/build.gradle.kts` — Android app 설정
- [ ] `app/src/main/AndroidManifest.xml` — 런처 Activity 선언
- [ ] `app/src/main/java/com/jik0226/autopus/MainActivity.kt` — 첫 Compose 화면

Codex에게 물어볼 질문 예시:

```text
MainActivity.kt를 초보자 기준으로 설명해줘. 특히 setContent, Composable, Modifier, Canvas가 뭔지 알려줘.
```

```text
app/build.gradle.kts의 compileSdk, targetSdk, minSdk 차이를 설명해줘.
```

```text
이 프로젝트가 지금 Android Studio에서 어떤 순서로 빌드되는지 설명해줘.
```

## AI에게 맡길 일

- Kotlin/Compose 코드 작성: Codex
- 빌드 에러 진단: Codex
- 문서/디자인 판단 정리: Claude
- README/포트폴리오 문구: Claude
- PR 리뷰: Claude
- 짧은 Android 개념 설명: Codex

## Week 1 완료 조건

- [ ] 캐릭터 방향 1개 선택
- [ ] Android Studio Gradle sync 통과
- [ ] 서브폰에서 앱 실행
- [ ] placeholder 화면 확인
- [ ] `MainActivity.kt`를 빌더가 자기 말로 설명 가능

완료 후 다음 단계는 Week 2 Floating Window다.
