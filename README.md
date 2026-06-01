# 득템시루 Android

## 소개

구매자·판매자 Android 앱(별도 설치)을 함께 관리하는 저장소입니다.

| 모듈 | 용도 | applicationId |
| --- | --- | --- |
| [buyer](buyer/README.md) | 매장 탐색·주문·픽업 | `com.example.deuktemsiru_buyer` |
| [seller](seller/README.md) | 상품·주문·픽업·정산 관리 | `com.example.deuktemsiru_seller` |
| `common` | HTTP 인증·카카오 로그인·암호화 저장소·푸시 처리 | 라이브러리 |

## 기술 스택

Kotlin · XML View / ViewBinding · Retrofit · OkHttp · Kakao 로그인 · Firebase Cloud Messaging ([build.gradle.kts](build.gradle.kts), [libs.versions.toml](gradle/libs.versions.toml), [common](common))

### 아키텍처

MVVM·클린 아키텍처를 사용하며 경계는 [기술 구조](https://github.com/deuktem-siru/.github/blob/main/03-tech/01-architecture.md#아키텍처와-경계)를 따릅니다.

## 시작하기

JDK 21, Android SDK 36.1, Android 10(API 29)+ 기기·에뮬레이터와 실행 중인 [dev 백엔드](https://github.com/deuktem-siru/backend#시작하기)가 필요합니다.

### 환경 설정

루트·모듈별 `local.properties.example`을 `local.properties`로 복사해 설정합니다(모듈 속성이 루트 속성을 덮어씀, 기본 `BACKEND_BASE_URL`은 `http://10.0.2.2:8080/`, 릴리스는 HTTPS 필수, 시크릿·서명 키 커밋 금지):

```text
android-app/
├── local.properties               # sdk.dir, 공통 BACKEND_BASE_URL
├── buyer/
│   ├── local.properties           # MAPS_API_KEY, TMAP_API_KEY, Kakao 키·서명 정보
│   ├── google-services.json       # 구매자 Firebase 설정 (선택, 있을 때만 플러그인 적용)
│   └── release.keystore           # 구매자 릴리스 서명 키 (선택, KEYSTORE_PASSWORD/KEY_ALIAS/KEY_PASSWORD)
└── seller/
    ├── local.properties           # 판매자 Kakao 키·서명 정보
    ├── google-services.json       # 판매자 Firebase 설정 (선택)
    └── release.keystore           # 판매자 릴리스 서명 키 (선택)
```

### 빌드 및 설치

저장소 루트에서 실행합니다(APK 경로: `<모듈>/build/outputs/apk/debug/`, Windows는 `.\gradlew`):

```bash
./gradlew :buyer:assembleDebug :seller:assembleDebug
./gradlew :buyer:installDebug
./gradlew :seller:installDebug
```

## 사용 방법

`dev` 서버의 디버그 로그인으로 [구매자 앱](buyer/README.md#사용-방법)에서 주문한 뒤 [판매자 앱](seller/README.md#사용-방법)에서 승인·픽업을 처리합니다.

## 테스트

```bash
./gradlew :buyer:testDebugUnitTest :seller:testDebugUnitTest :common:testDebugUnitTest
```

[CI](.github/workflows/ci.yml)는 두 앱의 Debug 빌드·단위 테스트·시크릿 검사를 실행합니다.

## 관련 문서

- [디자인 기준](https://github.com/deuktem-siru/.github/blob/main/02-uxui/01-design.md) · [구매자 앱](buyer/README.md) · [판매자 앱](seller/README.md) · [프로젝트 문서](https://github.com/deuktem-siru/.github#관련-문서) · [앱 릴리스 설정](https://github.com/deuktem-siru/.github/blob/main/05-ops/01-deploy.md#앱-릴리스-설정)
