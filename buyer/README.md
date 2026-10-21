# 득템시루 구매자 앱

마감 임박 할인 상품을 찾아 주문하고 픽업하는 구매자용 Android 앱입니다.

## 기술 스택

Kotlin · XML View / ViewBinding · Navigation Component · Retrofit · Kakao 로그인 · Google Maps · Tmap · ZXing

앱별 의존성은 [build.gradle.kts](build.gradle.kts), SDK와 공통 설정은 [Android 루트 README](../README.md)에서 확인합니다.

## 시작하기

[공통 개발 환경·환경 설정](../README.md#시작하기)을 먼저 완료합니다. Android Studio에서는 이 모듈이 아닌 `android-app` 루트를 엽니다. 서버 주소·Kakao 키·Firebase 파일·서명 키 설정은 [공통 설정](../README.md#환경-설정)을 따릅니다.

이 모듈의 `local.properties`에는 구매자 전용 지도 키를 설정합니다.

```properties
MAPS_API_KEY=...
TMAP_API_KEY=...
```

### 빌드 및 설치

`android-app` 루트에서 실행합니다.

```bash
./gradlew :buyer:assembleDebug
./gradlew :buyer:installDebug
```

`installDebug`에는 연결된 기기나 실행 중인 에뮬레이터가 필요합니다. Windows PowerShell에서는 `.\gradlew`를 사용합니다.

## 사용 방법

디버그 로그인 후 매장 탐색 → 장바구니 → 결제 → 판매자 승인 대기 → 픽업 코드·QR 확인 → 길찾기 순서로 시연합니다. 결제 범위와 주문 상태 규칙은 [요구사항](https://github.com/deuktem-siru/.github/blob/main/spec.md)를 참고합니다.

### 트러블슈팅

| 증상 | 확인할 항목 |
| --- | --- |
| 지도·길찾기 실패 | Maps SDK 활성화, `MAPS_API_KEY`·`TMAP_API_KEY` |
| 서버 연결 실패 | 백엔드 실행 여부, 에뮬레이터 주소·실기기의 서버 접근 가능 여부 |
| 로그인 실패 | Debug 빌드 여부, Kakao 네이티브 키·redirect scheme |
| 릴리스 실행 실패 | HTTPS `BACKEND_BASE_URL`과 인증서 |

## 테스트

`android-app` 루트에서 실행합니다.

```bash
./gradlew :buyer:testDebugUnitTest
```

자동 검증 설정은 [CI 워크플로](../.github/workflows/ci.yml)를 참고합니다.

## 관련 문서

| 문서 | 내용 |
| --- | --- |
| [Android 공통 안내](../README.md) | 환경 설정·공통 모듈·통합 이력 |
| [프로젝트 문서](https://github.com/deuktem-siru/.github#관련-문서) | 요구사항·설계·작업 현황·배포 안내 |
| [Backend](https://github.com/deuktem-siru/backend) | 서버 실행·샘플 데이터 |
| [판매자 앱](../seller/README.md) | 함께 사용하는 앱 |
