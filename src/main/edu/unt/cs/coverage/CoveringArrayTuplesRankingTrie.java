package edu.unt.cs.coverage;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.unt.cs.coverage.trie.AbstractFactorTrie;
import edu.unt.cs.coverage.trie.ArrayFactorTrie;
import edu.unt.cs.coverage.trie.FactorTrie;
import edu.unt.cs.coverage.trie.TrieIterator;
//import jdd.JDD;
//import jdd.JDDNode;

public class CoveringArrayTuplesRankingTrie extends CoveringArrayTuplesRanking {
	FactorTrie<RankingTrieArray> tuplesCovered;

	public CoveringArrayTuplesRankingTrie(int strength,
			Map<Integer, Integer> factorsPerLevel,
			String[] factorNames,
			int[] factorLevels) {
		super(strength, factorsPerLevel, factorNames, factorLevels);
		// TODO Auto-generated constructor stub
		
		int[] factorTuple = new int[strength+1];
		Object[] levelTuple = new Object[strength+1]; //can be either DD or int
		int numFactorTupleSymbols = 0;
		int numTuples = 1;
		for(int i = 1; i <= strength; i++){
			factorTuple[i]=i;			
			numFactorTupleSymbols += levelPerFactor[i];
			numTuples *= levelPerFactor[i];
		}
		AbstractFactorTrie.numFactors = numFactors;
		AbstractFactorTrie.strength = strength;
		tuplesCovered = new ArrayFactorTrie<RankingTrieArray>(0, 0, factorNames[0], null);
		int tuples = binomTable[numFactorTupleSymbols][strength];
		tuplesCovered.insert(factorTuple, new RankingTrieArray(tuples), factorNames);
		
		//get rest of factor tuples
		while(kSubsetLexSuccessor(factorTuple, numFactors)){		
			numFactorTupleSymbols = 0;
			numTuples = 1;
			for(int i = 1; i <= strength; i++){
				numFactorTupleSymbols += levelPerFactor[factorTuple[i]];	
				numTuples *= levelPerFactor[factorTuple[i]];
			}
			tuples = binomTable[numFactorTupleSymbols][strength];
			tuplesCovered.insert(factorTuple, new RankingTrieArray(tuples), factorNames);		
		}

	}

	public boolean updateTupleCoverage(List<Integer> row){
		//JDD.PrintCacheInfo();
		TrieIterator<RankingTrieArray> it = tuplesCovered.iterator();
		Set<int[]> toRemove = new HashSet<int[]>();
		
		while(it.hasNext()){
			FactorTrie<RankingTrieArray> factorCoverage = it.next();
			RankingTrieArray factorValueTuples = (RankingTrieArray)factorCoverage.getValue(); 
			int[] factorTuple = it.path();
			//System.out.println(Arrays.toString(factorTuple));
			
			
			int numFactorTupleSymbols = 0;
			Integer[] levelTuple = new Integer[strength+1]; 
			int offset = 0;
			for(int i = 1; i <= strength; i++){			
				levelTuple[i] = (Integer) setLevelTupleValueFromRow(row, factorTuple, i)+offset;
				offset += levelPerFactor[factorTuple[i]];
				numFactorTupleSymbols += levelPerFactor[factorTuple[i]];				
			}
		    int rank =  kSubsetLexRank(levelTuple, numFactorTupleSymbols);
			boolean old = factorValueTuples.coveredTuples[rank];
			if(!old){
				numCoveredTuples++;
				factorValueTuples.tuplesToCover--;
			}
			factorValueTuples.coveredTuples[rank] = true;
			if(factorValueTuples.tuplesToCover == 0){
				toRemove.add(factorTuple);
			}
		}
		//System.out.println("Removed tuples: " + toRemove.size());
		for(int[] tuple : toRemove){	
			//JDD.Deref(tuplesCovered.get(tuple));
			tuplesCovered.remove(tuple);
		}
		//JDD.PrintCacheInfo();
		return numCoveredTuples == numTuples;

	}
	Object setLevelTupleValueFromRow(List<Integer> row, int[] factorTuple, int factorIndex){
		return row.get(factorTuple[factorIndex]-1)+1;
	}

	
	private class RankingTrieArray {
		boolean[] coveredTuples;
		int tuplesToCover;
		public RankingTrieArray(int tuples) {			
			this.coveredTuples = new boolean[tuples];
			this.tuplesToCover = tuples;
		}
		
	}
}



