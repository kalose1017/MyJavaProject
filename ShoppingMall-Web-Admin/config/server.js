/**
 * ===========================================
 * YuhanMarket 운영자 백엔드 서버 (server.js)
 * ===========================================
 * 
 * 주요 기능:
 * - Express 기반 웹 서버 및 REST API 제공
 * - MySQL 데이터베이스 연동
 * - 정적 파일 서빙 (HTML, CSS, JS)
 * - 간단한 운영자 인증 관리
 * 
 * API 엔드포인트:
 * 
 * [인증 관련]
 * - POST /api/admin/login - 운영자 로그인
 * - POST /api/admin/logout - 로그아웃
 * 
 * [상품 관리]
 * - GET /api/admin/products - 상품 목록 조회
 * - GET /api/admin/products/:id - 상품 상세 조회
 * - POST /api/admin/products - 상품 추가
 * - PUT /api/admin/products/:id - 상품 수정
 * - DELETE /api/admin/products/:id - 상품 삭제
 * 
 * [카테고리 관리]
 * - GET /api/admin/categories - 카테고리 목록 조회
 * - POST /api/admin/categories - 카테고리 추가
 * - DELETE /api/admin/categories/:id - 카테고리 삭제
 * 
 * ===========================================
 */

const express = require('express');
const mysql = require('mysql2');
const cors = require('cors');
const bodyParser = require('body-parser');
const path = require('path');

const app = express();
const PORT = 8081; // 운영자 서버 포트 (고객용 8080과 구분)

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
function createSession(adminId) {
    const sessionId = Date.now().toString() + Math.random().toString(36);
    sessions.set(sessionId, { adminId, createdAt: Date.now() });
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

// ========================
// 인증 관련 API
// ========================

// 운영자 로그인 (간단한 하드코딩 - 실제로는 DB에서 관리)
app.post('/api/admin/login', (req, res) => {
    const { username, password } = req.body;
    
    if (!username || !password) {
        return res.json({ success: false, message: '아이디와 비밀번호를 입력해주세요.' });
    }
    
    // 간단한 운영자 인증 (실제로는 DB에서 확인해야 함)
    if (username === 'admin' && password === 'admin1234') {
        const sessionId = createSession('admin');
        return res.json({
            success: true,
            message: '로그인 성공',
            sessionId: sessionId,
            adminName: '관리자'
        });
    } else {
        return res.json({ success: false, message: '아이디 또는 비밀번호가 올바르지 않습니다.' });
    }
});

// 로그아웃
app.post('/api/admin/logout', (req, res) => {
    const sessionId = req.headers.authorization || req.body.sessionId;
    if (sessionId) {
        sessions.delete(sessionId);
    }
    res.json({ success: true, message: '로그아웃되었습니다.' });
});

// ========================
// 카테고리 관리 API
// ========================

// 카테고리 목록 조회
app.get('/api/admin/categories', (req, res) => {
    const sessionId = req.headers.authorization;
    if (!validateSession(sessionId)) {
        return res.status(401).json({ success: false, message: '인증이 필요합니다.' });
    }
    
    const sql = `
        SELECT CategoryID, CategoryName, COUNT(ProductID) as productCount
        FROM shopdatatable
        GROUP BY CategoryID, CategoryName
        ORDER BY CategoryID
    `;
    
    db.query(sql, (err, results) => {
        if (err) {
            console.error('카테고리 조회 오류:', err);
            return res.json({ success: false, message: '카테고리 조회 중 오류가 발생했습니다.' });
        }
        
        res.json({ success: true, categories: results });
    });
});

// 카테고리 추가
app.post('/api/admin/categories', (req, res) => {
    const sessionId = req.headers.authorization;
    if (!validateSession(sessionId)) {
        return res.status(401).json({ success: false, message: '인증이 필요합니다.' });
    }
    
    const { categoryName } = req.body;
    
    if (!categoryName || categoryName.trim() === '') {
        return res.json({ success: false, message: '카테고리명을 입력해주세요.' });
    }
    
    // 카테고리명 중복 확인
    const checkSql = 'SELECT COUNT(*) as count FROM shopdatatable WHERE CategoryName = ?';
    db.query(checkSql, [categoryName], (err, results) => {
        if (err) {
            console.error('카테고리 중복 확인 오류:', err);
            return res.json({ success: false, message: '카테고리 확인 중 오류가 발생했습니다.' });
        }
        
        if (results[0].count > 0) {
            return res.json({ success: false, message: '이미 존재하는 카테고리명입니다.' });
        }
        
        // 다음 카테고리 ID 조회
        const maxIdSql = 'SELECT MAX(CategoryID) as maxId FROM shopdatatable';
        db.query(maxIdSql, (err, results) => {
            if (err) {
                console.error('카테고리 ID 조회 오류:', err);
                return res.json({ success: false, message: '카테고리 ID 조회 중 오류가 발생했습니다.' });
            }
            
            const nextCategoryId = (results[0].maxId || 0) + 1;
            
            // 카테고리 추가 (빈 레코드 생성)
            const insertSql = 'INSERT INTO shopdatatable (CategoryID, CategoryName) VALUES (?, ?)';
            db.query(insertSql, [nextCategoryId, categoryName], (err, result) => {
                if (err) {
                    console.error('카테고리 추가 오류:', err);
                    return res.json({ success: false, message: '카테고리 추가 중 오류가 발생했습니다.' });
                }
                
                res.json({ 
                    success: true, 
                    message: '카테고리가 추가되었습니다.',
                    categoryId: nextCategoryId,
                    categoryName: categoryName
                });
            });
        });
    });
});

// 카테고리 삭제
app.delete('/api/admin/categories/:id', (req, res) => {
    const sessionId = req.headers.authorization;
    if (!validateSession(sessionId)) {
        return res.status(401).json({ success: false, message: '인증이 필요합니다.' });
    }
    
    const categoryId = parseInt(req.params.id);
    
    // 카테고리에 물품이 있는지 확인
    const checkSql = 'SELECT COUNT(*) as count FROM shopdatatable WHERE CategoryID = ? AND ProductID IS NOT NULL';
    db.query(checkSql, [categoryId], (err, results) => {
        if (err) {
            console.error('카테고리 확인 오류:', err);
            return res.json({ success: false, message: '카테고리 확인 중 오류가 발생했습니다.' });
        }
        
        if (results[0].count > 0) {
            return res.json({ 
                success: false, 
                message: '해당 카테고리에 물품이 있어서 삭제할 수 없습니다. 먼저 물품을 삭제해주세요.' 
            });
        }
        
        // 카테고리 삭제
        const deleteSql = 'DELETE FROM shopdatatable WHERE CategoryID = ?';
        db.query(deleteSql, [categoryId], (err, result) => {
            if (err) {
                console.error('카테고리 삭제 오류:', err);
                return res.json({ success: false, message: '카테고리 삭제 중 오류가 발생했습니다.' });
            }
            
            if (result.affectedRows === 0) {
                return res.json({ success: false, message: '삭제할 카테고리를 찾을 수 없습니다.' });
            }
            
            res.json({ success: true, message: '카테고리가 삭제되었습니다.' });
        });
    });
});

// ========================
// 상품 관리 API
// ========================

// 상품 목록 조회
app.get('/api/admin/products', (req, res) => {
    const sessionId = req.headers.authorization;
    if (!validateSession(sessionId)) {
        return res.status(401).json({ success: false, message: '인증이 필요합니다.' });
    }
    
    const { search, category } = req.query;
    
    let sql = `
        SELECT ProductID, ProductName, Price, StockQuantity, CategoryID, CategoryName, Origin
        FROM shopdatatable
        WHERE ProductID IS NOT NULL
    `;
    const params = [];
    
    if (search) {
        sql += ' AND ProductName LIKE ?';
        params.push(`%${search}%`);
    }
    
    if (category) {
        sql += ' AND CategoryName = ?';
        params.push(category);
    }
    
    sql += ' ORDER BY CategoryID, ProductID';
    
    db.query(sql, params, (err, results) => {
        if (err) {
            console.error('상품 조회 오류:', err);
            return res.json({ success: false, message: '상품 조회 중 오류가 발생했습니다.' });
        }
        
        res.json({ success: true, products: results });
    });
});

// 상품 상세 조회
app.get('/api/admin/products/:id', (req, res) => {
    const sessionId = req.headers.authorization;
    if (!validateSession(sessionId)) {
        return res.status(401).json({ success: false, message: '인증이 필요합니다.' });
    }
    
    const productId = parseInt(req.params.id);
    
    const sql = 'SELECT * FROM shopdatatable WHERE ProductID = ?';
    db.query(sql, [productId], (err, results) => {
        if (err) {
            console.error('상품 조회 오류:', err);
            return res.json({ success: false, message: '상품 조회 중 오류가 발생했습니다.' });
        }
        
        if (results.length === 0) {
            return res.json({ success: false, message: '상품을 찾을 수 없습니다.' });
        }
        
        res.json({ success: true, product: results[0] });
    });
});

// 상품 추가
app.post('/api/admin/products', (req, res) => {
    const sessionId = req.headers.authorization;
    if (!validateSession(sessionId)) {
        return res.status(401).json({ success: false, message: '인증이 필요합니다.' });
    }
    
    const { categoryId, productName, price, stockQuantity, origin } = req.body;
    
    // 유효성 검사
    if (!categoryId || !productName || price === undefined || stockQuantity === undefined) {
        return res.json({ success: false, message: '필수 항목을 모두 입력해주세요.' });
    }
    
    if (price < 0 || stockQuantity < 0) {
        return res.json({ success: false, message: '가격과 재고량은 0 이상이어야 합니다.' });
    }
    
    // 상품명 중복 확인
    const checkProductSql = 'SELECT COUNT(*) as count FROM shopdatatable WHERE ProductName = ? AND ProductID IS NOT NULL';
    db.query(checkProductSql, [productName], (err, checkResults) => {
        if (err) {
            console.error('상품명 중복 확인 오류:', err);
            return res.json({ success: false, message: '상품명 확인 중 오류가 발생했습니다.' });
        }
        
        if (checkResults[0].count > 0) {
            return res.json({ success: false, message: '이미 존재하는 상품입니다.' });
        }
        
        // 카테고리 정보 조회
        const categorySql = 'SELECT CategoryName FROM shopdatatable WHERE CategoryID = ? LIMIT 1';
        db.query(categorySql, [categoryId], (err, categoryResults) => {
            if (err || categoryResults.length === 0) {
                return res.json({ success: false, message: '카테고리를 찾을 수 없습니다.' });
            }
            
            const categoryName = categoryResults[0].CategoryName;
            
            // 다음 ProductID 생성 (CategoryID * 100 + 순차번호)
            const maxIdSql = 'SELECT MAX(ProductID) as maxId FROM shopdatatable WHERE CategoryID = ?';
            db.query(maxIdSql, [categoryId], (err, results) => {
            if (err) {
                console.error('ProductID 조회 오류:', err);
                return res.json({ success: false, message: 'ProductID 생성 중 오류가 발생했습니다.' });
            }
            
            let nextProductId;
            const maxId = results[0].maxId;
            
            if (!maxId) {
                // 해당 카테고리의 첫 번째 상품
                nextProductId = categoryId * 100 + 1;
            } else {
                // 기존 상품이 있는 경우
                const maxIdStr = String(maxId);
                const categoryIdStr = String(categoryId);
                
                if (maxIdStr.startsWith(categoryIdStr)) {
                    nextProductId = maxId + 1;
                } else {
                    nextProductId = categoryId * 100 + 1;
                }
            }
            
            // 상품 추가
            const insertSql = `
                INSERT INTO shopdatatable (CategoryID, CategoryName, ProductID, ProductName, Price, StockQuantity, Origin)
                VALUES (?, ?, ?, ?, ?, ?, ?)
            `;
            
            db.query(insertSql, [
                categoryId, 
                categoryName, 
                nextProductId, 
                productName, 
                price, 
                stockQuantity, 
                origin || '미입력'
            ], (err, result) => {
                if (err) {
                    console.error('상품 추가 오류:', err);
                    return res.json({ success: false, message: '상품 추가 중 오류가 발생했습니다.' });
                }
                
                res.json({ 
                    success: true, 
                    message: '상품이 추가되었습니다.',
                    productId: nextProductId
                });
            });
        });
        });
    });
});

// 상품 수정
app.put('/api/admin/products/:id', (req, res) => {
    const sessionId = req.headers.authorization;
    if (!validateSession(sessionId)) {
        return res.status(401).json({ success: false, message: '인증이 필요합니다.' });
    }
    
    const productId = parseInt(req.params.id);
    const { productName, price, stockQuantity, origin } = req.body;
    
    // 유효성 검사
    if (!productName || price === undefined || stockQuantity === undefined) {
        return res.json({ success: false, message: '필수 항목을 모두 입력해주세요.' });
    }
    
    if (price < 0 || stockQuantity < 0) {
        return res.json({ success: false, message: '가격과 재고량은 0 이상이어야 합니다.' });
    }
    
    // 상품 수정
    const updateSql = `
        UPDATE shopdatatable
        SET ProductName = ?, Price = ?, StockQuantity = ?, Origin = ?
        WHERE ProductID = ?
    `;
    
    db.query(updateSql, [productName, price, stockQuantity, origin || '미입력', productId], (err, result) => {
        if (err) {
            console.error('상품 수정 오류:', err);
            return res.json({ success: false, message: '상품 수정 중 오류가 발생했습니다.' });
        }
        
        if (result.affectedRows === 0) {
            return res.json({ success: false, message: '수정할 상품을 찾을 수 없습니다.' });
        }
        
        res.json({ success: true, message: '상품이 수정되었습니다.' });
    });
});

// 상품 삭제
app.delete('/api/admin/products/:id', (req, res) => {
    const sessionId = req.headers.authorization;
    if (!validateSession(sessionId)) {
        return res.status(401).json({ success: false, message: '인증이 필요합니다.' });
    }
    
    const productId = parseInt(req.params.id);
    
    // 상품 삭제
    const deleteSql = 'DELETE FROM shopdatatable WHERE ProductID = ?';
    db.query(deleteSql, [productId], (err, result) => {
        if (err) {
            console.error('상품 삭제 오류:', err);
            return res.json({ success: false, message: '상품 삭제 중 오류가 발생했습니다.' });
        }
        
        if (result.affectedRows === 0) {
            return res.json({ success: false, message: '삭제할 상품을 찾을 수 없습니다.' });
        }
        
        res.json({ success: true, message: '상품이 삭제되었습니다.' });
    });
});

// 서버 시작
app.listen(PORT, () => {
    console.log('');
    console.log('===========================================');
    console.log('  YuhanMarket 운영자 시스템');
    console.log('===========================================');
    console.log('MySQL 데이터베이스에 연결되었습니다.');
    console.log(`백엔드 서버가 포트 ${PORT}에서 실행 중입니다.`);
    console.log(`프론트엔드: http://localhost:${PORT}`);
    console.log(`API 서버: http://localhost:${PORT}/api`);
    console.log('');
    console.log('운영자 로그인 정보:');
    console.log('  아이디: admin');
    console.log('  비밀번호: admin1234');
    console.log('');
    console.log('(종료: Ctrl + C)');
    console.log('===========================================');
    console.log('');
});

