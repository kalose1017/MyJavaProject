/**
 * ===========================================
 * YuhanMarket 상품 관리자 (products.js)
 * ===========================================
 * 
 * 주요 기능:
 * - 상품 목록 조회 및 표시
 * - 상품 검색 (상품명 기준)
 * - 카테고리 필터링
 * - 상품 정렬 (가나다순, 가격순)
 * - 장바구니 담기 기능
 * - 화면 활성화 시 자동 새로고침
 * 
 * 주요 메서드:
 * - loadProducts(search, category, sortBy) - 상품 목록 로드
 * - searchProducts() - 검색 조건에 따른 상품 조회
 * - loadCategories() - 카테고리 목록 로드
 * - displayProducts(products) - 상품 카드 생성 및 표시
 * - createProductCard(product) - 개별 상품 카드 생성
 * - addToCartDirect(productId) - 장바구니에 직접 추가
 * 
 * 상품 정렬 옵션:
 * - name_asc: 가나다 오름차순 (기본값)
 * - name_desc: 가나다 내림차순
 * - price_asc: 가격 오름차순
 * - price_desc: 가격 내림차순
 * 
 * 상품 카드 구성:
 * - 상품명, 카테고리, 가격, 재고, 원산지
 * - 수량 선택 입력란
 * - 장바구니 담기 버튼
 * 
 * ===========================================
 */

// 상품 관련 기능
class ProductManager {
    constructor() {
        this.currentProductId = null;
        this.init();
    }

    init() {
        this.setupEventListeners();
        this.loadCategories();
        this.loadProducts();
    }

    setupEventListeners() {
        // 상품검색 화면이 활성화될 때마다 상품 목록 새로고침
        document.addEventListener('screenChanged', (e) => {
            if (e.detail.screen === 'products') {
                this.searchProducts();
            }
        });

        // 검색 버튼
        document.getElementById('searchBtn').addEventListener('click', () => {
            this.searchProducts();
        });

        // 검색 입력창 엔터키
        document.getElementById('searchInput').addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                this.searchProducts();
            }
        });

        // 카테고리 필터
        document.getElementById('categoryFilter').addEventListener('change', () => {
            this.searchProducts();
        });

        // 정렬 필터
        document.getElementById('sortFilter').addEventListener('change', () => {
            this.searchProducts();
        });

        // 장바구니 추가 모달
        document.getElementById('confirmAddCart').addEventListener('click', () => {
            this.confirmAddToCart();
        });

        document.getElementById('cancelAddCart').addEventListener('click', () => {
            this.closeModal('addCartModal');
        });
    }

    async loadCategories() {
        try {
            const categories = await db.getCategories();
            const select = document.getElementById('categoryFilter');
            
            // 기존 옵션 제거 (전체 제외)
            while (select.children.length > 1) {
                select.removeChild(select.lastChild);
            }
            
            categories.forEach(category => {
                const option = document.createElement('option');
                option.value = category;
                option.textContent = category;
                select.appendChild(option);
            });
        } catch (error) {
            console.error('카테고리 로드 오류:', error);
        }
    }

    async loadProducts(search = '', category = '', sortBy = 'name_asc') {
        try {
            const products = await db.getProducts(search, category, sortBy);
            this.displayProducts(products);
        } catch (error) {
            console.error('상품 로드 오류:', error);
            this.displayProducts([]);
        }
    }

    async searchProducts() {
        const searchText = document.getElementById('searchInput').value;
        const category = document.getElementById('categoryFilter').value;
        const sortBy = document.getElementById('sortFilter').value;
        await this.loadProducts(searchText, category, sortBy);
    }

    displayProducts(products) {
        const container = document.getElementById('productsContainer');
        const emptyMessage = document.getElementById('emptyMessage');
        
        if (products.length === 0) {
            container.innerHTML = '';
            emptyMessage.style.display = 'block';
            return;
        }
        
        emptyMessage.style.display = 'none';
        container.innerHTML = '';
        
        products.forEach(product => {
            const card = this.createProductCard(product);
            container.appendChild(card);
        });
    }

    createProductCard(product) {
        const card = document.createElement('div');
        card.className = 'product-card';
        
        const stockClass = product.stock < 10 ? 'low' : '';
        const stockText = product.stock === 0 ? '품절' : `재고: ${product.stock}개`;
        
        card.innerHTML = `
            <div class="product-name">${product.productName}</div>
            <div class="product-category">${product.categoryName || '미분류'}</div>
            <div class="product-price">${Math.floor(product.price).toLocaleString()}원</div>
            <div class="product-stock ${stockClass}">${stockText}</div>
            <div class="product-origin-quantity">
                <span class="product-origin">원산지: ${product.origin || '정보 없음'}</span>
                <div class="quantity-selector">
                    <label>수량:</label>
                    <input type="number" class="quantity-input-small" value="1" min="1" max="${product.stock}" id="qty-${product.productId}">
                </div>
            </div>
            <div class="product-actions">
                <button class="btn btn-primary btn-block" 
                        onclick="productManager.addToCartDirect(${product.productId})"
                        ${product.stock === 0 ? 'disabled' : ''}>
                    🛒 장바구니 담기
                </button>
            </div>
        `;
        
        return card;
    }

    openAddCartModal(productId, productName) {
        this.currentProductId = productId;
        document.getElementById('modalProductName').textContent = productName;
        document.getElementById('modalQuantity').value = 1;
        this.showModal('addCartModal');
    }

    async addToCartDirect(productId) {
        const quantityInput = document.getElementById(`qty-${productId}`);
        const quantity = parseInt(quantityInput.value);
        
        if (quantity < 1) {
            alert('수량은 1개 이상이어야 합니다.');
            return;
        }
        
        try {
            const result = await db.addToCart(productId, quantity);
            
            if (result.success) {
                alert(result.message);
                // 상품 목록 새로고침 (재고 업데이트 반영)
                await this.searchProducts();
            } else {
                alert(result.message);
            }
        } catch (error) {
            console.error('장바구니 추가 오류:', error);
            alert('장바구니 추가에 실패했습니다.');
        }
    }

    async confirmAddToCart() {
        const quantity = parseInt(document.getElementById('modalQuantity').value);
        
        if (quantity < 1) {
            alert('수량은 1개 이상이어야 합니다.');
            return;
        }
        
        try {
            const result = await db.addToCart(this.currentProductId, quantity);
            
            if (result.success) {
                alert(result.message);
                this.closeModal('addCartModal');
            } else {
                alert(result.message);
            }
        } catch (error) {
            console.error('장바구니 추가 오류:', error);
            alert('장바구니 추가에 실패했습니다.');
        }
    }

    showModal(modalId) {
        document.getElementById(modalId).classList.add('active');
    }

    closeModal(modalId) {
        document.getElementById(modalId).classList.remove('active');
    }
}

// 전역 상품 매니저 인스턴스
const productManager = new ProductManager();
