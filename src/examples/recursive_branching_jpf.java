import gov.nasa.jpf.vm.Verify;


public class recursive_branching_jpf {
	
	
	public static void branch_recursive (int max_val, int depth, int branch, int sum)
	{
		if (depth == 0)
		{
			System.out.println("Obtained sum of " + sum);
			return;
		}
		
		int rand_val= Verify.random(branch-1);
		System.out.println("Value obtained is " + rand_val);
		sum += rand_val;
		
		if (sum == max_val){
			System.out.println("This is wrong!!");
			throw new IllegalArgumentException("Invalid");
		}
		
		branch_recursive(max_val,depth-1,branch,sum);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int depth = 3;
		int branch = 2;
		int max_val = (depth)*(branch-1);
		branch_recursive(max_val,depth, branch, 0);
		
	}

}
