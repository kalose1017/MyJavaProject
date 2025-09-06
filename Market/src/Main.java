import java.util.Scanner;

public class Main {
	public static void main(String args[]) {
	      DefaultState();
	}
	
	static void DefaultState()
	{
		int choose = -1;
		Scanner scanner = new Scanner(System.in);
	      while(choose != 0)
	      {
	         System.out.println("*************");
	         System.out.println("1. 구매");
	         System.out.println("2. 판매");
	         System.out.println("3. 구매내역");
	         System.out.println("4. 판매내역");
	         System.out.println("5. 순이익금");
	         System.out.println("*************");
	         System.out.print("항목 선택(종료 : 0 입력) : ");
	         choose = scanner.nextInt();
	         if(choose == 1) GuMe.Buy();
	         else if(choose == 2) PanMe.Sell();
	         else if(choose == 3) GuMe.BuyState();
	         else if(choose == 4) PanMe.SellState();
	         else if(choose == 5) Search.totalMoney();
	         else { System.out.println("입력이 잘못되었습니다. 다시 입력해주세요."); }
	      }  
	}
}
