package org.avmframework.objective;

import org.avmframework.Monitor;
import org.avmframework.TerminationException;
import org.avmframework.Vector;

import java.util.HashMap;
import java.util.Map;

public abstract class ObjectiveFunction {

    public static final boolean USE_CACHE_DEFAULT = true;

    protected int iteration = 1; // init to 1, iterations indexed from 1. added - BSS

    public void setIteration(int index){
        iteration = index;
    }

    public void incrementIteration(){
        iteration++;
    }

    protected boolean useCache = USE_CACHE_DEFAULT;
    protected Map<Vector, ObjectiveValue> previousVals = new HashMap<>();
    protected Monitor monitor;

    public void setMonitor(Monitor monitor) {
        this.monitor = monitor;
    }

    public ObjectiveValue evaluate(Vector vector) throws TerminationException {
        monitor.observeVector();



        // If vector seen before, return previously calculated objective value
        if (useCache && previousVals.containsKey(vector)) {

            // only record key pair if record data is turned on by setting use visualiser to true
            if (monitor.getUseVisualiser()){
                monitor.recordKeyPair(vector, previousVals.get(vector), iteration); // added -BSS
            }

            return previousVals.get(vector); // returns objective value
        }


        // If vector is a new one, compute its objective value.
        ObjectiveValue objVal = computeObjectiveValue(vector);


        // record new vector and objvalue key pair in chache
        if (useCache) {
//            System.out.println("record it!!!");
            previousVals.put(vector.deepCopy(), objVal);
        }


        if (monitor != null) {
            monitor.observePreviouslyUnseenVector(vector, objVal);

            // only record key pair if record data is turned on
            if(monitor.getUseVisualiser()){
                monitor.recordKeyPair(vector, objVal, iteration); // added -BSS
            }

        }


        return objVal;
    }

    protected abstract ObjectiveValue computeObjectiveValue(Vector vector);
}
