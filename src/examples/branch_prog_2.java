
import gov.nasa.jpf.vm.Verify;


public class branch_prog_2 {
	
	public static void branching(){
		int num1 = Verify.random(1);
		int num2 = Verify.random(1);
		int num3 = Verify.random(1);

		
		if (num3 == 1 )
			throw new IllegalArgumentException("Found it");
	
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		branching();
	}

}