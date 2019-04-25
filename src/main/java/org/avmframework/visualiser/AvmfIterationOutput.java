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

    private int restartNo = 0;

    public ArrayList<Double> getVector(){
        return vector;
    }


    // getters
    public Double getObjVal(){
        return objectiveValue;
    }

    public int getIteration(){ return iteration;}

    public int getRestartNo(){ return restartNo;}


    // constructor
    public AvmfIterationOutput(Vector vector, ObjectiveValue objectiveValue, int iteration, int restartNo){

        if (vector.getVariable(0) instanceof StringVariable){

//            System.out.println("String add"); // debugging

            String stringOfChars = ((StringVariable) vector.getVariable(0)).asString();

//            System.out.println(stringOfChars);// debugging
//            System.out.println(stringOfChars.length()); // debuggging
            for(int i = 0; i < stringOfChars.length(); i++ ){
                this.vector.add((double) stringOfChars.charAt(i));
            }

        }
        else{
//            System.out.println("normal add"); // debugging
            for(int i = 0; i < vector.size(); i++ ) {
//                this.vector.add((double) i);
                this.vector.add(Double.valueOf(vector.getVariable(i).toString()));

            }
        }

        this.objectiveValue = Double.valueOf(objectiveValue.toString());
        this.iteration = iteration;
        this.restartNo = restartNo;
    }

}
