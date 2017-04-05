package com.example.aounl.comicfaceswap;

import android.app.Activity;
import android.content.res.AssetManager;
import android.util.JsonReader;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

/**
 * Created by aounl on 4/5/2017.
 */

class Librarian {

    private static Librarian instance;
    AssetManager assetManager;
    String[] files;
    JSONObject comicLib;
    JSONArray comicCharacters;
    private Activity creator; //might not need it

    private Librarian(Activity activity){
        creator = activity;
        assetManager = activity.getAssets();
        ImageAnalyser imageAnalyser = ImageAnalyser.getInstance();

        try{
            files = assetManager.list("");

            InputStream input = assetManager.open("marvel_heroes.json");
            JsonReader comicReader = new JsonReader(new InputStreamReader(input));
            StringWriter writer = new StringWriter();
            IOUtils.copy(input, writer, "UTF-8");
            String theString = writer.toString();

            comicLib = new JSONObject(theString);
            comicCharacters = (JSONArray) comicLib.get("results");

        } catch(Exception e){
        }
    }

    static Librarian getInstance() throws IllegalStateException{
        if(instance == null){throw new IllegalStateException("The Librarian should be initialized by an activity");}
        else return instance;
    }

    static Librarian getInstance(Activity activity){
        if(instance == null){instance = new Librarian(activity);}
        return instance;
    }




}
