
import java.util.Random;

import gov.nasa.jpf.vm.Verify;


public class iterative_branch {

    public static void iterative_branch(int depth, int branch)
	{
		int i = 0;
		int sum = 0;
		while (i < depth)  
		{
			int rand_val = Verify.random(branch-1);  //since 0 is included, im taking one less
			if (rand_val >= 0 && rand_val <= branch)
			{
				System.out.println("Branch taken: " + rand_val);
				sum += rand_val;
			}
			
			if (sum == (depth*(branch-1)))
			{
				System.out.println("Error encountered!");
				throw new IllegalArgumentException("Invalid");
			}
			
			i++;
		}
		
		System.out.println("Total Sum is: " + sum);
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int depth = 3;
		int branch = 2;
		
		iterative_branch(depth, branch);
	}
}
