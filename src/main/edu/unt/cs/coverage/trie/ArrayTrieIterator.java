package edu.unt.cs.coverage.trie;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ArrayTrieIterator<V> extends AbstractTrieIterator<V> implements TrieIterator<V> {

	int[] currentPathIt;
	ArrayFactorTrie<V> nextRoot;
	int nextChild;


	public ArrayTrieIterator(ArrayFactorTrie<V> arrayFactorTrie) {
		super(arrayFactorTrie);
		currentPathIt = new int[AbstractFactorTrie.strength];
		nextRoot = null;
		nextChild = -1;
	}

	@Override
	public boolean hasNext() {
		//true if first and path exists, or there is another path
		if(currentPath.size() == 0){
			//need to try and build a path
			getFirstPathFromRoot((ArrayFactorTrie<V>)trie);
			return currentPath != null;
		}
		else{
			//search from leaf node up for a next path
			//			System.out.println("Implement");


			Iterator<FactorTrie<V>> nodes = currentPath.iterator();
			for(int depth = currentPathIt.length-1; depth >= 0 ; depth--){
				int it = currentPathIt[depth];
				ArrayFactorTrie<V> root = (ArrayFactorTrie<V>) nodes.next();		
				ArrayFactorTrie<V>[] siblings = root.children;
				for(int s = it+1; s < siblings.length; s++){
					if(siblings[s] != null){
						nextRoot = root;
						nextChild = s;
						return true;
					}
				}
			}
			return false;
		}

	}

	private void getFirstPathFromRoot(ArrayFactorTrie<V> root){
		while(root.childrenRemaining > 0){
			ArrayFactorTrie<V>[] children = root.children;


			for(int child = 0; child < root.children.length; child++){
				if(root.children[child] != null){
					currentPathIt[root.depth] = child;
					break;		 
				}
			}

			currentPath.push(root);
			root = children[currentPathIt[root.depth]];
		}

		current = root;
	}


	@Override
	public FactorTrie<V> next() {
		if(first){
			first = false;
			return current;
		}

		//find iterator on currentpath that hasnext (up tree)
		int it = -1;
		ArrayFactorTrie<V> root = null;
		int depth = -1;
		
		if(nextRoot == null){ //didn't find next node from hasNext()

			depth = currentPathIt.length-1;
			for(; depth >= 0; depth--){
				it = currentPathIt[depth];
				root = (ArrayFactorTrie<V>)currentPath.pop();		
				ArrayFactorTrie<V>[] siblings = root.children;
				for(int s = it+1; s < siblings.length; s++){
					if(siblings[s] != null){
						it = s;
						break;
					}
				}
				if(it != currentPathIt[depth]){
					//found a new path
					break;
				}
				else{
					//didn't find a new path at this depth
					it = -1;
					root = null;
				}
			}
		}
		else{
			it = nextChild;
			while (root != nextRoot)
				root = (ArrayFactorTrie<V>)currentPath.pop();
			depth = root.depth;
			
			nextChild = -1;
			nextRoot = null;
			
		}
		
		//set rest of path (down tree)
		if(it >= 0){ //have a valid sibling with a path
			currentPathIt[depth] = it;
			currentPath.push(root);


			root = ((ArrayFactorTrie<V>)root).children[it];
			getFirstPathFromRoot((ArrayFactorTrie<V>)root);
			return current;
		}
		else{
			return null;
		}


	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub

	}



}
