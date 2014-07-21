

//
// Copyright (C) 2006 United States Government as represented by the
// Administrator of the National Aeronautics and Space Administration
// (NASA).  All Rights Reserved.
// 
// This software is distributed under the NASA Open Source Agreement
// (NOSA), version 1.3.  The NOSA has been approved by the Open Source
// Initiative.  See the file NOSA-1.3-JPF at the top of the distribution
// directory tree for the complete NOSA document.
// 
// THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY WARRANTY OF ANY
// KIND, EITHER EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT
// LIMITED TO, ANY WARRANTY THAT THE SUBJECT SOFTWARE WILL CONFORM TO
// SPECIFICATIONS, ANY IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
// A PARTICULAR PURPOSE, OR FREEDOM FROM INFRINGEMENT, ANY WARRANTY THAT
// THE SUBJECT SOFTWARE WILL BE ERROR FREE, OR ANY WARRANTY THAT
// DOCUMENTATION, IF PROVIDED, WILL CONFORM TO THE SUBJECT SOFTWARE.
//
package gov.nasa.jpf.search.heuristic;

import edu.unt.cs.coverage.CoveringArrayTuplesRankingArray;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.VM;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * a search strategy class that computes all immediate successors of a given
 * state, puts them into a priority queue (the priority is provided by a
 * Heuristic strategy object), and processes states in the sequence of highest
 * priorities. Note that the queue can be search-global, i.e. we might hop
 * between search levels.
 */
public abstract class HeuristicSearch extends Search {

	static final String DEFAULT_HEURISTIC_PACKAGE = "gov.nasa.jpf.search.heuristic.";
	protected int searchcounter=0;
	protected HeuristicState parentState;
	protected List<HeuristicState> childStates;
	protected HeuristicState initial;
	protected ArrayList<Integer> pathTracker=new ArrayList<Integer>();//MOD:used to keep track of branch counts
	protected boolean isPathSensitive = false;
	protected boolean errorfound = false;//MOD: was an error found?
	protected boolean readytorestart=false;//MOD: are we ready to restart search?
	protected ArrayList<CustomPathVar> paths=new ArrayList<CustomPathVar>();//MOD: list of paths encountered
	protected ArrayList<Integer> currentpath=new ArrayList<Integer>();//MOD:the current path in search run
	protected boolean repeat=false;
	protected int endrun=0;
	protected Map<Integer,Integer> manual=new HashMap<Integer,Integer>();//Emod map
	protected int[]factors=new int[4];//Emod for manual exploration //not automated
	protected int[]names=new int[4];//Emod name list for manual exploration //not automated
	protected CoveringArrayTuplesRankingArray rankingarray;//EMOD
	protected String[]namesstrings=new String[4];//EMOD not automated
	protected int[][] factorchoices;//EMODthis will map child states to factor choices for row generation...
	protected ArrayList<Integer>IDsthisRun=new ArrayList<Integer>();//EMOD used to guess next state generated for heuristic computation....
	
	/*
	 * do we use A* adaptation of state priorities, i.e. have a distance + cost
	 * heuristic (in this context, we just use the path length as the
	 * "distance")
	 */
	protected boolean useAstar;

	/*
	 * a beam search is a HeuristicSearch with a state queue that is reset at
	 * each search level (i.e. it doesn't hop between search levels when
	 * fetching the next state from the queue)
	 */
	protected boolean isBeamSearch;

	public HeuristicSearch(Config config, VM vm) {
		super(config, vm);

		useAstar = config.getBoolean("search.heuristic.astar");
		isBeamSearch = config.getBoolean("search.heuristic.beam_search");
	}

	// add the current state to the queue
	protected abstract HeuristicState queueCurrentState();

	// return the next queued state, which becomes the new parentState
	// implementors can also reset or modify the queue
	protected abstract HeuristicState getNextQueuedState();

	public abstract void resetQueue();//MOD METHOD
	public abstract int getQueueSize();

	public abstract boolean isQueueLimitReached();

	public int[][] getFactorChoices(){//EMOD Getter
		return factorchoices;
	}
	public ArrayList<Integer> getIDsthisRun(){//EMOD GETTER
		return IDsthisRun;
	}
	public HeuristicState getParentState() {
		return parentState;
	}
	public ArrayList<Integer> getcurrentpath(){//getter for MOD currentpath
		return currentpath;
	}

	public List<HeuristicState> getChildStates() {
		return childStates;
	}
	
	public ArrayList<Integer> getpathTracker(){//getter for MOD pathTracker...
		return pathTracker;
	}
	public CoveringArrayTuplesRankingArray getRankingArray(){// getter for EMOD rankingarray...
		return rankingarray;
	}
	public void setPathSensitive(boolean isPathSensitive) {
		this.isPathSensitive = isPathSensitive;
	}

	void backtrackToParent() {
		backtrack();

		depth--;
		notifyStateBacktracked();
	}

	/*
	 * generate the set of all child states for the current parent state
	 * 
	 * overriding methods can use the return value to determine if they have to
	 * process the childStates, e.g. to compute priorities that require the
	 * whole set
	 * 
	 * @returns false if this is cut short by a property termination or explicit
	 * termination request
	 */
	protected boolean generateChildren() {
		System.out.println("Hash of parent is " + parentState.hashCode()
				+ " and id is " + parentState.stateId + " and depth is "+parentState.getDepth());
		childStates = new ArrayList<HeuristicState>();

		while (!done) {

			if (!forward()) {
				notifyStateProcessed();
				return true;
			}

			depth++;
			System.out.println("advance!");
			
			notifyStateAdvanced();

			if (currentError != null) {
				errorfound=true;//mod
				notifyPropertyViolated();
				if (hasPropertyTermination()) {
					return false;
				}

				// note that we don't store the error state anymore, which means
				// we
				// might encounter it along different paths. However, this is
				// probably
				// what we want for search.multiple_errors.

			} else{	if (!isEndState() && !isIgnoredState()) {//else statement mod
					boolean isNewState = isNewState();
					
					if(searchcounter!=0)isNewState=true;//MOD MOD MOD Works!
					
					if (isNewState && depth >= depthLimit) {
						// we can't do this before we actually generated the VM
						// child state
						// since we don't want to report DEPTH_CONSTRAINTs for
						// parents
						// that have only visited or end state children
						notifySearchConstraintHit("depth limit reached: "
								+ depthLimit);

					} else if (isNewState || isPathSensitive) {

						if (isQueueLimitReached()) {
							notifySearchConstraintHit("queue limit reached: "
									+ getQueueSize());
						}

						HeuristicState newHState = queueCurrentState();
						if (newHState != null) {
							System.out.println("child made! Hash code is "
									+ newHState.hashCode() + " and ID is "
									+ newHState.stateId+" and depth is "+newHState.getDepth());
							childStates.add(newHState); // add breakpoint here
							IDsthisRun.add(newHState.stateId);//EMOD
							System.out.println("added " +IDsthisRun.get(IDsthisRun.size()-1)+" to list of ids this run");
							if(parentState.stateId<factors.length && parentState.stateId!=-1){//EMOD: parent is a factor
								int i=0;
								while(factorchoices[parentState.stateId][i]!=0){
									if(factorchoices[parentState.stateId][i]!=newHState.stateId) i++;
									else break;
								}
								factorchoices[parentState.stateId][i]=newHState.stateId;
							}//end EMOD if....
							notifyStateStored();
						}// add breakpoint here
					}

				} else if(isEndState()) {//MOD
					endrun++;//mod
					if(endrun>2){//mod if
					truncatepath(currentpath);//EMOD cutting down path....
					CustomPathVar goo=new CustomPathVar(deepcopy(currentpath));
					boolean isunique=true;
					//here check diff
					if(paths.size()==0)isunique=true;
					else{
					for(int y=0;y<paths.size();y++){
					if(!goo.isDiff(paths.get(y))) isunique=false;
					}
					}
					if(!isunique){
						repeat=true;//mod trying this
						System.out.println("seen before...");
						return false;
					}
					else{
					paths.add(goo);//MOD:adding path to list
					System.out.println("added following path to list");
					for(int i=0;i<currentpath.size();i++){
						if(i==currentpath.size()-1)System.out.print(currentpath.get(i)+"\n");
						else System.out.print(currentpath.get(i)+"-->");
					}
					int[] stuff=loadable(goo);//start EMOD
					System.out.println("loaded row was...(view next line) ");
					if(stuff!=null){
					for(int k=0;k<stuff.length;k++){
						System.out.print(stuff[k]+" ");
					}
					rankingarray.updateTupleCoverage(stuff);//end EMOD
					}
					done=true;//MOD
					readytorestart=true;//MOD
					return false;//MOD
					// end state or ignored transition
					}
					}}//mod end if
			}
			System.out.println("backtrack!");
			backtrackToParent();
		}
		return false;
	}

	private void restoreState(HeuristicState hState) {
		vm.restoreState(hState.getVMState());

		// note we have to query the depth from the VM because the state is
		// taken from the queue
		// and we have no idea when it was entered there
		depth = vm.getPathLength();
		notifyStateRestored();
	}

	@Override
	public void search() { // commented out code here is for attempting to loop
							// a heuristic search on state space
		if(searchcounter==0){//mod
			for(int f=0;f<5000;f++)pathTracker.add(0);//MOD populate arraylist
			makeMap();//start EMOD
			makeFactors();
			makeNames();
			rankingarray=new CoveringArrayTuplesRankingArray(2, manual, namesstrings, factors); //not automated
			factorchoices= new int[factors.length][3];//initialization of factorchoices EMOD not automated
		}//mod
		
		if(searchcounter==0)initial=queueCurrentState();//MOD MOD
		
		else queueCurrentState();
		
		notifyStateStored();

		// kind of stupid, but we need to get it out of the queue, and we
		// don't have to restore it since it's the first one
		parentState = getNextQueuedState();
		done = false;
		notifySearchStarted();
		if (!hasPropertyTermination()) {
			generateChildren();
			
		
			
			while (!done && (parentState = getNextQueuedState()) != null) {
				if(!repeat){//mod trying this
				currentpath.add(parentState.getStateId());//MOD
				System.out.println("added state "+parentState.stateId+" to current path");//MOD
				restoreState(parentState);
				generateChildren();
				}
				else{//mod trying this
					while(currentpath.size()!=parentState.getDepth()-1){
						currentpath.remove(currentpath.size()-1);
					}
					currentpath.add(parentState.getStateId());
					depth=parentState.getDepth();
					restoreState(parentState);
					generateChildren();
				}//end mod trying this
				}
				
		}
		if(errorfound){//MOD IF
			notifySearchFinished();
			terminate();//will this end it?....yes!
		}
		else if(getQueueSize()==0){
			notifySearchFinished();
			terminate();
		}
		else if(readytorestart){//MOD
			depth=0;//MOD
			searchcounter++;
			for(int y=0;y<currentpath.size();y++){
				pathTracker.set(currentpath.get(y)+1, pathTracker.get(currentpath.get(y)+1)+1);//MOD:here we increment the branch count
			}
			currentpath.clear();//make sure you have loaded the current path into path list b4 this!
			IDsthisRun.clear();//EMOD
			resetQueue();
			restoreState(initial);
			search();
		}//END MOD
	
	}

	@Override
	public boolean supportsBacktrack() {//test comment
		
		// we don't do multi-level backtracks, but automatically do
		// backtrackToParent()
		// after each child state generation
		return false;
	}
	
	public ArrayList<Integer> deepcopy(ArrayList<Integer> list){
		ArrayList<Integer> newlist=new ArrayList<Integer>();
		for(int i=0;i<list.size();i++){
			newlist.add(list.get(i));
		}
		return newlist;
	}

public void makeMap(){//EMOD METHOD TO POPULATE MAP not automated
		manual.put(4, 3);
	}
public void makeFactors(){//EMOD METHOD TO POPULATE FACTORS not automated
	for (int i=0;i<factors.length;i++){
		factors[i]=3;
	}
}
public void makeNames(){//EMOD METHOD TO POPULATE NAMES not automated
	for(int i=0;i<names.length;i++){
		names[i]=i;
	}
}
public void makeStringNames(){//EMOD Method to populate NamesStrings to satisfy coveringarraytuplesrankingarray object.... not automated
	for(int i=0;i<namesstrings.length;i++){
		namesstrings[i]="State "+i;
	}
}
public void truncatepath(ArrayList<Integer> foo){//not automated
	int i=1;//goal is to cut down to most recent node of each depth....
	int j=2;
	while (true){
		while(foo.get(i+1)<4){
			foo.remove(i);
		}
		if(j==foo.size()-1) break;
		else foo.remove(j);
	}
	
}

public int[] loadable(CustomPathVar x){//EMOD METHOD TO MAKE LOADABLE ARRAY Not automated
	int[] path =new int[names.length];//will return path
	for(int i=0;i<names.length;i++){
		int ind=x.binaryfindindex(names[i]);
		System.out.println("ind is "+ind);
		if(ind!=-1){//if it is there
			int input=x.getIDs().get(ind+1);
			int j=0;
			System.out.println("input is "+input);
			while(factorchoices[x.getIDs().get(ind)][j]!=input){
				System.out.println("a choice is "+factorchoices[ind][j]);
				j++;
				if(j==3){//so we know if we are dealing with first choice generation...
					return null;
				}
			}
			path[i]=j;
		}
		else path[i]=-1;
	}
	return path;
}

}

	