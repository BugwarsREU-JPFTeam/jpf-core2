package gov.nasa.jpf.search.heuristic;

import java.util.ArrayList;

import edu.unt.cs.coverage.CoveringArrayTuplesRanking;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.VM;

public class CombinatorialBranchCountHeuristic extends SimplePriorityHeuristic  {
	ArrayList<Integer> TreePath = getpathTracker();
	CoveringArrayTuplesRanking tuples = getStrength2();
	ArrayList<Integer> current = getcurrentpath();
	public CombinatorialBranchCountHeuristic(Config config, VM vm) {
		super(config, vm);
		Search.log.info("Combinatorial Branch Counting Heuristic");
		// TODO Auto-generated constructor stub
	}

	@Override
	protected int computeHeuristicValue() {
		//(want to be the count of the tuple + the count from pathtracker)-depth (if we wish)....
	return 1;
	}

}
