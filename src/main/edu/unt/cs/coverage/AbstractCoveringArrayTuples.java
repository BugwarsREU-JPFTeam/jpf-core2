package edu.unt.cs.coverage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractCoveringArrayTuples {

	int strength;
	int numFactors;
	int[] factorSymbolOffset;
	Map<Integer, Integer> factorsPerLevel;
	int[] levelPerFactor;
	int[][] binomTable;
	int numTuples;
	int factorTuples;
	int numSymbols;
	int numCoveredTuples;
	String[] factorNames;

	public AbstractCoveringArrayTuples(int strength,
			Map<Integer, Integer> factorsPerLevel, 
			String[] factorNames,
			int[] factorLevels) {

		this.strength = strength;
		this.factorsPerLevel = factorsPerLevel;
		this.factorNames = factorNames;

		numFactors = factorLevels.length;
		//		for(Integer level :  factorsPerLevel.keySet()){
		//			int factorsWithLevel = factorsPerLevel.get(level);
		//			numFactors += factorsWithLevel;
		//		}
		this.levelPerFactor = new int[numFactors+1];
		this.factorSymbolOffset = new int[numFactors+1];

		//int factor = 1;
		numSymbols = 0;
		//		for(Integer level :  factorsPerLevel.keySet()){
		//			int factorsWithLevel = factorsPerLevel.get(level);
		//			for(int i = 0; i < factorsWithLevel; i++){
		for(int i = 0; i < factorLevels.length; i++){
			factorSymbolOffset[i+1] = numSymbols;
			numSymbols += factorLevels[i];
			levelPerFactor[i+1] =  factorLevels[i];
		}			


		binomTable = new int[numSymbols+1][strength+1];
		nurmela_binom(numSymbols, strength);
		factorTuples = 0;
		numTuples = countTuples();

		numCoveredTuples = 0;
		//System.out.println("Num tuples =" + numTuples);
	}

	public int getFactorTuples(){
		return factorTuples;
	}

	private int countTuples() {

		int[] factorTuple = new int[strength+1];
		int numTuples = 0;
		int levelTuples = 1;
		factorTuples++;
		for(int i = 1; i <= strength; i++){
			factorTuple[i]=i; //initialize first subset of factors
			levelTuples *= levelPerFactor[i];
		}
		numTuples += levelTuples;


		//get rest of factor tuples
		while(kSubsetLexSuccessor(factorTuple, numFactors)){
			//		System.out.print(factorTuples + ": ");
			//		for(int i = 1; i <= strength; i++){
			//			System.out.print(factorTuple[i] + " ");			
			//		}
			//		System.out.println("");	

			levelTuples = 1;
			for(int i = 1; i <= strength; i++){
				levelTuples *= levelPerFactor[factorTuple[i]];
			}
			numTuples += levelTuples;
			factorTuples++;
		}


		return numTuples;
	}
	void nurmela_binom(int maxv,int i_Strength){

		int v,k;

		for(v = 0; v <= maxv; v++) {
			binomTable[v][0] = 1;
			if(v <i_Strength)
				binomTable[v][v+1] = 0;
			if(v<=i_Strength)
				binomTable[v][v]=1;
			for(k = 1; (k <= i_Strength && k <= v-1); k++) {
				binomTable[v][k] = binomTable[v - 1][k - 1] + binomTable[v - 1][k];
				if(binomTable[v][k] < binomTable[v - 1][k - 1] ||
						binomTable[v][k] < binomTable[v - 1][k] ||
						binomTable[v - 1][k - 1] == 0 ||
						(binomTable[v - 1][k] == 0 && k < v))
					binomTable[v][k] = 0; /* there was an overflow */
			}
		}
	}



	int kSubsetLexRank(Integer[] T, int n)
	/*
	 **  Algorithm 2.7
	 **
	 ** returns r, the rank of the k-subset T
	 ** T must be in increasing order
	 */
	{
		int i,j,lo,hi;
		int r = 0;
		T[0] = 0;
		for(i = 1;i<=strength;i=i+1)
		{
			lo = T[i-1]+1;
			hi = T[i]-1;
			if( lo <=  hi)
			{
				for(j = lo;j<=hi;j=j+1)
					r = r + BinCoef(n-j,strength-i);
			}
		}
		return r;
	}

	int BinCoef(int n, int r)
	/*
	 **  Computes the binomial coefficient "n choose r"
	 */
	{
		return binomTable[n][r];
	}

	void kSubsetLexUnrank(int r, int[] T, int n)
	/*
	 **  Algorithm 2.8
	 **
	 ** returns T, the k-subset having rank T
	 ** T is given in increasing order
	 */
	{
		int x,i,y;
		x = 1;
		for(i=1;i<=strength;i=i+1)
		{
			y= BinCoef(n-x,strength-i);
			while (y <= r)
			{
				r = r - y;
				x = x+1;
				y= BinCoef(n-x,strength-i);
			}
			T[i] =x;
			x = x + 1;
		}
	}
	boolean kSubsetLexSuccessor(int[] T,  int n)
	/*
	 **  Algorithm 2.6
	 **
	 ** replaces the ksubset T by its successor
	 ** T must be in increasing order 
	 ** flag is false if T has no successor
	 */
	{
		boolean flag;	
		int i,j;
		int[] U = new int[strength+1];
		flag= true;
		for(i=1;i<=strength;i=i+1)
			U[i]=T[i];
		i = strength;
		while ( (i >= 1) && (T[i] == (n-strength+i)) )
			i=i-1;
		if (i == 0) 
			flag=false;
		else
		{
			for (j=i;j<=strength;j=j+1)
				U[j]=T[i] + 1 + j - i;
			for (j=1;j<=strength;j=j+1)
				T[j]=U[j];
		}
		return flag;
	}

	public boolean updateTupleCoverage(int[] row){//FMOD
		int[] factorTuple = new int[strength+1];
		Object[] levelTuple = new Object[strength+1]; //can be either DD or int
		boolean dontcare=false;
		for(int i = 1; i <= strength; i++){
			factorTuple[i]=i;
			levelTuple[i]=setLevelTupleValueFromRow(row, factorTuple,i);
			if(levelTuple[i]==null){
				dontcare=true;
				break;
			}
		}
		if(!dontcare) markCovered(factorTuple, levelTuple);
		
		//get rest of factor tuples
		dontcare=false;
		while(kSubsetLexSuccessor(factorTuple, numFactors)){						
			for(int i = 1; i <= strength; i++){
				levelTuple[i]=setLevelTupleValueFromRow(row, factorTuple, i);
				if(levelTuple[i]==null){
					dontcare=true;
					break;
				}
			}
				if(!dontcare) markCovered(factorTuple, levelTuple);	
		}
		return numCoveredTuples == numTuples;

	}
	
	//new updateTupleCoverageMethod for retrieval, not storing should go here...
	public abstract int rankcount(int[]row);//EMOD abstract method for retrevial


	abstract protected void markCovered(int[] factorTuple, Object[] levelTuple);

	//	Object getCoverage(int rank) {
	//		// TODO Auto-generated method stub
	//		return null;
	//	}

	abstract Object setLevelTupleValueFromRow(int[] row, int[] factorTuple, int i);

	public void shutdown(){}

	public int getNumTuples() {
		// TODO Auto-generated method stub
		return numTuples;
	}

}
