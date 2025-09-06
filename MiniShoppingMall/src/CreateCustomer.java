import java.security.PublicKey;
import java.security.interfaces.RSAKey;
import java.sql.*;
import java.util.Scanner;

public class CreateCustomer {
	static String ID;
	static String NickName;
	static String PW;
	static String name;
	
	public static void Create() // 회원가입
	{
		String sql = "INSERT INTO Customer " +
                "(LoginID, LoginPW, NickName, PayCharge, TotalCharge, Grade) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
		Scanner sc = new Scanner(System.in);
		System.out.print("아이디를 입력하세요. : ");
		ID = sc.nextLine();
		System.out.print("닉네임을 입력하세요. : ");
		NickName = sc.nextLine();
		System.out.print("비밀번호를 입력하세요. : ");
		PW = sc.nextLine();
		
		while(true)
		{
			System.out.print("비밀번호를 다시 입력하세요. : ");
			String rePW = sc.nextLine();
			if(rePW.equals(PW)) break;
			else System.out.println("비밀번호가 일치하지 않습니다! 다시 입력해주세요. :");
		}

	    try (Connection conn = DriverManager.getConnection(Main.url, Main.user, Main.pass);
	       PreparedStatement ps = conn.prepareStatement(sql)) 
	    {
		   ps.setString(1, ID); // 로그인ID 입력
		   ps.setString(2, PW); // 로그인PW 입력
		   ps.setString(3, NickName); // 닉네임입력
		   ps.setDouble(4, 0);
		   ps.setDouble(5, 0);
		   ps.setString(6, "Bronez");
	
	       int rows = ps.executeUpdate();   // 실행
	       System.out.println("로그인 성공! 환영합니다, " + NickName + "님");
	       Main.MainInterface();
	    } 
	    catch (SQLException e) 
	    {
	       System.out.println("서버 오류!!");
	       e.printStackTrace();
	    }
	}
	
	public static void CheckPW() { // 마이페이지 접근을 위한 비밀번호 입력
	    Scanner sc = new Scanner(System.in);

	    while (true) {
	    	System.out.println();
	        System.out.print("비밀번호를 입력하세요. (나가기 : 0 입력) : ");
	        String inputPw = sc.nextLine();

	        if ("0".equals(inputPw)) {
	            Main.MainInterface();
	            return; 
	        }

	        String sql = "SELECT NickName FROM Customer WHERE LoginPW = ?";
	        try (Connection conn = DriverManager.getConnection(Main.url, Main.user, Main.pass);
	             PreparedStatement ps = conn.prepareStatement(sql)) {

	            ps.setString(1, inputPw);
	            
	            try (ResultSet rs = ps.executeQuery()) {
	                if (rs.next()) {
	                    String nick = rs.getString("NickName");
	                    name = nick;
	                    MyInfo();
	                    return;
	                } 
	                else 
	                {
	                    System.out.println("비밀번호가 일치하지 않습니다. 다시 입력해주세요.");
	                }
	            }
	        } catch (SQLException e) {
	            System.out.println("서버 오류!!");
	            e.printStackTrace();
	        }
	    }
	}
	
	public static void MyInfo() { // 마이페이지 인터페이스
	    Scanner sc = new Scanner(System.in);
	    int choose = -1;

	    while (true) {
	    	System.out.println();
            System.out.println("----- " + name + "님의 정보 -----");
            System.out.println("1. 정보 조회");
            System.out.println("2. 페이 충전");
            System.out.println("3. 비밀번호 변경");
            System.out.println("---------------------------------------");
            System.out.print("서비스를 선택하세요.(나가기 : 0 입력) : ");
            String input = sc.nextLine();
            try {
            	choose = Integer.parseInt(input);
            	if (choose == 1) CustomerState();
            	else if (choose == 2) ChangePayAndGrade();
            	//else if (choose == 3) ChangePassword();
            	else if (choose == 0) {
            		Main.MainInterface();
            		return; 
                }
            } 
            catch (NumberFormatException e) 
            {
            	System.out.println();
            	System.out.println("입력이 잘못되었습니다. 다시 입력해주세요.");
            }
	    }
	}
	public static void CustomerState() // 내 정보 조회
	{
		String sql = "SELECT CustomerID, LoginID, NickName, PayCharge, TotalCharge, Grade " 
				+ "FROM CUSTOMER WHERE NickName=?";
		Scanner sc = new Scanner(System.in);
		try (Connection conn = DriverManager.getConnection(Main.url, Main.user, Main.pass);
	         PreparedStatement ps = conn.prepareStatement(sql)) 
		{
			ps.setString(1, name);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) 
				{
				   System.out.println();
                   int id = rs.getInt("CustomerID");
                   String loginId = rs.getString("LoginID");
                   String nick = rs.getString("NickName");
                   double pay = rs.getDouble("PayCharge");
                   double total = rs.getDouble("TotalCharge");
                   String grade = rs.getString("Grade");

                   System.out.println("고객 ID: " + id);
                   System.out.println("로그인 ID: " + loginId);
                   System.out.println("닉네임: " + nick);
                   System.out.println("충전액: " + Math.round(pay) + "원");
                   System.out.println("누적 충전액: " + Math.round(total) + "원");
                   System.out.println("등급: " + grade);
               }     
				MyInfo();
			}
		}
		catch (SQLException e) {
			System.out.println("서버 오류!!");
		    e.printStackTrace();
		}
	}
	
	public static void ChangePayAndGrade()
	{
		
	}
}
