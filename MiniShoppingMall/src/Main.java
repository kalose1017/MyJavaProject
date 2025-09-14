import java.sql.*;
import java.util.Scanner;

/**
 * ===========================================
 * 
 * 주요 기능:
 * - 프로그램 진입점 (main 메소드)
 * - 메인 인터페이스 제공 (상품검색, 장바구니, 마이페이지 메뉴)
 * - 데이터베이스 연결 정보 관리
 * - 전체 애플리케이션의 흐름 제어
 * 
 * 포함된 메소드:
 * - main(): 프로그램 진입점
 * - MainInterface(): 메인 메뉴 인터페이스
 * 
 * ===========================================
 */
public class Main {
	static String url = "jdbc:mysql://localhost:3306/shoppingmall"
	           + "?useUnicode=true&characterEncoding=UTF-8"
	           + "&serverTimezone=Asia/Seoul"
	           + "&useSSL=false&allowPublicKeyRetrieval=true";
	static String user = "root";
	static String pass = "1369";	
	
	public static void main(String[] args) {
		Login.LoginInterface();
	}
	
	// 메인 인터페이스 - 상품검색, 장바구니, 마이페이지 메뉴 제공
	public static void MainInterface()
	{
		Scanner sc = new Scanner(System.in);
		int choose = -1;
		while(choose != 0)
		{
			try {
				System.out.println();
				System.out.println("----- JihoMarket에 오신 것을 환영합니다! -----");
				System.out.println("1. 상품검색");
				System.out.println("2. 장바구니");
				System.out.println("3. 마이페이지");
				System.out.println("-----------------------------------------");
				System.out.print("서비스를 선택하세요.(종료 : 0 입력) : ");
				String input = sc.nextLine();
				try {
					choose = Integer.parseInt(input);
					if(choose == 1) 
					{
						SearchingProduct.Searching(); // 전체 상품 조회 인터페이스
						return;
					}
					//else if(choose == 1) // 상품검색 인터페이스
					else if(choose == 2) ShopCart.CartInfo();
					else if(choose == 3) 
					{
						CreateCustomer.CheckPW(); // 고객 정보 인터페이스
						return;
					}
					else if(choose == 0) 
					{
						System.out.println("프로그램 종료");
                        System.exit(0);
					}
					else System.out.println("\n잘못된 선택입니다. 1-3번 중에서 선택해주세요.");
				} 
				catch (NumberFormatException e) {
					System.out.println("\n올바른 숫자를 입력해주세요.");
				}
				
			} catch (NumberFormatException e) {
				System.out.println("\n올바른 숫자를 입력해주세요.\n");
			}
		}
	}

}
