# YuhanMarket 운영자 시스템

## 📋 개요
YuhanMarket의 상품 및 카테고리를 관리하는 운영자 전용 웹 시스템입니다.

## 🚀 주요 기능

### 1. 대시보드
- 전체 상품 수 조회
- 카테고리 수 조회
- 재고 부족 상품 확인 (재고 10개 이하)
- 총 재고 가치 계산
- 빠른 작업 버튼

### 2. 상품 관리
- 상품 목록 조회 (검색, 카테고리 필터)
- 상품 추가 (카테고리 선택, 자동 ID 생성)
- 상품 수정 (가격, 재고량, 원산지)
- 상품 삭제

### 3. 카테고리 관리
- 카테고리 목록 조회
- 카테고리 추가 (자동 ID 생성, 중복 확인)
- 카테고리 삭제 (물품 존재 여부 확인)

## 🔧 설치 및 실행

### 1. Node.js 패키지 설치
```bash
cd config
npm install
```

### 2. 서버 실행
```bash
npm start
# 또는
node server.js
```

### 3. 브라우저 접속
```
http://localhost:8081
```

## 🔐 로그인 정보

- **아이디**: `admin`
- **비밀번호**: `admin1234`

## 📁 프로젝트 구조

```
ShoppingMall-Web-Admin/
├── index.html              # 메인 HTML (SPA)
├── css/
│   ├── common.css          # 공통 스타일
│   └── admin.css           # 운영자 페이지 스타일
├── js/
│   ├── database.js         # API 통신 함수
│   ├── auth.js             # 로그인/인증
│   ├── products.js         # 상품 관리 (CRUD)
│   ├── categories.js       # 카테고리 관리 (CRUD)
│   └── main.js             # 화면 전환 및 대시보드
├── config/
│   ├── server.js           # Express 백엔드 서버
│   └── package.json        # 의존성 설정
└── README.md               # 이 파일
```

## 🎨 화면 구성

### 로그인 화면
- 운영자 인증

### 대시보드
- 통계 정보 (상품 수, 카테고리 수, 재고 부족, 총 가치)
- 빠른 작업 카드

### 상품 관리
- 검색 및 필터링
- 테이블 형식 목록
- 추가/수정/삭제 모달

### 카테고리 관리
- 테이블 형식 목록
- 추가/삭제 모달

## 🔌 API 엔드포인트

### 인증
- `POST /api/admin/login` - 로그인
- `POST /api/admin/logout` - 로그아웃

### 상품
- `GET /api/admin/products` - 상품 목록 조회
- `GET /api/admin/products/:id` - 상품 상세 조회
- `POST /api/admin/products` - 상품 추가
- `PUT /api/admin/products/:id` - 상품 수정
- `DELETE /api/admin/products/:id` - 상품 삭제

### 카테고리
- `GET /api/admin/categories` - 카테고리 목록 조회
- `POST /api/admin/categories` - 카테고리 추가
- `DELETE /api/admin/categories/:id` - 카테고리 삭제

## 💾 데이터베이스

- **데이터베이스**: shoppingmall
- **테이블**: shopdatatable
- **포트**: 3306 (MySQL)
- **호스트**: localhost

## ⚠️ 주의사항

1. **고객용 서버와 포트 구분**
   - 고객용: http://localhost:8080
   - 운영자용: http://localhost:8081

2. **동시 실행 가능**
   - 고객용과 운영자용 서버를 동시에 실행할 수 있습니다.

3. **카테고리 삭제 제한**
   - 물품이 있는 카테고리는 삭제할 수 없습니다.
   - 먼저 해당 카테고리의 모든 물품을 삭제해야 합니다.

4. **상품 ID 자동 생성**
   - 형식: 카테고리ID * 100 + 순차번호
   - 예: 카테고리 ID 1 → 101, 102, 103...

## 🎯 기존 Java 프로그램과의 차이점

### Java 버전 (MiniShoppingMall(Employee))
- 콘솔 기반 인터페이스
- Scanner로 입력 받기
- 메뉴 번호 선택 방식

### 웹 버전 (ShoppingMall-Web-Admin)
- 웹 브라우저 기반 GUI
- 직관적인 버튼/폼 인터페이스
- 실시간 검색 및 필터링
- 대시보드 통계 기능 추가
- 모달 팝업으로 편리한 UX

## 🛠️ 기술 스택

- **Frontend**: HTML5, CSS3, JavaScript (Vanilla)
- **Backend**: Node.js, Express
- **Database**: MySQL
- **아키텍처**: SPA (Single Page Application)

## 📝 개발 정보

- 고객용 웹 페이지(ShoppingMall-Web-New)와 동일한 구조로 개발
- 동일한 데이터베이스 사용 (shoppingmall)
- RESTful API 설계
- 세션 기반 인증 (메모리 저장소)

---

**YuhanMarket 운영자 시스템** - 편리한 상품 관리를 위한 웹 기반 솔루션

