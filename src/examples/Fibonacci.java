
import gov.nasa.jpf.vm.Verify;

public class Fibonacci {
	
	public static void dumb_fib(int num)
	{
		
		
	}
	
	public static int ok_fib(int num)
	{
		if (num == 0)
			return 0;
		
		if (num == 1)
			return 1;
		
		if (num == 2)
			return 1;
		
		int[] numbers = new int[num+1];
		
		numbers[0] = 0;
		numbers[1] = 1;
		numbers[2] = 1;
		
		int n = 3;
		while (n <= num)
		{
			numbers[n] = numbers[n-1] + numbers[n-2];
			n++;
		}
		return numbers[num];
	}
	
	public static int fib(int num1)
	{
		if (num1 == 0)
		{
			return 0;
		}
		if (num1 == 1)
		{
			return 1;
		}
		
		return fib(num1-1) + fib(num1-2);
	}
	public static void main(String[] args)
	{
		int val = Verify.random(37);
		System.out.println("Fibonacci #" + val + " = " + fib(val));
	}

}
