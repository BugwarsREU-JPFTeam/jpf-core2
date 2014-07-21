package edu.unt.cs.coverage.trie;

import java.util.Iterator;


//import edu.unt.cs.coverage.CoveringArrayTuplesBDDTrie;
import edu.unt.cs.coverage.CoveringArrayTuplesStorage;


public interface FactorTrie<V> {

	
	void remove(int[] k);
	TrieIterator<V> iterator();
	void insert(int[] is, V v, String[] factorNames);
	V get(int[] k);
	V getValue();
	void setValue(V value);
	int getKey();
	String toJSON(CoveringArrayTuplesStorage coveringArrayTuplesBDDTrie);
	
}
