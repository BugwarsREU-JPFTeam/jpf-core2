import gov.nasa.jpf.vm.Verify;

public class FiniteEx2 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int x = Verify.random(2);
		int y = Verify.random(2);
		
		for (int i = 0; i < x; i++) {
			System.out.println("x,y,i:" + x + "," + y + "," + i);
		}
		for (int j = 0; j < y; j++) {
			System.out.println("x,y,j:" + x + "," + y + "," + j);
		}
		System.out.println("------------");
		x = 0;
		y = 0;
	}

}
