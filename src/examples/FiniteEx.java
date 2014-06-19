import gov.nasa.jpf.vm.Verify;

public class FiniteEx { // this should replicate the photo taken of branch chart

	public static void main(String[] args) {
		int x = Verify.random(1); // x is either 0 or 1
		int y = Verify.random(1); // y is either 0 or 1
		if (x == y) { // starting
			if (x == 1) { // b1
				System.out.println("pass (x and y = 1)");// b3
			} else
				Verify.assertTrue(x == 1);// b4 the error is here!
		} else if (x > y) { // b2
			System.out.println("pass (x>y)");// b3
		} else
			System.out.println("pass (x !> y");// b4
	}

}
