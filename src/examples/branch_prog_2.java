import gov.nasa.jpf.vm.Verify;


public class branch_prog_2 {
	
	public static void branching(){
		int num1 = Verify.random(2);
		int num2 = Verify.random(2);
		int num3 = Verify.random(2);
		
		int sum = num1 + num2 + num3;
		
		if (num1 == 0 && num2 ==1 && num3 ==0){
			throw new IllegalArgumentException("Found it");
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		branching();
	}

}
