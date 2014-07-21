package edu.unt.cs.coverage;

import java.util.Arrays;
import java.util.Map;

public class CoveringArrayTuplesRankingArray extends CoveringArrayTuplesRanking {
	int[] tuplesCovered;//mod change from boolean to int array
	public CoveringArrayTuplesRankingArray(int strength,
			Map<Integer, Integer> factorsPerLevel,
			String[] factorNames, int[] factorLevels) {
		super(strength, factorsPerLevel, factorNames, factorLevels);
		int n  =binomTable[numSymbols][strength]+1;
		tuplesCovered = new int[n];//mod change from boolean to int
	}

	protected void markCovered(int[] factorTuple, Object[] levelTuple){//mods to count rather than cover
		//mark covered
	    Integer[] lTuple = Arrays.copyOf(levelTuple, levelTuple.length, Integer[].class);
	    int rank =  kSubsetLexRank(lTuple, numSymbols);
	    if(rank >= tuplesCovered.length){
	    	System.out.println("Rank is greater than array length: " + rank + " " + tuplesCovered.length);
	    }
	    int old = tuplesCovered[rank];//mod boolean to int
		if(old==0){//mod !old to old==0
			numCoveredTuples++;
		}
		tuplesCovered[rank]++;//mod from true to ++

	}

	
	public int getCount(int[] factorTuple, Object[] levelTuple){//for retrieval
		 Integer[] lTuple = Arrays.copyOf(levelTuple, levelTuple.length, Integer[].class);
		    int rank =  kSubsetLexRank(lTuple, numSymbols);
		    if(rank >= tuplesCovered.length){
		    	System.out.println("Rank is greater than array length: " + rank + " " + tuplesCovered.length);
		    }
		    return tuplesCovered[rank];
	}
	
	@Override
	public int rankcount(int[]row){//EMOD: *here might want to copy whole update method....did...
		int[] factorTuple = new int[strength+1];
		Object[] levelTuple = new Object[strength+1]; //can be either DD or int
		for(int i = 1; i <= strength; i++){
			factorTuple[i]=i;
			levelTuple[i]=setLevelTupleValueFromRow(row, factorTuple,i);
		}
		int returnable=0;//EMOD:this is the part that i changed...normally i would return next line instead i add up all tuple counts....
		returnable+=getCount(factorTuple,levelTuple);
		while(kSubsetLexSuccessor(factorTuple,numFactors)){//get rest of factor tuples
			for(int i = 1;i<=strength;i++){
				levelTuple[i]=setLevelTupleValueFromRow(row, factorTuple, i);
			}
			returnable+=getCount(factorTuple,levelTuple);
		}
		return returnable;
	}
	
	
	
	public int[] getTuplesCovered(){//MOD Getter
		return tuplesCovered;
	}
	
}

