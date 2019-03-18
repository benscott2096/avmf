package org.avmframework.visualiser;

import java.io.BufferedReader;
import java.io.FileReader;
//import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class start {

    protected static AvmfRunLog runLog = new AvmfRunLog();





    public static void main(String[] args){

    launchVisualiser();


    }

    // method that launches the visualiser app
    public static void launchVisualiser(){
        // stub for continuing...
        System.out.println("Launching Visualiser");




        try{
            runLog = loadRunLog();
            System.out.println("File Loaded");
            String[] args = new String[] {""};
//            FirstGraph.main(args);
            FirstGraph.launchUI(args);
            // rough test for seeing if data loaded from JSON
//            System.out.println(runLog.getIterationData(6).getVector());
//            System.out.println(runLog.getIterationData(6).getObjVal());


        }
        catch(FileNotFoundException e){
            System.out.println("Error loading JSON run log file");
        }


    }


    //todo: pass in and use file name for loading.
    public static AvmfRunLog loadRunLog() throws FileNotFoundException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        BufferedReader bufferedReader = new BufferedReader(new FileReader( "testingjson.json"));

        AvmfRunLog runLog = gson.fromJson(bufferedReader, AvmfRunLog.class);
        return runLog;

    }


    public static ArrayList<AvmfIterationOutput> getDataPairs(){
        return runLog.getDataPairs();
    }



}

// todo: two paths for launching visualiser... launchVisualiser() called from inside implementation or launched from command line and pointed at file to load. Bassically the difference is in how the file loads. when called from in AVMf, a file path will be passed. When launched independently, either needs to supplied a file path as argument or if not, launches a file browser to locate file to load.