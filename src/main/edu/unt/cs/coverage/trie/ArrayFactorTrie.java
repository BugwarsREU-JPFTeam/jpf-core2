package edu.unt.cs.coverage.trie;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;

import edu.unt.cs.coverage.CoveringArrayTuplesStorage;

//import jdd.JDD;
//import jdd.JDDNode;

public class ArrayFactorTrie<V>  extends AbstractFactorTrie<V> {

	ArrayFactorTrie<V> parent;
	ArrayFactorTrie<V>[] children;
	public long weight;
	long[] childWeight;
	UUID id;
	int depth;
	int childrenRemaining;
	
	
	public ArrayFactorTrie(int key, int depth, String name, ArrayFactorTrie<V> parent){
		this.key = key;
		this.value = null;
		this.depth  = depth;
		this.name = name;
		int numChildren = (depth ==  AbstractFactorTrie.strength ? 0 : AbstractFactorTrie.numFactors - key - AbstractFactorTrie.strength + depth + 1);
		children = new ArrayFactorTrie[numChildren];
		weight = 0; 
		childrenRemaining = numChildren;
		this.parent= parent;
		id = UUID.randomUUID();
		childWeight = new long[numChildren];
	}
	
	public UUID getId(){
		return id;
	}

	public long getWeight(){
		if(children.length != 0){
			long sumWeight = 0;
			for(int i = 0; i < children.length; i++){
				if(children[i] != null){
					long cweight = children[i].getWeight();
					sumWeight += cweight;
					childWeight[i] = cweight;
				}
				else
					sumWeight += childWeight[i];
			
			}
			weight = sumWeight;
		}
		return weight;
	}

	public void insert(int[] k, V v, String[] factorNames) {
		insert(k, 0, v, factorNames);
	}

	public void insert(int[] k, int ki, V v, String[] factorNames) {

		if(ki + 1 == k.length){//last element
			value = v;
		}
		else{
			int kv = k[ki+1]-key-1;
			ArrayFactorTrie<V> child = children[kv];	
			if(child == null){
				children[kv] = new ArrayFactorTrie<V>(k[ki+1], depth+1, factorNames[k[ki+1]-1], this);
			}			
			children[kv].insert(k, ki+1, v, factorNames);
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
			int kv = k[ki+1]-key-1;
			ArrayFactorTrie<V> child = children[kv];	
			boolean childHasNoChildren = child.remove(k, ki+1);						
			if(childHasNoChildren){
				children[kv] = null;
				childrenRemaining--;
			}			
			return childrenRemaining == 0;
		}		

	}

	@Override
	public TrieIterator<V> iterator() {
		// TODO Auto-generated method stub
		return new ArrayTrieIterator<V>(this);
	}

	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("K: ").append(key).append(" V: ").append(value).append(" C: ");
		for(int i = 0; i < children.length; i++){
			if(children[i] != null)
				sb.append(children[i].key).append(" ");
			else
				sb.append("null").append(" ");
		}
		return sb.toString();

	}

	public V get(int[] k){
		if(k[0] != key)
			return null;

		ArrayFactorTrie<V> root = this;
		V value;
		for(int ki = 1; ki < k.length; ki++){
			root = root.children[k[ki]-key-1];
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

	public String toJSONNodes(CoveringArrayTuplesStorage storage){
		StringBuilder sb = new StringBuilder();
		//print this node
		sb.append("\n{\"id\":\"").append(id).append("\",");
		sb.append("\"component\":\"").append(name).append("\",");
		if(children.length > 0){
			sb.append("\"children\":[");
			for(AbstractFactorTrie<V> child : children){
				if(child != null){
					sb.append("\"").append(((ArrayFactorTrie<V>) child).getId()).append("\",");
				}
			}
			sb.delete(sb.length()-1, sb.length());
			sb.append("],");
			sb.append("\"coverage\":").append(weight).append(",");
			sb.append("\"interactions\":null");
		}
		else{
			sb.append("\"children\":null,");			
			sb.append("\"coverage\":").append(weight).append(",");
			sb.append("\"interactions\":[");
//			if(value instanceof JDDNode){
//				sb.append(storage.getInteractionStringFromBDD(this));				
//			}
//			else{
//				sb.append(value);
//			}
			sb.append("]");
		}
		sb.append("},");

		//print children
		for(ArrayFactorTrie<V> child : children){
			if(child != null){
				sb.append(child.toJSONNodes(storage));
			}
		}
		
		

		return sb.toString();
	}
	

	public String toJSON(CoveringArrayTuplesStorage storage){
		StringBuilder sb = new StringBuilder();

		sb.append("\"trie\":[");
		sb.append(toJSONNodes( storage));
		sb.delete(sb.length()-1, sb.length());
		sb.append("]");
		return sb.toString();


	}

	public int[] getPath() {
		int[] path = new int[strength+1];
		int index = strength;
		ArrayFactorTrie<V> node = this;
		while(node != null){
			path[index--] = node.key;
			node = node.parent;
		}

		return path;
	}
}
