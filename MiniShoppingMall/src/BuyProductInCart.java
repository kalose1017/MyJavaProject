import java.sql.*;
import java.util.Scanner;

/**
 * ===========================================
 * 
 * 주요 기능:
 * - 장바구니 상품 구매 처리
 * - 전체 구매 (장바구니의 모든 상품 구매)
 * - 선택 구매 (특정 상품만 선택하여 구매)
 * - 구매 시 충전액 차감 및 잔액 확인
 * - 구매 완료 후 장바구니에서 상품 제거
 * - 안전한 트랜잭션 처리
 * 
 * 포함된 메소드:
 * - purchaseAll(): 전체 구매 (충전액 차감, 장바구니 비우기)
 * - purchaseSelected(): 선택 구매 (특정 상품만 구매)
 * 
 * ===========================================
 */
public class BuyProductInCart {
	
	// 전체 구매 메소드 - 장바구니의 모든 상품을 구매
	public static void purchaseAll() {
		try (Connection conn = DriverManager.getConnection(Main.url, Main.user, Main.pass);
		     Scanner sc = new Scanner(System.in)) 
		{
			// 현재 고객 정보 가져오기
			int customerId = Login.getCurrentCustomerId();
			String loginId = Login.getCurrentLoginId();
			String nickName = Login.getCurrentNickName();
			
			// 장바구니 총 금액 계산
			String cartSql = "SELECT sc.ProductID, sc.ProductName, sc.Quantity, sd.Price " +
			                 "FROM SHOPCART sc " +
			                 "JOIN SHOPDATATABLE sd ON sc.ProductID = sd.ProductID AND sc.ProductName = sd.ProductName " +
			                 "WHERE sc.CustomerID = ? AND sc.LoginID = ? AND sc.NickName = ?";
			
			try (PreparedStatement cartPstmt = conn.prepareStatement(cartSql)) {
				cartPstmt.setInt(1, customerId);
				cartPstmt.setString(2, loginId);
				cartPstmt.setString(3, nickName);
				
				ResultSet cartRs = cartPstmt.executeQuery();
				int totalAmount = 0;
				
				// 총 금액 계산
				while (cartRs.next()) {
					int quantity = cartRs.getInt("Quantity");
					double price = cartRs.getDouble("Price");
					totalAmount += (int)(price * quantity);
				}
				
				if (totalAmount == 0) {
					System.out.println("장바구니가 비어있습니다.");
					ShopCart.CartInfo();
					return;
				}
				
				// 현재 잔액 확인
				String balanceSql = "SELECT PayCharge FROM Customer WHERE CustomerID = ? AND LoginID = ? AND NickName = ?";
				try (PreparedStatement balancePstmt = conn.prepareStatement(balanceSql)) {
					balancePstmt.setInt(1, customerId);
					balancePstmt.setString(2, loginId);
					balancePstmt.setString(3, nickName);
					
					ResultSet balanceRs = balancePstmt.executeQuery();
					if (balanceRs.next()) {
						int currentBalance = (int)balanceRs.getDouble("PayCharge");
						
						System.out.println();
						System.out.println("======= 구매 확인 =======");
						System.out.println("구매 총액: " + totalAmount + "원");
						System.out.println("현재 잔액: " + currentBalance + "원");
						
						if (currentBalance < totalAmount) {
							System.out.println("잔액이 부족합니다.");
							System.out.println("부족한 금액: " + (totalAmount - currentBalance) + "원");
							System.out.println("=====================");
							ShopCart.CartInfo();
							return;
						}
						
						System.out.println("구매 후 잔액: " + (currentBalance - totalAmount) + "원");
						System.out.println("=====================");
						System.out.print("정말로 구매하시겠습니까? (y/n): ");
						
						String confirm = sc.nextLine().trim();
						if (confirm.equalsIgnoreCase("y") || confirm.equalsIgnoreCase("yes")) {
							// 트랜잭션 시작
							conn.setAutoCommit(false);
							
							try {
								// 잔액 차감
								String updateSql = "UPDATE Customer SET PayCharge = PayCharge - ? WHERE CustomerID = ? AND LoginID = ? AND NickName = ?";
								try (PreparedStatement updatePstmt = conn.prepareStatement(updateSql)) {
									updatePstmt.setInt(1, totalAmount);
									updatePstmt.setInt(2, customerId);
									updatePstmt.setString(3, loginId);
									updatePstmt.setString(4, nickName);
									updatePstmt.executeUpdate();
								}
								
								// 장바구니 비우기
								String clearSql = "DELETE FROM SHOPCART WHERE CustomerID = ? AND LoginID = ? AND NickName = ?";
								try (PreparedStatement clearPstmt = conn.prepareStatement(clearSql)) {
									clearPstmt.setInt(1, customerId);
									clearPstmt.setString(2, loginId);
									clearPstmt.setString(3, nickName);
									clearPstmt.executeUpdate();
								}
								
								// 트랜잭션 커밋
								conn.commit();
								
								System.out.println();
								System.out.println("구매가 완료되었습니다!");
								System.out.println("구매 금액: " + totalAmount + "원");
								System.out.println("남은 잔액: " + (currentBalance - totalAmount) + "원");
								System.out.println("감사합니다!");
								
							} catch (SQLException e) {
								// 트랜잭션 롤백
								conn.rollback();
								throw e;
							} finally {
								conn.setAutoCommit(true);
							}
							
						} else {
							System.out.println("구매가 취소되었습니다.");
						}
					}
				}
			}
			
		} catch (SQLException e) {
			System.out.println("\n데이터베이스 오류가 발생했습니다.");
			e.printStackTrace();
		}
		
		ShopCart.CartInfo(); // 장바구니 다시 표시
	}
	
	// 선택 구매 메소드 - 장바구니에서 특정 상품만 선택하여 구매
	public static void purchaseSelected() {
		try (Connection conn = DriverManager.getConnection(Main.url, Main.user, Main.pass);
		     Scanner sc = new Scanner(System.in)) 
		{
			// 현재 고객 정보 가져오기
			int customerId = Login.getCurrentCustomerId();
			String loginId = Login.getCurrentLoginId();
			String nickName = Login.getCurrentNickName();
			
			// 장바구니 상품 목록 표시
			String cartSql = "SELECT sc.ProductID, sc.ProductName, sc.Quantity, sd.Price " +
			                 "FROM SHOPCART sc " +
			                 "JOIN SHOPDATATABLE sd ON sc.ProductID = sd.ProductID AND sc.ProductName = sd.ProductName " +
			                 "WHERE sc.CustomerID = ? AND sc.LoginID = ? AND sc.NickName = ? " +
			                 "ORDER BY sc.ProductName DESC";
			
			try (PreparedStatement cartPstmt = conn.prepareStatement(cartSql)) {
				cartPstmt.setInt(1, customerId);
				cartPstmt.setString(2, loginId);
				cartPstmt.setString(3, nickName);
				
				ResultSet cartRs = cartPstmt.executeQuery();
				
				System.out.println();
				System.out.println("======= 선택 구매 =======");
				System.out.println("구매할 상품의 ProductID를 입력하세요.");
				System.out.println("여러 상품을 구매하려면 쉼표로 구분하세요. (예: 1,3,5)");
				System.out.println("나가려면 0을 입력하세요.");
				System.out.println("=====================");
				
				int itemNumber = 1;
				while (cartRs.next()) {
					int productId = cartRs.getInt("ProductID");
					String productName = cartRs.getString("ProductName");
					int quantity = cartRs.getInt("Quantity");
					double price = cartRs.getDouble("Price");
					int itemTotal = (int)(price * quantity);
					
					System.out.println("[" + itemNumber + "] ProductID: " + productId);
					System.out.println("    상품명: " + productName);
					System.out.println("    단가: " + Math.round(price) + "원");
					System.out.println("    수량: " + quantity + "개");
					System.out.println("    소계: " + itemTotal + "원");
					System.out.println("----------------------");
					itemNumber++;
				}
				
				System.out.print("구매할 상품의 ProductID를 입력하세요: ");
				String input = sc.nextLine().trim();
				
				if (input.equals("0")) {
					ShopCart.CartInfo();
					return;
				}
				
				// 입력된 ProductID들을 파싱
				String[] productIds = input.split(",");
				int totalAmount = 0;
				
				// 선택된 상품들의 총 금액 계산 및 유효성 검사
				for (String productIdStr : productIds) {
					try {
						int productId = Integer.parseInt(productIdStr.trim());
						
						// 해당 상품이 장바구니에 있는지 확인
						String checkSql = "SELECT sc.ProductID, sc.ProductName, sc.Quantity, sd.Price " +
						                 "FROM SHOPCART sc " +
						                 "JOIN SHOPDATATABLE sd ON sc.ProductID = sd.ProductID AND sc.ProductName = sd.ProductName " +
						                 "WHERE sc.CustomerID = ? AND sc.LoginID = ? AND sc.NickName = ? AND sc.ProductID = ?";
						
						try (PreparedStatement checkPstmt = conn.prepareStatement(checkSql)) {
							checkPstmt.setInt(1, customerId);
							checkPstmt.setString(2, loginId);
							checkPstmt.setString(3, nickName);
							checkPstmt.setInt(4, productId);
							
							ResultSet checkRs = checkPstmt.executeQuery();
							if (checkRs.next()) {
								int quantity = checkRs.getInt("Quantity");
								double price = checkRs.getDouble("Price");
								totalAmount += (int)(price * quantity);
							} else {
								System.out.println("ProductID " + productId + "는 장바구니에 없습니다.");
								ShopCart.CartInfo();
								return;
							}
						}
					} catch (NumberFormatException e) {
						System.out.println("올바른 ProductID를 입력해주세요: " + productIdStr);
						ShopCart.CartInfo();
						return;
					}
				}
				
				if (totalAmount == 0) {
					System.out.println("구매할 상품이 없습니다.");
					ShopCart.CartInfo();
					return;
				}
				
				// 현재 잔액 확인
				String balanceSql = "SELECT PayCharge FROM Customer WHERE CustomerID = ? AND LoginID = ? AND NickName = ?";
				try (PreparedStatement balancePstmt = conn.prepareStatement(balanceSql)) {
					balancePstmt.setInt(1, customerId);
					balancePstmt.setString(2, loginId);
					balancePstmt.setString(3, nickName);
					
					ResultSet balanceRs = balancePstmt.executeQuery();
					if (balanceRs.next()) {
						int currentBalance = (int)balanceRs.getDouble("PayCharge");
						
						System.out.println();
						System.out.println("======= 구매 확인 =======");
						System.out.println("구매 총액: " + totalAmount + "원");
						System.out.println("현재 잔액: " + currentBalance + "원");
						
						if (currentBalance < totalAmount) {
							System.out.println("잔액이 부족합니다.");
							System.out.println("부족한 금액: " + (totalAmount - currentBalance) + "원");
							System.out.println("=====================");
							ShopCart.CartInfo();
							return;
						}
						
						System.out.println("구매 후 잔액: " + (currentBalance - totalAmount) + "원");
						System.out.println("=====================");
						System.out.print("정말로 구매하시겠습니까? (y/n): ");
						
						String confirm = sc.nextLine().trim();
						if (confirm.equalsIgnoreCase("y") || confirm.equalsIgnoreCase("yes")) {
							// 트랜잭션 시작
							conn.setAutoCommit(false);
							
							try {
								// 잔액 차감
								String updateSql = "UPDATE Customer SET PayCharge = PayCharge - ? WHERE CustomerID = ? AND LoginID = ? AND NickName = ?";
								try (PreparedStatement updatePstmt = conn.prepareStatement(updateSql)) {
									updatePstmt.setInt(1, totalAmount);
									updatePstmt.setInt(2, customerId);
									updatePstmt.setString(3, loginId);
									updatePstmt.setString(4, nickName);
									updatePstmt.executeUpdate();
								}
								
								// 선택된 상품들만 장바구니에서 삭제
								for (String productIdStr : productIds) {
									int productId = Integer.parseInt(productIdStr.trim());
									String deleteSql = "DELETE FROM SHOPCART WHERE CustomerID = ? AND LoginID = ? AND NickName = ? AND ProductID = ?";
									try (PreparedStatement deletePstmt = conn.prepareStatement(deleteSql)) {
										deletePstmt.setInt(1, customerId);
										deletePstmt.setString(2, loginId);
										deletePstmt.setString(3, nickName);
										deletePstmt.setInt(4, productId);
										deletePstmt.executeUpdate();
									}
								}
								
								// 트랜잭션 커밋
								conn.commit();
								
								System.out.println();
								System.out.println("선택 구매가 완료되었습니다!");
								System.out.println("구매 금액: " + totalAmount + "원");
								System.out.println("남은 잔액: " + (currentBalance - totalAmount) + "원");
								System.out.println("감사합니다!");
								
							} catch (SQLException e) {
								// 트랜잭션 롤백
								conn.rollback();
								throw e;
							} finally {
								conn.setAutoCommit(true);
							}
							
						} else {
							System.out.println("구매가 취소되었습니다.");
						}
					}
				}
			}
			
		} catch (SQLException e) {
			System.out.println("\n데이터베이스 오류가 발생했습니다.");
			e.printStackTrace();
		}
		
		ShopCart.CartInfo(); // 장바구니 다시 표시
	}
}
