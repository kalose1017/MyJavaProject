/**
 * ===========================================
 * YuhanMarket 데이터베이스 API 클라이언트 (database.js)
 * ===========================================
 * 
 * 주요 기능:
 * - 백엔드 서버와의 HTTP 통신 담당
 * - 세션 관리 (localStorage 활용)
 * - 모든 API 요청을 메서드로 캡슐화
 * 
 * 제공 메서드:
 * 
 * [인증 관련]
 * - login(loginId, loginPw) - 로그인
 * - logout() - 로그아웃
 * - signup(userData) - 회원가입
 * - checkIdDuplicate(loginId) - 아이디 중복 확인
 * - getCurrentUser() - 현재 로그인 사용자 정보 조회
 * 
 * [상품 관련]
 * - getProducts(search, category, sortBy) - 상품 목록 조회
 * - getCategories() - 카테고리 목록 조회
 * 
 * [장바구니 관련]
 * - getCart() - 장바구니 조회
 * - addToCart(productId, quantity) - 장바구니에 상품 추가
 * - removeFromCart(cartId) - 장바구니 상품 삭제
 * - updateCartQuantityWithStock(cartId, newQuantity, quantityDiff) - 수량 수정 (재고 조정)
 * - clearCartWithStockRestore() - 장바구니 비우기 (재고 복구)
 * 
 * [구매 관련]
 * - purchaseAll() - 전체 구매
 * - purchaseSelected(cartIds, quantity) - 선택 구매
 * 
 * [고객 관리]
 * - chargePay(amount) - 페이 충전
 * - changeNickname(newNickname) - 닉네임 변경
 * - changePassword(currentPw, newPw) - 비밀번호 변경
 * 
 * 세션 관리:
 * - saveSession() - 세션 정보 localStorage 저장
 * - restoreSession() - 세션 정보 복원
 * - clearSession() - 세션 정보 삭제
 * 
 * ===========================================
 */

// 데이터베이스 연결 및 API 호출
class DatabaseAPI {
    constructor() {
        this.baseURL = 'http://localhost:8080/api';
        this.currentUser = null;
        this.offlineMode = false; // 오프라인 모드 비활성화
        this.sessionId = null;
        
        // 페이지 로드 시 저장된 세션 정보 복원
        this.restoreSession();
    }
    
    // 저장된 세션 정보 복원
    restoreSession() {
        const savedSession = localStorage.getItem('userSession');
        if (savedSession) {
            const sessionData = JSON.parse(savedSession);
            this.sessionId = sessionData.sessionId;
            this.currentUser = sessionData.user;
        }
    }
    
    // 세션 정보 저장
    saveSession(sessionId, user) {
        const sessionData = {
            sessionId: sessionId,
            user: user,
            timestamp: Date.now()
        };
        localStorage.setItem('userSession', JSON.stringify(sessionData));
    }
    
    // 세션 정보 삭제
    clearSession() {
        localStorage.removeItem('userSession');
        this.sessionId = null;
        this.currentUser = null;
    }

    // 로그인
    async login(loginId, loginPw) {
        if (this.offlineMode) {
            // 오프라인 모드: 간단한 로그인 시뮬레이션
            if (loginId === 'leejiho1234' && loginPw === '1234') {
                this.currentUser = {
                    customerId: 1,
                    customerName: '이지호',
                    grade: 'VIP'
                };
                return { success: true, user: this.currentUser };
            } else {
                return { success: false, message: '아이디 또는 비밀번호가 올바르지 않습니다.' };
            }
        }
        
        try {
            const response = await fetch(`${this.baseURL}/customer/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    loginId: loginId,
                    loginPw: loginPw
                })
            });

            const data = await response.json();
            
            if (data.success) {
                this.currentUser = {
                    customerId: data.customerId,
                    customerName: data.customerName,
                    grade: data.grade
                };
                this.sessionId = data.sessionId;
                
                // 세션 정보를 로컬 스토리지에 저장
                this.saveSession(data.sessionId, this.currentUser);
                
                return { success: true, user: this.currentUser };
            } else {
                return { success: false, message: data.message };
            }
        } catch (error) {
            console.error('로그인 오류:', error);
            return { success: false, message: '서버 연결 오류' };
        }
    }

    // 회원가입
    async signup(userData) {
        try {
            const formData = new FormData();
            formData.append('loginId', userData.loginId);
            formData.append('loginPw', userData.loginPw);
            formData.append('nickName', userData.nickName);

            const response = await fetch(`${this.baseURL}/customer/signup`, {
                method: 'POST',
                body: formData
            });

            const data = await response.json();
            return data;
        } catch (error) {
            console.error('회원가입 오류:', error);
            return { success: false, message: '서버 연결 오류' };
        }
    }

    // 아이디 중복 확인
    async checkIdDuplicate(loginId) {
        try {
            const response = await fetch(`${this.baseURL}/customer/check-id?loginId=${encodeURIComponent(loginId)}`);
            const data = await response.json();
            return data;
        } catch (error) {
            console.error('아이디 중복 확인 오류:', error);
            return { success: false, message: '서버 연결 오류' };
        }
    }

    // 로그아웃
    async logout() {
        try {
            await fetch(`${this.baseURL}/customer/logout`, { 
                method: 'POST',
                headers: {
                    'Authorization': this.sessionId
                }
            });
            
            // 로컬 스토리지에서 세션 정보 삭제
            this.clearSession();
            
            return { success: true };
        } catch (error) {
            console.error('로그아웃 오류:', error);
            // 서버 오류가 있어도 로컬 세션은 삭제
            this.clearSession();
            return { success: false };
        }
    }

    // 현재 사용자 정보 조회
    async getCurrentUser() {
        try {
            const response = await fetch(`${this.baseURL}/customer/me`, {
                headers: {
                    'Authorization': this.sessionId
                }
            });
            const data = await response.json();
            
            if (data.loggedIn) {
                this.currentUser = {
                    customerId: data.customerId,
                    customerName: data.customerName,
                    grade: data.grade
                };
            }
            
            return data;
        } catch (error) {
            console.error('사용자 정보 조회 오류:', error);
            return { loggedIn: false };
        }
    }

    // 상품 목록 조회
    async getProducts(search = '', category = '', sortBy = 'name_asc') {
        if (this.offlineMode) {
            // 오프라인 모드: 샘플 상품 데이터
            const mockProducts = [
                { productId: 1, productName: '아이폰 15', price: 1200000, stock: 10, categoryName: '전자제품', origin: '미국' },
                { productId: 2, productName: '갤럭시 S24', price: 1100000, stock: 8, categoryName: '전자제품', origin: '한국' },
                { productId: 3, productName: '맥북 프로', price: 2500000, stock: 5, categoryName: '전자제품', origin: '미국' },
                { productId: 4, productName: '나이키 운동화', price: 150000, stock: 20, categoryName: '의류', origin: '중국' },
                { productId: 5, productName: '아디다스 티셔츠', price: 45000, stock: 30, categoryName: '의류', origin: '베트남' },
                { productId: 6, productName: '삼성 모니터', price: 300000, stock: 15, categoryName: '전자제품', origin: '한국' },
                { productId: 7, productName: '무지 후드티', price: 25000, stock: 50, categoryName: '의류', origin: '한국' },
                { productId: 8, productName: '커피 머신', price: 200000, stock: 3, categoryName: '생활용품', origin: '이탈리아' }
            ];
            
            let filteredProducts = mockProducts;
            
            if (search) {
                filteredProducts = filteredProducts.filter(product => 
                    product.productName.toLowerCase().includes(search.toLowerCase())
                );
            }
            
            if (category && category !== '전체') {
                filteredProducts = filteredProducts.filter(product => 
                    product.categoryName === category
                );
            }
            
            return filteredProducts;
        }
        
        try {
            let url = `${this.baseURL}/products?`;
            if (search) url += `search=${encodeURIComponent(search)}&`;
            if (category) url += `category=${encodeURIComponent(category)}&`;
            if (sortBy) url += `sortBy=${encodeURIComponent(sortBy)}`;
            
            const response = await fetch(url);
            const products = await response.json();
            return products;
        } catch (error) {
            console.error('상품 조회 오류:', error);
            return [];
        }
    }

    // 카테고리 목록 조회
    async getCategories() {
        if (this.offlineMode) {
            // 오프라인 모드: 샘플 카테고리 데이터
            return ['전체', '전자제품', '의류', '생활용품'];
        }
        
        try {
            const response = await fetch(`${this.baseURL}/categories`);
            const categories = await response.json();
            return categories;
        } catch (error) {
            console.error('카테고리 조회 오류:', error);
            return [];
        }
    }

    // 장바구니 조회
    async getCart() {
        if (this.offlineMode) {
            // 오프라인 모드: 로컬 스토리지에서 장바구니 데이터 조회
            const cartData = localStorage.getItem('shoppingCart');
            return cartData ? JSON.parse(cartData) : [];
        }
        
        try {
            const response = await fetch(`${this.baseURL}/cart`, {
                headers: {
                    'Authorization': this.sessionId
                }
            });
            const cart = await response.json();
            return cart;
        } catch (error) {
            console.error('장바구니 조회 오류:', error);
            return [];
        }
    }

    // 장바구니에 상품 추가
    async addToCart(productId, quantity) {
        if (this.offlineMode) {
            // 오프라인 모드: 로컬 스토리지에 장바구니 데이터 저장
            try {
                const cart = await this.getCart();
                const existingItem = cart.find(item => item.productId === productId);
                
                if (existingItem) {
                    existingItem.quantity += quantity;
                } else {
                    // 상품 정보 가져오기
                    const products = await this.getProducts();
                    const product = products.find(p => p.productId === productId);
                    if (product) {
                        cart.push({
                            cartId: Date.now(), // 임시 ID
                            productId: productId,
                            productName: product.productName,
                            price: product.price,
                            quantity: quantity,
                            totalPrice: product.price * quantity
                        });
                    }
                }
                
                localStorage.setItem('shoppingCart', JSON.stringify(cart));
                return { success: true, message: '장바구니에 추가되었습니다.' };
            } catch (error) {
                return { success: false, message: '장바구니 추가 실패' };
            }
        }
        
        try {
            const response = await fetch(`${this.baseURL}/cart/add`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': this.sessionId
                },
                body: JSON.stringify({
                    productId: productId,
                    quantity: quantity
                })
            });

            const data = await response.json();
            return data;
        } catch (error) {
            console.error('장바구니 추가 오류:', error);
            return { success: false, message: '서버 연결 오류' };
        }
    }

    // 장바구니에서 상품 삭제
    async removeFromCart(cartId) {
        if (this.offlineMode) {
            try {
                const cart = await this.getCart();
                const filteredCart = cart.filter(item => item.cartId !== cartId);
                localStorage.setItem('shoppingCart', JSON.stringify(filteredCart));
                return { success: true, message: '상품이 삭제되었습니다.' };
            } catch (error) {
                return { success: false, message: '삭제 실패' };
            }
        }
        
        try {
            const response = await fetch(`${this.baseURL}/cart/${cartId}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': this.sessionId
                }
            });

            const data = await response.json();
            return data;
        } catch (error) {
            console.error('장바구니 삭제 오류:', error);
            return { success: false, message: '서버 연결 오류' };
        }
    }

    // 장바구니 수량 수정 (재고 조정 포함)
    async updateCartQuantityWithStock(cartId, newQuantity, quantityDiff) {
        try {
            const response = await fetch(`${this.baseURL}/cart/update-with-stock`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': this.sessionId
                },
                body: JSON.stringify({
                    cartId: cartId,
                    newQuantity: newQuantity,
                    quantityDiff: quantityDiff
                })
            });

            const data = await response.json();
            return data;
        } catch (error) {
            console.error('장바구니 수량 수정 오류:', error);
            return { success: false, message: '서버 연결 오류' };
        }
    }

    // 장바구니 수량 수정
    async updateCartQuantity(cartId, quantity) {
        if (this.offlineMode) {
            try {
                const cart = await this.getCart();
                const item = cart.find(item => item.cartId === cartId);
                if (item) {
                    item.quantity = quantity;
                    item.totalPrice = item.price * quantity;
                    localStorage.setItem('shoppingCart', JSON.stringify(cart));
                    return { success: true, message: '수량이 수정되었습니다.' };
                }
                return { success: false, message: '상품을 찾을 수 없습니다.' };
            } catch (error) {
                return { success: false, message: '수정 실패' };
            }
        }
        
        try {
            const formData = new FormData();
            formData.append('quantity', quantity);

            const response = await fetch(`${this.baseURL}/cart/${cartId}`, {
                method: 'PUT',
                headers: {
                    'Authorization': this.sessionId
                },
                body: formData
            });

            const data = await response.json();
            return data;
        } catch (error) {
            console.error('장바구니 수량 수정 오류:', error);
            return { success: false, message: '서버 연결 오류' };
        }
    }

    // 장바구니 전체 비우기
    async clearCart() {
        if (this.offlineMode) {
            try {
                localStorage.removeItem('shoppingCart');
                return { success: true, message: '장바구니가 비워졌습니다.' };
            } catch (error) {
                return { success: false, message: '장바구니 비우기 실패' };
            }
        }
        
        try {
            const response = await fetch(`${this.baseURL}/cart/clear`, {
                method: 'DELETE',
                headers: {
                    'Authorization': this.sessionId
                }
            });

            const data = await response.json();
            return data;
        } catch (error) {
            console.error('장바구니 비우기 오류:', error);
            return { success: false, message: '서버 연결 오류' };
        }
    }

    // 전체 구매
    async purchaseAll() {
        if (this.offlineMode) {
            try {
                const cart = await this.getCart();
                if (cart.length === 0) {
                    return { success: false, message: '장바구니가 비어있습니다.' };
                }
                
                // 장바구니 비우기
                await this.clearCart();
                return { success: true, message: '구매가 완료되었습니다!' };
            } catch (error) {
                return { success: false, message: '구매 실패' };
            }
        }
        
        try {
            const response = await fetch(`${this.baseURL}/purchase/all`, {
                method: 'POST',
                headers: {
                    'Authorization': this.sessionId
                }
            });

            const data = await response.json();
            return data;
        } catch (error) {
            console.error('전체 구매 오류:', error);
            return { success: false, message: '서버 연결 오류' };
        }
    }

    // 선택 구매
    async purchaseSelected(cartIds, quantity) {
        if (this.offlineMode) {
            try {
                const cart = await this.getCart();
                const filteredCart = cart.filter(item => !cartIds.includes(item.cartId));
                localStorage.setItem('shoppingCart', JSON.stringify(filteredCart));
                return { success: true, message: '선택한 상품 구매가 완료되었습니다!' };
            } catch (error) {
                return { success: false, message: '구매 실패' };
            }
        }
        
        try {
            const response = await fetch(`${this.baseURL}/purchase/selected`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': this.sessionId
                },
                body: JSON.stringify({
                    cartIds: cartIds,
                    quantity: quantity
                })
            });

            const data = await response.json();
            return data;
        } catch (error) {
            console.error('선택 구매 오류:', error);
            return { success: false, message: '서버 연결 오류' };
        }
    }
    
    // 전체 비우기 (재고 복구)
    async clearCartWithStockRestore() {
        try {
            const response = await fetch(`${this.baseURL}/cart/clear-restore`, {
                method: 'DELETE',
                headers: {
                    'Authorization': this.sessionId
                }
            });

            const data = await response.json();
            return data;
        } catch (error) {
            console.error('장바구니 비우기 및 재고 복구 오류:', error);
            return { success: false, message: '서버 연결 오류' };
        }
    }

    // 페이 충전
    async chargePay(amount) {
        if (this.offlineMode) {
            return { success: true, message: `페이 ${Math.floor(amount).toLocaleString()}원이 충전되었습니다!` };
        }
        
        try {
            const response = await fetch(`${this.baseURL}/customer/charge`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': this.sessionId
                },
                body: JSON.stringify({ amount: amount })
            });

            const data = await response.json();
            return data;
        } catch (error) {
            console.error('페이 충전 오류:', error);
            return { success: false, message: '서버 연결 오류' };
        }
    }

    // 닉네임 변경
    async changeNickname(newNickname) {
        if (this.offlineMode) {
            return { success: true, message: '닉네임이 변경되었습니다!' };
        }
        
        try {
            const response = await fetch(`${this.baseURL}/customer/change-nickname`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': this.sessionId
                },
                body: JSON.stringify({ 
                    newNickname: newNickname
                })
            });

            const data = await response.json();
            return data;
        } catch (error) {
            console.error('닉네임 변경 오류:', error);
            return { success: false, message: '서버 연결 오류' };
        }
    }

    // 비밀번호 변경
    async changePassword(currentPw, newPw) {
        if (this.offlineMode) {
            return { success: true, message: '비밀번호가 변경되었습니다!' };
        }
        
        try {
            const response = await fetch(`${this.baseURL}/customer/change-password`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': this.sessionId
                },
                body: JSON.stringify({ 
                    currentPw: currentPw, 
                    newPw: newPw 
                })
            });

            const data = await response.json();
            return data;
        } catch (error) {
            console.error('비밀번호 변경 오류:', error);
            return { success: false, message: '서버 연결 오류' };
        }
    }
}

// 전역 데이터베이스 API 인스턴스
const db = new DatabaseAPI();
