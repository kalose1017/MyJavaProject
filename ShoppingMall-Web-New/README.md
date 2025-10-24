# YuhanMarket - 온라인 쇼핑몰

HTML, CSS, JavaScript와 Node.js 백엔드를 사용하여 구현한 웹 기반 쇼핑몰 애플리케이션입니다. MySQL 데이터베이스와 연동하여 실제 데이터를 처리합니다.

## 🚀 주요 기능

### 🔐 사용자 인증
- **로그인/로그아웃**: 기존 데이터베이스와 연동
- **회원가입**: 아이디 중복 확인 포함
- **비밀번호 변경**: 보안 강화

### 🛍️ 쇼핑 기능
- **상품 검색**: 상품명 및 카테고리별 검색
- **상품 목록**: 카테고리 필터링 지원
- **장바구니**: 상품 추가/삭제/수량 수정
- **구매**: 전체 구매 및 선택 구매

### 👤 고객 관리
- **마이페이지**: 개인정보 조회
- **페이 충전**: 가상 화폐 충전
- **등급 시스템**: 구매 금액에 따른 등급 관리

## 📁 프로젝트 구조

```
ShoppingMall-Web-New/
├── index.html              # 메인 HTML 파일
├── config/                 # 설정 파일들
│   ├── server.js           # 백엔드 서버 (Node.js + Express)
│   ├── package.json        # Node.js 의존성
│   └── package-lock.json   # 의존성 잠금 파일
├── css/
│   ├── common.css          # 공통 스타일
│   └── main.css            # 메인 화면 스타일
├── js/
│   ├── database.js         # 데이터베이스 API
│   ├── auth.js             # 인증 관리
│   ├── products.js         # 상품 관리
│   ├── cart.js             # 장바구니 관리
│   ├── mypage.js           # 마이페이지 관리
│   └── main.js             # 메인 애플리케이션
├── node_modules/           # Node.js 의존성 모듈들
└── README.md              # 프로젝트 설명
```

## 🎨 UI/UX 특징

### 🎯 최적화된 UI 배치
- **반응형 디자인**: 모바일/태블릿/데스크톱 지원
- **직관적 네비게이션**: 명확한 메뉴 구조
- **모던한 디자인**: 그라데이션과 그림자 효과
- **사용자 친화적**: 직관적인 버튼과 폼 디자인

### 🎨 색상 시스템
- **Primary**: #6366f1 (보라색)
- **Secondary**: #8b5cf6 (연보라색)
- **Success**: #10b981 (초록색)
- **Danger**: #ef4444 (빨간색)
- **Warning**: #f59e0b (주황색)

## 🔧 기술 스택

- **Frontend**: HTML5, CSS3, JavaScript (ES6+)
- **Backend**: Node.js + Express + MySQL2
- **Database**: MySQL (기존 데이터베이스 활용)
- **Styling**: CSS Grid, Flexbox, CSS Variables
- **API**: RESTful API 설계

## 🚀 실행 방법

### 방법 1: 빠른 실행 (권장)
```bash
# 프로젝트 폴더로 이동
cd "C:\Users\leeji\OneDrive\바탕 화면\MyJavaProject\ShoppingMall-Web-New"

# 서버 시작
node config/server.js

# 브라우저에서 http://localhost:8080 접속
```

### 방법 2: 명령 프롬프트 실행
1. 명령 프롬프트(cmd) 열기
2. 프로젝트 폴더로 이동: `cd "C:\Users\leeji\OneDrive\바탕 화면\MyJavaProject\ShoppingMall-Web-New"`
3. 서버 시작: `node config/server.js`
4. 브라우저에서 `http://localhost:8080` 접속

## 📊 데이터베이스 연동

기존 MiniShoppingMall 프로젝트와 동일한 MySQL 데이터베이스를 사용합니다:

- **데이터베이스**: shoppingmall
- **테이블**: customer, product, cart
- **연결 정보**: localhost:3306
- **사용자**: root
- **비밀번호**: ****

## 🎯 주요 화면

### 1. 로그인 화면
- 아이디/비밀번호 입력
- 회원가입 링크
- 깔끔한 로그인 폼

### 2. 홈 화면
- 환영 메시지
- 사용자 등급 표시
- 빠른 액션 카드들

### 3. 상품 검색 화면
- 검색창과 카테고리 필터
- 상품 카드 그리드 레이아웃
- 장바구니 추가 기능

### 4. 장바구니 화면
- 상품 목록과 수량 조절
- 총 금액 계산
- 구매 버튼

### 5. 마이페이지
- 개인정보 표시
- 페이 충전 기능
- 비밀번호 변경

## 🔄 기존 기능과의 호환성

기존 MiniShoppingMall의 모든 기능을 웹으로 구현:

- ✅ 로그인/회원가입
- ✅ 상품 검색 및 카테고리 필터링
- ✅ 장바구니 관리
- ✅ 구매 기능
- ✅ 페이 충전
- ✅ 등급 시스템
- ✅ 마이페이지

## 🎨 디자인 철학

1. **사용자 중심**: 직관적이고 사용하기 쉬운 인터페이스
2. **반응형**: 모든 디바이스에서 최적화된 경험
3. **접근성**: 명확한 색상 대비와 폰트 크기
4. **일관성**: 통일된 디자인 언어와 컴포넌트

## 📱 반응형 지원

- **모바일**: 320px ~ 768px
- **태블릿**: 768px ~ 1024px  
- **데스크톱**: 1024px+

## 🚀 향후 개선 사항

- [ ] PWA 지원 (오프라인 기능)
- [ ] 다크 모드 지원
- [ ] 다국어 지원
- [ ] 실시간 알림
- [ ] 소셜 로그인
- [ ] 결제 시스템 연동
