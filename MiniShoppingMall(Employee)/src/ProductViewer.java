import java.sql.*;
import java.util.Scanner;

/**
 * ===========================================
 * 
 * 물품 조회 클래스
 * 
 * 주요 기능:
 * - 모든 물품 목록 조회
 * - 물품 상세 정보 표시
 * - 카테고리별 조회
 * - 가격대별 조회
 * - 재고량별 조회
 * 
 * 포함된 메소드:
 * - viewAllProducts(): 모든 물품 조회 메인 메소드
 * - showAllProducts(): 전체 물품 목록 표시
 * - showProductDetails(): 특정 물품 상세 정보 표시
 * - showProductsByCategory(): 카테고리별 물품 조회
 * - showProductsByPriceRange(): 가격대별 물품 조회
 * - showProductsByStock(): 재고량별 물품 조회
 * 
 * ===========================================
 */
public class ProductViewer {
	
	// 물품 조회 메인 메소드
	public static void viewAllProducts() {
		Scanner sc = Main.getScanner();
		try {
			int choose = -1;
			
			while (choose != 0) {
				System.out.println();
				System.out.println("========== 물품 조회 ===========");
				System.out.println("1. 전체 물품 조회");
				System.out.println("2. 상품명으로 검색");
				System.out.println("3. 카테고리별 조회");
				System.out.println("4. 가격대별 조회");
				System.out.println("5. 재고량별 조회");
				System.out.println("6. 물품ID로 상세 조회");
				System.out.println("==============================");
				System.out.print("메뉴를 선택하세요 (메인메뉴: 0): ");
				
				try {
					choose = Integer.parseInt(sc.nextLine());
					
					switch (choose) {
						case 1:
							showAllProducts();
							break;
						case 2:
							searchProductsByName(sc);
							break;
						case 3:
							showProductsByCategory(sc);
							break;
						case 4:
							showProductsByPriceRange(sc);
							break;
						case 5:
							showProductsByStock(sc);
							break;
						case 6:
							showProductDetailsByID(sc);
							break;
						case 0:
							System.out.println("메인 메뉴로 돌아갑니다.");
							return;
						default:
							System.out.println("잘못된 선택입니다. 1-6번 중에서 선택해주세요.");
					}
				} catch (NumberFormatException e) {
					System.out.println("올바른 숫자를 입력해주세요.");
				}
			}
			
		} catch (Exception e) {
			System.out.println("오류가 발생했습니다: " + e.getMessage());
		}
		
		// 메인 메뉴로 돌아가기
		Main.MainInterface();
	}
	
	// 전체 물품 조회
	private static void showAllProducts() {
		String sql = "SELECT ProductID, ProductName FROM shopdatatable ORDER BY ProductID";
		
		try (Connection conn = DriverManager.getConnection(Main.url, Main.user, Main.pass);
		     PreparedStatement pstmt = conn.prepareStatement(sql);
		     ResultSet rs = pstmt.executeQuery()) {
			
			System.out.println();
			System.out.println("========= 전체 물품 목록 =========");
			
			boolean hasProducts = false;
			int count = 0;
			while (rs.next()) {
				hasProducts = true;
				count++;
				int productId = rs.getInt("ProductID");
				String productName = rs.getString("ProductName");
				
				System.out.println("품목ID: " + productId);
				System.out.println("품목명: " + productName);
				System.out.println("------------------------");
			}
			
			if (!hasProducts) {
				System.out.println("등록된 물품이 없습니다.");
			} else {
				System.out.println("총 " + count + "개의 물품이 등록되어 있습니다.");
			}
			
		} catch (SQLException e) {
			System.out.println("물품 목록 조회 중 오류가 발생했습니다.");
			e.printStackTrace();
		}
	}
	
	// ProductID로 상세 조회
	private static void showProductDetailsByID(Scanner sc) {
		System.out.print("조회할 물품ID를 입력하세요: ");
		String input = sc.nextLine().trim();
		
		if (input.isEmpty()) {
			System.out.println("물품ID를 입력해주세요.");
			return;
		}
		
		try {
			int productId = Integer.parseInt(input);
			
			String sql = "SELECT * FROM shopdatatable WHERE ProductID = ?";
			
			try (Connection conn = DriverManager.getConnection(Main.url, Main.user, Main.pass);
			     PreparedStatement pstmt = conn.prepareStatement(sql)) {
				
				pstmt.setInt(1, productId);
				
				try (ResultSet rs = pstmt.executeQuery()) {
					System.out.println();
					System.out.println("========= 상품 상세 정보 =========");
					
					if (rs.next()) {
						System.out.println("품목ID: " + rs.getInt("ProductID"));
						System.out.println("품목명: " + rs.getString("ProductName"));
						System.out.println("가격: " + (int)rs.getDouble("Price") + "원");
						System.out.println("재고량: " + rs.getInt("StockQuantity"));
						System.out.println("원산지: " + rs.getString("Origin"));
						System.out.println("------------------------");
					} else {
						System.out.println("해당 물품ID(" + productId + ")의 상품을 찾을 수 없습니다.");
					}
				}
				
			} catch (SQLException e) {
				System.out.println("상품 조회 중 오류가 발생했습니다.");
				e.printStackTrace();
			}
			
		} catch (NumberFormatException e) {
			System.out.println("올바른 물품ID를 입력해주세요.");
		}
	}
	
	// 상품명으로 검색
	private static void searchProductsByName(Scanner sc) {
		System.out.print("검색할 상품명을 입력하세요: ");
		String searchName = sc.nextLine().trim();
		
		if (searchName.isEmpty()) {
			System.out.println("검색어를 입력해주세요.");
			return;
		}
		
		String sql = "SELECT * FROM shopdatatable WHERE ProductName LIKE ? ORDER BY ProductID";
		
		try (Connection conn = DriverManager.getConnection(Main.url, Main.user, Main.pass);
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			pstmt.setString(1, "%" + searchName + "%");
			
			try (ResultSet rs = pstmt.executeQuery()) {
			System.out.println();
			System.out.println("========= '" + searchName + "' 검색 결과 =========");
			
			boolean hasProducts = false;
			int count = 0;
			while (rs.next()) {
				hasProducts = true;
				count++;
				int productId = rs.getInt("ProductID");
				String productName = rs.getString("ProductName");
				double price = rs.getDouble("Price");
				int stockQuantity = rs.getInt("StockQuantity");
				String origin = rs.getString("Origin");
				
				System.out.println("품목ID: " + productId);
				System.out.println("품목명: " + productName);
				System.out.println("가격: " + (int)price + "원");
				System.out.println("재고량: " + stockQuantity);
				System.out.println("원산지: " + origin);
				System.out.println("------------------------");
			}
			
			if (!hasProducts) {
				System.out.println("검색 결과가 없습니다.");
			} else {
				System.out.println("총 " + count + "개의 검색 결과가 있습니다.");
			}
			}
			
		} catch (SQLException e) {
			System.out.println("검색 중 오류가 발생했습니다.");
			e.printStackTrace();
		}
	}
	
	// 카테고리별 조회
	private static void showProductsByCategory(Scanner sc) {
		// 먼저 카테고리 목록을 보여줌
		String categorySql = "SELECT DISTINCT CategoryName FROM shopdatatable ORDER BY CategoryName";
		
		try (Connection conn = DriverManager.getConnection(Main.url, Main.user, Main.pass);
		     PreparedStatement pstmt = conn.prepareStatement(categorySql);
		     ResultSet rs = pstmt.executeQuery()) {
			
			System.out.println();
			System.out.println("========== 사용 가능한 카테고리 ==========");
			int categoryCount = 0;
			while (rs.next()) {
				categoryCount++;
				System.out.println(categoryCount + ". " + rs.getString("CategoryName"));
			}
			
			if (categoryCount == 0) {
				System.out.println("등록된 카테고리가 없습니다.");
				return;
			}
			
			System.out.println("=======================================");
			
			String category;
			while (true) {
				System.out.print("조회할 카테고리명을 입력하세요: ");
				category = sc.nextLine().trim();
				
				if (category.isEmpty()) {
					System.out.println("카테고리명을 입력해주세요.");
					continue;
				}
				
				// 숫자만 입력되었는지 확인
				if (category.matches("\\d+")) {
					System.out.println("이름을 입력해주세요.");
					continue;
				}
				
				break; // 유효한 입력이면 루프 종료
			}
			
			// 선택된 카테고리의 물품들 조회
			String sql = "SELECT * FROM shopdatatable WHERE CategoryName = ? ORDER BY ProductID";
			
			try (PreparedStatement productPstmt = conn.prepareStatement(sql)) {
				productPstmt.setString(1, category);
				
				try (ResultSet productRs = productPstmt.executeQuery()) {
				System.out.println();
				System.out.println("========= '" + category + "' 카테고리 물품 =========");
				
				boolean hasProducts = false;
				int count = 0;
				while (productRs.next()) {
					hasProducts = true;
					count++;
					int productId = productRs.getInt("ProductID");
					String productName = productRs.getString("ProductName");
					double price = productRs.getDouble("Price");
					int stockQuantity = productRs.getInt("StockQuantity");
					String origin = productRs.getString("Origin");
					
					System.out.println("품목ID: " + productId);
					System.out.println("품목명: " + productName);
					System.out.println("가격: " + (int)price + "원");
					System.out.println("재고량: " + stockQuantity);
					System.out.println("원산지: " + origin);
					System.out.println("------------------------");
				}
				
				if (!hasProducts) {
					System.out.println("해당 카테고리에 등록된 물품이 없습니다.");
				} else {
					System.out.println("총 " + count + "개의 물품이 있습니다.");
				}
				}
			}
			
		} catch (SQLException e) {
			System.out.println("카테고리별 조회 중 오류가 발생했습니다.");
			e.printStackTrace();
		}
	}
	
	// 가격대별 조회
	private static void showProductsByPriceRange(Scanner sc) {
		System.out.println();
		System.out.println("========== 가격대별 조회 ==========");
		
		double minPrice = 0;
		double maxPrice = 0;
		
		// 최소 가격 입력
		while (true) {
			System.out.print("최소 가격을 입력하세요 (원): ");
			try {
				minPrice = Double.parseDouble(sc.nextLine().trim());
				if (minPrice < 0) {
					System.out.println("가격은 0원 이상이어야 합니다.");
					continue;
				}
				break;
			} catch (NumberFormatException e) {
				System.out.println("올바른 가격을 입력해주세요.");
			}
		}
		
		// 최대 가격 입력
		while (true) {
			System.out.print("최대 가격을 입력하세요 (원): ");
			try {
				maxPrice = Double.parseDouble(sc.nextLine().trim());
				if (maxPrice < minPrice) {
					System.out.println("최대 가격은 최소 가격보다 크거나 같아야 합니다.");
					continue;
				}
				break;
			} catch (NumberFormatException e) {
				System.out.println("올바른 가격을 입력해주세요.");
			}
		}
		
		String sql = "SELECT * FROM shopdatatable WHERE Price >= ? AND Price <= ? ORDER BY Price";
		
		try (Connection conn = DriverManager.getConnection(Main.url, Main.user, Main.pass);
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			pstmt.setDouble(1, minPrice);
			pstmt.setDouble(2, maxPrice);
			
			try (ResultSet rs = pstmt.executeQuery()) {
			System.out.println();
			System.out.println("========= " + (int)minPrice + "원 ~ " + (int)maxPrice + "원 가격대 물품 =========");
			
			boolean hasProducts = false;
			int count = 0;
			while (rs.next()) {
				hasProducts = true;
				count++;
				int productId = rs.getInt("ProductID");
				String productName = rs.getString("ProductName");
				double price = rs.getDouble("Price");
				int stockQuantity = rs.getInt("StockQuantity");
				String origin = rs.getString("Origin");
				
				System.out.println("품목ID: " + productId);
				System.out.println("품목명: " + productName);
				System.out.println("가격: " + (int)price + "원");
				System.out.println("재고량: " + stockQuantity);
				System.out.println("원산지: " + origin);
				System.out.println("------------------------");
			}
			
			if (!hasProducts) {
				System.out.println("해당 가격대에 등록된 물품이 없습니다.");
			} else {
				System.out.println("총 " + count + "개의 물품이 있습니다.");
			}
			}
			
		} catch (SQLException e) {
			System.out.println("가격대별 조회 중 오류가 발생했습니다.");
			e.printStackTrace();
		}
	}
	
	// 재고량별 조회
	private static void showProductsByStock(Scanner sc) {
		System.out.println();
		System.out.println("========== 재고량별 조회 ==========");
		System.out.println("1. 재고 부족 (10개 미만)");
		System.out.println("2. 재고 충분 (10개 이상)");
		System.out.println("3. 재고 없음 (0개)");
		System.out.println("4. 사용자 정의 재고량");
		System.out.println("===============================");
		System.out.print("선택하세요: ");
		
		try {
			int choice = Integer.parseInt(sc.nextLine().trim());
			String sql = "";
			String title = "";
			
			switch (choice) {
				case 1:
					sql = "SELECT * FROM shopdatatable WHERE StockQuantity < 10 ORDER BY StockQuantity";
					title = "재고 부족 물품 (10개 미만)";
					break;
				case 2:
					sql = "SELECT * FROM shopdatatable WHERE StockQuantity >= 10 ORDER BY StockQuantity DESC";
					title = "재고 충분 물품 (10개 이상)";
					break;
				case 3:
					sql = "SELECT * FROM shopdatatable WHERE StockQuantity = 0 ORDER BY ProductID";
					title = "재고 없음 물품 (0개)";
					break;
				case 4:
					System.out.print("조회할 재고량을 입력하세요: ");
					int stock = Integer.parseInt(sc.nextLine().trim());
					if (stock < 0) {
						System.out.println("재고량은 0개 이상이어야 합니다.");
						return;
					}
					sql = "SELECT * FROM shopdatatable WHERE StockQuantity = " + stock + " ORDER BY ProductID";
					title = "재고량 " + stock + "개 물품";
					break;
				default:
					System.out.println("잘못된 선택입니다.");
					return;
			}
			
			try (Connection conn = DriverManager.getConnection(Main.url, Main.user, Main.pass);
			     PreparedStatement pstmt = conn.prepareStatement(sql);
			     ResultSet rs = pstmt.executeQuery()) {
				
			System.out.println();
			System.out.println("========= " + title + " =========");
			
			boolean hasProducts = false;
			int count = 0;
			while (rs.next()) {
				hasProducts = true;
				count++;
				int productId = rs.getInt("ProductID");
				String productName = rs.getString("ProductName");
				double price = rs.getDouble("Price");
				int stockQuantity = rs.getInt("StockQuantity");
				String origin = rs.getString("Origin");
				
				System.out.println("품목ID: " + productId);
				System.out.println("품목명: " + productName);
				System.out.println("가격: " + (int)price + "원");
				System.out.println("재고량: " + stockQuantity);
				System.out.println("원산지: " + origin);
				System.out.println("------------------------");
			}
			
			if (!hasProducts) {
				System.out.println("해당 조건에 맞는 물품이 없습니다.");
			} else {
				System.out.println("총 " + count + "개의 물품이 있습니다.");
			}
			}
			
		} catch (NumberFormatException e) {
			System.out.println("올바른 숫자를 입력해주세요.");
		} catch (SQLException e) {
			System.out.println("재고량별 조회 중 오류가 발생했습니다.");
			e.printStackTrace();
		}
	}
	
}