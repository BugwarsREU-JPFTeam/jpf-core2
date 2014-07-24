import gov.nasa.jpf.vm.Verify;


public class branch_prog_2 {
	
	public static void branching(){
		int num1 = Verify.random(4);
		int num2 = Verify.random(4);
		int num3 = Verify.random(4);
		int num4 = Verify.random(4);
		int num5 = Verify.random(4);
		
		int sum = num1 + num2 + num3;
		
		if (num1 == 1 && num2 ==0 && num3 ==0 && num4 ==0 && num5 ==0){
			throw new IllegalArgumentException("Found it");
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		branching();
	}

}
