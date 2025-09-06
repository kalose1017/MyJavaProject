import java.util.Scanner;

public class GuMe {
	static int Count = 0; // 구매할 품목의 종류 수.
	static String[] Product = new String[100]; // 구매할 품목의 이름
	static float[] Money = new float[100]; // 구매할 품목의 가격
	static int[] BuyCount = new int[100]; // 구매할 품목의 갯수
	static double cost = 0; // 이익금에서 차감할 원가
	
	public static void Buy()
	{
		Scanner BuyCountSc = new Scanner(System.in);
		Scanner BuyProductSc = new Scanner(System.in);
		Scanner BuyMoneySc = new Scanner(System.in);
		Scanner PurchaseNumSc = new Scanner(System.in);
		System.out.println();
		System.out.print("구매할 품목은 몇 종류입니까? : ");
		Count = BuyCountSc.nextInt();
		try {
			if(Count > 0 && Count <= 100)
			{
				for(int i = 0; i < Count; i++)
				{
					System.out.print((i + 1) + "번째 폼목의 이름을 입력하세요. : ");
					Product[i] = BuyProductSc.nextLine();
					System.out.print((i + 1) + "번째 폼목의 단가를 입력하세요. : ");
					Money[i] = BuyMoneySc.nextFloat();
					System.out.print("몇 개를 구매하였습니까? : ");
					BuyCount[i] = PurchaseNumSc.nextInt();
					
					cost += (Money[i] * BuyCount[i]);
				}
			}
			else {
				System.out.println("갯수 입력이 잘못되었습니다. 다시 입력해주세요.");
			}
				
		} catch (NumberFormatException e) {
			System.out.println("입력이 잘못되었습니다. 다시 입력해주세요.");
		}
	}
	
	public static void BuyState()
	{
		int choose = 1;
		while(choose != 0)
		{
			Scanner sc = new Scanner(System.in);
			System.out.println();
			System.out.println("===== 구매 내역 =====");
			for(int i = 0; i < Count; i++)
			{
				System.out.println("[" + (i + 1) + "번 항목]");
				System.out.println("이름 : " + Product[i]);
				System.out.println("가격 : " + Math.round(Money[i]) + "원");
				System.out.println("갯수 : " + BuyCount[i] + "개");
				System.out.println("--------------");
			}
			System.out.println("구매총액 : " + Math.round(cost) + "원");
			System.out.println("===================");
			System.out.print("나가기(0 입력) : ");
			choose = sc.nextInt();
			System.out.println();
			if(choose == 0) Main.DefaultState();
			else System.out.println("입력이 잘못되었습니다. 다시 입력해주세요.");
		}
	}
}
