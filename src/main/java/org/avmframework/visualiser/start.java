package org.avmframework.visualiser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class start {

    protected static AvmfRunLog runLog = new AvmfRunLog();
    private static String jsonFileName;

    public static void setJsonFileName(String fileName){
        jsonFileName = fileName;
    }
    public static boolean fileLoaded = true;

    // entry point for launch without implementation. choose file step
    public static void main(String[] args){

        jsonFileName = "something";
        fileLoaded = false;

//        ChooseFile.launchFileChooser(args);
//        System.out.println(jsonFileName);

        launchVisualiser(jsonFileName);
    }

    // method that launches the visualiser app, requires a file name as a string.
    public static void launchVisualiser(String fileName){
        // stub for continuing...
        System.out.println("Launching Visualiser");
        jsonFileName = fileName;



        if (fileLoaded) {
            try {
                loadRunLog(jsonFileName);
                System.out.println("File Loaded");



            } catch (FileNotFoundException e) {
                System.out.println("Error loading JSON run log file");
            }
        }

        String[] args = new String[]{""};
        FirstGraph.launchUI(args);

    }

    // experiment with void
    public static void loadRunLog(String jsonFileName) throws FileNotFoundException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        BufferedReader bufferedReader = new BufferedReader(new FileReader( jsonFileName));

        AvmfRunLog runLog1 = gson.fromJson(bufferedReader, AvmfRunLog.class);
        runLog = runLog1;

    }


    public static ArrayList<AvmfIterationOutput> getDataPairs(){
        return runLog.getDataPairs();
    }



}

// todo: two paths for launching visualiser... launchVisualiser() called from inside implementation or launched from command line and pointed at file to load. Bassically the difference is in how the file loads. when called from in AVMf, a file path will be passed. When launched independently, either needs to supplied a file path as argument or if not, launches a file browser to locate file to load.