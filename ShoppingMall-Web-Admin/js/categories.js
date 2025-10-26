/**
 * ===========================================
 * YuhanMarket 운영자 시스템 - Categories (categories.js)
 * ===========================================
 * 
 * 주요 기능:
 * - 카테고리 목록 조회
 * - 카테고리 추가
 * - 카테고리 삭제
 * 
 * ===========================================
 */

// DOM 요소
const addCategoryBtn = document.getElementById('addCategoryBtn');
const quickAddCategory = document.getElementById('quickAddCategory');
const addCategoryModal = document.getElementById('addCategoryModal');
const addCategoryForm = document.getElementById('addCategoryForm');
const cancelAddCategory = document.getElementById('cancelAddCategory');
const categoriesTable = document.getElementById('categoriesTable');
const categoriesEmpty = document.getElementById('categoriesEmpty');

// 카테고리 추가 버튼 이벤트
if (addCategoryBtn) {
    addCategoryBtn.addEventListener('click', () => {
        openAddCategoryModal();
    });
}

if (quickAddCategory) {
    quickAddCategory.addEventListener('click', () => {
        openAddCategoryModal();
    });
}

// 카테고리 추가 모달 열기
function openAddCategoryModal() {
    addCategoryModal.classList.add('active');
    document.getElementById('addCategoryName').value = '';
    document.getElementById('addCategoryName').focus();
}

// 카테고리 추가 모달 닫기
if (cancelAddCategory) {
    cancelAddCategory.addEventListener('click', () => {
        addCategoryModal.classList.remove('active');
    });
}

// 카테고리 추가 폼 제출
if (addCategoryForm) {
    addCategoryForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const categoryName = document.getElementById('addCategoryName').value.trim();
        
        if (!categoryName) {
            alert('카테고리명을 입력해주세요.');
            return;
        }
        
        // 카테고리 추가 요청
        const response = await apiPost('/admin/categories', {
            categoryName: categoryName
        });
        
        if (response.success) {
            alert(response.message);
            addCategoryModal.classList.remove('active');
            loadCategories();
            loadCategoryFilters();
            loadDashboard(); // 대시보드 업데이트
        } else {
            alert(response.message);
        }
    });
}

// 카테고리 목록 로드
async function loadCategories() {
    const response = await apiGet('/admin/categories');
    
    if (!response.success) {
        console.error('카테고리 로드 실패:', response.message);
        return;
    }
    
    const categories = response.categories;
    const tbody = categoriesTable.querySelector('tbody');
    tbody.innerHTML = '';
    
    if (categories.length === 0) {
        categoriesTable.style.display = 'none';
        categoriesEmpty.style.display = 'block';
        return;
    }
    
    categoriesTable.style.display = 'table';
    categoriesEmpty.style.display = 'none';
    
    categories.forEach(category => {
        const row = document.createElement('tr');
        
        row.innerHTML = `
            <td>${category.CategoryID}</td>
            <td>${category.CategoryName}</td>
            <td>${category.productCount}개</td>
            <td>
                <div class="action-buttons">
                    <button class="btn btn-danger btn-sm" onclick="deleteCategory(${category.CategoryID}, 
                    '${category.CategoryName}', 
                    ${category.productCount})">
                        삭제
                    </button>
                </div>
            </td>
        `;
        
        tbody.appendChild(row);
    });
}

// 카테고리 삭제
async function deleteCategory(categoryId, categoryName, productCount) {
    if (productCount > 0) {
        alert(`해당 카테고리에 ${productCount}개의 상품이 있어서 삭제할 수 없습니다.\n먼저 해당 카테고리의 모든 상품을 삭제해주세요.`);
        return;
    }
    
    if (!confirm(`정말로 "${categoryName}" 카테고리를 삭제하시겠습니까?\n이 작업은 되돌릴 수 없습니다!`)) {
        return;
    }
    
    const confirmText = prompt('삭제하려면 "DELETE"를 입력하세요:');
    if (confirmText !== 'DELETE') {
        alert('삭제가 취소되었습니다.');
        return;
    }
    
    const response = await apiDelete(`/admin/categories/${categoryId}`);
    
    if (response.success) {
        alert(response.message);
        loadCategories();
        loadCategoryFilters();
        loadDashboard(); // 대시보드 업데이트
    } else {
        alert(response.message);
    }
}

/**
 * 카테고리 필터 로드 (상품 검색용)
 */
async function loadCategoryFilters() {
    const response = await apiGet('/admin/categories');
    
    if (!response.success) return;
    
    const categories = response.categories;
    
    // 상품 추가 모달의 카테고리 셀렉트
    const addProductCategory = document.getElementById('addProductCategory');
    if (addProductCategory) {
        addProductCategory.innerHTML = '<option value="">카테고리 선택</option>';
        categories.forEach(category => {
            const option = document.createElement('option');
            option.value = category.CategoryID;
            option.textContent = category.CategoryName;
            addProductCategory.appendChild(option);
        });
    }
    
    // 상품 검색 필터
    const productCategoryFilter = document.getElementById('productCategoryFilter');
    if (productCategoryFilter) {
        productCategoryFilter.innerHTML = '<option value="">전체</option>';
        categories.forEach(category => {
            const option = document.createElement('option');
            option.value = category.CategoryName;
            option.textContent = category.CategoryName;
            productCategoryFilter.appendChild(option);
        });
    }
}

