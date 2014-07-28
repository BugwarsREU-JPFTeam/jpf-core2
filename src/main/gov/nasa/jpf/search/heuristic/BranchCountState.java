package gov.nasa.jpf.search.heuristic;

import java.util.ArrayList;

public class BranchCountState {//DMOD
	ArrayList<Integer> IDs=new ArrayList<Integer>();
	int depth;
	int value;
	int count=0;
	
	public BranchCountState(int depth,int value){
		this.depth=depth;
		this.value=value;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
	public void addID(int x){
		IDs.add(x);
	}
	public ArrayList<Integer> getIDs(){
		return IDs;
	}
	public boolean canbeassigned(HeuristicState h){
		return (h.getDepth()==depth&&(int)h.getValueChosen()==value);
	}
	public boolean canbeassigned(int x,int y){
		return (x==depth&&y==value);
	}
	public void incrementcount(){
		count++;
	}
	public int getcount(){
		return count;
	}

}
