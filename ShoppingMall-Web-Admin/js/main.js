/**
 * ===========================================
 * YuhanMarket 운영자 시스템 - Main (main.js)
 * ===========================================
 * 
 * 주요 기능:
 * - 화면 전환 관리
 * - 대시보드 통계 표시
 * - 네비게이션 관리
 * 
 * ===========================================
 */

// 화면 전환 함수
function showScreen(screenName) {
    const screens = document.querySelectorAll('.screen');
    screens.forEach(screen => {
        screen.classList.remove('active');
    });
    
    const targetScreen = document.getElementById(`${screenName}Screen`);
    if (targetScreen) {
        targetScreen.classList.add('active');
    }
}

// 콘텐츠 화면 전환 함수
function showContentScreen(screenName) {
    const screens = document.querySelectorAll('.content-screen');
    screens.forEach(screen => {
        screen.classList.remove('active');
    });
    
    const targetScreen = document.getElementById(`${screenName}Screen`);
    if (targetScreen) {
        targetScreen.classList.add('active');
    }
    
    // 네비게이션 버튼 활성화 상태 변경
    const navBtns = document.querySelectorAll('.nav-btn');
    navBtns.forEach(btn => {
        btn.classList.remove('active');
        if (btn.dataset.screen === screenName) {
            btn.classList.add('active');
        }
    });
    
    // 화면별 데이터 로드
    if (screenName === 'dashboard') {
        loadDashboard();
    } else if (screenName === 'products') {
        loadProducts();
        loadCategoryFilters();
    } else if (screenName === 'categories') {
        loadCategories();
    }
}

// 네비게이션 버튼 이벤트 리스너
document.addEventListener('DOMContentLoaded', () => {
    const navBtns = document.querySelectorAll('.nav-btn');
    navBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            const screen = btn.dataset.screen;
            if (screen) {
                showContentScreen(screen);
            }
        });
    });
    
    // 액션 카드 클릭 이벤트
    const actionCards = document.querySelectorAll('.action-card[data-screen]');
    actionCards.forEach(card => {
        card.addEventListener('click', () => {
            const screen = card.dataset.screen;
            if (screen) {
                showContentScreen(screen);
            }
        });
    });
});

/**
 * 대시보드 통계 로드
 */
async function loadDashboard() {
    // 상품 정보 조회
    const productsResponse = await apiGet('/admin/products');
    
    if (productsResponse.success) {
        const products = productsResponse.products;
        
        // 전체 상품 수
        const totalProducts = products.length;
        document.getElementById('totalProducts').textContent = `${totalProducts}개`;
        
        // 재고 부족 상품 수 (재고 10개 이하)
        const lowStock = products.filter(p => p.StockQuantity <= 10).length;
        document.getElementById('lowStock').textContent = `${lowStock}개`;
        
        // 총 재고 가치 계산
        const totalValue = products.reduce((sum, p) => {
            return sum + (p.Price * p.StockQuantity);
        }, 0);
        document.getElementById('totalValue').textContent = `${Math.floor(totalValue).toLocaleString()}원`;
    }
    
    // 카테고리 정보 조회
    const categoriesResponse = await apiGet('/admin/categories');
    
    if (categoriesResponse.success) {
        const categories = categoriesResponse.categories;
        document.getElementById('totalCategories').textContent = `${categories.length}개`;
    }
}

/**
 * 모달 외부 클릭 시 닫기
 */
document.addEventListener('click', (e) => {
    if (e.target.classList.contains('modal')) {
        e.target.classList.remove('active');
    }
});

/**
 * ESC 키로 모달 닫기
 */
document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
        const modals = document.querySelectorAll('.modal.active');
        modals.forEach(modal => {
            modal.classList.remove('active');
        });
    }
});

// 페이지 로드 시 초기화
window.addEventListener('load', () => {
    // 세션이 있으면 카테고리 필터 로드
    if (hasSession()) {
        loadCategoryFilters();
    }
});

