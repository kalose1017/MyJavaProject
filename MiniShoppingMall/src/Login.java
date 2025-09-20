import java.sql.*;
import java.util.Scanner;

/**
 * ===========================================
 * 
 * 주요 기능:
 * - 사용자 로그인 인증 처리
 * - 현재 로그인한 사용자 정보 관리
 * - 로그아웃 기능
 * - 로그인 상태 유지 및 정보 제공
 * 
 * 포함된 메소드:
 * - LoginInterface(): 로그인 화면 및 인증 처리
 * - getCurrentCustomerId(): 현재 고객 ID 반환
 * - getCurrentCustomerName(): 현재 고객 이름 반환
 * - getCurrentLoginId(): 현재 로그인 ID 반환
 * - getCurrentNickName(): 현재 닉네임 반환
 * - logout(): 로그아웃 처리
 * 
 * ===========================================
 */
public class Login {
	// 현재 로그인한 고객의 ID를 저장하는 정적 변수
	private static int currentCustomerId = -1;
	private static String currentCustomerName = "";
	private static String currentLoginId = "";
	
	// 현재 로그인한 고객 ID를 반환하는 메소드
	public static int getCurrentCustomerId() {
		return currentCustomerId;
	}
	
	// 현재 로그인한 고객 이름을 반환하는 메소드
	public static String getCurrentCustomerName() {
		return currentCustomerName;
	}
	
	// 현재 로그인한 고객의 LoginID를 반환하는 메소드
	public static String getCurrentLoginId() {
		return currentLoginId;
	}
	
	// 현재 로그인한 고객의 NickName을 반환하는 메소드
	public static String getCurrentNickName() {
		return currentCustomerName;
	}
	
	// 로그아웃 메소드
	public static void logout() {
		currentCustomerId = -1;
		currentCustomerName = "";
		currentLoginId = "";
	}
	
	// 프로그램 초기 로그인 화면 - 아이디/비밀번호 입력 및 인증
	public static void LoginInterface()
	{	
	    String sql = "SELECT * FROM Customer WHERE LoginID=? AND LoginPW=?";
		
	    String ID;
		String PW;
		int choice = -1;
		
		while(choice != 0)
		{
			try (Scanner sc = new Scanner(System.in);
			     Connection conn = DriverManager.getConnection(Main.url, Main.user, Main.pass);
		         PreparedStatement ps = conn.prepareStatement(sql)) 
			{
				System.out.println();
				System.out.print("아이디를 입력하세요. : ");
				ID = sc.nextLine();
				System.out.print("비밀번호를 입력하세요. : ");
				PW = sc.nextLine();
				ps.setString(1, ID);
	            ps.setString(2, PW);

	            try (ResultSet rs = ps.executeQuery()) // 서버에 ID와 PW가 존재하는지
	            {
	                if (rs.next()) 
	                {
	                	// 로그인 성공 시 고객 정보 저장
	                	currentCustomerId = rs.getInt("CustomerID");
	                	currentCustomerName = rs.getString("NickName");
	                	currentLoginId = ID;
	                	
	                	// CreateCustomer 클래스의 name 변수도 설정
	                	CreateCustomer.name = currentCustomerName;
	                	
	                	System.out.println();
	                    System.out.println("로그인 성공! 환영합니다, " + currentCustomerName + "님");
	                    Main.MainInterface();
	                } 
	                else 
	                {
	                    System.out.println();
	                    while(true)
	                    {
							System.out.print("틀렸거나 등록되지 않은 아이디와 비밀번호입니다. (재입력: 1, 회원가입 : 2, 종료: 0) : ");
	                        String input = sc.nextLine();
	                        try {
	                            choice = Integer.parseInt(input); // 문자열을 숫자 변환
	                            if (choice == 1) {
	                                // 재입력하도록 while 루프로 돌아감
	                                LoginInterface();
									return;
	                            } else if (choice == 2) {
	                                CreateCustomer.Create();
	                                return;
	                            } else if (choice == 0) {
	                                System.out.println("프로그램 종료");
	                                return;
	                            } else {
	                            	System.out.println("\n잘못된 선택입니다. 1, 2, 0 중에서 선택해주세요.");
	                            }
	                        } 
	                        catch (NumberFormatException e) {
	                            System.out.println("\n올바른 숫자를 입력해주세요.");
	                        }
	                    }
	                }
	            }
		    } 
			catch (SQLException e) 
			{
				System.out.println("\n데이터베이스 오류가 발생했습니다.");
				e.printStackTrace();
		    }
		}
	}
}
