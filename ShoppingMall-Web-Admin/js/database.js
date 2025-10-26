/**
 * ===========================================
 * YuhanMarket 운영자 시스템 - Database (database.js)
 * ===========================================
 * 
 * 주요 기능:
 * - API 서버와의 통신 함수
 * - 공통 API 호출 함수
 * 
 * ===========================================
 */

// API 베이스 URL
const API_BASE_URL = 'http://localhost:8081/api';

// 세션 ID 저장 (sessionStorage 사용 - 브라우저 탭 닫으면 자동 로그아웃)
let sessionId = sessionStorage.getItem('adminSessionId') || null;

/**
 * 공통 API 호출 함수
 * @param {string} endpoint - API 엔드포인트
 * @param {object} options - fetch 옵션
 * @returns {Promise<object>} - API 응답
 */
async function apiCall(endpoint, options = {}) {
    const url = `${API_BASE_URL}${endpoint}`;
    
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers
    };
    
    // 세션 ID가 있으면 헤더에 추가
    if (sessionId) {
        headers['Authorization'] = sessionId;
    }
    
    const config = {
        ...options,
        headers
    };
    
    try {
        const response = await fetch(url, config);
        
        // 인증 에러 처리 (401) - 로그아웃 중이거나 로그아웃 API인 경우 제외
        if (response.status === 401 && !window._isLoggingOut && !endpoint.includes('/admin/logout')) {
            console.error('인증 실패 - 자동 로그아웃');
            clearSession();
            showScreen('login');
            alert('세션이 만료되었습니다. 다시 로그인해주세요.');
            return { success: false, message: '인증이 필요합니다.' };
        }
        
        const data = await response.json();
        return data;
    } catch (error) {
        console.error('API 호출 오류:', error);
        
        // 네트워크 에러 (서버 연결 끊김) - 로그아웃 중이거나 로그아웃 API인 경우 제외
        if (error instanceof TypeError && error.message.includes('fetch') && !window._isLoggingOut && !endpoint.includes('/admin/logout')) {
            console.error('서버 연결 끊김 - 자동 로그아웃');
            clearSession();
            if (document.getElementById('loginScreen')) {
                showScreen('login');
                alert('서버와의 연결이 끊겼습니다. 다시 로그인해주세요.');
            }
        }
        
        return { success: false, message: '서버와의 통신 중 오류가 발생했습니다.' };
    }
}

/**
 * GET 요청
 */
async function apiGet(endpoint) {
    return await apiCall(endpoint, {
        method: 'GET'
    });
}

/**
 * POST 요청
 */
async function apiPost(endpoint, data) {
    return await apiCall(endpoint, {
        method: 'POST',
        body: JSON.stringify(data)
    });
}

/**
 * PUT 요청
 */
async function apiPut(endpoint, data) {
    return await apiCall(endpoint, {
        method: 'PUT',
        body: JSON.stringify(data)
    });
}

/**
 * DELETE 요청
 */
async function apiDelete(endpoint) {
    return await apiCall(endpoint, {
        method: 'DELETE'
    });
}

/**
 * 세션 ID 저장
 */
function saveSession(id) {
    sessionId = id;
    sessionStorage.setItem('adminSessionId', id);
}

/**
 * 세션 ID 삭제
 */
function clearSession() {
    sessionId = null;
    sessionStorage.removeItem('adminSessionId');
}

/**
 * 세션 ID 확인
 */
function hasSession() {
    return sessionId !== null;
}

