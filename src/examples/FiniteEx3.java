import gov.nasa.jpf.vm.Verify;

public class FiniteEx3 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int x = Verify.getInt(0,2);
		System.out.println("chose a x! Value is " + x);
		int y = Verify.getInt(0, 2);
		System.out.println("chose a y! Value is " + y);
		int z = Verify.getInt(0, 2);
		System.out.println("chose a z! Value is " + z);
		for (int i = 0; i < x; i++) {
			System.out.println(i + "th run of first loop");
			System.out.println("x,y,i:" + x + "," + y + "," + z + "," + i);
		}
		for (int j = 0; j < y; j++) {
			System.out.println(j + "th run of second loop");
			System.out.println("x,y,j:" + x + "," + y + "," + z + "," + j);
		}
		for (int k = 0; k < z; k++) {
			System.out.println(k + "th run of third loop");
			System.out.println("x,y,z,k:" + x + "," + y + "," + z + "," + k);
		}

		System.out.println("------------");
	}

}