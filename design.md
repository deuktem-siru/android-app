# Opaque Liquid / Soft Relief

두 프로젝트 공통 Android 디자인 기준. Liquid Glass의 둥근 윤곽과 토스에서 영감을 받은 부드러운 표면 깊이를 결합한다.

- 화면: 기존 탐색 구조와 기능을 유지하는 native Android XML dashboard/list/form.
- 표면: 불투명 회색 #F2F4F6 배경, 흰색 #FFFFFF 카드. 블러 및 콘텐츠 투과 없음.
- 깊이: 위/왼쪽 밝은 가장자리와 아래/오른쪽 회색 음영. 수평 1dp·수직 2dp 오프셋, 표면 inset 0.5dp·하단 1dp, native elevation 1dp로 얕게 표현한다. 라이트 음영 #EBEEF2 / 강조 #E1E6EC. 광원과 음영도 불투명하며 표면 전체 alpha를 낮추지 않는다.
- 형태: 카드 28dp, 입력·주요 버튼 18dp, 중간 컴포넌트 20dp, 탐색 32dp.
- 하단 탐색: 높이 72dp, 좌우 16dp, 하단 8dp + 시스템 안전 영역. 불투명한 밝은 테두리와 흰색 → #F9FAFC의 미세한 표면 명암, elevation 2dp. 시스템 여백은 바 바깥 margin에 적용한다. 화면 배경은 바 뒤까지 이어지고 콘텐츠 하단에는 바 높이와 안전 간격을 확보한다. 별도 배경 패널·구분선 없음.
- 텍스트: 시스템 sans-serif, 제목/본문 굵기로 계층 표현. 본문 #191F28, 보조 #333D4B, 설명 #596579.
- 간격: 4dp 기준, 화면 여백 20dp, 섹션 간격 24dp. 기존 화면별 제약을 유지한다.
- 브랜드: 득템시루 orange, 직원 blue, 사장 purple. 성공/경고/오류 의미색 유지.
- 상태: 기존 선택/누름 ripple, disabled 및 API loading 처리 유지. 장식 애니메이션 추가 없음.
- 다크: 구매자 앱 기존 dark theme 유지, 표면과 광원도 불투명하게 전환.
- 토큰: 각 모듈 res/values/colors.xml 및 dimens.xml, parttime-manager common/res/values/dimens.xml·common/res/drawable 공유.

## 득템시루 브랜드 팔레트

- 브랜드 원색 `brand_orange`: #ED6C00. 구매자·판매자 런처 아이콘에 공통 적용.
- UI 기본색 `primary` / 구매자 `primary_fixed`: #B64E00. 흰 글자 대비 5.15:1로 주요 버튼·배지·차트 및 밝은 표면의 강조 텍스트에 사용. #ED6C00 위 흰 글자는 3.11:1이므로 작은 글자가 있는 면에는 쓰지 않는다.
- 밝은 컨테이너 `primary_light` / 보조 버튼: #FFF0E3. 짙은 강조 `primary_dark`: #843B00. 컨테이너 글자: #351500.
- 구매자 다크 모드: 강조 #FFB77D, 컨테이너 #843B00, 강조 위 글자 #4D2200, 컨테이너 글자 #FFF0E3. 흰 글자가 있는 고정 브랜드 면은 #B64E00 유지.
- 구매자·판매자 모두 기존 시루 그린과 성공·경고·오류 색상 유지. 글꼴·레이아웃·표면 깊이는 기존 기준을 따른다.

## Reference

`gd-moit`의 흰색 패널, 차가운 회색 바탕, 28px 패널·18px 컨트롤·32px 탐색 곡률을 Android dp로 적용한다. 웹 그림자의 낮은 대비는 불투명 명암 레이어와 1dp elevation으로 표현한다. 화면 밀도와 시스템 안전 영역은 Android 기준을 유지한다.

## Validation
Android resource compilation and debug APK build for both app variants. Device visual QA requires an emulator or device.
