package org.avmframework.visualiser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class Launcher {

    protected static AvmfRunLog runLog = new AvmfRunLog();
    protected static String jsonFileName;
    protected static boolean fileLoaded = true;

    protected static void setJsonFileName(String fileName){
        jsonFileName = fileName;
    }

    // entry point for launch without implementation. choose file step
    public static void main(String[] args){

        jsonFileName = "file_name";
        fileLoaded = false;
        launchVisualiser(jsonFileName);

    }

    // method that launches the visualiser app, requires a file name as a string. Used directly as entry point when wiring visualiser directly into an instance of the AVMf.
    public static void launchVisualiser(String fileName){
        System.out.println("Launching Visualiser");
        jsonFileName = fileName;


        if (fileLoaded) {
            try {
                loadRunLog(jsonFileName);
                System.out.println("File Loaded: " + jsonFileName);

            } catch (FileNotFoundException e) {
                System.out.println("Error loading JSON run log file");
            }
        }

        // launch the GUI
        String[] args = new String[]{""};
        GUI.launchUI(args);

    }

    // experiment with void
    public static void loadRunLog(String jsonFileName) throws FileNotFoundException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        BufferedReader bufferedReader = new BufferedReader(new FileReader( jsonFileName));

        AvmfRunLog runLog1 = gson.fromJson(bufferedReader, AvmfRunLog.class);
        runLog = runLog1;

//        System.out.println("HEADER"); // debugging
//        System.out.println(runLog.getHeader()); // debugging
    }


    public static ArrayList<AvmfIterationOutput> getDataPairs(){
        return runLog.getDataPairs();
    }


}