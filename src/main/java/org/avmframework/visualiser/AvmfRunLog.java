package org.avmframework.visualiser;

import org.avmframework.Vector;
import java.util.ArrayList;



public class AvmfRunLog {

    protected ArrayList<AvmfIterationOutput> vecObjValPairs = new ArrayList<AvmfIterationOutput>();

    public AvmfRunLog(){}


    public void addIteration(AvmfIterationOutput iteration){
        vecObjValPairs.add(iteration);
    }

//    public ArrayList


    public AvmfIterationOutput getIterationData(int index){
        return vecObjValPairs.get(index);
    }

    public ArrayList<AvmfIterationOutput> getDataPairs(){
        return vecObjValPairs;

    }
}
