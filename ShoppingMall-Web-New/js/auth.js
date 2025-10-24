/**
 * ===========================================
 * YuhanMarket 인증 관리자 (auth.js)
 * ===========================================
 * 
 * 주요 기능:
 * - 사용자 로그인/로그아웃 처리
 * - 회원가입 처리 및 유효성 검사
 * - 세션 상태 확인 및 자동 로그인
 * - 화면 전환 관리 (로그인/회원가입/메인)
 * - 사용자 정보 표시 (닉네임, 등급)
 * 
 * 주요 메서드:
 * - handleLogin() - 로그인 처리
 * - handleSignup() - 회원가입 처리
 * - handleLogout() - 로그아웃 처리
 * - checkLoginStatus() - 로그인 상태 확인 및 자동 로그인
 * - checkIdDuplicate() - 아이디 중복 확인
 * - updateUserInfo() - 사용자 정보 업데이트 (닉네임, 등급 표시 및 색상 적용)
 * - showScreen() - 화면 전환 (로그인/회원가입/메인)
 * 
 * 등급 색상 자동 적용:
 * - Bronze: 구리색, Silver: 은색, Gold: 금색
 * - Diamond: 시안색, VIP: 보라색
 * 
 * ===========================================
 */

// 인증 관련 기능
class AuthManager {
    constructor() {
        this.currentUser = null;
        this.init();
    }

    init() {
        this.setupEventListeners();
        this.checkLoginStatus();
        this.setupBeforeUnload();
    }
    
    // 브라우저 창 닫기 감지
    setupBeforeUnload() {
        window.addEventListener('beforeunload', () => {
            // 창이 닫힐 때는 로컬 스토리지에 세션 정보를 유지
            // (서버 세션은 자동으로 만료됨)
        });
        
        // 페이지 숨김 감지 (탭 전환 등)
        document.addEventListener('visibilitychange', () => {
            if (document.hidden) {
                // 페이지가 숨겨질 때는 아무것도 하지 않음
            } else {
                // 페이지가 다시 보일 때 세션 유효성 확인
                this.checkLoginStatus();
            }
        });
    }

    setupEventListeners() {
        // 로그인 폼
        document.getElementById('loginForm').addEventListener('submit', (e) => {
            e.preventDefault();
            this.handleLogin();
        });

        // 회원가입 폼
        document.getElementById('signupForm').addEventListener('submit', (e) => {
            e.preventDefault();
            this.handleSignup();
        });

        // 화면 전환 버튼들
        document.getElementById('signupBtn').addEventListener('click', () => {
            this.showScreen('signupScreen');
        });

        document.getElementById('backToLoginBtn').addEventListener('click', () => {
            this.showScreen('loginScreen');
        });

        document.getElementById('logoutBtn').addEventListener('click', () => {
            this.handleLogout();
        });

        // 아이디 중복 확인
        document.getElementById('checkIdBtn').addEventListener('click', () => {
            this.checkIdDuplicate();
        });
    }

    async checkLoginStatus() {
        try {
            // 먼저 로컬 스토리지에서 세션 정보 확인
            if (db.currentUser && db.sessionId) {
                // 서버에서 세션 유효성 확인
                const userData = await db.getCurrentUser();
                if (userData.loggedIn) {
                    this.currentUser = userData;
                    this.showMainScreen();
                    this.updateUserInfo();
                    
                    // 자동 로그인 시에도 홈 화면으로 이동
                    if (window.mainApp) {
                        window.mainApp.showScreen('home');
                    }
                    return;
                } else {
                    // 서버에서 세션이 무효한 경우 로컬 세션도 삭제
                    db.clearSession();
                }
            }
            
            // 로그인되지 않은 상태
            this.showScreen('loginScreen');
        } catch (error) {
            console.error('로그인 상태 확인 오류:', error);
            // 서버 연결 오류 시에도 로그인 화면으로
            this.showScreen('loginScreen');
        }
    }

    async handleLogin() {
        const loginId = document.getElementById('loginId').value;
        const loginPw = document.getElementById('loginPw').value;
        const errorDiv = document.getElementById('loginError');

        if (!loginId || !loginPw) {
            this.showError(errorDiv, '아이디와 비밀번호를 입력해주세요.');
            return;
        }

        try {
            const result = await db.login(loginId, loginPw);
            
            if (result.success) {
                this.currentUser = result.user;
                this.showMainScreen();
                this.updateUserInfo();
                this.clearForm('loginForm');
                this.hideError(errorDiv);
                
                // 로그인 성공 시 항상 홈 화면으로 이동
                if (window.mainApp) {
                    window.mainApp.showScreen('home');
                }
            } else {
                this.showError(errorDiv, result.message);
            }
        } catch (error) {
            console.error('로그인 오류:', error);
            this.showError(errorDiv, '로그인 중 오류가 발생했습니다.');
        }
    }

    async handleSignup() {
        const formData = {
            loginId: document.getElementById('signupId').value,
            loginPw: document.getElementById('signupPw').value,
            nickName: document.getElementById('signupNickName').value
        };
        
        const errorDiv = document.getElementById('signupError');

        // 유효성 검사
        if (!formData.loginId || !formData.loginPw || !formData.nickName) {
            this.showError(errorDiv, '모든 필드를 입력해주세요.');
            return;
        }

        if (formData.loginPw.length < 4) {
            this.showError(errorDiv, '비밀번호는 4자 이상이어야 합니다.');
            return;
        }

        try {
            const result = await db.signup(formData);
            
            if (result.success) {
                this.showSuccess(errorDiv, '회원가입이 완료되었습니다. 로그인해주세요.');
                setTimeout(() => {
                    this.showScreen('loginScreen');
                    this.clearForm('signupForm');
                    this.hideError(errorDiv);
                }, 2000);
            } else {
                this.showError(errorDiv, result.message);
            }
        } catch (error) {
            console.error('회원가입 오류:', error);
            this.showError(errorDiv, '회원가입 중 오류가 발생했습니다.');
        }
    }

    async checkIdDuplicate() {
        const loginId = document.getElementById('signupId').value;
        const errorDiv = document.getElementById('signupError');

        if (!loginId) {
            this.showError(errorDiv, '아이디를 입력해주세요.');
            return;
        }

        try {
            const result = await db.checkIdDuplicate(loginId);
            
            if (result.success) {
                if (result.available) {
                    this.showSuccess(errorDiv, '사용 가능한 아이디입니다.');
                } else {
                    this.showError(errorDiv, '이미 사용 중인 아이디입니다.');
                }
            } else {
                this.showError(errorDiv, result.message);
            }
        } catch (error) {
            console.error('아이디 중복 확인 오류:', error);
            this.showError(errorDiv, '아이디 중복 확인 중 오류가 발생했습니다.');
        }
    }

    async handleLogout() {
        if (confirm('로그아웃 하시겠습니까?')) {
            try {
                await db.logout();
                this.currentUser = null;
                this.showScreen('loginScreen');
                this.clearAllForms();
                
                // 로그아웃 시 메인 앱 상태도 초기화 (다음 로그인 시 홈으로 시작)
                if (window.mainApp) {
                    window.mainApp.currentScreen = 'home';
                    // 저장된 화면 정보 삭제
                    localStorage.removeItem('lastScreen');
                }
            } catch (error) {
                console.error('로그아웃 오류:', error);
            }
        }
    }

    showMainScreen() {
        this.showScreen('mainScreen');
        this.updateUserInfo();
    }

    updateUserInfo() {
        if (this.currentUser) {
            const userInfo = document.getElementById('userInfo');
            const userGrade = document.getElementById('userGrade');
            
            if (userInfo) {
                userInfo.textContent = `${this.currentUser.customerName}님`;
            }
            
            if (userGrade) {
                userGrade.textContent = `등급: ${this.currentUser.grade}`;
                
                // 등급에 따른 색상 클래스 추가
                userGrade.className = 'grade-info';
                const grade = this.currentUser.grade.toLowerCase();
                if (['bronze', 'silver', 'gold', 'diamond', 'vip'].includes(grade)) {
                    userGrade.classList.add(grade);
                }
            }
        }
    }

    showScreen(screenId) {
        // 모든 화면 숨기기
        document.querySelectorAll('.screen').forEach(screen => {
            screen.classList.remove('active');
        });
        
        // 선택된 화면 보이기
        document.getElementById(screenId).classList.add('active');
    }

    showError(element, message) {
        element.textContent = message;
        element.style.display = 'block';
        element.className = 'error-message';
    }

    showSuccess(element, message) {
        element.textContent = message;
        element.style.display = 'block';
        element.className = 'success-message';
    }

    hideError(element) {
        element.style.display = 'none';
    }

    clearForm(formId) {
        document.getElementById(formId).reset();
    }

    clearAllForms() {
        this.clearForm('loginForm');
        this.clearForm('signupForm');
        document.querySelectorAll('.error-message, .success-message').forEach(el => {
            el.style.display = 'none';
        });
    }
}

// 전역 인증 매니저 인스턴스
const auth = new AuthManager();
