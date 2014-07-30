package gov.nasa.jpf.search.heuristic;

import java.io.IOException;
import java.util.ArrayList;

import edu.unt.cs.coverage.CoveringArrayTuplesRanking;
import edu.unt.cs.coverage.CoveringArrayTuplesRankingArray;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.VM;

public class CombinatorialBranchCountHeuristic extends SimplePriorityHeuristic  {
	ArrayList<Integer> values = getlist_vals();
	int strengthdesired=2;//auto:need to change for diff strength
	CoveringArrayTuplesRankingArray tuples;
	public CombinatorialBranchCountHeuristic(Config config, VM vm) throws IOException {
		super(config, vm);
		Search.log.info("Combinatorial Branch Counting Heuristic");
		// TODO Auto-generated constructor stub
	}

	@Override
	protected int computeHeuristicValue() {
		//(want to be the count of the tuple + the count from pathtracker)-depth (if we wish)....
		if(getStateId()<=0) return 0;//dont care
		else if(depth<=2) return 0-depth;
		else{//we can make a tuple!
			Row[depth-2]=(Integer) VM.getVM().getChoiceGenerator().getNextChoice();
			tuples=getRankingArray();
			System.out.println("partial row is (see below)");
			for(int i=0;i<Row.length;i++){
				System.out.print(Row[i]+" ");
			}
			int count = tuples.rankcount(Row);
			System.out.println("Special!!! Heuristic computed is "+(count-depth));//uncomment for vanilla combinatorial branch counting...
			Row[depth-2]=-1;//removing
			return count-depth;//uncomment for vanilla combinatorial branch counting....
		}
	}

}
