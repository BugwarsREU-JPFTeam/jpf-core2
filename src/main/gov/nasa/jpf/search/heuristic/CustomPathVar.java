package gov.nasa.jpf.search.heuristic;

import java.util.ArrayList;

public class CustomPathVar {
private ArrayList<Integer> IDs = new ArrayList<Integer>();

public CustomPathVar(ArrayList<Integer> IDs){
this.IDs=IDs;	
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

public int binaryfindindex(int x){//will work since path will always be in ascending order....
int lo=0;
int hi=IDs.size()-1;
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
