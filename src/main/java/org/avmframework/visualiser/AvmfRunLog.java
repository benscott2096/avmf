package org.avmframework.visualiser;

import org.avmframework.Vector;
import org.avmframework.objective.ObjectiveValue;

import java.util.ArrayList;



public class AvmfRunLog {

    protected AvmfRunHeader header;

    protected ArrayList<AvmfIterationOutput> vecObjValPairs = new ArrayList<AvmfIterationOutput>();

    public AvmfRunLog(){}


    public void addIteration(AvmfIterationOutput iteration){
        vecObjValPairs.add(iteration);
    }



    public AvmfIterationOutput getIterationData(int index){
        return vecObjValPairs.get(index);
    }

    public ArrayList<AvmfIterationOutput> getDataPairs(){
        return vecObjValPairs;

    }

    public void addHeader(ObjectiveValue bestObjVal, Vector bestVector, int numEvaluations, int numUniqueEvaluations, int numRestarts, long runningTime){
        this.header = new AvmfRunHeader(bestObjVal, bestVector, numEvaluations, numUniqueEvaluations, numRestarts, runningTime);
    }

    public AvmfRunHeader getHeader() {
        return header;
    }
}
