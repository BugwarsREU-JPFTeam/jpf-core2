package edu.unt.cs.coverage.trie;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class DefaultFactorTrie<V>  extends AbstractFactorTrie<V> {

	Map<Integer, DefaultFactorTrie<V>> children;
	long numCovered;
	
	public DefaultFactorTrie(int key){
		this.key = key;
		this.value = null;
		children = new HashMap<Integer, DefaultFactorTrie<V>>();
		numCovered = 0;
	}
	
	public void insert(int[] k, V v) {
		insert(k, 0, v);
	}
		
	public void insert(int[] k, int ki, V v) {
		
		if(ki + 1 == k.length){//last element
			value = v;
		}
		else{
			int kv = k[ki+1];
			DefaultFactorTrie<V> child = children.get(kv);	
			if(child == null){
				children.put( kv, new DefaultFactorTrie<V>(k[ki+1]));
			}			
			children.get(kv).insert(k, ki+1, v);
		}		
	}

	public void remove(int[] k) {
		 remove(k, 0);
	}
	
	
	public boolean remove(int[] k, int ki) {
		//return value indicates that this node can be removed
		
		if(ki + 1 == k.length){//last element
			return true;
		}
		else{
			int kv = k[ki+1];
			DefaultFactorTrie<V> child = children.get(kv);
			
			boolean childHasNoChildren = child.remove(k, ki+1);						
			if(childHasNoChildren){
				children.remove(kv);
			}			
			return children.size() == 0;
		}		
		
	}

	@Override
	public TrieIterator<V> iterator() {
		// TODO Auto-generated method stub
		return new DefaultTrieIterator<V>(this);
	}

	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("K: ").append(key).append(" V: ").append(value).append(" C: ");
		for(Integer c : children.keySet())
			sb.append(c).append(" ");
		return sb.toString();
			
	}
	
	public V get(int[] k){
		if(k[0] != key)
			return null;
		
		DefaultFactorTrie<V> root = this;
		V value;
		for(int ki = 1; ki < k.length; ki++){
			root = root.children.get(k[ki]);
			if(root == null)
				return null;
		}
		
		return root.value;
	}

	public V getValue(){
		return value;
	}
	
	public void setValue(V value){
		this.value = value;
	}
}
