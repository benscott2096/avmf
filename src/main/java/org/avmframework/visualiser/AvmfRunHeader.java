package org.avmframework.visualiser;

import org.avmframework.Vector;
import org.avmframework.objective.ObjectiveValue;
import org.avmframework.variable.StringVariable;

import java.util.ArrayList;

public class AvmfRunHeader {

    // base data from monitor
    protected double bestObjVal;

    protected ArrayList<Double> bestVector = new ArrayList<Double>();

    protected int numEvaluations;

    protected int numUniqueEvaluations;

    protected int numRestarts;

    protected long runningTime;

    // search type

    protected String searchName;

    // termination policy data.
    protected boolean terminateOnOptimal;
    protected int maxEvaluations;
    protected int maxRestarts;
    protected long tpRunningTime;


    public AvmfRunHeader(ObjectiveValue bestObjVal, Vector bestVector, int numEvaluations, int numUniqueEvaluations, int numRestarts, long runningTime){

        // conditional because there must be some evaluations for there to be an objective value.
        if (numEvaluations > 0){
            this.bestObjVal = Double.valueOf(bestObjVal.toString());
        }


        if (bestVector != null){

            if (bestVector.getVariable(0) instanceof StringVariable){
                String stringOfChars = ((StringVariable) bestVector.getVariable(0)).asString();

                for(int i = 0; i < stringOfChars.length(); i++ ){
                    this.bestVector.add((double) stringOfChars.charAt(i));
                }

            }
            else{
                for(int i = 0; i < bestVector.size(); i++ ) {
                    this.bestVector.add(Double.valueOf(bestVector.getVariable(i).toString()));
                }
            }


        }


        this.numEvaluations = numEvaluations;
        this.numUniqueEvaluations = numUniqueEvaluations;
        this.numRestarts = numRestarts;
        this.runningTime = runningTime;
    }


    public void addSearchName(String searchName){
        this.searchName = searchName;
    }

    public void addTerminationPolicy(boolean terminateOnOptimal, int maxEvaluations, int maxRestarts, long tpRunningTime){
        this.terminateOnOptimal = terminateOnOptimal;
        this.maxEvaluations = maxEvaluations;
        this.maxRestarts = maxRestarts;
        this.tpRunningTime = tpRunningTime;

    }

}
