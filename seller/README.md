# 득템시루 판매자 앱

마감 임박 상품을 등록하고 주문·픽업·매출·정산 신청을 관리하는 판매자용 Android 앱입니다.

## 기술 스택

Kotlin · XML View / ViewBinding · Activity · Retrofit · Kakao 로그인 · ZXing ([build.gradle.kts](build.gradle.kts), [Android 루트 README](../README.md))

## 시작하기

Android Studio에서 `android-app` 루트를 열고 [공통 시작하기](../README.md#시작하기)·[환경 설정](../README.md#환경-설정)·[빌드 및 설치](../README.md#빌드-및-설치)를 따릅니다.

## 사용 방법

로그인 후 상품을 등록하고 주문 화면에서 승인·픽업 검증을 진행합니다([요구사항](https://github.com/deuktem-siru/.github/blob/main/01-func/01-requirements.md)). Debug 빌드 로그인 창에서는 아래 별칭(백엔드 연동 시 `mock` 제외) 또는 이메일을 입력합니다:

| 입력 | 계정 |
| --- | --- |
| `mock` | 백엔드 없이 앱 내부 Mock 데이터 |
| `bakery`/`oido`/`오이도`, `cafe`/`baegot`/`배곧`, `bunsik`/`정왕`, `dosirak`/`은행`, `mart`/`목감`, 이메일 | 각 매장 판매자 시드 계정 |

| 증상 | 확인할 항목 |
| --- | --- |
| 서버 연결 / 디버그 로그인 실패 | 백엔드(`dev` 프로파일·시드) 실행 여부 및 서버 주소 접근성 |
| Kakao 로그인 / QR·알림 실패 | `KAKAO_NATIVE_APP_KEY`·패키지명 / 카메라·알림(Android 13+) 권한 |

## 테스트

[Android 테스트](../README.md#테스트)에서 두 앱과 공통 모듈을 함께 검증합니다.

## 관련 문서

- [Android 공통 안내](../README.md) · [구매자 앱](../buyer/README.md) · [Backend](https://github.com/deuktem-siru/backend) · [프로젝트 문서](https://github.com/deuktem-siru/.github#관련-문서)
