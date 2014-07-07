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



}
