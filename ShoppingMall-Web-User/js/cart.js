/**
 * ===========================================
 * YuhanMarket 장바구니 관리자 (cart.js)
 * ===========================================
 * 
 * 주요 기능:
 * - 장바구니 조회 및 표시
 * - 수량 변경 시 실시간 재고 조정
 * - 선택 구매 (잔액 확인, 재고 조정)
 * - 전체 구매 및 전체 비우기
 * - 화면 활성화 시 자동 새로고침
 * 
 * 주요 메서드:
 * - loadCart() - 장바구니 데이터 로드
 * - displayCart() - 장바구니 UI 렌더링
 * - handleQuantityChange(cartId, newQuantity) - 수량 변경 및 재고 조정
 * - updateTotalAmount() - 총 금액 재계산
 * - purchaseSelected(cartId) - 선택 구매
 * - confirmClearCart() - 전체 비우기 확인 팝업
 * - clearCart() - 전체 비우기 실행 (재고 복구)
 * - confirmPurchaseAll() - 전체 구매 확인 팝업
 * - purchaseAll() - 전체 구매 실행
 * - removeItem(cartId) - 상품 삭제 (재고 복구)
 * 
 * 장바구니 아이템 구성:
 * - 상품명, 가격, 수량 입력 필드
 * - 선택 구매, 삭제 버튼
 * - 개별 금액 표시
 * 
 * 재고 관리:
 * - 수량 증가: 재고 차감 (재고 부족 시 오류)
 * - 수량 감소: 재고 복구
 * - 삭제/비우기: 재고 복구
 * - 구매: 재고 유지 (이미 담을 때 차감됨)
 * 
 * ===========================================
 */

// 장바구니 관련 기능
class CartManager {
    constructor() {
        this.cartItems = [];
        this.init();
    }

    init() {
        this.setupEventListeners();
    }

    setupEventListeners() {
        // 장바구니 화면이 활성화될 때마다 로드
        document.addEventListener('screenChanged', (e) => {
            if (e.detail.screen === 'cart') {
                this.loadCart();
            }
        });
    }

    async loadCart() {
        try {
            this.cartItems = await db.getCart();
            this.displayCart();
        } catch (error) {
            console.error('장바구니 로드 오류:', error);
            this.displayCart();
        }
    }

    displayCart() {
        const container = document.getElementById('cartContainer');
        const emptyMessage = document.getElementById('cartEmpty');
        
        if (this.cartItems.length === 0) {
            container.innerHTML = '';
            emptyMessage.style.display = 'block';
            return;
        }
        
        emptyMessage.style.display = 'none';
        
        let totalAmount = 0;
        let cartHTML = '';
        
        this.cartItems.forEach((item, index) => {
            const itemTotal = item.price * item.quantity;
            totalAmount += itemTotal;
            
            cartHTML += `
                <div class="cart-item" data-item-id="${item.cartId}">
                    <div class="cart-item-info">
                        <div class="cart-item-name">${item.productName}</div>
                        <div class="cart-item-price">${Math.floor(item.price).toLocaleString()}원</div>
                    </div>
                    <div class="cart-item-controls">
                        <div class="quantity-display">
                            <span class="quantity-label">수량:</span>
                            <input type="number" class="quantity-input-editable" value="${item.quantity}" 
                                   min="1" data-cart-id="${item.cartId}" data-price="${item.price}"
                                   onchange="cartManager.handleQuantityChange(${item.cartId}, this.value)">
                        </div>
                        <button class="btn btn-success btn-sm" onclick="cartManager.purchaseSelected(${item.cartId})">선택 구매</button>
                        <button class="btn btn-danger btn-sm" onclick="cartManager.removeItem(${item.cartId})">삭제</button>
                    </div>
                    <div class="cart-item-total" data-total="${item.cartId}">${Math.floor(itemTotal).toLocaleString()}원</div>
                </div>
            `;
        });
        
        cartHTML += `
            <div class="cart-summary">
                <div class="cart-total" id="totalAmount">총 금액: ${Math.floor(totalAmount).toLocaleString()}원</div>
                <div class="cart-actions">
                    <button class="btn btn-warning" onclick="cartManager.confirmClearCart()">전체 비우기</button>
                    <button class="btn btn-primary" onclick="cartManager.confirmPurchaseAll()">전체 구매</button>
                </div>
            </div>
        `;
        
        container.innerHTML = cartHTML;
    }

    // 수량 변경 시 즉시 금액 업데이트 및 재고 조정
    async handleQuantityChange(cartId, newQuantity) {
        const quantity = parseInt(newQuantity);
        
        if (quantity < 1 || isNaN(quantity)) {
            alert('수량은 1개 이상이어야 합니다.');
            await this.loadCart();
            return;
        }
        
        // 이전 수량 찾기
        const item = this.cartItems.find(item => item.cartId === cartId);
        if (!item) {
            alert('상품 정보를 찾을 수 없습니다.');
            return;
        }
        
        const oldQuantity = item.quantity;
        const quantityDiff = quantity - oldQuantity; // 양수면 증가, 음수면 감소
        
        // 수량 변경이 없으면 리턴
        if (quantityDiff === 0) {
            return;
        }
        
        try {
            // 서버에 수량 변경 요청 (재고 조정 포함)
            const result = await db.updateCartQuantityWithStock(cartId, quantity, quantityDiff);
            
            if (result.success) {
                // UI 즉시 업데이트
                const priceElement = document.querySelector(`[data-cart-id="${cartId}"]`);
                const price = parseFloat(priceElement.dataset.price);
                const newTotal = price * quantity;
                
                const totalElement = document.querySelector(`[data-total="${cartId}"]`);
                totalElement.textContent = `${Math.floor(newTotal).toLocaleString()}원`;
                
                this.updateTotalAmount();
                item.quantity = quantity;
            } else {
                alert(result.message);
                await this.loadCart();
            }
        } catch (error) {
            console.error('수량 변경 오류:', error);
            alert('수량 변경에 실패했습니다.');
            await this.loadCart();
        }
    }
    
    // 전체 금액 재계산
    updateTotalAmount() {
        let total = 0;
        document.querySelectorAll('.quantity-input-editable').forEach(input => {
            const quantity = parseInt(input.value);
            const price = parseFloat(input.dataset.price);
            total += price * quantity;
        });
        
        document.getElementById('totalAmount').textContent = `총 금액: ${Math.floor(total).toLocaleString()}원`;
    }

    async removeItem(cartId) {
        if (confirm('이 상품을 장바구니에서 삭제하시겠습니까?')) {
            try {
                const result = await db.removeFromCart(cartId);
                
                if (result.success) {
                    await this.loadCart();
                } else {
                    alert(result.message);
                }
            } catch (error) {
                console.error('상품 삭제 오류:', error);
                alert('상품 삭제에 실패했습니다.');
            }
        }
    }

    // 선택 구매
    async purchaseSelected(cartId) {
        const quantityInput = document.querySelector(`[data-cart-id="${cartId}"]`);
        const quantity = parseInt(quantityInput.value);
        const price = parseFloat(quantityInput.dataset.price);
        const totalPrice = price * quantity;
        
        const item = this.cartItems.find(item => item.cartId === cartId);
        
        if (confirm(`${item.productName} ${quantity}개를 ${Math.floor(totalPrice).toLocaleString()}원에 구매하시겠습니까?`)) {
            try {
                const result = await db.purchaseSelected([cartId], quantity);
                
                if (result.success) {
                    alert('구매가 완료되었습니다!');
                    await this.loadCart();
                } else {
                    alert(result.message);
                }
            } catch (error) {
                console.error('구매 오류:', error);
                alert('구매에 실패했습니다.');
            }
        }
    }
    
    // 전체 비우기 확인
    confirmClearCart() {
        if (this.cartItems.length === 0) {
            alert('장바구니가 비어있습니다.');
            return;
        }
        
        if (confirm('전체 비우기를 진행하시겠습니까?\n\n예: 장바구니를 비우고 재고 복구\n아니오: 취소')) {
            this.clearCart();
        }
    }
    
    // 전체 비우기 (재고 복구)
    async clearCart() {
        try {
            const result = await db.clearCartWithStockRestore();
            
            if (result.success) {
                alert('장바구니가 비워졌고 재고가 복구되었습니다.');
                await this.loadCart();
            } else {
                alert(result.message);
            }
        } catch (error) {
            console.error('장바구니 비우기 오류:', error);
            alert('장바구니 비우기에 실패했습니다.');
        }
    }
    
    // 전체 구매 확인
    confirmPurchaseAll() {
        if (this.cartItems.length === 0) {
            alert('장바구니가 비어있습니다.');
            return;
        }
        
        let totalAmount = 0;
        document.querySelectorAll('.quantity-input-editable').forEach(input => {
            const quantity = parseInt(input.value);
            const price = parseFloat(input.dataset.price);
            totalAmount += price * quantity;
        });
        
        if (confirm(`전체 구매를 진행하시겠습니까?\n\n총 금액: ${Math.floor(totalAmount).toLocaleString()}원\n\n예: 구매 진행\n아니오: 취소`)) {
            this.purchaseAll();
        }
    }

    // 전체 구매
    async purchaseAll() {
        try {
            const result = await db.purchaseAll();
            
            if (result.success) {
                alert('구매가 완료되었습니다!');
                await this.loadCart();
            } else {
                alert(result.message);
            }
        } catch (error) {
            console.error('구매 오류:', error);
            alert('구매에 실패했습니다.');
        }
    }
}

// 전역 장바구니 매니저 인스턴스
const cartManager = new CartManager();

