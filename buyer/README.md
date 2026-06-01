# 득템시루 구매자 앱

마감 임박 할인 상품을 찾아 주문·픽업하는 구매자용 Android 앱입니다.

## 기술 스택

Kotlin · XML View / ViewBinding · Navigation Component · Retrofit · Kakao 로그인 · Google Maps · Tmap · ZXing ([build.gradle.kts](build.gradle.kts), [Android 루트 README](../README.md))

## 시작하기

Android Studio에서 `android-app` 루트를 열고 [공통 시작하기](../README.md#시작하기)·[환경 설정](../README.md#환경-설정)을 따른 뒤, `buyer/local.properties`에 지도 키를 추가하고 [빌드 및 설치](../README.md#빌드-및-설치)를 실행합니다:

```properties
MAPS_API_KEY=...
TMAP_API_KEY=...
```

## 사용 방법

디버그 로그인 → 매장 탐색 → 장바구니 → 결제 → 판매자 승인 대기 → 픽업 코드·QR 확인 → 길찾기 순서로 시연합니다([요구사항](https://github.com/deuktem-siru/.github/blob/main/01-func/01-requirements.md)).

| 증상 | 확인할 항목 |
| --- | --- |
| 지도·길찾기 / 로그인 실패 | Maps SDK·`MAPS_API_KEY`·`TMAP_API_KEY` / Debug 빌드·Kakao 네이티브 키·redirect scheme |
| 서버 연결 / 릴리스 실행 실패 | 백엔드 실행·서버 주소 접근성 / HTTPS `BACKEND_BASE_URL`·인증서 |

## 테스트

[Android 테스트](../README.md#테스트)에서 두 앱과 공통 모듈을 함께 검증합니다.

## 관련 문서

- [Android 공통 안내](../README.md) · [판매자 앱](../seller/README.md) · [Backend](https://github.com/deuktem-siru/backend) · [프로젝트 문서](https://github.com/deuktem-siru/.github#관련-문서)
