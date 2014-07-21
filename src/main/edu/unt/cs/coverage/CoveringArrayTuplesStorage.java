package edu.unt.cs.coverage;

import java.util.List;

import edu.unt.cs.coverage.trie.ArrayFactorTrie;

public interface CoveringArrayTuplesStorage {

	boolean updateTupleCoverage(int[] row);

	int getNumTuples();

	int getFactorTuples();

	long getMemoryUsage();

    void shutdown(boolean closeCudd);

	long getWeight();
	
	int rankcount(int[] row);
	
	String toJSON();

	String getInteractionStringFromBDD(ArrayFactorTrie arrayFactorTrie);
	
}
