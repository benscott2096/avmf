package org.avmframework.objective;

import org.avmframework.Monitor;
import org.avmframework.TerminationException;
import org.avmframework.Vector;

import java.util.HashMap;
import java.util.Map;

public abstract class ObjectiveFunction {

    public static final boolean USE_CACHE_DEFAULT = true;

    protected boolean useCache = USE_CACHE_DEFAULT;
    protected Map<Vector, ObjectiveValue> previousVals = new HashMap<>();
    protected Monitor monitor;

    public void setMonitor(Monitor monitor) {
        this.monitor = monitor;
    }

    public ObjectiveValue evaluate(Vector vector) throws TerminationException {
        monitor.observeVector(); // marker -- might need to do something like this -- BSS


        // TODO: think im going to need a fresh monitor function for recording data of all vectors considered for each restart.


        // If vector seen before, return previously calculated objective value
        if (useCache && previousVals.containsKey(vector)) {
//            System.out.println("repeat!!!");

            // only record key pair if record data is turned on
            if (monitor.getUseVisualiser()){
                monitor.recordKeyPair(vector, previousVals.get(vector)); // added -BSS
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
                monitor.recordKeyPair(vector, objVal); // added -BSS
            }

        }



        return objVal;
    }

    protected abstract ObjectiveValue computeObjectiveValue(Vector vector);
}
