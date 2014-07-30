

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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * a search strategy class that computes all immediate successors of a given
 * state, puts them into a priority queue (the priority is provided by a
 * Heuristic strategy object), and processes states in the sequence of highest
 * priorities. Note that the queue can be search-global, i.e. we might hop
 * between search levels.
 */
public abstract class HeuristicSearch extends Search {
	protected ArrayList<Integer> list_vals; //WILL OBTAIN INFO FROM A FILE
	static final String DEFAULT_HEURISTIC_PACKAGE = "gov.nasa.jpf.search.heuristic.";
	protected int searchcounter=0;
	protected HeuristicState parentState;
	protected List<HeuristicState> childStates;
	protected HeuristicState initial;
	protected boolean isPathSensitive = false;
	protected boolean errorfound = false;//MOD: was an error found?
	protected boolean readytorestart=false;//MOD: are we ready to restart search?
	protected ArrayList<CustomPathVar> paths=new ArrayList<CustomPathVar>();//MOD: list of paths encountered
	protected ArrayList<HeuristicState>currentstatelist= new ArrayList<HeuristicState>();//using to make sure we are truncating....
	protected boolean repeat=false;
	protected int endrun=0;
	protected Map<Integer,Integer> manual=new HashMap<Integer,Integer>();//Emod map
	protected int[]factors;
	protected CoveringArrayTuplesRankingArray rankingarray;//EMOD
	protected String[]namesstrings;
	protected int[] Row;
	/*
	 * do we use A* adaptation of state priorities, i.e. have a distance + cost
	 * heuristic (in this context, we just use the path length as the
	 * "distance")
	 */
	protected boolean useAstar;
	private String INPUT_PATH;

	/*
	 * a beam search is a HeuristicSearch with a state queue that is reset at
	 * each search level (i.e. it doesn't hop between search levels when
	 * fetching the next state from the queue)
	 */
	protected boolean isBeamSearch;

	public HeuristicSearch(Config config, VM vm) throws IOException {
		super(config, vm);

		useAstar = config.getBoolean("search.heuristic.astar");
		isBeamSearch = config.getBoolean("search.heuristic.beam_search");
		INPUT_PATH= config.getString("File.choose");
		create_list(INPUT_PATH);
		getReady();
		
		System.out.println("Contents are: " + list_vals + "\n");
	}
	
	private void create_list(String input) throws IOException {
		list_vals = new ArrayList<Integer>();
		Scanner scanner = new Scanner(new File(input));
		
		while(scanner.hasNextInt()){
			list_vals.add(scanner.nextInt());
		}	
	}
	private void getReady(){
		makeMap(list_vals.get(0), list_vals.get(1));
		makeFactors(list_vals.get(0),list_vals.get(1));
		makeStringNames();
	}
	// add the current state to the queue
	protected abstract HeuristicState queueCurrentState();

	// return the next queued state, which becomes the new parentState
	// implementors can also reset or modify the queue
	protected abstract HeuristicState getNextQueuedState();

	public abstract void resetQueue();//MOD METHOD
	public abstract int getQueueSize();

	public ArrayList<Integer> getlist_vals(){
		return list_vals;
	}
	public abstract boolean isQueueLimitReached();
	
	public int[] getRow(){
		return Row;
	}
	public HeuristicState getParentState() {
		return parentState;
	}

	public List<HeuristicState> getChildStates() {
		return childStates;
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
							notifyStateStored();
						}// add breakpoint here
					}

				} else if(isEndState()) {//MOD
					endrun++;//mod
					System.out.println("endrun is "+endrun);
					if(endrun>(list_vals.get(1)-1)){
					CustomPathVar goo=new CustomPathVar(Row);
					boolean isunique=true;
					//here check diff
					if(paths.size()==0)isunique=true;
					else{
					for(int y=0;y<paths.size();y++){
					if(!goo.isDiffRow(paths.get(y))) isunique=false;
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
					System.out.println("loaded row was...(view next line) ");
					for(int k=0;k<Row.length;k++){
						System.out.print(Row[k]+" ");
					}
					rankingarray.updateTupleCoverage(Row);//end EMOD
					done=true;//MOD
					readytorestart=true;//MOD
					endrun=0;
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
			Row=new int[list_vals.get(0)];
			for(int z=0;z<Row.length;z++){
				Row[z]=-1;
			}
			rankingarray=new CoveringArrayTuplesRankingArray(2, manual, namesstrings, factors); //not automated
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
					if((!currentstatelist.isEmpty())&&currentstatelist.get(currentstatelist.size()-1).depth>parentState.depth-1&&parentState.getStateId()>0){
						System.out.println("woah! trying to add "+parentState.getStateId()+" with depth "+parentState.getDepth());
						int j=0;
						while(currentstatelist.get(currentstatelist.size()-1).getDepth()!=parentState.getDepth()){
							System.out.println(j);
							currentstatelist.remove(currentstatelist.size()-1);
							j++;
						}
						currentstatelist.remove(currentstatelist.size()-1);
						for(int u=Row.length-1;u>parentState.getDepth()-2;u--){//resetting...
							Row[u]=-1;
						}
					}
				currentstatelist.add(parentState);
				System.out.println("added state "+parentState.stateId+" to current path");//MOD
				if(parentState.stateId>0){
					Row[parentState.getDepth()-2]=(int) parentState.getValueChosen();
				}
				restoreState(parentState);
				generateChildren();
				}
				else{//mod trying this
					if((!currentstatelist.isEmpty())&&currentstatelist.get(currentstatelist.size()-1).depth>parentState.depth-1&&parentState.getStateId()>0){
					while(currentstatelist.get(currentstatelist.size()-1).getDepth()!=parentState.getDepth()){
						currentstatelist.remove(currentstatelist.size()-1);
					}
					currentstatelist.remove(currentstatelist.size()-1);
					currentstatelist.add(parentState);
					depth=parentState.getDepth();
					for(int u=Row.length-1;u>parentState.getDepth()-2;u--){//resetting...
						Row[u]=-1;
					}}
					if(parentState.stateId>0) Row[parentState.getDepth()-2]=(int) parentState.getValueChosen();
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
			currentstatelist.clear();
			resetQueue();
			for(int y=0;y<Row.length;y++){
				Row[y]=-1;
			}
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

public void makeMap(int x, int y){//EMOD METHOD TO POPULATE MAP not automated
		manual.put(x, y);
	}
public void makeFactors(int x, int y){//EMOD METHOD TO POPULATE FACTORS not automated
	factors= new int[x];
	for (int i=0;i<factors.length;i++){
		factors[i]=y;
	}
}
public void makeStringNames(){//EMOD Method to populate NamesStrings to satisfy coveringarraytuplesrankingarray object.... not automated
	namesstrings=new String[list_vals.get(0)];
	for(int i=0;i<list_vals.get(0);i++){
		namesstrings[i]="Variable "+i;
	}

	
}

}

	