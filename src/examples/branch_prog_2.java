import gov.nasa.jpf.vm.Verify;


public class branch_prog_2 {
	
	public static void branching(){ //be sure to adjust depth with number of vars
		int num1 = Verify.getInt(0,3); //and branch with the verify tag - 1
		int num2 = Verify.getInt(0,3); 
		int num3 = Verify.getInt(0,3); 
		int num4 = Verify.getInt(0,3); 
		
		
		int sum = num1 + num2 + num3;
		
		if (num1 == 0 && num2 ==0 && num3 ==1 &&
			num4 == 0){
			throw new IllegalArgumentException("Found it");
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		branching();
	}

}
