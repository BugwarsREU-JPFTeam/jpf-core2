import gov.nasa.jpf.vm.Verify;

public class ProgramMaker {

	public static void gameplay(double x) {
		System.out.println("x is " + x);

		if (x <= 100) {
			return;
		}

		if (x > 100) {
			x = x % 3;
			if (x == 0) {
				x = Verify.random(299);
				gameplay(x);
			}
			if (x >= 1) {
				x = Verify.random(6);
				System.out.println("Your dice roll is " + x);
				if (x >= 4) {
					System.out.println("You've slain the dragon!");
					double lvl = x * 4;
					System.out.println("Display lvl = " + lvl);
					if (lvl >= 20) {
						System.out.println("You proceed to the next area!");
					}
					if (lvl < 20) {
						System.out.println("Too weak, run coward!");
						x = Verify.random(299);
						gameplay(x);
					}

				}
				if (x == 3) {
					System.out.println("The undead rip you asunder!");
				}
				if (x == 2) {
					System.out.println("ERROR! ERROR! :(");
				}
			}

			// System.out.println("ERROR! ERROR! :(");
			// System.exit(-1);
		}

	}

	public static void main(String[] args) {
		int x = Verify.random(50);

		// the parameter ranges from 0-300 inclusive
		gameplay(x);
	}

}
