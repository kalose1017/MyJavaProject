/**
 * ===========================================
 * YuhanMarket 백엔드 서버 (server.js)
 * ===========================================
 * 
 * 주요 기능:
 * - Express 기반 웹 서버 및 REST API 제공
 * - MySQL 데이터베이스 연동 (Customer, SHOPCART, shopdatatable)
 * - 정적 파일 서빙 (HTML, CSS, JS)
 * - 세션 기반 사용자 인증 관리
 * 
 * API 엔드포인트:
 * 
 * [인증 관련]
 * - POST /api/customer/login - 로그인
 * - POST /api/customer/logout - 로그아웃
 * - GET /api/customer/me - 현재 사용자 정보 조회
 * 
 * [상품 관련]
 * - GET /api/products - 상품 목록 조회 (검색, 카테고리, 정렬)
 * - GET /api/categories - 카테고리 목록 조회
 * 
 * [장바구니 관련]
 * - GET /api/cart - 장바구니 조회
 * - POST /api/cart/add - 장바구니에 상품 추가 (재고 차감)
 * - DELETE /api/cart/:cartId - 장바구니 상품 삭제 (재고 복구)
 * - PUT /api/cart/update-with-stock - 장바구니 수량 수정 (재고 조정)
 * - DELETE /api/cart/clear-restore - 장바구니 전체 비우기 (재고 복구)
 * 
 * [구매 관련]
 * - POST /api/purchase/all - 전체 구매
 * - POST /api/purchase/selected - 선택 구매 (잔액 확인, 재고 조정)
 * 
 * [고객 관리]
 * - POST /api/customer/charge - 페이 충전 (누적 충전액, 등급 자동 업데이트)
 * - POST /api/customer/change-nickname - 닉네임 변경 (중복 확인)
 * - POST /api/customer/change-password - 비밀번호 변경
 * 
 * [관리자]
 * - POST /api/admin/sync-total-charge - 누적 충전액 동기화
 * 
 * 등급 시스템:
 * - Bronze: 기본 등급
 * - Silver: 누적 충전액 50만원 이상
 * - Gold: 누적 충전액 150만원 이상
 * - Diamond: 누적 충전액 300만원 이상
 * - VIP: 누적 충전액 500만원 이상
 * 
 * ===========================================
 */

const express = require('express');
const mysql = require('mysql2');
const cors = require('cors');
const bodyParser = require('body-parser');
const path = require('path');

const app = express();
const PORT = 8080; // 백엔드 서버 포트

// 미들웨어 설정
app.use(cors());
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

// 정적 파일 서빙 (프론트엔드)
app.use(express.static('..'));

// MySQL 데이터베이스 연결 설정
const dbConfig = {
    host: 'localhost',
    user: 'root',
    password: '1369',
    database: 'shoppingmall',
    port: 3306,
    charset: 'utf8mb4',
    timezone: '+09:00'
};

const db = mysql.createConnection(dbConfig);

// 데이터베이스 연결
db.connect((err) => {
    if (err) {
        console.error('MySQL 연결 실패:', err);
        process.exit(1);
    }
});

// 세션 저장소 (간단한 메모리 저장소)
const sessions = new Map();

// 세션 생성 함수
function createSession(userId) {
    const sessionId = Date.now().toString() + Math.random().toString(36);
    sessions.set(sessionId, { userId, createdAt: Date.now() });
    return sessionId;
}

// 세션 검증 함수
function validateSession(sessionId) {
    const session = sessions.get(sessionId);
    if (!session) return null;
    
    // 세션 만료 시간: 24시간
    if (Date.now() - session.createdAt > 24 * 60 * 60 * 1000) {
        sessions.delete(sessionId);
        return null;
    }
    
    return session;
}

// API 라우트

// 로그인
app.post('/api/customer/login', (req, res) => {
    const { loginId, loginPw } = req.body;
    
    
    if (!loginId || !loginPw) {
        return res.json({ success: false, message: '아이디와 비밀번호를 입력해주세요.' });
    }
    
    const sql = 'SELECT CustomerID, LoginID, LoginPW, NickName as CustomerName, Grade FROM Customer WHERE LoginID = ?';
    
    db.query(sql, [loginId], (err, results) => {
        if (err) {
            return res.json({ success: false, message: '데이터베이스 오류가 발생했습니다.' });
        }
        
        
        if (results.length === 0) {
            return res.json({ success: false, message: '존재하지 않는 아이디입니다.' });
        }
        
        const user = results[0];
        
        if (user.LoginPW !== loginPw) {
            return res.json({ success: false, message: '비밀번호가 올바르지 않습니다.' });
        }
        
        // 세션 생성
        const sessionId = createSession(user.CustomerID);
        
        res.json({
            success: true,
            customerId: user.CustomerID,
            customerName: user.CustomerName,
            grade: user.Grade,
            sessionId: sessionId
        });
    });
});

// 로그아웃
app.post('/api/customer/logout', (req, res) => {
    const sessionId = req.headers.authorization;
    if (sessionId) {
        sessions.delete(sessionId);
    }
    res.json({ success: true, message: '로그아웃되었습니다.' });
});

// 현재 사용자 정보 조회
app.get('/api/customer/me', (req, res) => {
    const sessionId = req.headers.authorization;
    const session = validateSession(sessionId);
    
    if (!session) {
        return res.json({ loggedIn: false });
    }
    
    const sql = 'SELECT CustomerID, LoginID, NickName as CustomerName, Grade, PayCharge FROM Customer WHERE CustomerID = ?';
    
    db.query(sql, [session.userId], (err, results) => {
        if (err) {
            return res.json({ loggedIn: false });
        }
        
        if (results.length === 0) {
            return res.json({ loggedIn: false });
        }
        
        const user = results[0];
        res.json({
            loggedIn: true,
            customerId: user.CustomerID,
            loginId: user.LoginID,
            customerName: user.CustomerName,
            grade: user.Grade,
            balance: user.PayCharge
        });
    });
});

// 상품 목록 조회
app.get('/api/products', (req, res) => {
    const { search, category, sortBy } = req.query;
    
    let sql = `
        SELECT ProductID as productId, ProductName as productName, Price as price, 
               StockQuantity as stock, CategoryName as categoryName, Origin as origin
        FROM shopdatatable
        WHERE 1=1
    `;
    const params = [];
    
    if (search) {
        sql += ' AND ProductName LIKE ?';
        params.push(`%${search}%`);
    }
    
    if (category && category !== '전체') {
        sql += ' AND CategoryName = ?';
        params.push(category);
    }
    
    // 정렬 기준 처리
    let orderBy = 'ProductName ASC'; // 기본값: 가나다 오름차순
    if (sortBy === 'name_desc') {
        orderBy = 'ProductName DESC';
    } else if (sortBy === 'price_asc') {
        orderBy = 'Price ASC';
    } else if (sortBy === 'price_desc') {
        orderBy = 'Price DESC';
    }
    
    sql += ` ORDER BY ${orderBy}`;
    
    db.query(sql, params, (err, results) => {
        if (err) {
            return res.json([]);
        }
        
        res.json(results);
    });
});

// 카테고리 목록 조회
app.get('/api/categories', (req, res) => {
    const sql = 'SELECT DISTINCT CategoryName FROM shopdatatable ORDER BY CategoryName';
    
    db.query(sql, (err, results) => {
        if (err) {
            return res.json([]);
        }
        
        const categories = results.map(row => row.CategoryName);
        res.json(categories);
    });
});

// 장바구니 조회
app.get('/api/cart', (req, res) => {
    const sessionId = req.headers.authorization;
    const session = validateSession(sessionId);
    
    if (!session) {
        return res.json([]);
    }
    
    // 현재 사용자 정보 가져오기
    const userSql = 'SELECT LoginID, NickName FROM Customer WHERE CustomerID = ?';
    db.query(userSql, [session.userId], (err, userResults) => {
        if (err || userResults.length === 0) {
            return res.json([]);
        }
        
        const user = userResults[0];
        
        const sql = `
            SELECT sc.ProductID, sc.ProductName, sc.Quantity, sd.Price,
                   (sd.Price * sc.Quantity) as TotalPrice
            FROM SHOPCART sc
            JOIN SHOPDATATABLE sd ON sc.ProductID = sd.ProductID AND sc.ProductName = sd.ProductName
            WHERE sc.CustomerID = ? AND sc.LoginID = ? AND sc.NickName = ?
            ORDER BY sc.ProductID
        `;
        
        db.query(sql, [session.userId, user.LoginID, user.NickName], (err, results) => {
            if (err) {
                return res.json([]);
            }
            
            // CartID를 ProductID로 매핑 (기존 구조와 호환)
            const cartResults = results.map((row, index) => ({
                cartId: row.ProductID, // 임시로 ProductID를 cartId로 사용
                productId: row.ProductID,
                productName: row.ProductName,
                price: row.Price,
                quantity: row.Quantity,
                totalPrice: row.TotalPrice
            }));
            
            res.json(cartResults);
        });
    });
});

// 장바구니에 상품 추가
app.post('/api/cart/add', (req, res) => {
    const sessionId = req.headers.authorization;
    const session = validateSession(sessionId);
    
    if (!session) {
        return res.json({ success: false, message: '로그인이 필요합니다.' });
    }
    
    const { productId, quantity } = req.body;
    
    if (!productId || !quantity || quantity <= 0) {
        return res.json({ success: false, message: '올바른 상품과 수량을 입력해주세요.' });
    }
    
    // 사용자 정보 가져오기
    const userSql = 'SELECT LoginID, NickName FROM Customer WHERE CustomerID = ?';
    db.query(userSql, [session.userId], (err, userResults) => {
        if (err || userResults.length === 0) {
            return res.json({ success: false, message: '사용자 정보를 찾을 수 없습니다.' });
        }
        
        const user = userResults[0];
        
        // 상품 정보 및 재고 확인
        const productSql = 'SELECT ProductName, Price, StockQuantity FROM shopdatatable WHERE ProductID = ?';
        db.query(productSql, [productId], (err, productResults) => {
            if (err) {
                return res.json({ success: false, message: '상품 정보 조회 중 오류가 발생했습니다.' });
            }
            
            if (productResults.length === 0) {
                return res.json({ success: false, message: '존재하지 않는 상품입니다.' });
            }
            
            const product = productResults[0];
            const currentStock = product.StockQuantity;
            
            // 기존 장바구니 수량 확인
            const cartSql = 'SELECT Quantity FROM SHOPCART WHERE CustomerID = ? AND LoginID = ? AND NickName = ? AND ProductID = ? AND ProductName = ?';
            db.query(cartSql, [session.userId, user.LoginID, user.NickName, productId, product.ProductName], (err, cartResults) => {
                if (err) {
                    return res.json({ success: false, message: '장바구니 확인 중 오류가 발생했습니다.' });
                }
                
                const currentCartQuantity = cartResults.length > 0 ? cartResults[0].Quantity : 0;
                
                if (currentCartQuantity + quantity > currentStock) {
                    return res.json({ 
                        success: false, 
                        message: `재고가 부족합니다. 현재 재고: ${currentStock}개, 장바구니 수량: ${currentCartQuantity}개` 
                    });
                }
                
                // 장바구니에 추가 또는 수량 업데이트
                const insertCartSql = 'INSERT INTO SHOPCART (CustomerID, LoginID, NickName, ProductID, ProductName, Quantity) VALUES (?, ?, ?, ?, ?, ?) ' +
                           'ON DUPLICATE KEY UPDATE Quantity = Quantity + ?';
                db.query(insertCartSql, [session.userId, user.LoginID, user.NickName, productId, product.ProductName, quantity, quantity], (err) => {
                    if (err) {
                        return res.json({ success: false, message: '장바구니 추가 중 오류가 발생했습니다.' });
                    }
                    
                    // 재고 감소
                    const updateStockSql = 'UPDATE shopdatatable SET StockQuantity = StockQuantity - ? WHERE ProductID = ?';
                    db.query(updateStockSql, [quantity, productId], (err) => {
                        if (err) {
                            return res.json({ success: false, message: '재고 업데이트 중 오류가 발생했습니다.' });
                        }
                        res.json({ success: true, message: '장바구니에 추가되었습니다.' });
                    });
                });
            });
        });
    });
});

// 장바구니에서 상품 삭제 (재고 복구)
app.delete('/api/cart/:cartId', (req, res) => {
    const sessionId = req.headers.authorization;
    const session = validateSession(sessionId);
    
    if (!session) {
        return res.json({ success: false, message: '로그인이 필요합니다.' });
    }
    
    const { cartId } = req.params;
    
    // 사용자 정보 조회
    const userSql = 'SELECT LoginID, NickName FROM Customer WHERE CustomerID = ?';
    db.query(userSql, [session.userId], (err, userResults) => {
        if (err || userResults.length === 0) {
            return res.json({ success: false, message: '사용자 정보를 찾을 수 없습니다.' });
        }
        
        const user = userResults[0];
        
        // 삭제할 상품의 수량 조회 (재고 복구용)
        const cartSql = 'SELECT ProductID, Quantity FROM SHOPCART WHERE ProductID = ? AND CustomerID = ? AND LoginID = ? AND NickName = ?';
        db.query(cartSql, [cartId, session.userId, user.LoginID, user.NickName], (err, cartResults) => {
            if (err || cartResults.length === 0) {
                return res.json({ success: false, message: '삭제할 상품을 찾을 수 없습니다.' });
            }
            
            const item = cartResults[0];
            
            // 재고 복구
            const restoreStockSql = 'UPDATE shopdatatable SET StockQuantity = StockQuantity + ? WHERE ProductID = ?';
            db.query(restoreStockSql, [item.Quantity, item.ProductID], (err) => {
                if (err) {
                    return res.json({ success: false, message: '재고 복구 중 오류가 발생했습니다.' });
                }
                
                // 장바구니에서 삭제
                const deleteSql = 'DELETE FROM SHOPCART WHERE ProductID = ? AND CustomerID = ? AND LoginID = ? AND NickName = ?';
                db.query(deleteSql, [cartId, session.userId, user.LoginID, user.NickName], (err, results) => {
                    if (err) {
                        return res.json({ success: false, message: '삭제 중 오류가 발생했습니다.' });
                    }
                    
                    if (results.affectedRows === 0) {
                        return res.json({ success: false, message: '삭제할 상품을 찾을 수 없습니다.' });
                    }
                    
                    res.json({ success: true, message: '상품이 삭제되고 재고가 복구되었습니다.' });
                });
            });
        });
    });
});

// 장바구니 수량 수정
app.put('/api/cart/:cartId', (req, res) => {
    const sessionId = req.headers.authorization;
    const session = validateSession(sessionId);
    
    if (!session) {
        return res.json({ success: false, message: '로그인이 필요합니다.' });
    }
    
    const { cartId } = req.params;
    const { quantity } = req.body;
    
    if (!quantity || quantity <= 0) {
        return res.json({ success: false, message: '올바른 수량을 입력해주세요.' });
    }
    
    const sql = 'UPDATE Cart SET Quantity = ? WHERE CartID = ? AND CustomerID = ?';
    db.query(sql, [quantity, session.userId, cartId], (err, results) => {
        if (err) {
            return res.json({ success: false, message: '수정 중 오류가 발생했습니다.' });
        }
        
        if (results.affectedRows === 0) {
            return res.json({ success: false, message: '수정할 상품을 찾을 수 없습니다.' });
        }
        
        res.json({ success: true, message: '수량이 수정되었습니다.' });
    });
});

// 장바구니 수량 수정 (재고 조정 포함)
app.put('/api/cart/update-with-stock', (req, res) => {
    const sessionId = req.headers.authorization;
    const session = validateSession(sessionId);
    
    if (!session) {
        return res.json({ success: false, message: '로그인이 필요합니다.' });
    }
    
    const { cartId, newQuantity, quantityDiff } = req.body;
    
    if (!cartId || !newQuantity || newQuantity < 1) {
        return res.json({ success: false, message: '올바른 수량을 입력해주세요.' });
    }
    
    // 수량 증가 시 재고 확인
    if (quantityDiff > 0) {
        const stockSql = 'SELECT StockQuantity FROM shopdatatable WHERE ProductID = ?';
        db.query(stockSql, [cartId], (err, stockResults) => {
            if (err || stockResults.length === 0) {
                return res.json({ success: false, message: '상품 정보를 찾을 수 없습니다.' });
            }
            
            const currentStock = stockResults[0].StockQuantity;
            
            if (currentStock < quantityDiff) {
                return res.json({ success: false, message: `재고가 부족합니다. (현재 재고: ${currentStock}개)` });
            }
            
            // 재고 충분 - 업데이트 진행
            updateCartAndStock();
        });
    } else {
        // 수량 감소 또는 동일 - 바로 업데이트
        updateCartAndStock();
    }
    
    function updateCartAndStock() {
        // 사용자 정보 조회
        const userSql = 'SELECT LoginID, NickName FROM Customer WHERE CustomerID = ?';
        db.query(userSql, [session.userId], (err, userResults) => {
            if (err || userResults.length === 0) {
                return res.json({ success: false, message: '사용자 정보를 찾을 수 없습니다.' });
            }
            
            const user = userResults[0];
            
            // 재고 조정 (양수면 재고 감소, 음수면 재고 증가)
            const updateStockSql = 'UPDATE shopdatatable SET StockQuantity = StockQuantity - ? WHERE ProductID = ?';
            db.query(updateStockSql, [quantityDiff, cartId], (err) => {
                if (err) {
                    return res.json({ success: false, message: '재고 업데이트 중 오류가 발생했습니다.' });
                }
                
                // 장바구니 수량 업데이트
                const updateCartSql = 'UPDATE SHOPCART SET Quantity = ? WHERE ProductID = ? AND CustomerID = ? AND LoginID = ? AND NickName = ?';
                db.query(updateCartSql, [newQuantity, cartId, session.userId, user.LoginID, user.NickName], (err, results) => {
                    if (err) {
                        return res.json({ success: false, message: '수량 수정 중 오류가 발생했습니다.' });
                    }
                    
                    if (results.affectedRows === 0) {
                        return res.json({ success: false, message: '수정할 상품을 찾을 수 없습니다.' });
                    }
                    
                    res.json({ success: true, message: '수량이 변경되었습니다.' });
                });
            });
        });
    }
});

// 장바구니 전체 비우기
app.delete('/api/cart/clear', (req, res) => {
    const sessionId = req.headers.authorization;
    const session = validateSession(sessionId);
    
    if (!session) {
        return res.json({ success: false, message: '로그인이 필요합니다.' });
    }
    
    const sql = 'DELETE FROM Cart WHERE CustomerID = ?';
    db.query(sql, [session.userId], (err) => {
        if (err) {
            return res.json({ success: false, message: '장바구니 비우기 중 오류가 발생했습니다.' });
        }
        
        res.json({ success: true, message: '장바구니가 비워졌습니다.' });
    });
});

// 전체 구매
app.post('/api/purchase/all', (req, res) => {
    const sessionId = req.headers.authorization;
    const session = validateSession(sessionId);
    
    if (!session) {
        return res.json({ success: false, message: '로그인이 필요합니다.' });
    }
    
    // 장바구니 조회
    const cartSql = 'SELECT * FROM Cart WHERE CustomerID = ?';
    db.query(cartSql, [session.userId], (err, cartResults) => {
        if (err) {
            return res.json({ success: false, message: '장바구니 조회 중 오류가 발생했습니다.' });
        }
        
        if (cartResults.length === 0) {
            return res.json({ success: false, message: '장바구니가 비어있습니다.' });
        }
        
        // 구매 처리 (간단한 시뮬레이션)
        const clearCartSql = 'DELETE FROM Cart WHERE CustomerID = ?';
        db.query(clearCartSql, [session.userId], (err) => {
            if (err) {
                return res.json({ success: false, message: '구매 처리 중 오류가 발생했습니다.' });
            }
            
            res.json({ success: true, message: '구매가 완료되었습니다!' });
        });
    });
});

// 선택 구매 (잔액 확인 포함)
app.post('/api/purchase/selected', (req, res) => {
    const sessionId = req.headers.authorization;
    const session = validateSession(sessionId);
    
    if (!session) {
        return res.json({ success: false, message: '로그인이 필요합니다.' });
    }
    
    const { cartIds, quantity } = req.body;
    
    if (!cartIds || !Array.isArray(cartIds) || cartIds.length === 0 || !quantity) {
        return res.json({ success: false, message: '구매할 상품을 선택해주세요.' });
    }
    
    const cartId = cartIds[0]; // 단일 상품 선택 구매
    const purchaseQuantity = parseInt(quantity); // 구매할 수량 (사용자가 입력한 수량)
    
    // 장바구니 상품 정보 조회
    const cartSql = 'SELECT sc.ProductID, sc.ProductName, sc.Quantity, sd.Price FROM SHOPCART sc JOIN SHOPDATATABLE sd ON sc.ProductID = sd.ProductID WHERE sc.ProductID = ? AND sc.CustomerID = ? LIMIT 1';
    
    db.query(cartSql, [cartId, session.userId], (err, cartResults) => {
        if (err || cartResults.length === 0) {
            return res.json({ success: false, message: '상품 정보를 찾을 수 없습니다.' });
        }
        
        const item = cartResults[0];
        const cartQuantity = item.Quantity; // 장바구니에 담긴 원래 수량
        const totalPrice = item.Price * purchaseQuantity;
        
        // 수량 증가 시 재고 확인
        if (purchaseQuantity > cartQuantity) {
            const stockSql = 'SELECT StockQuantity FROM shopdatatable WHERE ProductID = ?';
            db.query(stockSql, [cartId], (err, stockResults) => {
                if (err || stockResults.length === 0) {
                    return res.json({ success: false, message: '상품 정보를 찾을 수 없습니다.' });
                }
                
                const currentStock = stockResults[0].StockQuantity;
                const additionalQuantity = purchaseQuantity - cartQuantity;
                
                if (currentStock < additionalQuantity) {
                    return res.json({ success: false, message: `재고가 부족합니다. (현재 재고: ${currentStock}개, 추가 필요: ${additionalQuantity}개)` });
                }
                
                // 재고 충분 - 구매 진행
                processPurchase();
            });
        } else {
            // 수량 감소 또는 동일 - 바로 구매 진행
            processPurchase();
        }
        
        function processPurchase() {
            // 사용자 잔액 확인
            const balanceSql = 'SELECT PayCharge FROM Customer WHERE CustomerID = ?';
            db.query(balanceSql, [session.userId], (err, balanceResults) => {
                if (err || balanceResults.length === 0) {
                    return res.json({ success: false, message: '사용자 정보를 찾을 수 없습니다.' });
                }
                
                const balance = balanceResults[0].PayCharge;
                
                if (balance < totalPrice) {
                    return res.json({ success: false, message: `잔액이 부족합니다. (현재 잔액: ${Math.floor(balance).toLocaleString()}원)` });
                }
                
                // 잔액 차감
                const newBalance = balance - totalPrice;
                const updateBalanceSql = 'UPDATE Customer SET PayCharge = ? WHERE CustomerID = ?';
                db.query(updateBalanceSql, [newBalance, session.userId], (err) => {
                    if (err) {
                        return res.json({ success: false, message: '결제 처리 중 오류가 발생했습니다.' });
                    }
                    
                    // 재고 조정
                    const stockDiff = purchaseQuantity - cartQuantity; // 양수: 추가 차감, 음수: 복구
                    
                    const updateStockSql = 'UPDATE shopdatatable SET StockQuantity = StockQuantity - ? WHERE ProductID = ?';
                    db.query(updateStockSql, [stockDiff, cartId], (err) => {
                        if (err) {
                            return res.json({ success: false, message: '재고 업데이트 중 오류가 발생했습니다.' });
                        }
                        
                        // 장바구니에서 제거
                        const deleteSql = 'DELETE FROM SHOPCART WHERE ProductID = ? AND CustomerID = ?';
                        db.query(deleteSql, [cartId, session.userId], (err) => {
                            if (err) {
                                return res.json({ success: false, message: '구매 처리 중 오류가 발생했습니다.' });
                            }
                            
                            res.json({ success: true, message: `${purchaseQuantity}개 구매가 완료되었습니다!` });
                        });
                    });
                });
            });
        }
    });
});

// 전체 비우기 (재고 복구)
app.delete('/api/cart/clear-restore', (req, res) => {
    const sessionId = req.headers.authorization;
    const session = validateSession(sessionId);
    
    if (!session) {
        return res.json({ success: false, message: '로그인이 필요합니다.' });
    }
    
    // 사용자 정보 조회
    const userSql = 'SELECT LoginID, NickName FROM Customer WHERE CustomerID = ?';
    db.query(userSql, [session.userId], (err, userResults) => {
        if (err || userResults.length === 0) {
            return res.json({ success: false, message: '사용자 정보를 찾을 수 없습니다.' });
        }
        
        const user = userResults[0];
        
        // 장바구니 정보 조회 (재고 복구용)
        const cartSql = 'SELECT ProductID, Quantity FROM SHOPCART WHERE CustomerID = ? AND LoginID = ? AND NickName = ?';
        db.query(cartSql, [session.userId, user.LoginID, user.NickName], (err, cartItems) => {
            if (err) {
                return res.json({ success: false, message: '장바구니 조회 중 오류가 발생했습니다.' });
            }
            
            if (cartItems.length === 0) {
                return res.json({ success: true, message: '장바구니가 이미 비어있습니다.' });
            }
            
            // 재고 복구
            let completedUpdates = 0;
            cartItems.forEach(item => {
                const restoreStockSql = 'UPDATE shopdatatable SET StockQuantity = StockQuantity + ? WHERE ProductID = ?';
                db.query(restoreStockSql, [item.Quantity, item.ProductID], (err) => {
                    if (err) {
                        console.error('재고 복구 오류:', err);
                    }
                    
                    completedUpdates++;
                    
                    // 모든 재고 복구 완료 후 장바구니 비우기
                    if (completedUpdates === cartItems.length) {
                        const clearSql = 'DELETE FROM SHOPCART WHERE CustomerID = ? AND LoginID = ? AND NickName = ?';
                        db.query(clearSql, [session.userId, user.LoginID, user.NickName], (err) => {
                            if (err) {
                                return res.json({ success: false, message: '장바구니 비우기 중 오류가 발생했습니다.' });
                            }
                            
                            res.json({ success: true, message: '장바구니가 비워졌고 재고가 복구되었습니다.' });
                        });
                    }
                });
            });
        });
    });
});

// 페이 충전
app.post('/api/customer/charge', (req, res) => {
    const sessionId = req.headers.authorization;
    const session = validateSession(sessionId);
    
    if (!session) {
        return res.json({ success: false, message: '로그인이 필요합니다.' });
    }
    
    const amount = parseInt(req.body.amount);
    
    if (!amount || amount < 1000) {
        return res.json({ success: false, message: '최소 충전 금액은 1,000원입니다.' });
    }
    
    // 현재 잔액 및 누적 충전액 조회
    const getBalanceSql = 'SELECT PayCharge, TotalCharge FROM Customer WHERE CustomerID = ?';
    
    db.query(getBalanceSql, [session.userId], (err, results) => {
        if (err) {
            return res.json({ success: false, message: '잔액 조회 중 오류가 발생했습니다.' });
        }
        
        if (results.length === 0) {
            return res.json({ success: false, message: '사용자를 찾을 수 없습니다.' });
        }
        
        const currentBalance = parseFloat(results[0].PayCharge) || 0;
        const currentTotalCharge = parseFloat(results[0].TotalCharge) || 0;
        const newBalance = currentBalance + amount;
        const newTotalCharge = currentTotalCharge + amount;
        
        // 누적 충전액에 따른 등급 결정
        let newGrade = 'Bronze';
        if (newTotalCharge >= 5000000) {
            newGrade = 'VIP';
        } else if (newTotalCharge >= 3000000) {
            newGrade = 'Diamond';
        } else if (newTotalCharge >= 1500000) {
            newGrade = 'Gold';
        } else if (newTotalCharge >= 500000) {
            newGrade = 'Silver';
        }
        
        // 잔액, 누적 충전액, 등급 업데이트
        const updateSql = 'UPDATE Customer SET PayCharge = ?, TotalCharge = ?, Grade = ? WHERE CustomerID = ?';
        db.query(updateSql, [newBalance, newTotalCharge, newGrade, session.userId], (err) => {
            if (err) {
                return res.json({ success: false, message: '충전 중 오류가 발생했습니다.' });
            }
            
            res.json({ 
                success: true, 
                message: `페이 ${Math.floor(amount).toLocaleString()}원이 충전되었습니다!`,
                newBalance: newBalance,
                newGrade: newGrade,
                newTotalCharge: newTotalCharge
            });
        });
    });
});

// 닉네임 변경
app.post('/api/customer/change-nickname', (req, res) => {
    const sessionId = req.headers.authorization;
    const session = validateSession(sessionId);
    
    if (!session) {
        return res.json({ success: false, message: '로그인이 필요합니다.' });
    }
    
    const { newNickname } = req.body;
    
    if (!newNickname || newNickname.trim().length < 2) {
        return res.json({ success: false, message: '닉네임은 2자 이상이어야 합니다.' });
    }
    
    // 닉네임 중복 확인
    const checkSql = 'SELECT CustomerID FROM Customer WHERE NickName = ? AND CustomerID != ?';
    db.query(checkSql, [newNickname, session.userId], (err, results) => {
        if (err) {
            return res.json({ success: false, message: '닉네임 확인 중 오류가 발생했습니다.' });
        }
        
        if (results.length > 0) {
            return res.json({ success: false, message: '이미 사용 중인 닉네임입니다.' });
        }
        
        // 닉네임 업데이트
        const updateSql = 'UPDATE Customer SET NickName = ? WHERE CustomerID = ?';
        db.query(updateSql, [newNickname, session.userId], (err) => {
            if (err) {
                return res.json({ success: false, message: '닉네임 변경 중 오류가 발생했습니다.' });
            }
            
            res.json({ success: true, message: '닉네임이 변경되었습니다!' });
        });
    });
});

// 비밀번호 변경
app.post('/api/customer/change-password', (req, res) => {
    const sessionId = req.headers.authorization;
    const session = validateSession(sessionId);
    
    if (!session) {
        return res.json({ success: false, message: '로그인이 필요합니다.' });
    }
    
    const { currentPw, newPw } = req.body;
    
    if (!currentPw || !newPw) {
        return res.json({ success: false, message: '현재 비밀번호와 새 비밀번호를 입력해주세요.' });
    }
    
    // 현재 비밀번호 확인
    const checkSql = 'SELECT LoginPW FROM Customer WHERE CustomerID = ?';
    db.query(checkSql, [session.userId], (err, results) => {
        if (err) {
            return res.json({ success: false, message: '비밀번호 확인 중 오류가 발생했습니다.' });
        }
        
        if (results.length === 0) {
            return res.json({ success: false, message: '사용자를 찾을 수 없습니다.' });
        }
        
        if (results[0].LoginPW !== currentPw) {
            return res.json({ success: false, message: '현재 비밀번호가 올바르지 않습니다.' });
        }
        
        // 비밀번호 업데이트
        const updateSql = 'UPDATE Customer SET LoginPW = ? WHERE CustomerID = ?';
        db.query(updateSql, [newPw, session.userId], (err) => {
            if (err) {
                return res.json({ success: false, message: '비밀번호 변경 중 오류가 발생했습니다.' });
            }
            
            res.json({ success: true, message: '비밀번호가 변경되었습니다!' });
        });
    });
});

// 누적 충전액 초기화 (현재 잔액 기준) - 관리자 API
app.post('/api/admin/sync-total-charge', (req, res) => {
    // 모든 고객의 현재 잔액을 기준으로 누적 충전액 및 등급 업데이트
    const selectSql = 'SELECT CustomerID, PayCharge FROM Customer';
    
    db.query(selectSql, (err, customers) => {
        if (err) {
            return res.json({ success: false, message: '고객 정보 조회 중 오류가 발생했습니다.' });
        }
        
        if (customers.length === 0) {
            return res.json({ success: true, message: '업데이트할 고객이 없습니다.' });
        }
        
        let updatedCount = 0;
        const totalCustomers = customers.length;
        
        customers.forEach(customer => {
            const payCharge = parseFloat(customer.PayCharge) || 0;
            
            // 누적 충전액을 현재 잔액으로 설정
            let grade = 'Bronze';
            if (payCharge >= 5000000) {
                grade = 'VIP';
            } else if (payCharge >= 3000000) {
                grade = 'Diamond';
            } else if (payCharge >= 1500000) {
                grade = 'Gold';
            } else if (payCharge >= 500000) {
                grade = 'Silver';
            }
            
            const updateSql = 'UPDATE Customer SET TotalCharge = ?, Grade = ? WHERE CustomerID = ?';
            db.query(updateSql, [payCharge, grade, customer.CustomerID], (err) => {
                if (err) {
                    console.error('업데이트 오류:', err);
                }
                
                updatedCount++;
                
                if (updatedCount === totalCustomers) {
                    res.json({ 
                        success: true, 
                        message: `${totalCustomers}명의 고객 정보가 업데이트되었습니다.`,
                        updatedCount: updatedCount
                    });
                }
            });
        });
    });
});

// 서버 시작
app.listen(PORT, () => {
    console.log('');
    console.log('MySQL 데이터베이스에 연결되었습니다.');
    console.log(`백엔드 서버가 포트 ${PORT}에서 실행 중입니다.`);
    console.log(`프론트엔드: http://localhost:${PORT}`);
    console.log(`API 서버: http://localhost:${PORT}/api`);
    console.log('(종료 : ctrl + c)');
    console.log('');
});
