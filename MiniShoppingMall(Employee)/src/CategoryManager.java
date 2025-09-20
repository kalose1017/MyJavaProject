import java.sql.*;
import java.util.Scanner;

/**
 * ===========================================
 * 
 * 주요 기능:
 * - 카테고리 추가 (CategoryID 자동 생성, 중복 확인)
 * - 카테고리 삭제 (물품 존재 여부 확인)
 * - 카테고리 목록 조회
 * - 카테고리 중복 확인
 * 
 * 포함된 메소드:
 * - addCategory(): 카테고리 추가 메인 메소드
 * - deleteCategory(): 카테고리 삭제 메인 메소드
 * - showAllCategories(): 전체 카테고리 목록 표시
 * - getNextCategoryId(): 다음 카테고리 ID 조회
 * - isCategoryNameExists(): 카테고리명 중복 확인
 * - hasProductsInCategory(): 카테고리에 물품 존재 여부 확인
 * - insertCategory(): 데이터베이스에 카테고리 추가
 * - removeCategory(): 데이터베이스에서 카테고리 삭제
 * 
 * ===========================================
 */
public class CategoryManager {
	
	// 카테고리 추가 메인 메소드
	public static void addCategory() {
		Scanner sc = Main.getScanner();
		
		System.out.println();
		System.out.println("========== 카테고리 추가 ==========");
		
		try {
			// 기존 카테고리 목록 표시
			if (!showAllCategories()) {
				System.out.println("등록된 카테고리가 없습니다.");
			}
			
			// 카테고리명 입력
			String categoryName;
			while (true) {
				System.out.print("추가할 카테고리명을 입력하세요: ");
				categoryName = sc.nextLine().trim();
				
				if (categoryName.isEmpty()) {
					System.out.println("카테고리명은 필수입니다.");
					continue;
				}
				
				// 카테고리명 중복 확인
				if (isCategoryNameExists(categoryName)) {
					System.out.println("이미 존재하는 카테고리명입니다. 다른 이름을 입력해주세요.");
					continue;
				}
				
				break;
			}
			
			// 다음 카테고리 ID 조회
			int nextCategoryId = getNextCategoryId();
			
			// 입력 정보 확인
			System.out.println();
			System.out.println("========== 입력 정보 확인 ==========");
			System.out.println("카테고리 ID: " + nextCategoryId);
			System.out.println("카테고리명: " + categoryName);
			System.out.println("=================================");
			
			System.out.print("위 정보로 카테고리를 추가하시겠습니까? (y/n): ");
			String confirm = sc.nextLine().trim();
			
			if (confirm.equalsIgnoreCase("y") || confirm.equalsIgnoreCase("yes")) {
				// 데이터베이스에 카테고리 추가
				if (insertCategory(nextCategoryId, categoryName)) {
					System.out.println();
					System.out.println("카테고리가 성공적으로 추가되었습니다!");
					System.out.println("카테고리 ID: " + nextCategoryId);
					System.out.println("카테고리명: " + categoryName);
				} else {
					System.out.println();
					System.out.println("카테고리 추가에 실패했습니다. 다시 시도해주세요.");
				}
			} else {
				System.out.println("카테고리 추가가 취소되었습니다.");
			}
			
		} catch (Exception e) {
			System.out.println("오류가 발생했습니다: " + e.getMessage());
		}
		
		// 메인 메뉴로 돌아가기
		Main.MainInterface();
	}
	
	// 카테고리 삭제 메인 메소드
	public static void deleteCategory() {
		Scanner sc = Main.getScanner();
		
		System.out.println();
		System.out.println("========== 카테고리 삭제 ==========");
		
		try {
			// 기존 카테고리 목록 표시
			if (!showAllCategories()) {
				System.out.println("삭제할 카테고리가 없습니다.");
				Main.MainInterface();
				return;
			}
			
			// 삭제할 카테고리 ID 입력
			System.out.print("삭제할 카테고리 ID를 입력하세요 (취소: 0): ");
			String input = sc.nextLine().trim();
			
			if (input.equals("0")) {
				System.out.println("삭제가 취소되었습니다.");
				Main.MainInterface();
				return;
			}
			
			int categoryId;
			try {
				categoryId = Integer.parseInt(input);
			} catch (NumberFormatException e) {
				System.out.println("올바른 카테고리 ID를 입력해주세요.");
				Main.MainInterface();
				return;
			}
			
			// 카테고리 존재 여부 확인
			if (!isCategoryExists(categoryId)) {
				System.out.println("해당 ID의 카테고리를 찾을 수 없습니다.");
				Main.MainInterface();
				return;
			}
			
			// 카테고리에 물품이 있는지 확인
			if (hasProductsInCategory(categoryId)) {
				System.out.println("해당 카테고리에 등록된 물품이 있어서 삭제할 수 없습니다.");
				System.out.println("먼저 해당 카테고리의 모든 물품을 삭제한 후 카테고리를 삭제해주세요.");
				Main.MainInterface();
				return;
			}
			
			// 카테고리 정보 조회
			String categoryName = getCategoryName(categoryId);
			
			// 삭제 확인
			System.out.println();
			System.out.println("========== 삭제할 카테고리 정보 ==========");
			System.out.println("카테고리 ID: " + categoryId);
			System.out.println("카테고리명: " + categoryName);
			System.out.println("=================================");
			
			System.out.println();
			System.out.println("경고: 이 작업은 되돌릴 수 없습니다!");
			System.out.print("정말로 이 카테고리를 삭제하시겠습니까? (y/n): ");
			String confirm = sc.nextLine().trim();
			
			if (confirm.equalsIgnoreCase("y") || confirm.equalsIgnoreCase("yes")) {
				// 추가 확인
				System.out.print("한 번 더 확인합니다. 정말 삭제하시겠습니까? (DELETE 입력): ");
				String finalConfirm = sc.nextLine().trim();
				
				if (finalConfirm.equals("DELETE")) {
					// 데이터베이스에서 카테고리 삭제
					if (removeCategory(categoryId)) {
						System.out.println();
						System.out.println("카테고리가 성공적으로 삭제되었습니다!");
						System.out.println("삭제된 카테고리: " + categoryName);
					} else {
						System.out.println();
						System.out.println("카테고리 삭제에 실패했습니다. 다시 시도해주세요.");
					}
				} else {
					System.out.println("삭제가 취소되었습니다.");
				}
			} else {
				System.out.println("삭제가 취소되었습니다.");
			}
			
		} catch (Exception e) {
			System.out.println("오류가 발생했습니다: " + e.getMessage());
		}
		
		// 메인 메뉴로 돌아가기
		Main.MainInterface();
	}
	
	// 전체 카테고리 목록 표시
	public static boolean showAllCategories() {
		String sql = "SELECT CategoryID, CategoryName FROM shopdatatable GROUP BY CategoryID, CategoryName ORDER BY CategoryID";
		
		try (Connection conn = DriverManager.getConnection(Main.url, Main.user, Main.pass);
		     PreparedStatement pstmt = conn.prepareStatement(sql);
		     ResultSet rs = pstmt.executeQuery()) {
			
			System.out.println();
			System.out.println("========== 카테고리 목록 ==========");
			System.out.printf("%-10s %-20s%n", "ID", " Name");
			System.out.println("================================");
			
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
				System.out.println("================================");
			}
			
			return hasCategories;
			
		} catch (SQLException e) {
			System.out.println("카테고리 목록 조회 중 오류가 발생했습니다.");
			e.printStackTrace();
			return false;
		}
	}
	
	// 다음 카테고리 ID 조회
	private static int getNextCategoryId() {
		String sql = "SELECT MAX(CategoryID) as maxId FROM shopdatatable";
		
		try (Connection conn = DriverManager.getConnection(Main.url, Main.user, Main.pass);
		     PreparedStatement pstmt = conn.prepareStatement(sql);
		     ResultSet rs = pstmt.executeQuery()) {
			
			if (rs.next()) {
				int maxId = rs.getInt("maxId");
				return maxId + 1;
			}
			
		} catch (SQLException e) {
			System.out.println("카테고리 ID 조회 중 오류가 발생했습니다.");
			e.printStackTrace();
		}
		
		return 1; // 첫 번째 카테고리인 경우
	}
	
	// 카테고리명 중복 확인
	private static boolean isCategoryNameExists(String categoryName) {
		String sql = "SELECT COUNT(*) as count FROM shopdatatable WHERE CategoryName = ?";
		
		try (Connection conn = DriverManager.getConnection(Main.url, Main.user, Main.pass);
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			pstmt.setString(1, categoryName);
			
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt("count") > 0;
				}
			}
			
		} catch (SQLException e) {
			System.out.println("카테고리명 중복 확인 중 오류가 발생했습니다.");
			e.printStackTrace();
		}
		
		return false;
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
	
	// 카테고리에 물품이 있는지 확인
	private static boolean hasProductsInCategory(int categoryId) {
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
			System.out.println("카테고리 물품 존재 확인 중 오류가 발생했습니다.");
			e.printStackTrace();
		}
		
		return false;
	}
	
	// 카테고리명 조회
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
			System.out.println("카테고리명 조회 중 오류가 발생했습니다.");
			e.printStackTrace();
		}
		
		return "알 수 없음";
	}
	
	// 데이터베이스에 카테고리 추가
	private static boolean insertCategory(int categoryId, String categoryName) {
		String sql = "INSERT INTO shopdatatable (CategoryID, CategoryName) VALUES (?, ?)";
		
		try (Connection conn = DriverManager.getConnection(Main.url, Main.user, Main.pass);
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			pstmt.setInt(1, categoryId);
			pstmt.setString(2, categoryName);
			
			int result = pstmt.executeUpdate();
			return result > 0;
			
		} catch (SQLException e) {
			System.out.println("데이터베이스 오류가 발생했습니다.");
			e.printStackTrace();
			return false;
		}
	}
	
	// 데이터베이스에서 카테고리 삭제
	private static boolean removeCategory(int categoryId) {
		String sql = "DELETE FROM shopdatatable WHERE CategoryID = ?";
		
		try (Connection conn = DriverManager.getConnection(Main.url, Main.user, Main.pass);
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			pstmt.setInt(1, categoryId);
			
			int result = pstmt.executeUpdate();
			return result > 0;
			
		} catch (SQLException e) {
			System.out.println("데이터베이스 오류가 발생했습니다.");
			e.printStackTrace();
			return false;
		}
	}
}