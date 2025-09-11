import java.sql.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Collections;

public class SearchingProduct {
	public static void Searching()
	{
		String sql = "SELECT DISTINCT CategoryName FROM shopdatatable ORDER BY CategoryName";
		
		try (Connection conn = DriverManager.getConnection(Main.url, Main.user, Main.pass);
	             Statement stmt = conn.createStatement();
	             ResultSet rs = stmt.executeQuery(sql);
	             Scanner sc = new Scanner(System.in)) 
		{
			ArrayList<String> categories = new ArrayList<>();
			
			// CategoryName들을 ArrayList에 저장
			while (rs.next()) {
				String categoryName = rs.getString("CategoryName");
				categories.add(categoryName);
			}
			
			// 오름차순으로 정렬 (이미 SQL에서 ORDER BY로 정렬되어 있지만 확실히 하기 위해)
			Collections.sort(categories);
			
			System.out.println();
			System.out.println("=== 카테고리 목록 ===");
			
			// 번호를 매기면서 출력
			for (int i = 0; i < categories.size(); i++) {
				System.out.println((i + 1) + ". " + categories.get(i));
			}
			
			System.out.println();
			System.out.print("카테고리를 선택하세요 (나가기: 0): ");
			String input = sc.nextLine();
			
			try {
				int choice = Integer.parseInt(input);
				if (choice == 0) {
					Main.MainInterface();
					return;
				} else if (choice >= 1 && choice <= categories.size()) {
					String selectedCategory = categories.get(choice - 1);
					System.out.println("선택된 카테고리: " + selectedCategory);
					// 선택된 카테고리로 상품 검색하는 로직을 여기에 추가할 수 있습니다
					SearchByCategory(selectedCategory);
				} else {
					System.out.println("잘못된 선택입니다.");
					Searching();
				}
			} catch (NumberFormatException e) {
				System.out.println("숫자를 입력해주세요.");
				Searching();
			}
		}
		catch (SQLException e) {
			System.out.println("서버 오류!!");
			e.printStackTrace();
		}
	}
	
	public static void SearchByCategory(String categoryName)
	{
		String sql = "SELECT ProductID, ProductName, Price, StockQuantity, Origin, CategoryName FROM shopdatatable WHERE CategoryName = ? ORDER BY ProductName";
		int choose = -1;
		
		while(choose != 0)
		{
			try (Connection conn = DriverManager.getConnection(Main.url, Main.user, Main.pass);
		             PreparedStatement pstmt = conn.prepareStatement(sql);
		             Scanner sc = new Scanner(System.in)) 
			{
				pstmt.setString(1, categoryName);
				ResultSet rs = pstmt.executeQuery();
				
				// 상품 정보를 저장할 ArrayList
				ArrayList<ProductInfo> products = new ArrayList<>();
				
				System.out.println();
				System.out.println("=== " + categoryName + " 카테고리 상품 목록 ===");
				int productNumber = 1;
				
	            while (rs.next()) {
	                int productId = rs.getInt("ProductID");
	                String productName = rs.getString("ProductName");
	                double price = rs.getDouble("Price");
	                int stock = rs.getInt("StockQuantity");
	                String origin = rs.getString("Origin");
	                
	                // 상품 정보를 ArrayList에 저장
	                products.add(new ProductInfo(productId, productName, price, stock, origin));
	                
	                System.out.println("[" + productNumber + "]");
	                System.out.println("상품명 : " + productName);
	                System.out.println("판매가 : " + Math.round(price) + "원");
	                System.out.println("입고량 : " + stock);
	                System.out.println("원산지 : " + origin);
	                System.out.println("-----------------------------------");
	                productNumber++;
	            }
	            
	            while(true)
	            {
	            	System.out.print("구매할 상품을 선택하십시오.(나가기 : 0) : ");
	            	String input = sc.nextLine();
	            	try {
	            		choose = Integer.parseInt(input);
	            		if(choose == 0) 
	            		{
	            			Searching();
	            			return;
	            		}
	            		else if(choose >= 1 && choose <= products.size())
	            		{
	            			// 선택된 상품의 정보 가져오기
	            			ProductInfo selectedProduct = products.get(choose - 1);
	            			
	            			// 수량 입력받기
	            			System.out.print("구매할 수량을 입력하세요: ");
	            			String quantityInput = sc.nextLine();
	            			
	            			try {
	            				int quantity = Integer.parseInt(quantityInput);
	            				
	            				if(quantity <= 0) {
	            					System.out.println("수량은 1개 이상이어야 합니다.");
	            					continue;
	            				}
	            				
	            				if(quantity > selectedProduct.stock) {
	            					System.out.println("재고가 부족합니다. 현재 재고: " + selectedProduct.stock + "개");
	            					continue;
	            				}
	            				
	            				// 장바구니에 추가
	            				addToCart(selectedProduct.productId, quantity);
	            				System.out.println(selectedProduct.productName + " " + quantity + "개가 장바구니에 추가되었습니다.");
	            				
	            			} catch (NumberFormatException e) {
	            				System.out.println("올바른 숫자를 입력해주세요.");
	            			}
	            		}
	            		else
	            		{
	            			System.out.println("잘못된 선택입니다. 다시 선택해주세요.");
	            		}
					} 
	            	catch (NumberFormatException e) {
	            		System.out.println();
	    				System.out.println("입력이 잘못되었습니다. 다시 입력해주세요.");
	    				System.out.println();
					}
	            }
			}
			catch (SQLException e) {
				System.out.println("서버 오류!!");
				e.printStackTrace();
			}
		}
	}
	
	// 장바구니에 상품 추가하는 메소드
	public static void addToCart(int productId, int quantity)
	{
		String sql = "INSERT INTO SHOPCART (CustomerID, ProductID, Quantity) VALUES (?, ?, ?) " +
		             "ON DUPLICATE KEY UPDATE Quantity = Quantity + ?";
		
		try (Connection conn = DriverManager.getConnection(Main.url, Main.user, Main.pass);
		     PreparedStatement pstmt = conn.prepareStatement(sql)) 
		{
			// 현재 로그인한 고객 ID를 가져옵니다
			int customerId = Login.getCurrentCustomerId();
			
			pstmt.setInt(1, customerId);
			pstmt.setInt(2, productId);
			pstmt.setInt(3, quantity);
			pstmt.setInt(4, quantity);
			
			int result = pstmt.executeUpdate();
			
			if(result > 0) {
				System.out.println("장바구니에 상품이 추가되었습니다.");
			} else {
				System.out.println("장바구니 추가에 실패했습니다.");
			}
			
		} catch (SQLException e) {
			System.out.println("장바구니 추가 중 오류가 발생했습니다.");
			e.printStackTrace();
		}
	}
	
	
	// 상품 정보를 저장하는 내부 클래스
	static class ProductInfo
	{
		int productId;
		String productName;
		double price;
		int stock;
		String origin;
		
		public ProductInfo(int productId, String productName, double price, int stock, String origin)
		{
			this.productId = productId;
			this.productName = productName;
			this.price = price;
			this.stock = stock;
			this.origin = origin;
		}
	}
}
