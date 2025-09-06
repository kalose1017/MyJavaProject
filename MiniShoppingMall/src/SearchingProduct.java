import java.sql.*;
import java.util.Scanner;

public class SearchingProduct {
	public static void Search()
	{
		String sql = "SELECT CategoryName, ProductID, ProductName, Price, StockQuantity, Origin FROM shopdatatable ORDER BY CATEGORYNAME, PRODUCTNAME";
		Scanner sc = new Scanner(System.in);
		int choose = -1;
		while(choose != 0)
		{
			try (Connection conn = DriverManager.getConnection(Main.url, Main.user, Main.pass);
		             Statement stmt = conn.createStatement();
		             ResultSet rs = stmt.executeQuery(sql)) 
			{
				System.out.println();
				System.out.println("=== 조회 결과 ===");
	            while (rs.next()) {
	                String categoryName = rs.getString("CategoryName");
	                int productId = rs.getInt("ProductID");
	                String productName = rs.getString("ProductName");
	                double price = rs.getDouble("Price");
	                int stock = rs.getInt("StockQuantity");
	                String origin = rs.getString("Origin");
	                
	                System.out.println("종류명 : " + categoryName);
	                System.out.println("상품명 : " + productName);
	                System.out.println("판매가 : " + Math.round(price) + "원");
	                System.out.println("입고량 : " + stock);
	                System.out.println("원산지 : " + origin);
	                System.out.println("-----------------------------------"); // 구분선
	            }
	            while(true)
	            {
	            	System.out.print("구매할 상품을 선택하십시오.(뒤로가기 : 0) : ");
	            	String input = sc.nextLine();
	            	try {
	            		choose = Integer.parseInt(input);
	            		if(choose == 0) 
	            		{
	            			Main.MainInterface();
	            			return;
	            		}
					} 
	            	catch (NumberFormatException e) {
	            		System.out.println();
	    				System.out.println("입력이 잘못되었습니다. 다시 입력해주세요.");
	    				System.out.println();
					}
	            	// else Buy.BuyInterface();	            	
	            }
			}
			catch (SQLException e) {
				System.out.println("서버 오류!!");
				e.printStackTrace();
			}
		}
	}
}
