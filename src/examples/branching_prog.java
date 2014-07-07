import gov.nasa.jpf.vm.Verify;


public class branching_prog {
    public static void branching_prog (int max_val, int depth, int branch, int sum)
	{
		for (int i = depth; i > 0; i--)
		{
		  int rand_val= Verify.random(branch-1);
		  System.out.println("Value obtained is " + rand_val);
		  sum += rand_val;
		  System.out.println("added to sum");
		  if (sum == max_val){
			 System.out.println("This is wrong!!");
			 throw new IllegalArgumentException("Invalid");
		  }
		}
		
		System.out.println("Obtained sum of " + sum);
		return;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int depth = 3;
		int branch = 3;
		int max_val = (depth)*(branch-1);
		branching_prog(max_val,depth, branch, 0);
		
	}
}