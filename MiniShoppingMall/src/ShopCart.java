
import java.sql.*;
import java.util.Scanner;
import java.util.HashMap;
import java.util.Map;

/**
 * ===========================================
 * 
 * 주요 기능:
 * - 장바구니 상품 목록 조회 및 표시
 * - 장바구니 상품 삭제
 * - 장바구니 상품 수량 수정
 * - 장바구니 전체 비우기
 * - 총 금액 계산 및 표시
 * - 장바구니 관리 메뉴 제공
 * - 구매 기능 연동 (BuyProductInCart 클래스 호출)
 * 
 * 포함된 메소드:
 * - CartInfo(): 장바구니 조회 및 관리 메인 메소드
 * - showCartMenu(): 장바구니 메뉴 표시 및 처리
 * - removeItem(): 상품 삭제
 * - updateQuantity(): 수량 수정
 * - clearCart(): 장바구니 전체 비우기
 * 
 * 구매 관련 기능은 BuyProductInCart 클래스에서 처리됩니다.
 * 
 * ===========================================
 */
public class ShopCart {
	// itemNumber와 ProductID, ProductName 매핑을 위한 static 변수
	private static Map<Integer, Integer> itemNumberToProductId = new HashMap<>();
	private static Map<Integer, String> itemNumberToProductName = new HashMap<>();
	
	// 다른 클래스에서 매핑 정보에 접근할 수 있도록 하는 getter 메소드
	public static Integer getProductIdByItemNumber(int itemNumber) {
		return itemNumberToProductId.get(itemNumber);
	}
	
	public static String getProductNameByItemNumber(int itemNumber) {
		return itemNumberToProductName.get(itemNumber);
	}
	
	public static boolean isValidItemNumber(int itemNumber) {
		return itemNumberToProductId.containsKey(itemNumber);
	}
	
	// 장바구니 조회 및 관리 메인 메소드
	public static void CartInfo()
	{
		String sql = "SELECT sc.ProductID, sc.ProductName, sc.Quantity, sd.Price " +
		             "FROM SHOPCART sc " +
		             "JOIN SHOPDATATABLE sd ON sc.ProductID = sd.ProductID AND sc.ProductName = sd.ProductName " +
		             "WHERE sc.CustomerID = ? AND sc.LoginID = ? AND sc.NickName = ? " +
		             "ORDER BY sc.ProductName DESC";
		
		try (Connection conn = DriverManager.getConnection(Main.url, Main.user, Main.pass);
		     PreparedStatement pstmt = conn.prepareStatement(sql)) 
		{
			// 매핑 초기화
			itemNumberToProductId.clear();
			itemNumberToProductName.clear();
			
			// 현재 로그인한 고객 정보 가져오기
			int customerId = Login.getCurrentCustomerId();
			String loginId = Login.getCurrentLoginId();
			String nickName = Login.getCurrentNickName();
			
			pstmt.setInt(1, customerId);
			pstmt.setString(2, loginId);
			pstmt.setString(3, nickName);
			
			ResultSet rs = pstmt.executeQuery();
			
			System.out.println();
			System.out.println("======= 장바구니 =======");
			System.out.println("고객: " + nickName + " (" + loginId + ")");
			System.out.println("=====================");
			
			boolean hasItems = false;
			int totalPrice = 0;
			int itemNumber = 1;
			
			while (rs.next()) {
				hasItems = true;
				int productId = rs.getInt("ProductID");
				String productName = rs.getString("ProductName");
				int quantity = rs.getInt("Quantity");
				double price = rs.getDouble("Price");
				
				int itemTotal = (int)(price * quantity);
				totalPrice += itemTotal;
				
				System.out.println("[" + itemNumber + "]");
				System.out.println("상품명: " + productName);
				System.out.println("단가: " + Math.round(price) + "원");
				System.out.println("수량: " + quantity + "개");
				System.out.println("소계: " + itemTotal + "원");
				System.out.println("----------------------");
				
				// itemNumber와 ProductID 매핑을 위해 저장 (static 변수 사용)
				itemNumberToProductId.put(itemNumber, productId);
				itemNumberToProductName.put(itemNumber, productName);
				itemNumber++;
			}
			
			if (!hasItems) {
				System.out.println("장바구니가 비어있습니다.");
				System.out.println("=====================");
			} else {
				System.out.println("총 금액: " + totalPrice + "원");
				System.out.println("=====================");
			}
			
			// 장바구니 관리 메뉴 표시 및 처리
			showCartMenu(hasItems);
			
		} catch (SQLException e) {
			System.out.println("\n데이터베이스 오류가 발생했습니다.");
			e.printStackTrace();
		}
	}
	
	// 장바구니 메뉴 표시 및 처리 메소드
	public static void showCartMenu(boolean hasItems) {
		try (Scanner sc = new Scanner(System.in)) {
			if (hasItems) {
			System.out.println("1. 상품 삭제");
			System.out.println("2. 수량 수정");
			System.out.println("3. 전체 비우기");
			System.out.println("4. 전체 구매");
			System.out.println("5. 선택 구매");
			System.out.println("6. 장바구니 품목 다시 확인");
			System.out.println("0. 메인으로 돌아가기");
			System.out.println("=====================");
			System.out.print("선택하세요: ");
			
			String input = sc.nextLine();
			try {
				int choice = Integer.parseInt(input);
				switch (choice) {
					case 1:
						removeItem();
						break;
					case 2:
						updateQuantity();
						break;
					case 3:
						clearCart();
						break;
					case 4:
						BuyProductInCart.purchaseAll();
						break;
					case 5:
						BuyProductInCart.purchaseSelected();
						break;
					case 6:
						CartInfo();
						break;
					case 0:
						Main.MainInterface();
						return;
					default:
						System.out.println("\n잘못된 선택입니다.");
						System.out.println("=====================");
						showCartMenu(hasItems);
						return;
				}
			} catch (NumberFormatException e) {
				System.out.println("\n올바른 숫자를 입력해주세요.");
				System.out.println("=====================");
				showCartMenu(hasItems);
				return;
			}
			} else {
				System.out.println();
				System.out.println("0. 메인으로 돌아가기");
				System.out.print("선택하세요: ");
				String input = sc.nextLine();
				if (input.equals("0")) {
					Main.MainInterface();
				} else {
					System.out.println("\n올바른 숫자를 입력해주세요.");
					showCartMenu(false);
				}
			}
		} catch (Exception e) {
			System.out.println("\n오류가 발생했습니다.");
			e.printStackTrace();
		}
	}
	
	// 상품 삭제 메소드
	public static void removeItem() {
		String sql = "DELETE FROM SHOPCART WHERE CustomerID = ? AND LoginID = ? AND NickName = ? AND ProductID = ? AND ProductName = ?";
		
		try (Connection conn = DriverManager.getConnection(Main.url, Main.user, Main.pass);
		     PreparedStatement pstmt = conn.prepareStatement(sql);
		     Scanner sc = new Scanner(System.in)) 
		{
			while (true) {
				System.out.print("삭제할 상품의 번호를 입력하세요 (나가기: 0): ");
				String itemNumberInput = sc.nextLine().trim();
				
				if (itemNumberInput.equals("0")) {
					CartInfo();
					return;
				}
				
				try {
					int itemNumber = Integer.parseInt(itemNumberInput);
					
					// itemNumber로 ProductID와 ProductName 찾기
					Integer productId = itemNumberToProductId.get(itemNumber);
					String productName = itemNumberToProductName.get(itemNumber);
					
					if (productId == null || productName == null) {
						System.out.println("해당 번호의 상품을 찾을 수 없습니다.");
						continue;
					}
					
					int customerId = Login.getCurrentCustomerId();
					String loginId = Login.getCurrentLoginId();
					String nickName = Login.getCurrentNickName();
					
					pstmt.setInt(1, customerId);
					pstmt.setString(2, loginId);
					pstmt.setString(3, nickName);
					pstmt.setInt(4, productId);
					pstmt.setString(5, productName);
					
					int result = pstmt.executeUpdate();
					
					if (result > 0) {
						System.out.println("상품이 장바구니에서 삭제되었습니다.");
					} else {
						System.out.println("해당 상품을 찾을 수 없습니다.");
					}
					break;
					
				} catch (NumberFormatException e) {
					System.out.println("올바른 번호를 입력해주세요.");
				}
			}
			
		} catch (SQLException e) {
			System.out.println("\n데이터베이스 오류가 발생했습니다.");
			e.printStackTrace();
		}
		
		CartInfo(); // 장바구니 다시 표시
	}
	
	// 수량 수정 메소드
	public static void updateQuantity() {
		String sql = "UPDATE SHOPCART SET Quantity = ? WHERE CustomerID = ? AND LoginID = ? AND NickName = ? AND ProductID = ? AND ProductName = ?";
		
		try (Connection conn = DriverManager.getConnection(Main.url, Main.user, Main.pass);
		     PreparedStatement pstmt = conn.prepareStatement(sql);
		     Scanner sc = new Scanner(System.in)) 
		{
			while (true) {
				System.out.print("수정할 상품의 번호를 입력하세요 (나가기: 0): ");
				String itemNumberInput = sc.nextLine().trim();
				
				if (itemNumberInput.equals("0")) {
					CartInfo();
					return;
				}
				
				try {
					int itemNumber = Integer.parseInt(itemNumberInput);
					
					// itemNumber로 ProductID와 ProductName 찾기
					Integer productId = itemNumberToProductId.get(itemNumber);
					String productName = itemNumberToProductName.get(itemNumber);
					
					if (productId == null || productName == null) {
						System.out.println("해당 번호의 상품을 찾을 수 없습니다.");
						continue;
					}
					
					System.out.print("새로운 수량을 입력하세요: ");
					String quantityInput = sc.nextLine().trim();
					
					int quantity = Integer.parseInt(quantityInput);
					
					if (quantity <= 0) {
						System.out.println("수량은 1개 이상이어야 합니다.");
						continue;
					}
					
					int customerId = Login.getCurrentCustomerId();
					String loginId = Login.getCurrentLoginId();
					String nickName = Login.getCurrentNickName();
					
					pstmt.setInt(1, quantity);
					pstmt.setInt(2, customerId);
					pstmt.setString(3, loginId);
					pstmt.setString(4, nickName);
					pstmt.setInt(5, productId);
					pstmt.setString(6, productName);
					
					int result = pstmt.executeUpdate();
					
					if (result > 0) {
						System.out.println("수량이 수정되었습니다.");
					} else {
						System.out.println("해당 상품을 찾을 수 없습니다.");
					}
					break;
					
				} catch (NumberFormatException e) {
					System.out.println("\n올바른 숫자를 입력해주세요.");
				}
			}
			
		} catch (SQLException e) {
			System.out.println("\n데이터베이스 오류가 발생했습니다.");
			e.printStackTrace();
		}
		
		CartInfo(); // 장바구니 다시 표시
	}
	
	// 장바구니 전체 비우기 메소드
	public static void clearCart() {
		String sql = "DELETE FROM SHOPCART WHERE CustomerID = ? AND LoginID = ? AND NickName = ?";
		
		try (Connection conn = DriverManager.getConnection(Main.url, Main.user, Main.pass);
		     PreparedStatement pstmt = conn.prepareStatement(sql);
		     Scanner sc = new Scanner(System.in)) 
		{
			while (true) {
				System.out.print("정말로 장바구니를 비우시겠습니까? (y/n): ");
				String confirm = sc.nextLine().trim();
				
				if (confirm.equalsIgnoreCase("y") || confirm.equalsIgnoreCase("yes")) {
					int customerId = Login.getCurrentCustomerId();
					String loginId = Login.getCurrentLoginId();
					String nickName = Login.getCurrentNickName();
					
					pstmt.setInt(1, customerId);
					pstmt.setString(2, loginId);
					pstmt.setString(3, nickName);
					
					int result = pstmt.executeUpdate();
					
					if (result > 0) {
						System.out.println("장바구니가 비워졌습니다.");
					} else {
						System.out.println("장바구니가 이미 비어있습니다.");
					}
					break;
				} else if (confirm.equalsIgnoreCase("n") || confirm.equalsIgnoreCase("no")) {
					System.out.println("취소되었습니다.");
					break;
				} else {
					System.out.println("\n잘못된 입력입니다. y 또는 n을 입력해주세요.");
				}
			}
			
		} catch (SQLException e) {
			System.out.println("\n데이터베이스 오류가 발생했습니다.");
			e.printStackTrace();
		}
		
		CartInfo(); // 장바구니 다시 표시
	}
	
}
