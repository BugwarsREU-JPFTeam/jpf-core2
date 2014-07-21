package edu.unt.cs.coverage.trie;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class DefaultTrieIterator<V> extends AbstractTrieIterator<V> implements TrieIterator<V> {

	LinkedList<Iterator<Integer>> currentPathIt = null;
	

	public DefaultTrieIterator(DefaultFactorTrie<V> defaultFactorTrie){
		super(defaultFactorTrie);
		
		currentPathIt = new LinkedList<Iterator<Integer>>();	
		getFirstPathFromRoot((DefaultFactorTrie<V>)trie);
				
	}


	private void getFirstPathFromRoot(DefaultFactorTrie<V> root){
		while(root.children.size() > 0){
			Map<Integer, DefaultFactorTrie<V>> children = root.children;
			Set<Integer> keys = children.keySet();
			Iterator<Integer> keysIt = keys.iterator();
			Integer key = keysIt.next();
			currentPathIt.push(keysIt);
			currentPath.push(root);
			root = children.get(key);
		}

		current = root;
	}


	@Override
	public boolean hasNext() {
		if(first) return true;
		
		Iterator<Iterator<Integer>> it = currentPathIt.iterator();
		 
		while( it.hasNext() ){
			Iterator<Integer> iti = it.next();
			if(iti.hasNext()) return true;
		}
		return false;
	}

	@Override
	public FactorTrie<V> next() {
		
		if(first){
			first = false;
			return current;
		}
		
		
		//find iterator on currentpath that hasnext (up tree)
		Iterator<Integer> it = currentPathIt.pop();
		FactorTrie<V> root = currentPath.pop();		
		while(it != null && !it.hasNext()){
			if(currentPathIt.size() > 0){
			it = currentPathIt.pop();
			root = currentPath.pop();
			}
			else {
				it = null;
				root = null;
			}
		
		}

		//set rest of path (down tree)
		if(it != null){
			currentPathIt.push(it);
			currentPath.push(root);

			Integer nextChild = it.next();
			root = ((DefaultFactorTrie<V>)root).children.get(nextChild);
			getFirstPathFromRoot((DefaultFactorTrie<V>)root);
			return current;
		}
		else{
			return null;
		}
	}

	@Override
	public void remove() {
		
	}



}
