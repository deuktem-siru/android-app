# 득템시루 Seller App

마감 임박 상품을 등록하고 주문·픽업·매출·정산 신청을 관리하는 판매자용 Android 앱입니다.

## 기술 스택

Kotlin · XML View / ViewBinding · Activity · Retrofit · Kakao 로그인 · ZXing

의존성과 Android SDK 설정은 [app/build.gradle.kts](app/build.gradle.kts)에서 관리합니다.

## 시작하기

### 사전 요구사항

- Android Studio, JDK 21, Android SDK 36.1
- Android 10(API 29) 이상의 기기 또는 에뮬레이터
- [Backend 시작하기](https://github.com/DeuktemSiru/Backend#시작하기)에 따라 별도 터미널에서 실행한 `dev` 서버

### 환경 설정

저장소 루트의 `local.properties`에 다음을 설정합니다.

```properties
KAKAO_NATIVE_APP_KEY=...
BACKEND_BASE_URL=http://10.0.2.2:8080/
```

실기기에서는 `BACKEND_BASE_URL`을 기기가 접근할 수 있는 서버 주소로 바꿉니다. 릴리스에는 HTTPS 주소가 필요합니다.
푸시를 사용하려면 `app/google-services.json`을 준비합니다.

릴리스 서명은 공통 [앱 릴리스 설정](https://github.com/DeuktemSiru/.github/blob/main/reference/deploy.md#앱-릴리스-설정)을 따릅니다. `local.properties`와 `google-services.json`은 커밋하지 않습니다.

### 빌드 및 설치

이 앱 저장소의 루트에서 실행합니다.

```bash
./gradlew assembleDebug
./gradlew installDebug
```

`installDebug`에는 연결된 기기나 실행 중인 에뮬레이터가 필요합니다. Windows PowerShell에서는 `.\gradlew`를 사용합니다.

## 사용 방법

로그인 후 상품을 등록하고 주문 화면에서 승인·픽업 검증을 진행합니다. 주문·정산 상태 규칙은 [요구사항](https://github.com/DeuktemSiru/.github/blob/main/spec.md)를 참고합니다.

### 디버그 로그인

Debug 빌드 로그인 화면의 입력창에 다음 값을 사용합니다.

| 입력 | 계정 |
| --- | --- |
| `mock` | 백엔드 없이 앱 내부 Mock 데이터 |
| `bakery` / `oido` / `오이도` | 오이도 매장 판매자 |
| `cafe` / `baegot` / `배곧` | 배곧 매장 판매자 |
| `bunsik` / `정왕` | 정왕 매장 판매자 |
| `dosirak` / `은행` | 은행 매장 판매자 |
| `mart` / `목감` | 목감 매장 판매자 |
| 이메일 | 해당 이메일로 Debug 로그인 |

백엔드 연동 시연에서는 `mock` 대신 매장 계정을 사용합니다.

### 트러블슈팅

| 증상 | 확인할 항목 |
| --- | --- |
| 서버 연결 실패 | 백엔드 실행 여부, 에뮬레이터 주소·실기기의 서버 접근 가능 여부 |
| Kakao 로그인 실패 | `KAKAO_NATIVE_APP_KEY`, Kakao Developers의 Android 패키지명 |
| 디버그 로그인 실패 | 백엔드 `dev` 프로파일, 해당 판매자 시드 존재 여부 |
| QR·알림 실패 | 카메라 권한, Android 13 이상에서 알림 권한 |

## 테스트

```bash
./gradlew testDebugUnitTest
```

자동 검증 설정은 [CI 워크플로](.github/workflows/ci.yml)를 참고합니다.

## 관련 문서

| 문서 | 내용 |
| --- | --- |
| [프로젝트 문서](https://github.com/DeuktemSiru/.github#관련-문서) | 요구사항·설계·작업 현황·배포 안내 |
| [Backend](https://github.com/DeuktemSiru/Backend) | 서버 실행·샘플 데이터 |
| [구매자 앱](https://github.com/DeuktemSiru/BuyerApp) | 함께 사용하는 앱 |
