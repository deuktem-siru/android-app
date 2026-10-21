# 득템시루 Android

구매자·판매자 Android 앱을 함께 관리하는 저장소입니다. 두 앱은 기존 패키지명과 기능을 유지하며 각각 설치합니다.

| 모듈 | 용도 | applicationId |
| --- | --- | --- |
| [buyer](buyer/README.md) | 매장 탐색·주문·픽업 | `com.example.deuktemsiru_buyer` |
| [seller](seller/README.md) | 상품·주문·픽업·정산 관리 | `com.example.deuktemsiru_seller` |
| `common` | HTTP 인증·카카오 로그인·암호화 저장소·푸시 처리 | 라이브러리 |

## 기술 스택

Kotlin · XML View / ViewBinding · Retrofit · OkHttp · Kakao 로그인 · Firebase Cloud Messaging

공통 빌드 설정은 [build.gradle.kts](build.gradle.kts), 의존성 버전은 [버전 카탈로그](gradle/libs.versions.toml), 공통 런타임 구현은 [common](common/)에서 관리합니다. 앱별 의존성과 리소스는 각 모듈에 둡니다.

## 시작하기

### 개발 환경

Android Studio에서 이 폴더를 엽니다. JDK 21, Android SDK 36.1, Android 10(API 29) 이상의 기기 또는 에뮬레이터가 필요합니다. 서버 연동에는 별도로 실행한 [dev 백엔드](https://github.com/deuktem-siru/backend#시작하기)가 필요합니다.

### 환경 설정

`local.properties.example`을 `local.properties`로 복사하고 SDK 경로와 서버 주소를 설정합니다. 앱별 예제도 각 모듈의 `local.properties`로 복사해 키를 설정합니다.

```text
android-app/
├── local.properties               # sdk.dir, 공통 BACKEND_BASE_URL
├── buyer/
│   ├── local.properties           # Maps, Tmap, 구매자 Kakao 키·서명 정보
│   ├── google-services.json       # 구매자 Firebase 설정 (선택)
│   └── release.keystore           # 구매자 릴리스 서명 키 (선택)
└── seller/
    ├── local.properties           # 판매자 Kakao 키·서명 정보
    ├── google-services.json       # 판매자 Firebase 설정 (선택)
    └── release.keystore           # 판매자 릴리스 서명 키 (선택)
```

앱 빌드 설정은 루트 속성을 읽은 뒤 모듈 속성으로 덮어씁니다. `sdk.dir`은 루트에 설정합니다. 앱별 `BACKEND_BASE_URL`도 지정할 수 있습니다. 기본 서버 주소는 `http://10.0.2.2:8080/`이며 실기기에서는 접근 가능한 주소로 바꿉니다. 릴리스에서는 HTTPS 주소를 사용합니다.

Firebase 파일이 없는 앱은 Google Services 플러그인을 적용하지 않습니다. 릴리스 서명에는 각 모듈의 `release.keystore`와 `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`가 필요합니다. 기존 앱을 업데이트할 때는 기존 서명 키를 사용합니다. 실제 키·속성·Firebase 파일은 커밋하지 않습니다.

### 빌드 및 설치

저장소 루트에서 실행합니다. Windows PowerShell에서는 `./gradlew` 대신 `.\gradlew`를 사용합니다.

```bash
# 두 앱 빌드
./gradlew :buyer:assembleDebug :seller:assembleDebug

# 앱별 설치
./gradlew :buyer:installDebug
./gradlew :seller:installDebug
```

APK는 각 모듈의 `build/outputs/apk/debug/`에 생성됩니다. 설치에는 연결된 기기나 실행 중인 에뮬레이터가 필요합니다.

## 사용 방법

[구매자 앱](buyer/README.md#사용-방법)에서 주문한 뒤 [판매자 앱](seller/README.md#사용-방법)에서 승인·픽업을 처리합니다. 디버그 로그인은 `dev` 서버에서 사용합니다.

## 테스트

```bash
./gradlew :buyer:testDebugUnitTest :seller:testDebugUnitTest :common:testDebugUnitTest
```

[CI](.github/workflows/ci.yml)는 두 앱의 Debug 빌드·앱 단위 테스트와 시크릿 커밋 검사를 실행합니다. 공통 모듈의 단위 테스트는 위 명령으로 함께 확인합니다.

## 관련 문서

- [구매자 앱](buyer/README.md): 지도 키·사용 흐름·문제 해결
- [판매자 앱](seller/README.md): 디버그 계정·사용 흐름·문제 해결
- [프로젝트 문서](https://github.com/deuktem-siru/.github#관련-문서): 요구사항·API·데이터 모델·배포
- [앱 릴리스 설정](https://github.com/deuktem-siru/.github/blob/main/reference/deploy.md#앱-릴리스-설정): 릴리스 빌드와 실기기 점검

## 저장소 통합 이력

기존 두 저장소의 추적된 앱 소스를 통합했으며, 원본 저장소는 보존했습니다. 통합 커밋은 두 저장소의 기존 `main` 마지막 커밋을 부모로 가지므로 기존 커밋 해시·작성자·메시지·변경 이력을 그대로 유지합니다. 과거 커밋을 체크아웃하면 통합 전 디렉터리 구조가 표시됩니다.

원본 브랜치는 `archive/buyer/main`, `archive/seller/main`에 보관하고, 기존 태그에는 충돌을 피하기 위해 `buyer/`, `seller/` 접두사를 붙였습니다. 다음 명령으로 이력을 확인할 수 있습니다.

```bash
git log --graph --oneline --all
git log archive/buyer/main
git log archive/seller/main
```

원격 저장소는 아직 연결하지 않았습니다. 새 원격에 `main`을 푸시하면 두 앱의 기존 `main` 이력도 함께 전송됩니다. 보관 브랜치와 태그까지 게시하려면 해당 참조도 별도로 푸시합니다. 공통 구현은 `common` 모듈로 추출했으며, 각 앱의 Application이 세션을 하나씩 관리합니다. 앱별 저장소 이름과 판매자 mock 동작은 유지합니다.
