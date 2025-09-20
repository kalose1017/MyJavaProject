import java.sql.*;
import java.util.Scanner;

/**
 * ===========================================
 * 
 * 주요 기능:
 * - 물품 추가 (카테고리 선택, ProductID 자동 생성)
 * - 물품 수정
 * - 물품 삭제
 * - 물품 조회
 * - 재고 관리
 * 
 * 포함된 메소드:
 * - addProduct(): 새 물품 추가
 * - validateInput(): 입력값 유효성 검사
 * - getNextProductId(): 다음 상품 ID 조회 (카테고리ID + 순차번호)
 * - selectCategory(): 카테고리 선택
 * - showAllCategories(): 카테고리 목록 표시
 * 
 * ===========================================
 */
public class ProductManager {
	
	// 물품 추가 메인 메소드
	public static void addProduct() {
		Scanner sc = Main.getScanner();
		
		System.out.println();
		System.out.println("========== 물품 추가 ==========");
		
		try {
			// 1. 카테고리 선택 (가장 먼저)
			int categoryId = selectCategory(sc);
			if (categoryId == -1) {
				System.out.println("물품 추가가 취소되었습니다.");
				Main.MainInterface();
				return;
			}
			
			// 2. 상품명 입력
			System.out.print("상품명을 입력하세요: ");
			String productName = sc.nextLine().trim();
			if (productName.isEmpty()) {
				System.out.println("상품명은 필수입니다.");
				Main.MainInterface();
				return;
			}
			
			// 3. 가격 입력
			double price = 0;
			while (true) {
				System.out.print("가격을 입력하세요 (원): ");
				try {
					price = Double.parseDouble(sc.nextLine().trim());
					if (price < 0) {
						System.out.println("가격은 0원 이상이어야 합니다.");
						continue;
					}
					break;
				} catch (NumberFormatException e) {
					System.out.println("올바른 가격을 입력해주세요.");
				}
			}
			
			// 4. 재고량 입력
			int stockQuantity = 0;
			while (true) {
				System.out.print("재고량을 입력하세요 (개): ");
				try {
					stockQuantity = Integer.parseInt(sc.nextLine().trim());
					if (stockQuantity < 0) {
						System.out.println("재고량은 0개 이상이어야 합니다.");
						continue;
					}
					break;
				} catch (NumberFormatException e) {
					System.out.println("올바른 재고량을 입력해주세요.");
				}
			}
			
			// 5. 원산지 입력
			System.out.print("원산지를 입력하세요: ");
			String origin = sc.nextLine().trim();
			if (origin.isEmpty()) {
				origin = "미입력";
			}
			
			// 6. 다음 ProductID 생성
			int productId = getNextProductId(categoryId);
			
			// 7. 입력 정보 확인
			System.out.println();
			System.out.println("========== 입력 정보 확인 ==========");
			System.out.println("상품 ID: " + productId);
			System.out.println("상품명: " + productName);
			System.out.println("가격: " + (int)price + "원");
			System.out.println("재고량: " + stockQuantity + "개");
			System.out.println("원산지: " + origin);
			System.out.println("카테고리 ID: " + categoryId);
			System.out.println("=================================");
			
			System.out.print("위 정보로 물품을 추가하시겠습니까? (y/n): ");
			String confirm = sc.nextLine().trim();
			
			if (confirm.equalsIgnoreCase("y") || confirm.equalsIgnoreCase("yes")) {
				// 데이터베이스에 물품 추가
				if (insertProduct(productId, productName, price, stockQuantity, origin, categoryId)) {
					System.out.println();
					System.out.println("물품이 성공적으로 추가되었습니다!");
					System.out.println("상품 ID: " + productId);
					System.out.println("상품명: " + productName);
					System.out.println("가격: " + (int)price + "원");
					System.out.println("재고량: " + stockQuantity + "개");
				} else {
					System.out.println();
					System.out.println("물품 추가에 실패했습니다. 다시 시도해주세요.");
				}
			} else {
				System.out.println("물품 추가가 취소되었습니다.");
			}
			
		} catch (Exception e) {
			System.out.println("오류가 발생했습니다: " + e.getMessage());
		}
		
		// 메인 메뉴로 돌아가기
		Main.MainInterface();
	}
	
	// 카테고리 선택
	private static int selectCategory(Scanner sc) {
		// 카테고리 목록 표시
		if (!showAllCategories()) {
			System.out.println("등록된 카테고리가 없습니다.");
			System.out.println("먼저 카테고리를 추가해주세요.");
			return -1;
		}
		
		// 카테고리 ID 입력
		while (true) {
			System.out.print("물품의 카테고리 ID를 입력하세요 (취소: 0): ");
			String input = sc.nextLine().trim();
			
			if (input.equals("0")) {
				return -1;
			}
			
			try {
				int categoryId = Integer.parseInt(input);
				
				// 카테고리 존재 여부 확인
				if (isCategoryExists(categoryId)) {
					return categoryId;
				} else {
					System.out.println("해당 ID의 카테고리를 찾을 수 없습니다. 다시 입력해주세요.");
				}
			} catch (NumberFormatException e) {
				System.out.println("올바른 카테고리 ID를 입력해주세요.");
			}
		}
	}
	
	// 카테고리 목록 표시
	private static boolean showAllCategories() {
		String sql = "SELECT DISTINCT CategoryID, CategoryName FROM shopdatatable ORDER BY CategoryID";
		
		try (Connection conn = DriverManager.getConnection(Main.url, Main.user, Main.pass);
		     PreparedStatement pstmt = conn.prepareStatement(sql);
		     ResultSet rs = pstmt.executeQuery()) {
			
			System.out.printf("%-10s %-20s%n", "ID", " Name");
			System.out.println("=============================");
			
			boolean hasCategories = false;
			while (rs.next()) {
				hasCategories = true;
				int categoryId = rs.getInt("CategoryID");
				String categoryName = rs.getString("CategoryName");
				
				System.out.printf("%-10d %-20s%n", categoryId, categoryName);
			}
			
			if (!hasCategories) {
				System.out.println("등록된 카테고리가 없습니다.");
			} else {
				System.out.println("=====================================");
			}
			
			return hasCategories;
			
		} catch (SQLException e) {
			System.out.println("카테고리 목록 조회 중 오류가 발생했습니다.");
			e.printStackTrace();
			return false;
		}
	}
	
	// 카테고리 존재 여부 확인
	private static boolean isCategoryExists(int categoryId) {
		String sql = "SELECT COUNT(*) as count FROM shopdatatable WHERE CategoryID = ?";
		
		try (Connection conn = DriverManager.getConnection(Main.url, Main.user, Main.pass);
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			pstmt.setInt(1, categoryId);
			
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt("count") > 0;
				}
			}
			
		} catch (SQLException e) {
			System.out.println("카테고리 존재 확인 중 오류가 발생했습니다.");
			e.printStackTrace();
		}
		
		return false;
	}
	
	// 다음 ProductID 생성 (카테고리ID + 순차번호)
	private static int getNextProductId(int categoryId) {
		String sql = "SELECT MAX(ProductID) as maxId FROM shopdatatable WHERE CategoryID = ?";
		
		try (Connection conn = DriverManager.getConnection(Main.url, Main.user, Main.pass);
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			pstmt.setInt(1, categoryId);
			
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					int maxId = rs.getInt("maxId");
					
					if (maxId == 0) {
						// 해당 카테고리의 첫 번째 물품
						return categoryId * 100 + 1;
					} else {
						// 기존 물품이 있는 경우, 다음 번호 생성
						String maxIdStr = String.valueOf(maxId);
						String categoryIdStr = String.valueOf(categoryId);
						
						// 카테고리ID로 시작하는 ProductID인지 확인
						if (maxIdStr.startsWith(categoryIdStr)) {
						// 다음 순차번호 생성
						return maxId + 1;
						} else {
							// 카테고리ID로 시작하지 않는 경우, 새로 시작
							return categoryId * 100 + 1;
						}
					}
				}
			}
			
		} catch (SQLException e) {
			System.out.println("ProductID 생성 중 오류가 발생했습니다.");
			e.printStackTrace();
		}
		
		// 오류 발생 시 기본값 반환
		return categoryId * 100 + 1;
	}
	
	// 데이터베이스에 물품 추가
	private static boolean insertProduct(int productId, String productName, double price, 
	                                   int stockQuantity, String origin, int categoryId) {
		// CategoryID로부터 CategoryName 조회
		String categoryName = getCategoryName(categoryId);
		if (categoryName == null) {
			System.out.println("해당 카테고리 ID를 찾을 수 없습니다.");
			return false;
		}
		
		String sql = "INSERT INTO shopdatatable (CategoryID, CategoryName, ProductID, ProductName, Price, StockQuantity, Origin) VALUES (?, ?, ?, ?, ?, ?, ?)";
		
		try (Connection conn = DriverManager.getConnection(Main.url, Main.user, Main.pass);
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			pstmt.setInt(1, categoryId);
			pstmt.setString(2, categoryName);
			pstmt.setInt(3, productId);
			pstmt.setString(4, productName);
			pstmt.setDouble(5, price);
			pstmt.setInt(6, stockQuantity);
			pstmt.setString(7, origin);
			
			int result = pstmt.executeUpdate();
			return result > 0;
			
		} catch (SQLException e) {
			System.out.println("데이터베이스 오류가 발생했습니다.");
			e.printStackTrace();
			return false;
		}
	}
	
	// CategoryID로부터 CategoryName 조회
	private static String getCategoryName(int categoryId) {
		String sql = "SELECT CategoryName FROM shopdatatable WHERE CategoryID = ? LIMIT 1";
		
		try (Connection conn = DriverManager.getConnection(Main.url, Main.user, Main.pass);
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			pstmt.setInt(1, categoryId);
			
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getString("CategoryName");
				}
			}
			
		} catch (SQLException e) {
			System.out.println("카테고리 조회 중 오류가 발생했습니다.");
			e.printStackTrace();
		}
		
		return null;
	}
}