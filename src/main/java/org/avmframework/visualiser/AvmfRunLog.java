package org.avmframework.visualiser;

import org.avmframework.Vector;
import org.avmframework.objective.ObjectiveValue;

import java.util.ArrayList;



public class AvmfRunLog {

    // initialised empty header
    protected AvmfRunHeader header;

    // initialised as empty array list.
    protected ArrayList<AvmfIterationOutput> vecObjValPairs = new ArrayList<AvmfIterationOutput>();

    // constructor
    public AvmfRunLog(){}

    // method to add an iteration
    public void addIteration(AvmfIterationOutput iteration){
        vecObjValPairs.add(iteration);
    }

    // method returns iteration data at given index
    public AvmfIterationOutput getIterationData(int index){
        return vecObjValPairs.get(index);
    }

    // method returns all data pairs
    public ArrayList<AvmfIterationOutput> getDataPairs(){ return vecObjValPairs; }

    // method initialises header
    public void addHeader(ObjectiveValue bestObjVal, Vector bestVector, int numEvaluations, int numUniqueEvaluations, int numRestarts, long runningTime){
        this.header = new AvmfRunHeader(bestObjVal, bestVector, numEvaluations, numUniqueEvaluations, numRestarts, runningTime);
    }

    // method returns header object
    public AvmfRunHeader getHeader() { return header; }
}
