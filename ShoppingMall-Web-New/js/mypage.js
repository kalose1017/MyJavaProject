/**
 * ===========================================
 * YuhanMarket 마이페이지 관리자 (mypage.js)
 * ===========================================
 * 
 * 주요 기능:
 * - 사용자 정보 조회 및 표시
 * - 닉네임 변경 (중복 확인)
 * - 비밀번호 변경
 * - 페이 충전 (누적 충전액 관리, 등급 자동 승급)
 * - 등급 안내 모달
 * - 화면 활성화 시 자동 새로고침
 * 
 * 주요 메서드:
 * - loadUserInfo() - 사용자 정보 로드
 * - displayUserInfo(userData) - 사용자 정보 표시 (아이디, 닉네임, 등급, 잔액)
 * - showChangeNicknameModal() - 닉네임 변경 모달 표시
 * - confirmChangeNickname() - 닉네임 변경 처리
 * - showChangePasswordModal() - 비밀번호 변경 모달 표시
 * - confirmChangePassword() - 비밀번호 변경 처리
 * - showChargeModal() - 페이 충전 모달 표시
 * - confirmCharge() - 페이 충전 처리 (등급 승급 알림 포함)
 * - showGradeGuideModal() - 등급 안내 모달 표시
 * 
 * 등급 시스템:
 * - Bronze: 기본 등급
 * - Silver: 50만원 이상
 * - Gold: 150만원 이상
 * - Diamond: 300만원 이상
 * - VIP: 500만원 이상
 * 
 * 페이 충전 시:
 * - TotalCharge(누적 충전액) 증가
 * - 등급 자동 계산 및 업데이트
 * - 승급 시 축하 메시지 표시
 * 
 * ===========================================
 */

// 마이페이지 관련 기능
class MyPageManager {
    constructor() {
        this.init();
    }

    init() {
        this.setupEventListeners();
    }

    setupEventListeners() {
        // 마이페이지 화면이 활성화될 때마다 사용자 정보 로드
        document.addEventListener('screenChanged', (e) => {
            if (e.detail.screen === 'mypage') {
                this.loadUserInfo();
            }
        });

        // 닉네임 변경 버튼
        document.getElementById('changeNicknameBtn').addEventListener('click', () => {
            this.showChangeNicknameModal();
        });

        // 비밀번호 변경 버튼
        document.getElementById('changePwBtn').addEventListener('click', () => {
            this.showChangePasswordModal();
        });

        // 등급 안내 버튼
        document.getElementById('gradeGuideBtn').addEventListener('click', () => {
            this.showGradeGuideModal();
        });

        // 닉네임 변경 모달
        document.getElementById('confirmChangeNickname').addEventListener('click', () => {
            this.confirmChangeNickname();
        });

        document.getElementById('cancelChangeNickname').addEventListener('click', () => {
            this.closeModal('changeNicknameModal');
        });

        // 비밀번호 변경 모달
        document.getElementById('confirmChangePw').addEventListener('click', () => {
            this.confirmChangePassword();
        });

        document.getElementById('cancelChangePw').addEventListener('click', () => {
            this.closeModal('changePwModal');
        });

        // 등급 안내 모달
        document.getElementById('closeGradeGuide').addEventListener('click', () => {
            this.closeModal('gradeGuideModal');
        });

        // 헤더 페이 충전 버튼
        document.getElementById('headerChargeBtn').addEventListener('click', () => {
            this.showChargeModal();
        });

        // 페이 충전 모달
        document.getElementById('confirmCharge').addEventListener('click', () => {
            this.confirmCharge();
        });

        document.getElementById('cancelCharge').addEventListener('click', () => {
            this.closeModal('chargeModal');
        });
    }

    async loadUserInfo() {
        try {
            const userData = await db.getCurrentUser();
            
            if (userData.loggedIn) {
                this.displayUserInfo(userData);
            }
        } catch (error) {
            console.error('사용자 정보 로드 오류:', error);
        }
    }

    displayUserInfo(userData) {
        const userInfoDisplay = document.getElementById('userInfoDisplay');
        
        userInfoDisplay.innerHTML = `
            <div class="user-info-item">
                <span class="user-info-label">아이디:</span>
                <span class="user-info-value">${userData.loginId || '정보 없음'}</span>
            </div>
            <div class="user-info-item">
                <span class="user-info-label">닉네임:</span>
                <span class="user-info-value">${userData.customerName || '정보 없음'}</span>
            </div>
            <div class="user-info-item">
                <span class="user-info-label">등급:</span>
                <span class="user-info-value">${userData.grade || '정보 없음'}</span>
            </div>
            <div class="user-info-item">
                <span class="user-info-label">잔액:</span>
                <span class="user-info-value">${userData.balance ? Math.floor(userData.balance).toLocaleString() : '0'}원</span>
            </div>
        `;
    }

    showChangeNicknameModal() {
        document.getElementById('newNickname').value = '';
        this.showModal('changeNicknameModal');
    }

    async confirmChangeNickname() {
        const newNickname = document.getElementById('newNickname').value.trim();
        
        if (!newNickname) {
            alert('새 닉네임을 입력해주세요.');
            return;
        }
        
        if (newNickname.length < 2) {
            alert('닉네임은 2자 이상이어야 합니다.');
            return;
        }
        
        try {
            const result = await db.changeNickname(newNickname);
            
            if (result.success) {
                alert('닉네임이 변경되었습니다!');
                this.closeModal('changeNicknameModal');
                this.loadUserInfo();
            } else {
                alert(result.message);
            }
        } catch (error) {
            console.error('닉네임 변경 오류:', error);
            alert('닉네임 변경에 실패했습니다.');
        }
    }

    showChangePasswordModal() {
        document.getElementById('currentPw').value = '';
        document.getElementById('newPw').value = '';
        document.getElementById('confirmNewPw').value = '';
        this.showModal('changePwModal');
    }

    async confirmChangePassword() {
        const currentPw = document.getElementById('currentPw').value;
        const newPw = document.getElementById('newPw').value;
        const confirmNewPw = document.getElementById('confirmNewPw').value;
        
        if (!currentPw || !newPw || !confirmNewPw) {
            alert('모든 필드를 입력해주세요.');
            return;
        }
        
        if (newPw !== confirmNewPw) {
            alert('새 비밀번호가 일치하지 않습니다.');
            return;
        }
        
        if (newPw.length < 4) {
            alert('새 비밀번호는 4자 이상이어야 합니다.');
            return;
        }
        
        try {
            const result = await db.changePassword(currentPw, newPw);
            
            if (result.success) {
                alert('비밀번호가 변경되었습니다.');
                this.closeModal('changePwModal');
            } else {
                alert(result.message);
            }
        } catch (error) {
            console.error('비밀번호 변경 오류:', error);
            alert('비밀번호 변경에 실패했습니다.');
        }
    }

    showChargeModal() {
        document.getElementById('chargeAmount').value = '';
        this.showModal('chargeModal');
    }

    async confirmCharge() {
        const amount = parseInt(document.getElementById('chargeAmount').value);
        
        if (!amount || amount < 1000) {
            alert('최소 충전 금액은 1,000원입니다.');
            return;
        }
        
        if (confirm(`${Math.floor(amount).toLocaleString()}원을 충전하시겠습니까?`)) {
            try {
                const result = await db.chargePay(amount);
                
                if (result.success) {
                    // 등급 변경 알림 추가
                    let message = result.message;
                    if (result.newGrade && auth.currentUser && result.newGrade !== auth.currentUser.grade) {
                        message += `\n\n축하합니다! 등급이 ${result.newGrade}(으)로 승급되었습니다! 🎉`;
                    }
                    alert(message);
                    this.closeModal('chargeModal');
                    
                    // 사용자 정보 다시 로드하여 잔액 및 등급 업데이트
                    await this.loadUserInfo();
                    
                    // auth의 currentUser 정보도 업데이트
                    if (result.newGrade && auth.currentUser) {
                        auth.currentUser.grade = result.newGrade;
                        auth.updateUserInfo();
                    }
                } else {
                    alert(result.message);
                }
            } catch (error) {
                console.error('페이 충전 오류:', error);
                alert('충전에 실패했습니다.');
            }
        }
    }

    showGradeGuideModal() {
        // 등급 안내 내용 생성
        const gradeContent = document.getElementById('gradeGuideContent');
        
        gradeContent.innerHTML = `
            <div class="grade-guide-text">
                <p><strong>💎 YuhanMarket 등급 시스템</strong></p>
                <p>누적 충전액에 따라 등급이 자동으로 부여됩니다.</p>
            </div>
            <div class="grade-info-list">
                <div class="grade-info-item">
                    <div class="grade-badge bronze">Bronze</div>
                    <div class="grade-description">기본 등급</div>
                </div>
                <div class="grade-info-item">
                    <div class="grade-badge silver">Silver</div>
                    <div class="grade-description">누적 충전액 50만원 이상</div>
                </div>
                <div class="grade-info-item">
                    <div class="grade-badge gold">Gold</div>
                    <div class="grade-description">누적 충전액 150만원 이상</div>
                </div>
                <div class="grade-info-item">
                    <div class="grade-badge diamond">Diamond</div>
                    <div class="grade-description">누적 충전액 300만원 이상</div>
                </div>
                <div class="grade-info-item">
                    <div class="grade-badge vip">VIP</div>
                    <div class="grade-description">누적 충전액 500만원 이상</div>
                </div>
            </div>
        `;
        
        this.showModal('gradeGuideModal');
    }

    showModal(modalId) {
        document.getElementById(modalId).classList.add('active');
    }

    closeModal(modalId) {
        document.getElementById(modalId).classList.remove('active');
    }
}

// 전역 마이페이지 매니저 인스턴스
const myPageManager = new MyPageManager();
