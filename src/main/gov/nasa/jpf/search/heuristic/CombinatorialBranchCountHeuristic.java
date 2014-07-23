package gov.nasa.jpf.search.heuristic;

import java.io.IOException;
import java.util.ArrayList;

import edu.unt.cs.coverage.CoveringArrayTuplesRanking;
import edu.unt.cs.coverage.CoveringArrayTuplesRankingArray;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.VM;

public class CombinatorialBranchCountHeuristic extends SimplePriorityHeuristic  {//not automated.....
	ArrayList<Integer> TreePath = getpathTracker();
	ArrayList<Integer> current = getcurrentpath();
	int[][] choices= getFactorChoices();
	int strengthdesired=2;
	CoveringArrayTuplesRankingArray tuples;
	public CombinatorialBranchCountHeuristic(Config config, VM vm) throws IOException {
		super(config, vm);
		Search.log.info("Combinatorial Branch Counting Heuristic");
		// TODO Auto-generated constructor stub
	}

	@Override
	protected int computeHeuristicValue() {
		//(want to be the count of the tuple + the count from pathtracker)-depth (if we wish)....
		int lastID;
		if(getIDsthisRun().isEmpty()){//then we dont care about it....give it a zero
			return 0;
		}
		
		else {//now pretend to have a path with this yet to be computed heuristic state.....
			lastID=getIDsthisRun().get(getIDsthisRun().size()-1);
			if(lastID==6){
				current.add(8);
			}
			else current.add(lastID+1);
		}
		
		
		if(current.size()<strengthdesired+1){//cannot make tuple of size desired....so we just do normal branch counting
			System.out.println("Heuristic computed is "+(TreePath.get(getStateId()+1)-depth));//uncommment for branch count combo
			//System.out.println("Heuristic computed is "+(0-depth));//uncomment for vanilla combinatorial branch counting...
			current.remove(current.size()-1);//removing...
			return TreePath.get(getStateId()+1)-depth;//uncomment for branch count combo
			//return 0-depth; //uncomment for vanilla combinatorial branch counting....acts weird cause we need to be greedy!
		}
		else{//we can make a tuple!
			CustomPathVar goo= new CustomPathVar(current);
			int[] partialrow= loadable(goo);
		
			if (partialrow==null){//do normal branch counting if we have not seen this choice b4....
				System.out.println("semi-special... Heuristic computed is "+(TreePath.get(getStateId()+1)-depth));// uncomment for branch count combo
				//System.out.println("semi-special.... Heuristic computed is "+(0-depth));//uncomment for vanilla combinatorial branch counting...
				current.remove(current.size()-1);//removing...
				return TreePath.get(getStateId()+1)-depth;//uncomment for branch count combo
				//return 0-depth;//uncomment for vanilla combinatorial branch counting....
			}
			System.out.println("partial row is (see below)");
			for(int i=0;i<4;i++){
				System.out.print(partialrow[i]+" ");
			}
			tuples=getRankingArray();
			int count = tuples.rankcount(partialrow);
			//System.out.println("Special!!! Heuristic computed is "+(count-depth));//uncomment for vanilla combinatorial branch counting...
			System.out.println("Special!!!! Heuristic computed is "+(TreePath.get(getStateId()+1)-depth+count));//uncomment for branch count combo
			current.remove(current.size()-1);//removing
			return TreePath.get(getStateId()+1)-depth+count;//uncomment for branch count combo
			//return count-depth;//uncomment for vanilla combinatorial branch counting....
		}
	}

}
