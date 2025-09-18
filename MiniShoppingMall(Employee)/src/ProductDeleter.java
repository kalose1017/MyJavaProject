import java.sql.*;
import java.util.Scanner;

/**
 * ===========================================
 * 
 * 물품 삭제 클래스
 * 
 * 주요 기능:
 * - 물품 목록 조회
 * - 특정 물품 선택
 * - 삭제 확인
 * - 물품 삭제 실행
 * - 삭제 결과 확인
 * 
 * 포함된 메소드:
 * - deleteProduct(): 물품 삭제 메인 메소드
 * - showProductList(): 물품 목록 표시
 * - getProductById(): 특정 물품 정보 조회
 * - removeProduct(): 데이터베이스에서 물품 삭제
 * 
 * ===========================================
 */
public class ProductDeleter {
	
	// 물품 삭제 메인 메소드
	public static void deleteProduct() {
		Scanner sc = new Scanner(System.in);
		
		System.out.println();
		System.out.println("========== 물품 삭제 ==========");
		
		try {
			// 물품 목록 표시
			if (!showProductList()) {
				System.out.println("삭제할 물품이 없습니다.");
				Main.MainInterface();
				return;
			}
			
			// 삭제할 물품 선택
			System.out.print("삭제할 물품의 ID를 입력하세요 (취소: 0): ");
			String input = sc.nextLine().trim();
			
			if (input.equals("0")) {
				System.out.println("삭제가 취소되었습니다.");
				Main.MainInterface();
				return;
			}
			
			int productId;
			try {
				productId = Integer.parseInt(input);
			} catch (NumberFormatException e) {
				System.out.println("올바른 상품 ID를 입력해주세요.");
				Main.MainInterface();
				return;
			}
			
			// 선택된 물품 정보 조회
			ProductInfo product = getProductById(productId);
			if (product == null) {
				System.out.println("해당 ID의 물품을 찾을 수 없습니다.");
				Main.MainInterface();
				return;
			}
			
			// 삭제할 물품 정보 표시
			System.out.println();
			System.out.println("========== 삭제할 물품 정보 ==========");
			System.out.println("상품 ID: " + product.productId);
			System.out.println("상품명: " + product.productName);
			System.out.println("가격: " + (int)product.price + "원");
			System.out.println("재고량: " + product.stockQuantity + "개");
			System.out.println("원산지: " + product.origin);
			System.out.println("카테고리: " + product.category);
			System.out.println("=================================");
			
			// 삭제 확인
			System.out.println();
			System.out.println("⚠️  경고: 이 작업은 되돌릴 수 없습니다!");
			System.out.print("정말로 이 물품을 삭제하시겠습니까? (y/n): ");
			String confirm = sc.nextLine().trim();
			
			if (confirm.equalsIgnoreCase("y") || confirm.equalsIgnoreCase("yes")) {
				// 추가 확인
				System.out.print("한 번 더 확인합니다. 정말 삭제하시겠습니까? (DELETE 입력): ");
				String finalConfirm = sc.nextLine().trim();
				
				if (finalConfirm.equals("DELETE")) {
					// 데이터베이스에서 물품 삭제
					if (removeProduct(productId)) {
						System.out.println();
						System.out.println("물품이 성공적으로 삭제되었습니다!");
						System.out.println("삭제된 상품: " + product.productName);
					} else {
						System.out.println();
						System.out.println("물품 삭제에 실패했습니다. 다시 시도해주세요.");
					}
				} else {
					System.out.println("삭제가 취소되었습니다.");
				}
			} else {
				System.out.println("삭제가 취소되었습니다.");
			}
			
		} catch (Exception e) {
			System.out.println("오류가 발생했습니다: " + e.getMessage());
		} finally {
			sc.close();
		}
		
		// 메인 메뉴로 돌아가기
		System.out.println();
		System.out.print("메인 메뉴로 돌아가려면 Enter를 누르세요...");
		try (Scanner returnSc = new Scanner(System.in)) {
			returnSc.nextLine();
		}
		Main.MainInterface();
	}
	
	// 물품 목록 표시
	private static boolean showProductList() {
		String sql = "SELECT ProductID, ProductName, Price, StockQuantity, Origin, CategoryName " +
		             "FROM shopdatatable ORDER BY ProductID";
		
		try (Connection conn = DriverManager.getConnection(Main.url, Main.user, Main.pass);
		     PreparedStatement pstmt = conn.prepareStatement(sql);
		     ResultSet rs = pstmt.executeQuery()) {
			
			System.out.println();
			System.out.println("========== 물품 목록 ==========");
			System.out.printf("%-5s %-20s %-10s %-8s %-15s %-15s%n", 
			                 "ID", "상품명", "가격", "재고", "원산지", "카테고리");
			System.out.println("================================================");
			
			boolean hasProducts = false;
			while (rs.next()) {
				hasProducts = true;
				int productId = rs.getInt("ProductID");
				String productName = rs.getString("ProductName");
				double price = rs.getDouble("Price");
				int stockQuantity = rs.getInt("StockQuantity");
				String origin = rs.getString("Origin");
				String category = rs.getString("CategoryName");
				
				System.out.printf("%-5d %-20s %-10d %-8d %-15s %-15s%n",
				                 productId, 
				                 productName.length() > 20 ? productName.substring(0, 17) + "..." : productName,
				                 (int)price, 
				                 stockQuantity,
				                 origin.length() > 15 ? origin.substring(0, 12) + "..." : origin,
				                 category.length() > 15 ? category.substring(0, 12) + "..." : category);
			}
			
			if (!hasProducts) {
				System.out.println("등록된 물품이 없습니다.");
			}
			
			System.out.println("================================================");
			return hasProducts;
			
		} catch (SQLException e) {
			System.out.println("물품 목록 조회 중 오류가 발생했습니다.");
			e.printStackTrace();
			return false;
		}
	}
	
	// 특정 물품 정보 조회
	private static ProductInfo getProductById(int productId) {
		String sql = "SELECT ProductID, ProductName, Price, StockQuantity, Origin, CategoryName " +
		             "FROM shopdatatable WHERE ProductID = ?";
		
		try (Connection conn = DriverManager.getConnection(Main.url, Main.user, Main.pass);
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			pstmt.setInt(1, productId);
			
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return new ProductInfo(
						rs.getInt("ProductID"),
						rs.getString("ProductName"),
						rs.getDouble("Price"),
						rs.getInt("StockQuantity"),
						rs.getString("Origin"),
						rs.getString("CategoryName")
					);
				}
			}
			
		} catch (SQLException e) {
			System.out.println("물품 정보 조회 중 오류가 발생했습니다.");
			e.printStackTrace();
		}
		
		return null;
	}
	
	// 데이터베이스에서 물품 삭제
	private static boolean removeProduct(int productId) {
		String sql = "DELETE FROM shopdatatable WHERE ProductID = ?";
		
		try (Connection conn = DriverManager.getConnection(Main.url, Main.user, Main.pass);
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			pstmt.setInt(1, productId);
			
			int result = pstmt.executeUpdate();
			return result > 0;
			
		} catch (SQLException e) {
			System.out.println("데이터베이스 오류가 발생했습니다.");
			e.printStackTrace();
			return false;
		}
	}
	
	// 물품 정보를 저장하는 내부 클래스
	static class ProductInfo {
		int productId;
		String productName;
		double price;
		int stockQuantity;
		String origin;
		String category;
		
		public ProductInfo(int productId, String productName, double price, 
		                  int stockQuantity, String origin, String category) {
			this.productId = productId;
			this.productName = productName;
			this.price = price;
			this.stockQuantity = stockQuantity;
			this.origin = origin;
			this.category = category;
		}
	}
}
