/**
 * ===========================================
 * YuhanMarket 운영자 시스템 - Auth (auth.js)
 * ===========================================
 * 
 * 주요 기능:
 * - 로그인/로그아웃
 * - 세션 관리
 * 
 * ===========================================
 */

// DOM 요소
const loginForm = document.getElementById('loginForm');
const loginError = document.getElementById('loginError');
const logoutBtn = document.getElementById('logoutBtn');

// 로그인 폼 이벤트
if (loginForm) {
    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const username = document.getElementById('username').value;
        const password = document.getElementById('password').value;
        
        if (!username || !password) {
            showLoginError('아이디와 비밀번호를 입력해주세요.');
            return;
        }
        
        // 로그인 요청
        const response = await apiPost('/admin/login', {
            username: username,
            password: password
        });
        
        if (response.success) {
            // 세션 저장
            saveSession(response.sessionId);
            
            // 메인 화면으로 전환
            showScreen('main');
            
            // 관리자 이름 표시
            document.getElementById('adminInfo').textContent = `${response.adminName}님`;
            
            // 로그인 폼 초기화
            loginForm.reset();
            loginError.textContent = '';
            
            // 대시보드 및 카테고리 데이터 로드
            loadDashboard();
            loadCategoryFilters();
        } else {
            showLoginError(response.message);
        }
    });
}

// 로그아웃 버튼 이벤트
if (logoutBtn) {
    logoutBtn.addEventListener('click', async () => {
        if (confirm('로그아웃 하시겠습니까?')) {
            // 로그아웃 중임을 표시 (다른 에러 알림 방지)
            window._isLoggingOut = true;
            
            // 로그아웃 API 호출
            try {
                await apiPost('/admin/logout');
            } catch (e) {
                // 에러 무시
            }
            
            // 세션 삭제
            clearSession();
            
            // 로그인 화면으로 전환
            showScreen('login');
            
            // 로그아웃 플래그 제거
            window._isLoggingOut = false;
        }
    });
}

/**
 * 로그인 에러 메시지 표시
 */
function showLoginError(message) {
    if (loginError) {
        loginError.textContent = message;
        setTimeout(() => {
            loginError.textContent = '';
        }, 3000);
    }
}

/**
 * 페이지 로드 시 세션 확인
 */
window.addEventListener('DOMContentLoaded', () => {
    if (hasSession()) {
        showScreen('main');
        loadDashboard();
        loadCategoryFilters();
    } else {
        showScreen('login');
    }
});
