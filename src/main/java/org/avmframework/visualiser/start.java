package org.avmframework.visualiser;

import java.io.BufferedReader;
import java.io.FileReader;
//import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.avmframework.variable.Variable;


public class start {

    protected static AvmfRunLog runLog = new AvmfRunLog();



    public static AvmfRunLog readJson() throws FileNotFoundException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        BufferedReader bufferedReader = new BufferedReader(new FileReader( "testingjson.json"));

        AvmfRunLog runLog = gson.fromJson(bufferedReader, AvmfRunLog.class);
        return runLog;

    }

    public static void main(String[] args){


        try{
            runLog = readJson();
            System.out.println(runLog.getIterationData(6).getVector());
            System.out.println(runLog.getIterationData(6).getObjVal());
        }
        catch(FileNotFoundException e){

        }

    }





}
