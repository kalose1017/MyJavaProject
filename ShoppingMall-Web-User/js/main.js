/**
 * ===========================================
 * YuhanMarket 메인 애플리케이션 (main.js)
 * ===========================================
 * 
 * 주요 기능:
 * - 화면 전환 관리 (홈, 상품검색, 장바구니, 마이페이지)
 * - 네비게이션 버튼 및 액션 카드 이벤트 처리
 * - 화면 전환 이벤트 발생 (screenChanged)
 * - 마지막 화면 기억 기능 (새로고침 시 복원)
 * - 모달 관리 유틸리티
 * 
 * 주요 메서드:
 * - showScreen(screenId) - 화면 전환 및 네비게이션 상태 업데이트
 * - dispatchScreenChangedEvent(screenId) - 화면 전환 이벤트 발생
 * - restoreLastScreen() - 마지막 화면 복원 (새로고침 시)
 * - saveCurrentScreen(screenId) - 현재 화면 저장 (localStorage)
 * 
 * 전역 유틸리티 함수:
 * - showModal(modalId) - 모달 표시
 * - closeModal(modalId) - 모달 닫기
 * - showScreen(screenId) - 화면 전환 (전역 접근)
 * 
 * 화면 ID:
 * - home: 홈 화면
 * - products: 상품검색 화면
 * - cart: 장바구니 화면
 * - mypage: 마이페이지 화면
 * 
 * 특징:
 * - 새로고침 시 마지막 화면 복원 (로그인 상태일 때만)
 * - 로그아웃 시 저장된 화면 정보 삭제
 * 
 * ===========================================
 */

// 메인 애플리케이션 로직
class MainApp {
    constructor() {
        this.currentScreen = 'home';
        this.init();
    }

    init() {
        this.setupEventListeners();
        this.setupNavigation();
        this.restoreLastScreen();
    }
    
    // 마지막 화면 복원
    restoreLastScreen() {
        const lastScreen = localStorage.getItem('lastScreen');
        if (lastScreen && lastScreen !== 'home') {
            // 로그인 상태일 때만 복원
            if (db.currentUser && db.sessionId) {
                this.showScreen(lastScreen);
            }
        }
    }
    
    // 현재 화면 저장
    saveCurrentScreen(screenId) {
        localStorage.setItem('lastScreen', screenId);
    }

    setupEventListeners() {
        // 네비게이션 버튼들
        document.querySelectorAll('.nav-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const screen = e.target.dataset.screen;
                this.showScreen(screen);
            });
        });

        // 액션 카드들 (홈 화면)
        document.querySelectorAll('.action-card').forEach(card => {
            card.addEventListener('click', (e) => {
                const screen = e.currentTarget.dataset.screen;
                this.showScreen(screen);
            });
        });

        // 모달 외부 클릭 시 닫기
        document.querySelectorAll('.modal').forEach(modal => {
            modal.addEventListener('click', (e) => {
                if (e.target === modal) {
                    modal.classList.remove('active');
                }
            });
        });
    }

    setupNavigation() {
        // 화면 전환 이벤트 발생
        const originalShowScreen = this.showScreen.bind(this);
        this.showScreen = (screenId) => {
            originalShowScreen(screenId);
            this.dispatchScreenChangedEvent(screenId);
        };
    }

    showScreen(screenId) {
        // 네비게이션 버튼 활성화 상태 업데이트
        document.querySelectorAll('.nav-btn').forEach(btn => {
            btn.classList.remove('active');
        });
        
        const navBtn = document.querySelector(`[data-screen="${screenId}"]`);
        if (navBtn) {
            navBtn.classList.add('active');
        }

        // 컨텐츠 화면 전환
        document.querySelectorAll('.content-screen').forEach(screen => {
            screen.classList.remove('active');
        });
        
        const targetScreen = document.getElementById(screenId + 'Screen');
        if (targetScreen) {
            targetScreen.classList.add('active');
        }

        this.currentScreen = screenId;
        
        // 현재 화면 저장 (로그인 상태일 때만)
        if (db.currentUser && db.sessionId) {
            this.saveCurrentScreen(screenId);
        }
    }

    dispatchScreenChangedEvent(screenId) {
        const event = new CustomEvent('screenChanged', {
            detail: { screen: screenId }
        });
        document.dispatchEvent(event);
    }
}

// 애플리케이션 초기화
document.addEventListener('DOMContentLoaded', () => {
    const app = new MainApp();
    
    // 전역에서 접근 가능하도록 설정
    window.mainApp = app;
});

// 유틸리티 함수들
function showModal(modalId) {
    document.getElementById(modalId).classList.add('active');
}

function closeModal(modalId) {
    document.getElementById(modalId).classList.remove('active');
}

function showScreen(screenId) {
    if (window.mainApp) {
        window.mainApp.showScreen(screenId);
    }
}

// 전역 함수들 (HTML에서 사용)
window.showModal = showModal;
window.closeModal = closeModal;
window.showScreen = showScreen;
