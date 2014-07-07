import gov.nasa.jpf.vm.Verify;

public class FiniteEx2 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int x = Verify.random(2);
		System.out.println("chose a x! Value is " + x);
		int y = Verify.random(2);
		System.out.println("chose a y! Value is " + y);
		for (int i = 0; i < x; i++) {
			System.out.println(i + "th run of first loop");
			System.out.println("x,y,i:" + x + "," + y + "," + i);
		}
		for (int j = 0; j < y; j++) {
			System.out.println(j + "th run of second loop");
			System.out.println("x,y,j:" + x + "," + y + "," + j);
		}
		System.out.println("------------");
		x=0;
		y=0;
	}

}
