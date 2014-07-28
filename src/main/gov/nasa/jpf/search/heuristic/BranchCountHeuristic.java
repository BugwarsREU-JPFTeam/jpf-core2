package gov.nasa.jpf.search.heuristic;

import java.io.IOException;
import java.util.ArrayList;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.VM;

public class BranchCountHeuristic extends SimplePriorityHeuristic {
	ArrayList<BranchCountState> goo=getsuperpathTracker();
	
	public BranchCountHeuristic(Config config, VM vm) throws IOException {
		super(config, vm);
		Search.log.info("Branch Counting Heuristic");
		// TODO Auto-generated constructor stub
	}

	@Override
	protected int computeHeuristicValue() {
		if(getStateId()<1)return 0;
		else{
			System.out.println(getStateId());
			for(int z=0;z<goo.size();z++){
				if(goo.get(z).canbeassigned(depth, (Integer) vm.getChoiceGenerator().getNextChoice())){
					System.out.println("heuristic value computed is "+(goo.get(z).count-depth)+" because depth is "+depth+" and count is "+goo.get(z).count);
					return goo.get(z).count-depth;
				}
			}
		}
		System.out.println("ERROR!");
		return 500;

}	}
