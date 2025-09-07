import java.sql.*;
import java.util.Scanner;

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
				System.out.println("2. 전체상품 조회");
				System.out.println("3. 장바구니");
				System.out.println("4. 마이페이지");
				System.out.println("-----------------------------------------");
				System.out.print("서비스를 선택하세요.(종료 : 0 입력) : ");
				String input = sc.nextLine();
				try {
					choose = Integer.parseInt(input);
					if(choose == 2) 
					{
						SearchingProduct.Search(); // 전체 상품 조회 인터페이스
						return;
					}
					//else if(choose == 1) // 상품검색 인터페이스
					//else if(choose == 3) // 장바구니 인터페이스
					else if(choose == 4) 
					{
						CreateCustomer.CheckPW(); // 고객 정보 인터페이스
						return;
					}
					else if(choose == 0) 
					{
						System.out.println("프로그램 종료");
                        System.exit(0);
					}
					else System.out.println("숫자를 다시 입력해주세요!");
				} 
				catch (NumberFormatException e) {
					System.out.println();
					System.out.println("잘못된 입력입니다.");
				}
				
			} catch (NumberFormatException e) {
				System.out.println();
				System.out.println("입력이 잘못되었습니다. 다시 입력해주세요.");
				System.out.println();
			}
		}
	}

}
