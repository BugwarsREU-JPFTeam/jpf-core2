package edu.unt.cs.coverage;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;





import edu.unt.cs.coverage.trie.ArrayFactorTrie;



public class CoveringArrayTuplesRanking extends AbstractCoveringArrayTuples implements CoveringArrayTuplesStorage {
	
	
	public CoveringArrayTuplesRanking(int strength, Map<Integer, Integer> factorsPerLevel, String[] factorNames, int[] factorLevels) {
		super(strength, factorsPerLevel, factorNames, factorLevels);
		
		
	}


	
   public void shutdown(boolean closeCudd){}




//	//TODO: Can be made abstract
//	public boolean updateTupleCoverage(List<Integer> row){	
//
//
//		int[] factorTuple = new int[strength+1];
//		int[] levelTuple = new int[strength+1];
//		for(int i = 1; i <= strength; i++){
//			factorTuple[i]=i;
//			levelTuple[i]=row.get(factorTuple[i]-1)+factorSymbolOffset.get(factorTuple[i])+1;			
//		}
//		//mark covered
//		int rank =  kSubsetLexRank(levelTuple, numSymbols);
//		boolean old = tuplesCovered[rank];
//		if(!old){
//			numCoveredTuples++;
//		}
//		tuplesCovered[rank] = true;
//
//		//get rest of factor tuples
//		while(kSubsetLexSuccessor(factorTuple, numFactors)){						
//			for(int i = 1; i <= strength; i++){
//				levelTuple[i]=row.get(factorTuple[i]-1)+factorSymbolOffset.get(factorTuple[i])+1;
//			}
//
//			rank =  kSubsetLexRank(levelTuple, numSymbols);
//			old = tuplesCovered[rank];
//			if(!old){
//				numCoveredTuples++;
//			}
//			tuplesCovered[rank] = true;
//		}
//		return numCoveredTuples == numTuples;
//
//	}
	
	Object setLevelTupleValueFromRow(int[] row, int[] factorTuple, int factorIndex){
		if(row[factorTuple[factorIndex]-1]==-1) return null;//FMOD
		return row[factorTuple[factorIndex]-1]+factorSymbolOffset[factorTuple[factorIndex]]+1;
	}
	
	int binCoef(int n, int r)
	/*
	 **  Computes the binomial coefficient "n choose r"
	 */
	{
		//Pair<Integer, Integer> bpair = new Pair<Integer, Integer>(n, r);
		int value = binomTable[n][r];
		return value;

	}

	



	public static void main(String[] args){
		int strength = 3;
		Map<Integer, Integer> factorsPerLevel = new HashMap<Integer, Integer>();				
		List<int[]> coveringArray = new ArrayList<int[]>();


		factorsPerLevel.put(2, 2);
		factorsPerLevel.put(3, 1);
//		coveringArray.add(Arrays.asList(0, 0, 2));
//		coveringArray.add(Arrays.asList(0, 1, 2));
//		coveringArray.add(Arrays.asList(1, 0, 2));
//		coveringArray.add(Arrays.asList(1, 1, 2));
//		coveringArray.add(Arrays.asList(0, 0, 1));
//		coveringArray.add(Arrays.asList(0, 1, 1));
//		coveringArray.add(Arrays.asList(1, 0, 1));
//		coveringArray.add(Arrays.asList(1, 1, 1));
//		coveringArray.add(Arrays.asList(0, 0, 0));
//		coveringArray.add(Arrays.asList(0, 1, 0));
//		coveringArray.add(Arrays.asList(1, 0, 0));
//		coveringArray.add(Arrays.asList(1, 1, 0));

		//		factorsPerLevel.put(2, 1);
		//		factorsPerLevel.put(3, 1);
		//		coveringArray.add(Arrays.asList(0, 0));
		//		coveringArray.add(Arrays.asList(0, 1));
		//		coveringArray.add(Arrays.asList(0, 2));
		//		coveringArray.add(Arrays.asList(1, 0));
		//		coveringArray.add(Arrays.asList(1, 1));
		//		coveringArray.add(Arrays.asList(1, 2));

		String[] factorNames = new String[3];
		int[] factorLevels = new int[3];
		CoveringArrayTuplesRanking cat = new CoveringArrayTuplesRanking(strength, factorsPerLevel, factorNames, factorLevels);

		for(int[] row : coveringArray){
			System.out.println("Row: " + row);
			if(cat.updateTupleCoverage(row))
				System.out.println("Covered all tuples");

		}

	}





	@Override
	public long getMemoryUsage() {
		// TODO Auto-generated method stub
		return   numTuples;

	}



	@Override
	protected void markCovered(int[] factorTuple, Object[] levelTuple) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public long getWeight() {
		// TODO Auto-generated method stub
		return 0;
	}



	@Override
	public String toJSON() {
		// TODO Auto-generated method stub
		return null;
	}



//	@Override
//	public boolean updateTupleCoverage(int[] row) {
//		// TODO Auto-generated method stub
//		return false;
//	}



	@Override
	public String getInteractionStringFromBDD(ArrayFactorTrie arrayFactorTrie) {
		// TODO Auto-generated method stub
		return null;
	}






	@Override
	public int rankcount(int[] row) {
		// TODO Auto-generated method stub
		return 0;
	}








	

}
