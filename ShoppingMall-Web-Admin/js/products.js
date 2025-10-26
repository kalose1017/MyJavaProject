/**
 * ===========================================
 * YuhanMarket 운영자 시스템 - Products (products.js)
 * ===========================================
 * 
 * 주요 기능:
 * - 상품 목록 조회
 * - 상품 추가
 * - 상품 수정
 * - 상품 삭제
 * - 상품 검색/필터
 * 
 * ===========================================
 */

// DOM 요소
const addProductBtn = document.getElementById('addProductBtn');
const addProductModal = document.getElementById('addProductModal');
const addProductForm = document.getElementById('addProductForm');
const cancelAddProduct = document.getElementById('cancelAddProduct');

const editProductModal = document.getElementById('editProductModal');
const editProductForm = document.getElementById('editProductForm');
const cancelEditProduct = document.getElementById('cancelEditProduct');

const productsTable = document.getElementById('productsTable');
const productsEmpty = document.getElementById('productsEmpty');

const productSearchInput = document.getElementById('productSearchInput');
const productSearchBtn = document.getElementById('productSearchBtn');
const productCategoryFilter = document.getElementById('productCategoryFilter');

// 상품 추가 버튼 이벤트
if (addProductBtn) {
    addProductBtn.addEventListener('click', () => {
        openAddProductModal();
    });
}

// 상품 추가 모달 열기
function openAddProductModal() {
    addProductModal.classList.add('active');
    addProductForm.reset();
    document.getElementById('addProductCategory').focus();
}

// 상품 추가 모달 닫기
if (cancelAddProduct) {
    cancelAddProduct.addEventListener('click', () => {
        addProductModal.classList.remove('active');
    });
}

// 상품 추가 폼 제출
if (addProductForm) {
    addProductForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const categoryId = parseInt(document.getElementById('addProductCategory').value);
        const productName = document.getElementById('addProductName').value.trim();
        const price = parseFloat(document.getElementById('addProductPrice').value);
        const stockQuantity = parseInt(document.getElementById('addProductStock').value);
        const origin = document.getElementById('addProductOrigin').value.trim() || '미입력';
        
        // 유효성 검사
        if (!categoryId) {
            alert('카테고리를 선택해주세요.');
            return;
        }
        
        if (!productName) {
            alert('상품명을 입력해주세요.');
            return;
        }
        
        if (isNaN(price) || price < 0) {
            alert('올바른 가격을 입력해주세요.');
            return;
        }
        
        if (isNaN(stockQuantity) || stockQuantity < 0) {
            alert('올바른 재고량을 입력해주세요.');
            return;
        }
        
        // 상품 추가 요청
        const response = await apiPost('/admin/products', {
            categoryId: categoryId,
            productName: productName,
            price: price,
            stockQuantity: stockQuantity,
            origin: origin
        });
        
        if (response.success) {
            alert(response.message);
            addProductModal.classList.remove('active');
            loadProducts();
            loadDashboard(); // 대시보드 업데이트
        } else {
            alert(response.message);
        }
    });
}

// 상품 수정 모달 닫기
if (cancelEditProduct) {
    cancelEditProduct.addEventListener('click', () => {
        editProductModal.classList.remove('active');
    });
}

// 상품 수정 폼 제출
if (editProductForm) {
    editProductForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const productId = parseInt(document.getElementById('editProductId').value);
        const productName = document.getElementById('editProductName').value.trim();
        const price = parseFloat(document.getElementById('editProductPrice').value);
        const stockQuantity = parseInt(document.getElementById('editProductStock').value);
        const origin = document.getElementById('editProductOrigin').value.trim() || '미입력';
        
        // 유효성 검사
        if (!productName) {
            alert('상품명을 입력해주세요.');
            return;
        }
        
        if (isNaN(price) || price < 0) {
            alert('올바른 가격을 입력해주세요.');
            return;
        }
        
        if (isNaN(stockQuantity) || stockQuantity < 0) {
            alert('올바른 재고량을 입력해주세요.');
            return;
        }
        
        // 상품 수정 요청
        const response = await apiPut(`/admin/products/${productId}`, {
            productName: productName,
            price: price,
            stockQuantity: stockQuantity,
            origin: origin
        });
        
        if (response.success) {
            alert(response.message);
            editProductModal.classList.remove('active');
            loadProducts();
            loadDashboard(); // 대시보드 업데이트
        } else {
            alert(response.message);
        }
    });
}

// 검색 버튼 이벤트
if (productSearchBtn) {
    productSearchBtn.addEventListener('click', () => {
        loadProducts();
    });
}

// 검색 입력 엔터키 이벤트
if (productSearchInput) {
    productSearchInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            loadProducts();
        }
    });
}

// 카테고리 필터 변경 이벤트
if (productCategoryFilter) {
    productCategoryFilter.addEventListener('change', () => {
        loadProducts();
    });
}

/**
 * 상품 목록 로드
 */
async function loadProducts() {
    const search = productSearchInput ? productSearchInput.value.trim() : '';
    const category = productCategoryFilter ? productCategoryFilter.value : '';
    
    let endpoint = '/admin/products?';
    if (search) endpoint += `search=${encodeURIComponent(search)}&`;
    if (category) endpoint += `category=${encodeURIComponent(category)}&`;
    
    const response = await apiGet(endpoint);
    
    if (!response.success) {
        console.error('상품 로드 실패:', response.message);
        return;
    }
    
    const products = response.products;
    const tbody = productsTable.querySelector('tbody');
    tbody.innerHTML = '';
    
    if (products.length === 0) {
        productsTable.style.display = 'none';
        productsEmpty.style.display = 'block';
        return;
    }
    
    productsTable.style.display = 'table';
    productsEmpty.style.display = 'none';
    
    products.forEach(product => {
        const row = document.createElement('tr');
        
        // 재고 상태에 따라 클래스 추가
        const stockClass = product.StockQuantity <= 10 ? 'stock-low' : 'stock-normal';
        
        row.innerHTML = `
            <td>${product.ProductID}</td>
            <td>${product.ProductName}</td>
            <td>${product.CategoryName}</td>
            <td>${Math.floor(product.Price).toLocaleString()}원</td>
            <td class="${stockClass}">${product.StockQuantity}개</td>
            <td>${product.Origin || '미입력'}</td>
            <td>
                <div class="action-buttons">
                    <button class="btn btn-info btn-sm" onclick="editProduct(${product.ProductID})">
                        수정
                    </button>
                    <button class="btn btn-danger btn-sm" onclick="deleteProduct(${product.ProductID}, '${product.ProductName}')">
                        삭제
                    </button>
                </div>
            </td>
        `;
        
        tbody.appendChild(row);
    });
}

/**
 * 상품 수정 모달 열기
 */
async function editProduct(productId) {
    const response = await apiGet(`/admin/products/${productId}`);
    
    if (!response.success) {
        alert('상품 정보를 불러올 수 없습니다.');
        return;
    }
    
    const product = response.product;
    
    document.getElementById('editProductId').value = product.ProductID;
    document.getElementById('editProductIdDisplay').value = product.ProductID;
    document.getElementById('editProductCategory').value = product.CategoryName;
    document.getElementById('editProductName').value = product.ProductName;
    document.getElementById('editProductPrice').value = product.Price;
    document.getElementById('editProductStock').value = product.StockQuantity;
    document.getElementById('editProductOrigin').value = product.Origin || '';
    
    editProductModal.classList.add('active');
}

/**
 * 상품 삭제
 */
async function deleteProduct(productId, productName) {
    if (!confirm(`정말로 "${productName}" 상품을 삭제하시겠습니까?\n이 작업은 되돌릴 수 없습니다!`)) {
        return;
    }
    
    const response = await apiDelete(`/admin/products/${productId}`);
    
    if (response.success) {
        alert(response.message);
        loadProducts();
        loadDashboard(); // 대시보드 업데이트
    } else {
        alert(response.message);
    }
}

