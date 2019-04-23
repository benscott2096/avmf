package org.avmframework;

import org.avmframework.objective.ObjectiveValue;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.avmframework.visualiser.AvmfIterationOutput;
import org.avmframework.visualiser.AvmfRunLog;

import java.util.Date;
import java.text.SimpleDateFormat;
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

    protected String searchName = "";

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


    // BSS added variables and objects
    public static final boolean USE_VISUALISER_DEFAULT = false;
    private boolean useVisualiser; // default should be false ... however we end up handling that.
    private static String fileName;

    public static String getFileName(){
        return fileName;
    }

    public boolean getUseVisualiser(){
        return useVisualiser;
    }

    public void setUseVisualiser(boolean useVisualiser){
        this.useVisualiser = useVisualiser;
    }

    /**
     * Constructs a Monitor instance.
     * @param tp The termination policy being used by the search.
     */

    // todo: set the default value for useVisualiser here. -- make this overloadable? - think this is done?

    // constructor 1, setting useVisualiser to default value.
    public Monitor(TerminationPolicy tp) {
        this.tp = tp;
        bestObjVal = null;
        bestVector = null;
        numEvaluations = 0;
        numUniqueEvaluations = 0;
        numRestarts = 0;
        startTime = System.currentTimeMillis();

        // set use visualiser to default (false).
        this.useVisualiser = USE_VISUALISER_DEFAULT;

    }

    // constructor 2, overloaded to enable optional setting of useVisualiser to true.
    public Monitor(TerminationPolicy tp, boolean useVisualiser, String searchName) {
        this.tp = tp;
        this.searchName = searchName;
        bestObjVal = null;
        bestVector = null;
        numEvaluations = 0;
        numUniqueEvaluations = 0;
        numRestarts = 0;
        startTime = System.currentTimeMillis();

        // set use visualiser to boolean passed in to constructor -- BSS
        this.useVisualiser = useVisualiser;



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


    public void recordKeyPair(Vector vector, ObjectiveValue objVal, int iteration) throws TerminationException {
//        System.out.println("vector: " + vector + ", objVal: " + objVal);

        runLog.addIteration(new AvmfIterationOutput(vector, objVal, iteration));
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

        // adding header data to runlog
        runLog.addHeader(getBestObjVal(), getBestVector(), getNumEvaluations(), getNumUniqueEvaluations(), getNumRestarts(), getRunningTime());
        runLog.getHeader().addSearchName(searchName);
        runLog.getHeader().addTerminationPolicy(tp.getTerminateOnOptimal(), tp.getMaxEvaluations(), tp.getMaxRestarts(), tp.getRunningTime());



//        System.out.println("in monitor: " + runLog.getHeader().toString());

        try {




            // file handling
            System.out.println(generateFileName());


            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();

            //Generate unique filename for JSON file
            fileName = generateFileName();
            FileWriter writer = new FileWriter(fileName);
            // Write Json to file
            writer.write(gson.toJson(runLog));
            writer.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Generates a unique filename based on the timestamp of the AVMf run.
     * @return string filename generated
     */
    private String generateFileName(){
        String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
        String fileName = "AVMf_Run_Output_" + timeStamp + ".json";
        return fileName;
    }
}
