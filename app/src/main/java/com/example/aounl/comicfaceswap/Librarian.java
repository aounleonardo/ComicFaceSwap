package com.example.aounl.comicfaceswap;

import android.app.Activity;
import android.content.res.AssetManager;
import android.util.JsonReader;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by aounl on 4/5/2017.
 */

class Librarian {

    private static Librarian instance;
    AssetManager assetManager;
    String[] files;
    JSONObject comicLib;
    JSONArray comicCharacters;
    JSONObject universesLib;
    private int multiverseSize;
    private int nbSelectedUniverses;
    private Universe[] universes;
    private Activity creator; //might not need it

    public static class Universe{
        final String name;
        final String icon;
        final String json;

        Universe(String name, String icon, String json){
            this.name = name;
            this.icon = icon;
            this.json = json;
        }

    }

    private Librarian(Activity activity){
        creator = activity;
        assetManager = activity.getAssets();
        multiverseSize = 0;

        try{
            files = assetManager.list("");

            InputStream input = assetManager.open("universes.json");
            JsonReader reader = new JsonReader(new InputStreamReader(input));
            StringWriter writer = new StringWriter();
            IOUtils.copy(input, writer, "UTF-8");
            String theString = writer.toString();

            universesLib = new JSONObject(theString);
            JSONArray universesJSON = (JSONArray) universesLib.get("results");
            multiverseSize = universesJSON.length();
            universes = new Universe[multiverseSize];
            for(int i = 0; i < multiverseSize; i++){
                JSONObject universe = universesJSON.getJSONObject(i);
                universes[i] = new Universe(universe.getString("name"),
                        universe.getString("icon"), universe.getString("json"));
            }

        } catch (Exception e){
            Log.w("Leo", "Librarian", e);
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

    public int multiverseSize(){
        return multiverseSize;
    }

    public int nbSelectedUniverses(){
        return nbSelectedUniverses;
    }

    public Universe universe(int i){
        return universes[i];
    }




}
