
public class Search {
	static double totalAmount = 0; // 순이익금 총액
	
	public static void totalMoney()
	{
		double amount = 0;
		System.out.println("===================");
		System.out.println("구매총액 : " + Math.round(GuMe.cost) + "원");
		System.out.println("판매총액 : " + Math.round(PanMe.SalesAmount) + "원");
		System.out.println("-------------------");
		amount = PanMe.SalesAmount - GuMe.cost;
		System.out.println("세전 이익금 : " + Math.round(amount) + "원");
		System.out.println("-------------------");
		totalAmount = amount * 0.9;
		System.out.println("세후(10%) 이익금 : " + Math.round(totalAmount) + "원");
		System.out.println("===================");
	}
}
