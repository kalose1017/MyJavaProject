import java.sql.*;
import java.util.Scanner;

public class Login {
	
	
	
	public static void LoginInterface() // 프로그램 초기 로그인 화면
	{	
	    String sql = "SELECT * FROM Customer WHERE LoginID=? AND LoginPW=?";
	    Scanner sc = new Scanner(System.in);
		
	    String ID;
		String PW;
		int choice = -1;
		
		while(choice != 0)
		{
			System.out.print("아이디를 입력하세요. : ");
			ID = sc.nextLine();
			System.out.print("비밀번호를 입력하세요. : ");
			PW = sc.nextLine();
			try (Connection conn = DriverManager.getConnection(Main.url, Main.user, Main.pass);
		         PreparedStatement ps = conn.prepareStatement(sql)) 
			{
				ps.setString(1, ID);
	            ps.setString(2, PW);

	            try (ResultSet rs = ps.executeQuery()) // 서버에 ID와 PW가 존재하는지
	            {
	                if (rs.next()) 
	                {
	                	System.out.println();
	                    System.out.println("로그인 성공! 환영합니다, " + rs.getString("NickName") + "님");
	                    Main.MainInterface();
	                } 
	                else 
	                {
	                    while(true)
	                    {
	                    	System.out.print("등록되지 않았습니다. 회원가입을 하시겠습니까? (예: 1, 종료: 0) : ");
	                        String input = sc.nextLine();
	                        try {
	                            choice = Integer.parseInt(input); // 문자열을 숫자 변환
	                            if (choice == 1) {
	                                CreateCustomer.Create();
	                                return;
	                            } else if (choice == 0) {
	                                System.out.println("프로그램 종료");
	                                break;
	                            } else System.out.println("숫자를 다시 입력해주세요.");
	                        } 
	                        catch (NumberFormatException e) {
	                            System.out.println("잘못된 입력입니다.");
	                        }
	                    }
	                }
	            }
		    } 
			catch (SQLException e) 
			{
				System.out.println("서버오류!!");
				e.printStackTrace();
		    }
		}
	}
}
