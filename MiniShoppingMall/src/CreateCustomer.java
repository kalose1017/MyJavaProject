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
		
		// 비밀번호 입력 (4자리 이상 검증)
		while(true)
		{
			System.out.print("비밀번호를 입력하세요. (4자리 이상) : ");
			PW = sc.nextLine();
			if(PW.length() >= 4) break;
			else System.out.println("비밀번호는 4자리 이상이어야 합니다. 다시 입력해주세요.");
		}
		
		// 비밀번호 확인
		while(true)
		{
			System.out.print("비밀번호를 다시 입력하세요. : ");
			String rePW = sc.nextLine();
			if(rePW.equals(PW)) break;
			else System.out.println("비밀번호가 일치하지 않습니다! 다시 입력해주세요.");
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
            System.out.println("-------------------------");
            System.out.print("서비스를 선택하세요.(나가기 : 0 입력) : ");
            String input = sc.nextLine();
            try {
            	choose = Integer.parseInt(input);
            	if (choose == 1) CustomerState();
            	else if (choose == 2) PayInterface();
            	else if (choose == 3) ChangePassword();
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
	
	public static void PayInterface()
	{
		Scanner sc = new Scanner(System.in);
		int choose = -1;
		while(true)
		{
			System.out.println();
			System.out.println("------- 충전하실 금액을 선택하세요. -------");
			System.out.println("1. 10,000원");
			System.out.println("2. 30,000원");
			System.out.println("3. 50,000원");
			System.out.println("4. 100,000원");
			System.out.println("-------------------------------------");
			System.out.print("원하시는 금액의 번호를 입력하세요.(나가기 : 0 입력) : ");
			String input = sc.nextLine();
			try {
				choose = Integer.parseInt(input);
				if(choose == 1)
				{
					PayCharging(10000);
					return;
				}
				else if(choose == 2)
				{
					PayCharging(30000);
					return;
				}
				else if(choose == 3)
				{
					PayCharging(50000);
					return;
				}
				else if(choose == 4)
				{
					PayCharging(100000);
					return;
				}
				else if(choose == 0) {
					MyInfo(); 
					return;
				}
				else {
					System.out.println();
					System.out.println("잘못된 번호입니다. 1-4번 중에서 선택해주세요.");
				}
			} catch (NumberFormatException e) {
				System.out.println();
            	System.out.println("입력이 잘못되었습니다. 다시 입력해주세요.");
			}
		}
	}
	
	public static void PayCharging(double chargeAmount)
	{
		String sql = "UPDATE Customer SET PayCharge = PayCharge + ?, TotalCharge = TotalCharge + ? WHERE NickName = ?";
		
		try (Connection conn = DriverManager.getConnection(Main.url, Main.user, Main.pass);
		     PreparedStatement ps = conn.prepareStatement(sql)) 
		{
			ps.setDouble(1, chargeAmount);  // PayCharge에 충전 금액 추가
			ps.setDouble(2, chargeAmount);  // TotalCharge에 충전 금액 추가
			ps.setString(3, name);          // 현재 로그인한 사용자의 닉네임
			
			int rows = ps.executeUpdate();
			
			if (rows > 0) {
				System.out.println();
				System.out.println("충전이 완료되었습니다!");
				System.out.println("충전 금액: " + (int)chargeAmount + "원");
				System.out.println();
				
				// 충전 후 현재 잔액 확인
				String checkSql = "SELECT PayCharge FROM Customer WHERE NickName = ?";
				try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
					checkPs.setString(1, name);
					try (ResultSet rs = checkPs.executeQuery()) {
						if (rs.next()) {
							double currentBalance = rs.getDouble("PayCharge");
							System.out.println("현재 잔액: " + (int)currentBalance + "원");
						}
					}
				}
			} else {
				System.out.println();
				System.out.println("충전에 실패했습니다. 다시 시도해주세요.");
			}
			// 충전 인터페이스로 돌아가기
			PayInterface();
			
		} 
		catch (SQLException e) {
			System.out.println("서버 오류!!");
			e.printStackTrace();
		}
	}

	public static void ChangePassword()
	{
		Scanner sc = new Scanner(System.in);
		String currentPassword;
		String newPassword;
		String confirmPassword;
		
		while (true) {
			System.out.println();
			System.out.println("------- 비밀번호 변경 -------");
			System.out.print("현재 비밀번호를 입력하세요. (나가기 : 0 입력) : ");
			currentPassword = sc.nextLine();
			
			// 나가기 옵션
			if ("0".equals(currentPassword)) {
				MyInfo();
				return;
			}
			
			// 현재 비밀번호 확인
			String checkSql = "SELECT LoginPW FROM Customer WHERE NickName = ?";
			try (Connection conn = DriverManager.getConnection(Main.url, Main.user, Main.pass);
			     PreparedStatement ps = conn.prepareStatement(checkSql)) {
				
				ps.setString(1, name);
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next()) {
						String dbPassword = rs.getString("LoginPW");
						if (currentPassword.equals(dbPassword)) {
							// 현재 비밀번호가 맞으면 새 비밀번호 입력 받기
							break;
						} else {
							System.out.println();
							System.out.println("현재 비밀번호가 일치하지 않습니다. 다시 입력해주세요.");
							continue;
						}
					}
				}
			} catch (SQLException e) {
				System.out.println();
				System.out.println("서버 오류가 발생했습니다!");
				e.printStackTrace();
				MyInfo();
				return;
			}
		}
		
		// 새 비밀번호 입력
		while (true) {
			System.out.print("새로운 비밀번호를 입력하세요. : ");
			newPassword = sc.nextLine();
			
			if (newPassword.length() < 4) {
				System.out.println("비밀번호는 최소 4자 이상이어야 합니다.");
				continue;
			}
			
			System.out.print("새로운 비밀번호를 다시 입력하세요. : ");
			confirmPassword = sc.nextLine();
			
			if (newPassword.equals(confirmPassword)) {
				break;
			} else {
				System.out.println();
				System.out.println("비밀번호가 일치하지 않습니다. 다시 입력해주세요.");
			}
		}
		
		// 비밀번호 업데이트
		String updateSql = "UPDATE Customer SET LoginPW = ? WHERE NickName = ?";
		try (Connection conn = DriverManager.getConnection(Main.url, Main.user, Main.pass);
		     PreparedStatement ps = conn.prepareStatement(updateSql)) {
			
			ps.setString(1, newPassword);
			ps.setString(2, name);
			
			int rows = ps.executeUpdate();
			
			if (rows > 0) {
				System.out.println();
				System.out.println("비밀번호가 성공적으로 변경되었습니다!");
			} else {
				System.out.println();
				System.out.println("비밀번호 변경에 실패했습니다. 다시 시도해주세요.");
			}
			
		} catch (SQLException e) {
			System.out.println("서버 오류!!");
			e.printStackTrace();
		}
		
		// 마이페이지로 돌아가기
		MyInfo();
	}
}
