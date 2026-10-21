# 득템시루 판매자 앱

마감 임박 상품을 등록하고 주문·픽업·매출·정산 신청을 관리하는 판매자용 Android 앱입니다.

## 기술 스택

Kotlin · XML View / ViewBinding · Activity · Retrofit · Kakao 로그인 · ZXing

앱별 의존성은 [build.gradle.kts](build.gradle.kts), SDK와 공통 설정은 [Android 루트 README](../README.md)에서 확인합니다.

## 시작하기

[공통 개발 환경·환경 설정](../README.md#시작하기)을 먼저 완료합니다. Android Studio에서는 이 모듈이 아닌 `android-app` 루트를 엽니다. 서버 주소·Kakao 키·Firebase 파일·서명 키 설정은 [공통 설정](../README.md#환경-설정)을 따릅니다.

### 빌드 및 설치

`android-app` 루트에서 실행합니다.

```bash
./gradlew :seller:assembleDebug
./gradlew :seller:installDebug
```

`installDebug`에는 연결된 기기나 실행 중인 에뮬레이터가 필요합니다. Windows PowerShell에서는 `.\gradlew`를 사용합니다.

## 사용 방법

로그인 후 상품을 등록하고 주문 화면에서 승인·픽업 검증을 진행합니다. 주문·정산 상태 규칙은 [요구사항](https://github.com/deuktem-siru/.github/blob/main/spec.md)를 참고합니다.

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

`android-app` 루트에서 실행합니다.

```bash
./gradlew :seller:testDebugUnitTest
```

자동 검증 설정은 [CI 워크플로](../.github/workflows/ci.yml)를 참고합니다.

## 관련 문서

| 문서 | 내용 |
| --- | --- |
| [Android 공통 안내](../README.md) | 환경 설정·공통 모듈·통합 이력 |
| [프로젝트 문서](https://github.com/deuktem-siru/.github#관련-문서) | 요구사항·설계·작업 현황·배포 안내 |
| [Backend](https://github.com/deuktem-siru/backend) | 서버 실행·샘플 데이터 |
| [구매자 앱](../buyer/README.md) | 함께 사용하는 앱 |
