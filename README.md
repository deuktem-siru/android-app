# 득템시루 Android

구매자·판매자 Android 앱을 함께 관리하는 저장소입니다. 두 앱은 기존 패키지명과 기능을 유지하며 각각 설치합니다.


| 모듈                       | 용도                                               | applicationId                    |
| ---------------------------- | ---------------------------------------------------- | ---------------------------------- |
| [buyer](buyer/README.md)   | 매장 탐색·주문·픽업                              | `com.example.deuktemsiru_buyer`  |
| [seller](seller/README.md) | 상품·주문·픽업·정산 관리                        | `com.example.deuktemsiru_seller` |
| `common`                   | HTTP 인증·카카오 로그인·암호화 저장소·푸시 처리 | 라이브러리                       |

## 기술 스택

Kotlin · XML View / ViewBinding · Retrofit · OkHttp · Kakao 로그인 · Firebase Cloud Messaging

공통 빌드 설정은 [build.gradle.kts](build.gradle.kts), 의존성 버전은 [버전 카탈로그](gradle/libs.versions.toml), 공통 런타임 구현은 [common](common/)에서 관리합니다. 앱별 의존성과 리소스는 각 모듈에 둡니다.

## 아키텍처

MVVM과 클린 아키텍처의 의존성 경계를 사용합니다.


| 경로                                | 책임                                                 |
| ------------------------------------- | ------------------------------------------------------ |
| `domain/model`, `domain/repository` | Android·Retrofit에 의존하지 않는 모델과 저장소 계약 |
| `domain`의 기능 패키지              | 업무 규칙과 유스케이스                               |
| `presentation/widget`               | 목록 어댑터·차트 등 표시 컴포넌트                   |
| `data`                              | HTTP·파일 업로드·세션 저장소 구현과 JSON 변환      |
| `presentation/view`                 | 화면 표시·입력·탐색                                |
| `presentation/viewmodel`            | 화면 수명에 연결된 요청과 상태                       |
| `AppGraph`                          | 저장소 구현 주입과 ViewModel 생성                    |

화면의 서버 요청은 ViewModel을 거쳐 도메인 저장소 계약으로 전달합니다. ViewModel은 View·Context·ViewBinding을 보관하지 않으며, `ViewModelProvider`가 화면 재생성 시 인스턴스를 유지합니다. 단순 요청은 `AppViewModel`에서 처리하고, 별도 상태가 필요한 흐름은 전용 ViewModel로 분리합니다. 기존 화면의 일회성 렌더링은 suspend 반환값을 사용하며 모든 화면을 StateFlow 관찰 방식으로 전환한 것은 아닙니다.

`CleanArchitectureTest`는 도메인·ViewModel의 데이터 구현 의존성과 화면의 직접 API 호출을 검사합니다. 앱 ID·저장소 키·API 계약은 유지합니다.

구매자 홈·매장·장바구니·결제, 판매자 매출은 전용 ViewModel을 사용합니다. 홈·매출 상태는 StateFlow로 관찰하고, 매출 요청은 기간 전환 시 이전 요청을 취소합니다. JSON 별칭 변환은 `data/remote/WireJson.kt`와 공통 어댑터에서 처리합니다.

상세 의존성 방향·적용 범위는 [아키텍처 설계](https://github.com/deuktem-siru/.github/blob/main/plan.md#아키텍처)를 따릅니다.

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
