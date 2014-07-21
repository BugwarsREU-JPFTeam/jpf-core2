package edu.unt.cs.coverage.trie;

import java.util.Iterator;

import edu.unt.cs.coverage.CoveringArrayTuplesStorage;

public class AbstractFactorTrie<V> implements FactorTrie<V> {

	public static int numFactors;
	public static int strength;

	int key;
	V value;
	String name;
	
	@Override
	public TrieIterator<V> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void insert(int[] is, V v, String[] factorNames) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void remove(int[] k) {
		// TODO Auto-generated method stub
		
	}
	
	public V get(int[] k){
		return null;
	}

	@Override
	public V getValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setValue(V value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getKey() {
		// TODO Auto-generated method stub
		return key;
	}

	
	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}

	@Override
	public String toJSON(CoveringArrayTuplesStorage coveringArrayTuplesBDDTrie) {
		// TODO Auto-generated method stub
		return null;
	}


}
