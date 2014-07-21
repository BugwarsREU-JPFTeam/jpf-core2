package edu.unt.cs.coverage.trie;

import java.util.LinkedList;

public class AbstractTrieIterator<V>   implements TrieIterator<V> {

	FactorTrie<V> trie = null;
	FactorTrie<V> current = null;
	boolean first;
	LinkedList<FactorTrie<V>> currentPath = null;
	
	public AbstractTrieIterator(FactorTrie<V> factorTrie) {
		first = true;
		this.trie = factorTrie;
		currentPath = new LinkedList<FactorTrie<V>>();
	}

	@Override
	public boolean hasNext() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public FactorTrie<V> next() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub

	}

	@Override
	public int[] path() {
		int[] path = new int[currentPath.size()+1];
		int node = current.getKey();
		int i = currentPath.size();
		path[i--] = node;
		for(FactorTrie<V> node1 : currentPath){
			path[i--] = node1.getKey();			
		}
		
		
		return path;
	}

}
