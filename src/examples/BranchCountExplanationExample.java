import gov.nasa.jpf.vm.Verify;

public class BranchCountExplanationExample {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int a = Verify.getInt(0,1);
		System.out.println("chose a a! Value is " + a);
		int b = Verify.getInt(0,1);
		System.out.println("chose a b! Value is " + b);
		int c = Verify.getInt(0,1);
		System.out.println("chose a c! Value is " + c);
		for (int i = 0; i < a; i++) {
			System.out.println(i + "th run of first loop");
			System.out.println("a,b,c,i:" + a + "," + b + "," + c + "," + i);
		}
		for (int j = 0; j < b; j++) {
			System.out.println(j + "th run of second loop");
			System.out.println("a,b,c,j:" + a + "," + b + "," + c + "," + j);
		}
		for (int k = 0; k < c; k++) {
			System.out.println(k + "th run of third loop");
			System.out.println("a,b,c,k:" + a + "," + b + "," + c + "," + k);
		}

		System.out.println("------------");
	}

}