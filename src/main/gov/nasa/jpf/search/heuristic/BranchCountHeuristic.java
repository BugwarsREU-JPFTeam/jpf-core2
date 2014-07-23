package gov.nasa.jpf.search.heuristic;

import java.io.IOException;
import java.util.ArrayList;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.VM;

public class BranchCountHeuristic extends SimplePriorityHeuristic {
	ArrayList<Integer> goo = getpathTracker();
	
	public BranchCountHeuristic(Config config, VM vm) throws IOException {
		super(config, vm);
		Search.log.info("Branch Counting Heuristic");
		// TODO Auto-generated constructor stub
	}

	@Override
	protected int computeHeuristicValue() {
		System.out.println("Heuristic computed is "+(goo.get(getStateId()+1)-depth));
		return goo.get(getStateId()+1)-depth;
	}

}
