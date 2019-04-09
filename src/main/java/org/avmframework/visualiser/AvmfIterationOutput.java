package org.avmframework.visualiser;

import org.avmframework.Vector;
import org.avmframework.objective.ObjectiveValue;

import java.util.ArrayList;

public class AvmfIterationOutput {

    private ArrayList<Double> vector = new ArrayList<Double>();

    private Double objectiveValue;

    private int iteration = 0;







    public ArrayList<Double> getVector(){
        return vector;
    }

//    public void setVector(Vector vector){
//     this.vector = vector.deepCopy();
//    }

    public Double getObjVal(){
        return objectiveValue;
    }
//
//    public void setObjVal(double objectiveValue){
//        this.objectiveValue = objectiveValue;
//    }

    public int getIteration(){ return iteration;}


    public AvmfIterationOutput(Vector vector, ObjectiveValue objectiveValue, int iteration){


        for(int i = 0; i < vector.size(); i++ ) {
            this.vector.add(Double.valueOf(vector.getVariable(i).toString()));
        }


        this.objectiveValue = Double.valueOf(objectiveValue.toString());

        this.iteration = iteration;
    }

}