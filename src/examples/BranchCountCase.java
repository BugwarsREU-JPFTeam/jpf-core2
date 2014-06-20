import gov.nasa.jpf.vm.Verify;


public class BranchCountCase {
	
	public static void main(String[] args) {
		int x = Verify.random(2);
		int y= Verify.random(2);
		int sum;
		System.out.println("State 0");
		
		sum = x + y;
		System.out.println("State 1");
		
		if(sum == 0)
		{
			System.out.println("State 2");
			int x1 = Verify.random(2);
			int x1sum = x1 + sum;
			
			if(x1sum == 0)
			{
				/**System.out.println("The output is 0!");**/
				System.out.println("State 5");
			}
			else
				decisionstate(x1sum);
		}
		else if(sum > 0 && sum < 4)
		{
			System.out.println("State 3");
			int x1 = Verify.random(2);
			int x1sum = x1 + sum;
			
			if(x1sum == 1)
			{
				/**System.out.println("The output is 1!");**/
				System.out.println("State 6");
			}
			else
				decisionstate(x1sum);
		}
		else
		{
			System.out.println("State 4");
			int x1 = Verify.random(2);
			int x1sum = x1 + sum;
			
			if(x1sum == 4)
			{
				/**System.out.println("The output is 4!");**/
				System.out.println("State 7");
			}
			else
				decisionstate(x1sum);
		}
		
		
	}
	
	static public void decisiongateway1(int sum)
	{
		System.out.println("State 17");
		onethruthree(sum);
	}
	
	static public void decisiongateway2(int sum)
	{
		System.out.println("State 16");
		fourthrusix(sum);
	}
	
	static public void decisionstate(int sum)
	{
		System.out.println("State 8");
		switch(sum)
		{
			case 1: sum = 1;
				System.out.println("State 15");
				decisiongateway1(sum);
				break;
			case 2: sum = 2;
				System.out.println("State 14");
				decisiongateway1(sum);
				break;
			case 3: sum = 3;
				System.out.println("State 13");
				decisiongateway1(sum);
				break;
			case 4: sum = 4;
				System.out.println("State 12");
				decisiongateway2(sum);
				break;
			case 5: sum = 5;
				System.out.println("State 11");
				decisiongateway2(sum);
				break;
			case 6: sum = 6;
				System.out.println("State 10");
				decisiongateway2(sum);
				break;
			default:;
				/**System.out.println("Error: Out of Bounds!");**/
			System.out.println("State 9");
				break;
		}
	}
	
	static public void onethruthree(int sum)
	{
		/**System.out.println("The sum is between 0 and 3.");**/
		System.out.println("State 19");
	}
	
	static public void fourthrusix(int sum)
	{
		/**System.out.println("The sum is between 4 and 6.");**/
		System.out.println("State 18");
	}
	
	

}