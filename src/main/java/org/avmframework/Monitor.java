package org.avmframework;

import org.avmframework.objective.ObjectiveValue;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.avmframework.visualiser.AvmfIterationOutput;
import org.avmframework.visualiser.AvmfRunLog;

/**
 * A Monitor instance is used by an AVM object to keep track of the candidate solution with the best objective value,
 * the number of objective function evaluations that have taken place, and other information. It is also responsible
 * for triggering a {@link org.avmframework.TerminationException} when the conditions of the search's
 * {@link org.avmframework.TerminationPolicy} have been satisfied.
 * @author Phil McMinn
 */
public class Monitor {

    /**
     * The termination policy being used by the search.
     */
    protected TerminationPolicy tp;

    /**
     * The best objective value observed so far during hte search.
     */
    protected ObjectiveValue bestObjVal;

    /**
     * The vector with the best objective value observed so far by the search.
     */
    protected Vector bestVector;

    /**
     * The number of objective function evaluations that have taken place so far. The count includes non-unique
     * vectors. So if the same vector is evaluated twice, both evaluations will be recorded by this counter.
     */
    protected int numEvaluations;

    /**
     * The number of unique objective function evaluations that have taken place so far. If a vector is evaluated
     * more than once, this count should not increase.
     */
    protected int numUniqueEvaluations;

    /**
     * The number of times the AVM search has been restarted.
     */
    protected int numRestarts;

    /**
     * The system time (in milliseconds) that the search was started.
     */
    protected long startTime;

    /**
     * The system time (in milliseconds) that the search was ended.
     */
    protected long endTime;

    protected BufferedWriter fileWriter;




    /**
     * Constructs a Monitor instance.
     * @param tp The termination policy being used by the search.
     */
    public Monitor(TerminationPolicy tp) {
        this.tp = tp;
        bestObjVal = null;
        bestVector = null;
        numEvaluations = 0;
        numUniqueEvaluations = 0;
        numRestarts = 0;
        startTime = System.currentTimeMillis();

        try {
            fileWriter = new BufferedWriter(new FileWriter("/Users/ben/the_degree/3rd_year/COM3610_Dissertation_Project/project/avmf/samplefile.json"));
        }
        catch (IOException e){
            e.printStackTrace();
        }

    }

    /**
     * Gets the best objective value observed by the search so far.
     * @return THe best objective value.
     */
    public ObjectiveValue getBestObjVal() {
        return bestObjVal;
    }

    /**
     * Gets the vector with the best objective value observed by the search so far.
     * @return The best vector.
     */
    public Vector getBestVector() {
        return bestVector;
    }

    /**
     * Gets the number of objective function evaluations that have taken place so far. The count includes non-unique
     * vectors. So if the same vector is evaluated twice, both evaluations will be recorded by this counter.
     * @return The number of objective function evaluations.
     */
    public int getNumEvaluations() {
        return numEvaluations;
    }

    /**
     * Gets the number of unique objective function evaluations that have taken place so far. If a vector is evaluated
     * more than once, this count should not increase.
     * @return The number of unique objective function evaluations.
     */
    public int getNumUniqueEvaluations() {
        return numUniqueEvaluations;
    }

    /**
     * Get the number of times the AVM search has been restarted (following hitting a local optimum).
     * @return The number of times the AVM search has been restarted.
     */
    public int getNumRestarts() {
        return numRestarts;
    }

    /**
     * Gets the running time (in milliseconds) of the search from start to finish. If the search has not terminated,
     * the value is undefined.
     * @return The run time of the search.
     */
    // TODO: should this return the running time also.
    public long getRunningTime() {
        return endTime - startTime;
    }

    // TODO: Better name? No parameter.
    public void observeVector() throws TerminationException {
        tp.checkExhaustedEvaluations(this);
        tp.checkExhaustedTime(this);
        numEvaluations ++;
    }


    public void observePreviouslyUnseenVector(Vector vector, ObjectiveValue objVal) throws TerminationException {
        if (bestObjVal == null || objVal.betterThan(bestObjVal)) {
            bestObjVal = objVal;
            bestVector = vector.deepCopy();
        }
        numUniqueEvaluations ++;

        tp.checkFoundOptimal(this);

        // handle writing variables and objective values to file here -- BSS
    }


    protected AvmfRunLog runLog = new AvmfRunLog();


    public void testOutput(Vector vector, ObjectiveValue objVal) throws TerminationException {
        System.out.println("vector: " + vector + ", objVal: " + objVal);
        try {
            fileWriter.write(vector + "," + objVal);
            fileWriter.write(System.lineSeparator());


            runLog.addIteration(new AvmfIterationOutput(vector, objVal));
        }
        catch (IOException e){
            e.printStackTrace();
        }
        // handle writing variables and objective values to file here -- BSS
    }

    public void observeRestart() throws TerminationException {
        tp.checkExhaustedRestarts(this);
        tp.checkExhaustedTime(this);
        numRestarts ++;
        System.out.println("restart");
        // handle writing restarts to file here -BSS
    }

    public void observeTermination() {
        endTime = System.currentTimeMillis();
        try {
//            fileWriter.write(getBestVector().toString());
//            fileWriter.write(System.lineSeparator());
////
//            fileWriter.write(getBestObjVal().toString());
//            fileWriter.write(System.lineSeparator());
//
//            fileWriter.write(getNumEvaluations());
//            fileWriter.write(System.lineSeparator());
//
//            fileWriter.write(getNumUniqueEvaluations());
//            fileWriter.write(System.lineSeparator());

//            fileWriter.write((int)getRunningTime());
//            fileWriter.write(System.lineSeparator());

            fileWriter.close();

            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            FileWriter writer = new FileWriter("testingjson.json");
//            AvmfRunLog plot = new AvmfRunLog(getBestVector());
//            System.out.println(getBestVector());
            writer.write(gson.toJson(runLog));
            writer.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
