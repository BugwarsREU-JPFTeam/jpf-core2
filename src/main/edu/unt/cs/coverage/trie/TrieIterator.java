package edu.unt.cs.coverage.trie;

import java.util.Iterator;

public interface TrieIterator<V> extends Iterator<FactorTrie<V>> {

	int[] path();

}
