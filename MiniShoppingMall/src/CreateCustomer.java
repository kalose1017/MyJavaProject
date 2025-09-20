import java.sql.*;
import java.util.Scanner;

/**
 * ===========================================
 * 
 * 주요 기능:
 * - 신규 고객 회원가입 처리
 * - 고객 정보 조회 및 관리
 * - 페이 충전 기능
 * - 비밀번호 변경 기능
 * - 마이페이지 인터페이스 제공
 * - 아이디 중복 확인
 * 
 * 포함된 메소드:
 * - Create(): 회원가입 처리
 * - CheckPW(): 마이페이지 접근을 위한 비밀번호 인증
 * - MyInfo(): 마이페이지 인터페이스
 * - CustomerState(): 고객 정보 조회
 * - PayInterface(): 페이 충전 메뉴
 * - PayCharging(): 페이 충전 실행
 * - ChangePassword(): 비밀번호 변경
 * - checkIdDuplicate(): 아이디 중복 확인
 * 
 * ===========================================
 */
public class CreateCustomer {
	static String ID;
	static String NickName;
	static String PW;
	public static String name;
	
	// 회원가입 - 새 고객 정보 입력 및 데이터베이스 저장
	public static void Create()
	{
		String sql = "INSERT INTO Customer " +
                "(LoginID, LoginPW, NickName, PayCharge, TotalCharge, Grade) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
		
		try (Scanner sc = new Scanner(System.in);
		     Connection conn = DriverManager.getConnection(Main.url, Main.user, Main.pass);
		     PreparedStatement ps = conn.prepareStatement(sql)) 
		{
			// 아이디 입력 및 중복 확인
			while(true) {
				System.out.print("아이디를 입력하세요. : ");
				ID = sc.nextLine();
				
				if(checkIdDuplicate(ID)) {
					System.out.println("이미 사용 중인 아이디입니다. 다른 아이디를 입력해주세요.");
					continue;
				} else {
					System.out.println("사용 가능한 아이디입니다.");
					break;
				}
			}
			
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
			ps.setString(1, ID); // 로그인ID 입력
			ps.setString(2, PW); // 로그인PW 입력
			ps.setString(3, NickName); // 닉네임입력
			ps.setDouble(4, 0);
			ps.setDouble(5, 0);
			ps.setString(6, "Bronze");

			ps.executeUpdate();   // 실행
			System.out.println("회원가입 성공! 환영합니다, " + NickName + "님");
			Main.MainInterface();
		} 
		catch (SQLException e) 
		{
			System.out.println("\n데이터베이스 오류가 발생했습니다.");
			e.printStackTrace();
		}
	}
	
	// 마이페이지 접근을 위한 비밀번호 입력 및 인증
	public static void CheckPW() {
	    try (Scanner sc = new Scanner(System.in)) {
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
	            System.out.println("\n데이터베이스 오류가 발생했습니다.");
	            e.printStackTrace();
	        }
		    }
		}
	}
	
	// 마이페이지 인터페이스 - 정보 조회, 페이 충전, 비밀번호 변경 메뉴
	public static void MyInfo() {
	    try (Scanner sc = new Scanner(System.in)) {
		    int choose = -1;

		    while (true) {
	    	System.out.println();
            System.out.println("----- " + name + "님의 정보 -----");
            System.out.println("1. 정보 조회");
            System.out.println("2. 페이 충전");
            System.out.println("3. 비밀번호 변경");
            System.out.println("4. 등급 정보 보기");
            System.out.println("-------------------------");
            System.out.print("서비스를 선택하세요.(나가기 : 0 입력) : ");
            String input = sc.nextLine();
            try {
            	choose = Integer.parseInt(input);
            	if (choose == 1) {
            		CustomerState();
            		return;
            	}
            	else if (choose == 2) {
            		PayInterface();
            		return;
            	}
            	else if (choose == 3) {
            		ChangePassword();
            		return;
            	}
            	else if (choose == 4) {
            		showGradeBenefits();
            		return;
            	}
            	else if (choose == 0) {
            		Main.MainInterface();
            		return; 
                }
                else {
                	System.out.println();
                	System.out.println("잘못된 선택입니다. 1-4번 중에서 선택해주세요.");
                }
            } 
            catch (NumberFormatException e) 
            {
            	System.out.println();
            	System.out.println("올바른 숫자를 입력해주세요.");
            }
		    }
		}
	}
	
	// 내 정보 조회 - 고객의 개인정보 및 충전액 표시
	public static void CustomerState()
	{
		String sql = "SELECT CustomerID, LoginID, NickName, PayCharge, TotalCharge, Grade " 
				+ "FROM CUSTOMER WHERE NickName=?";
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

                   System.out.println("========== 내 정보 ==========");
                   System.out.println("고객 ID: " + id);
                   System.out.println("로그인 ID: " + loginId);
                   System.out.println("닉네임: " + nick);
                   System.out.println("현재 잔액: " + Math.round(pay) + "원");
                   System.out.println("누적 충전액: " + Math.round(total) + "원");
                   System.out.println("현재 등급: " + grade);
                   System.out.println("============================");
                   
                   // 등급 정보 상세 표시
                   System.out.println();
                   System.out.println(CustomerGrade.getGradeInfo(id, loginId, nick));
                   System.out.println();
               }     
				MyInfo();
			}
		}
		catch (SQLException e) {
			System.out.println("\n데이터베이스 오류가 발생했습니다.");
		    e.printStackTrace();
		}
	
	}
	
	// 페이 충전 인터페이스 - 충전 금액 선택 메뉴
	public static void PayInterface()
	{
		try (Scanner sc = new Scanner(System.in)) {
			int choose = -1;
			while(true)
			{
				System.out.println();
				System.out.println("------- 충전하실 금액을 선택하세요. -------");
				System.out.println("1. 10,000원");
				System.out.println("2. 30,000원");
				System.out.println("3. 50,000원");
				System.out.println("4. 100,000원");
				System.out.println("[현재 잔액 : " + Math.round(getCurrentPayBalance()) + "원]");				
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
					System.out.println("올바른 숫자를 입력해주세요.");
				}
			}
		}
	}
	
	// 페이 충전 실행 - 선택된 금액을 고객 계정에 충전
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
				
				// 충전 후 현재 잔액 및 등급 확인
				String checkSql = "SELECT CustomerID, LoginID, PayCharge, TotalCharge, Grade FROM Customer WHERE NickName = ?";
				try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
					checkPs.setString(1, name);
					try (ResultSet rs = checkPs.executeQuery()) {
						if (rs.next()) {
							int customerId = rs.getInt("CustomerID");
							String loginId = rs.getString("LoginID");
							double currentBalance = rs.getDouble("PayCharge");
							double totalCharge = rs.getDouble("TotalCharge");
							String oldGrade = rs.getString("Grade");
							
							System.out.println("현재 잔액: " + (int)currentBalance + "원");
							System.out.println("누적 충전액: " + (int)totalCharge + "원");
							
							// 등급 업데이트
							boolean gradeUpdated = CustomerGrade.updateCustomerGrade(customerId, loginId, name);
							if (gradeUpdated) {
								// 업데이트된 등급 확인
								String newGrade = CustomerGrade.calculateGrade(totalCharge);
								if (!newGrade.equals(oldGrade)) {
									System.out.println();
									System.out.println("🎉 등급이 업그레이드되었습니다!");
									System.out.println("이전 등급: " + oldGrade + " → 새로운 등급: " + newGrade);
									System.out.println();
								}
							}
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
			System.out.println("\n데이터베이스 오류가 발생했습니다.");
			e.printStackTrace();
		}
	}

	// 비밀번호 변경 - 현재 비밀번호 확인 후 새 비밀번호로 변경
	public static void ChangePassword()
	{
		try (Scanner sc = new Scanner(System.in)) {
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
				System.out.println("\n데이터베이스 오류가 발생했습니다.");
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
			System.out.println("\n데이터베이스 오류가 발생했습니다.");
			e.printStackTrace();
		}
		
		// 마이페이지로 돌아가기
		MyInfo();
		}
	}
	
	// 아이디 중복 확인 메소드
	public static boolean checkIdDuplicate(String loginId) {
		String sql = "SELECT LoginID FROM Customer WHERE LoginID = ?";
		
		try (Connection conn = DriverManager.getConnection(Main.url, Main.user, Main.pass);
		     PreparedStatement ps = conn.prepareStatement(sql)) {
			
			ps.setString(1, loginId);
			
			try (ResultSet rs = ps.executeQuery()) {
				// 결과가 있으면 중복 (true 반환)
				return rs.next();
			}
			
		} catch (SQLException e) {
			System.out.println("\n데이터베이스 오류가 발생했습니다.");
			e.printStackTrace();
			// 오류 발생 시 안전하게 중복으로 처리
			return true;
		}
	}
	
	// 등급 혜택 정보 표시
	public static void showGradeBenefits() {
		try (Scanner sc = new Scanner(System.in)) {
			System.out.println();
			System.out.println(CustomerGrade.getGradeBenefits());
			System.out.println();
			System.out.print("마이페이지로 돌아가려면 Enter를 누르세요...");
			sc.nextLine();
			MyInfo();
		}
	}
	
	// 현재 로그인한 사용자의 페이 잔액을 조회하는 메서드
	private static double getCurrentPayBalance() {
		String sql = "SELECT PayCharge FROM Customer WHERE NickName = ?";
		
		try (Connection conn = DriverManager.getConnection(Main.url, Main.user, Main.pass);
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			pstmt.setString(1, name);
			
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getDouble("PayCharge");
				}
			}
			
		} catch (SQLException e) {
			System.out.println("잔액 조회 중 오류가 발생했습니다.");
			e.printStackTrace();
		}
		
		return 0.0; // 오류 시 0 반환
	}
	
	// 현재 로그인한 사용자의 등급을 조회화는 메서드
	public static String getCurrentGrade() {
 		String sql = "SELECT GRADE FROM Customer WHERE NickName = ?";
 		
 		try (Connection conn = DriverManager.getConnection(Main.url, Main.user, Main.pass);
 		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
 			
 			pstmt.setString(1, name);
 			
 			try (ResultSet rs = pstmt.executeQuery()) {
 				if (rs.next()) {
 					return rs.getString("GRADE");
 				}
 			}
 			
 		} catch (SQLException e) {
 			System.out.println("등급 조회 중 오류가 발생했습니다.");
 			e.printStackTrace();
 		}
 		
 		return "Error";
 	}		 	

}
