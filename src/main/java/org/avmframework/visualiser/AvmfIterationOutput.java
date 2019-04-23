package org.avmframework.visualiser;

import org.avmframework.Vector;
import org.avmframework.objective.ObjectiveValue;
import org.avmframework.variable.StringVariable;
import org.avmframework.variable.Variable;

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


//        System.out.println("String processing");
//        System.out.println(vector.getVariable(0));
//        System.out.println(vector.getVariable(0).getClass());

        String var = vector.toString();
//        System.out.println(var);
//        System.out.println(var.length());

//        System.out.println(var.getClass());

//        for(int i = 0; i < vector.size(); i++ ) {
//
//
//            this.vector.add(Double.valueOf(vector.getVariable(i).toString()));
//        }


        if (vector.getVariable(0) instanceof StringVariable){
            String stringOfChars = ((StringVariable) vector.getVariable(0)).asString();

            System.out.println(stringOfChars);
            System.out.println(stringOfChars.length());
            for(int i = 0; i < stringOfChars.length(); i++ ){
                this.vector.add((double) stringOfChars.charAt(i));
            }

        }
        else{
            for(int i = 0; i < vector.size(); i++ ) {
                this.vector.add((double) i);
            }
        }


        this.objectiveValue = Double.valueOf(objectiveValue.toString());

        this.iteration = iteration;
    }

}
