package gov.nasa.jpf.search.heuristic;

import java.util.ArrayList;

public class CustomPathVar {
private ArrayList<Integer> IDs = new ArrayList<Integer>();
private int[] loadrow;

public CustomPathVar(ArrayList<Integer> IDs){
this.IDs=IDs;

}
public CustomPathVar(int[] x){
	loadrow=new int[x.length];
	for(int i=0;i<loadrow.length;i++){
		loadrow[i]=x[i];
	}
}

public boolean isDiffRow(CustomPathVar x){
	for(int i=0;i<loadrow.length;i++){
		if(loadrow[i]!=x.getLoadRow()[i]) return true;
		}
	return false;
}
	


public boolean isDiff(CustomPathVar x){
	if(IDs.size()!=x.IDs.size()){
		System.out.println("sizes not same!?");
		return true;
	}
	else{
		for (int i=0;i<IDs.size();i++){
			if(IDs.get(i)!=x.IDs.get(i)) return true;
		}
	return false;
	}
}

public ArrayList<Integer> getIDs() {
	return IDs;
}
public int[] getLoadRow(){
	return loadrow;
}

public int binaryfindindex(int x){//will work since path will always be in ascending order....
int lo=0;
int hi=IDs.size()-1;
if(IDs.get(lo)==x)return lo;
if(IDs.get(hi)==x)return hi;
while(hi>=lo){
	int mid= (hi+lo)/2;
	if(IDs.get(mid)==x) return mid;
	
	if(IDs.get(mid)>x){
		hi=mid-1;
	}
	if(IDs.get(mid)<x){
		lo=mid+1;
	}
}
return -1;//not found
}
}
