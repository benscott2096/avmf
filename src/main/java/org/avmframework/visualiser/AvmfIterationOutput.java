package org.avmframework.visualiser;

import org.avmframework.Vector;
import org.avmframework.objective.ObjectiveValue;

import java.util.ArrayList;

public class AvmfIterationOutput {

    private ArrayList<Double> vector = new ArrayList<Double>();

    private Double objectiveValue;






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


    public AvmfIterationOutput(Vector vector, ObjectiveValue objectiveValue){

        // todo take Vector type in, convert variables to array list of Doubles (object) then store in vector array list. do the same for objective value, but is only a single value.

        for(int i = 0; i < vector.size(); i++ ) {
            this.vector.add(Double.valueOf(vector.getVariable(i).toString()));
        }


        this.objectiveValue = Double.valueOf(objectiveValue.toString());
    }

}
