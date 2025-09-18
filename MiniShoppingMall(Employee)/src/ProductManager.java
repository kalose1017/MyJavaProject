import java.sql.*;
import java.util.Scanner;

/**
 * ===========================================
 * 
 * 물품 관리 클래스
 * 
 * 주요 기능:
 * - 물품 추가 (상품명, 가격, 재고량, 원산지, 카테고리)
 * - 물품 수정
 * - 물품 삭제
 * - 물품 조회
 * - 재고 관리
 * 
 * 포함된 메소드:
 * - addProduct(): 새 물품 추가
 * - validateInput(): 입력값 유효성 검사
 * - getNextProductId(): 다음 상품 ID 조회
 * 
 * ===========================================
 */
public class ProductManager {
	
	// 물품 추가 메인 메소드
	public static void addProduct() {
		Scanner sc = new Scanner(System.in);
		
		System.out.println();
		System.out.println("========== 물품 추가 ==========");
		
		try {
			// 상품명 입력
			System.out.print("상품명을 입력하세요: ");
			String productName = sc.nextLine().trim();
			if (productName.isEmpty()) {
				System.out.println("상품명은 필수입니다.");
				Main.MainInterface();
				return;
			}
			
			// 가격 입력
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
			
			// 재고량 입력
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
			
			// 원산지 입력
			System.out.print("원산지를 입력하세요: ");
			String origin = sc.nextLine().trim();
			if (origin.isEmpty()) {
				origin = "미입력";
			}
			
			// 카테고리 입력
			System.out.print("카테고리를 입력하세요: ");
			String category = sc.nextLine().trim();
			if (category.isEmpty()) {
				System.out.println("카테고리는 필수입니다.");
				Main.MainInterface();
				return;
			}
			
			// 입력 정보 확인
			System.out.println();
			System.out.println("========== 입력 정보 확인 ==========");
			System.out.println("상품명: " + productName);
			System.out.println("가격: " + (int)price + "원");
			System.out.println("재고량: " + stockQuantity + "개");
			System.out.println("원산지: " + origin);
			System.out.println("카테고리: " + category);
			System.out.println("=================================");
			
			System.out.print("위 정보로 물품을 추가하시겠습니까? (y/n): ");
			String confirm = sc.nextLine().trim();
			
			if (confirm.equalsIgnoreCase("y") || confirm.equalsIgnoreCase("yes")) {
				// 데이터베이스에 물품 추가
				if (insertProduct(productName, price, stockQuantity, origin, category)) {
					System.out.println();
					System.out.println("물품이 성공적으로 추가되었습니다!");
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
	
	// 데이터베이스에 물품 추가
	private static boolean insertProduct(String productName, double price, int stockQuantity, String origin, String category) {
		String sql = "INSERT INTO shopdatatable (ProductName, Price, StockQuantity, Origin, CategoryName) VALUES (?, ?, ?, ?, ?)";
		
		try (Connection conn = DriverManager.getConnection(Main.url, Main.user, Main.pass);
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			pstmt.setString(1, productName);
			pstmt.setDouble(2, price);
			pstmt.setInt(3, stockQuantity);
			pstmt.setString(4, origin);
			pstmt.setString(5, category);
			
			int result = pstmt.executeUpdate();
			return result > 0;
			
		} catch (SQLException e) {
			System.out.println("데이터베이스 오류가 발생했습니다.");
			e.printStackTrace();
			return false;
		}
	}
	
}
