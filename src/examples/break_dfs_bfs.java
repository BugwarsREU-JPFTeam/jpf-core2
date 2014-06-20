import gov.nasa.jpf.vm.Verify;


public class break_dfs_bfs {
	
	
	public static void main(String[] args){
		int num1 = Verify.random(10);
		int num2;
		int num3;
		int num4;
		
			for (int i = 0; i < num1; i++){
			
				num2 = Verify.random(10);
				for (int j = 0; j < num2; j++){
						num3 = Verify.random(10);
							for (int k = 0; k < num3; k++){	
							  System.out.println(i + "," + j + "," + k);
							  
				 			  if (k < 2 && j < 2)
							  {
				 				 num4 = Verify.random(4);
									for (int k1 = 0; k1 < num4; k1++)
										System.out.println("k and j are both less than two");
							  }
				 			  
				 			  if (k > 2 || j < 2)
				 			  {
				 				  num4 = Verify.random(3);
				 				  	for (int k1 = 0; k1 < num4; k1++)
				 				  		System.out.println("Either k is greater than two, j is less than two or both");
				 			  }
				 			  
				 			  if (j == 5){
				 				 num4 = Verify.random(7);
				 			  		for (int k1 = 0; k1 < num4; k1++)
				 			  			System.out.println("j is equal to 5");
				 			  }
				 			  
				 			  
							}
							
				}
			}
				
	}
		
		
}

