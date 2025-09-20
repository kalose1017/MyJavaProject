import java.util.Scanner;

/**
 * ===========================================
 * 
 * 직원용 쇼핑몰 관리 시스템
 * 
 * 주요 기능:
 * - 물품 추가, 수정, 삭제
 * - 재고 관리
 * - 상품 정보 조회
 * 
 * ===========================================
 */
public class Main {
	
	// 데이터베이스 연결 정보 (고객용과 동일한 데이터베이스 사용)
	static String url = "jdbc:mysql://localhost:3306/shoppingmall"
	           + "?useUnicode=true&characterEncoding=UTF-8"
	           + "&serverTimezone=Asia/Seoul"
	           + "&useSSL=false&allowPublicKeyRetrieval=true";
	public static final String user = "root";
	public static final String pass = "1369";
	
	// 전역 Scanner (프로그램 전체에서 공유)
	private static Scanner globalScanner = new Scanner(System.in);
	
	public static void main(String[] args) {
		try {
			MainInterface();
		} finally {
			globalScanner.close();
		}
	}
	
	// 직원용 메인 인터페이스
	public static void MainInterface() {
		int choose = -1;
		
		while (choose != 0) {
			System.out.println();
			System.out.println("===== 직원용 쇼핑몰 관리 시스템 =====");
			System.out.println("1. 물품 추가");
			System.out.println("2. 물품 수정");
			System.out.println("3. 물품 삭제");
			System.out.println("4. 물품 조회");
			System.out.println("5. 카테고리 추가");
			System.out.println("6. 카테고리 삭제");
			System.out.println("7. 재고 관리");
			System.out.println("==============================");
			System.out.print("메뉴를 선택하세요 (종료: 0): ");
			
			try {
				choose = Integer.parseInt(globalScanner.nextLine());
				
				switch (choose) {
					case 1:
						ProductManager.addProduct();
						break;
					case 2:
						ProductEditor.editProduct();
						break;
					case 3:
						ProductDeleter.deleteProduct();
						break;
					case 4:
						ProductViewer.viewAllProducts();
						break;
					case 5:
						CategoryManager.addCategory();
						break;
					case 6:
						CategoryManager.deleteCategory();
						break;
					case 7:
						System.out.println("재고 관리 기능은 준비 중입니다.");
						break;
					case 0:
						System.out.println("시스템을 종료합니다.");
						break;
					default:
						System.out.println("잘못된 선택입니다. 1-7번 중에서 선택해주세요.");
				}
			} catch (NumberFormatException e) {
				System.out.println("올바른 숫자를 입력해주세요.");
			}
		}
	}
	
	// 전역 Scanner 접근 메서드
	public static Scanner getScanner() {
		return globalScanner;
	}
}
