package com.example.aounl.comicfaceswap;

import android.app.Activity;
import android.content.res.AssetManager;
import android.util.JsonReader;
import android.util.Log;

import com.microsoft.projectoxford.emotion.contract.Scores;

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

    private String[] files;
    private int nbSelectedUniverses;
    private Universe[] universes;
    private boolean[] checks;
    private List<Character> characters;
    //private Activity creator; //might not need it

    static class Universe{
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
        //creator = activity;
        assetManager = activity.getAssets();
        characters = new ArrayList<>();
        try{
            files = assetManager.list("");

            InputStream input = assetManager.open("universes.json");
            //JsonReader reader = new JsonReader(new InputStreamReader(input));
            StringWriter writer = new StringWriter();
            IOUtils.copy(input, writer, "UTF-8");
            String theString = writer.toString();

            JSONObject universesLib = new JSONObject(theString);
            JSONArray universesJSON = (JSONArray) universesLib.get("results");
            int multiverseSize = universesJSON.length();
            universes = new Universe[multiverseSize];
            checks = new boolean[multiverseSize];
            for(int i = 0; i < multiverseSize; i++){
                JSONObject universe = universesJSON.getJSONObject(i);
                universes[i] = new Universe(universe.getString("name"),
                        universe.getString("icon"), universe.getString("json"));
            }

        } catch (Exception e){
            Log.w("Leo", "Librarian", e);
        }
    }

    void updateLibrary(boolean[] checks){
        characters = new ArrayList<>();
        nbSelectedUniverses = 0;
        int len = checks.length;
        for(int i = 0; i < len; i++){
            this.checks[i] = checks[i];
            if(checks[i]){
                nbSelectedUniverses++;
                addUniverse(i);
            }

        }
    }

    private void addUniverse(int i){
        Universe u = universes[i];
        try{
            InputStream input = assetManager.open(u.json);
            StringWriter writer = new StringWriter();
            IOUtils.copy(input, writer, "UTF-8");
            String theString = writer.toString();

            JSONObject comicUniverse = new JSONObject(theString);
            JSONArray comicCharacters = (JSONArray) comicUniverse.get("results");

            int len = comicCharacters.length();
            for(int c = 0; c < len; c++){
                JSONObject character = comicCharacters.getJSONObject(c);
                characters.add(new Character(character));
            }

        } catch(Exception e){
            Log.w("Leo", "Exception in addUniverse " + e.getMessage());
        }
    }


    double getCost(Scores emotions, float age, String gender, int comicChar){
        double cost = 0.0f;
        Character character = characters.get(comicChar);
        try{
            if(!gender.equals(character.gender)){
                cost = 100.f;
            } else{

                cost += Math.pow(Math.abs(emotions.anger - character.anger), 2);
                cost += Math.pow(Math.abs(emotions.contempt - character.contempt), 2);
                cost += Math.pow(Math.abs(emotions.disgust - character.disgust), 2);
                cost += Math.pow(Math.abs(emotions.fear - character.fear), 2);
                cost += Math.pow(Math.abs(emotions.happiness - character.happiness), 2);
                cost += Math.pow(Math.abs(emotions.neutral - character.neutral), 2);
                cost += Math.pow(Math.abs(emotions.sadness - character.sadness), 2);
                cost += Math.pow(Math.abs(emotions.surprise - character.surprise), 2);

                cost += Math.pow(Math.abs(age - character.age)/50.f, 2);
            }
        }catch(Exception e){
            Log.w("Leo", "Exception in getCost " + e.getMessage());
        }
        return cost;
    }

    static Librarian getInstance() throws IllegalStateException{
        if(instance == null){throw new IllegalStateException("The Librarian should be initialized by an activity");}
        else return instance;
    }

    static Librarian getInstance(Activity activity){
        if(instance == null){instance = new Librarian(activity);}
        return instance;
    }

    int multiverseSize(){
        return universes.length;
    }

    int nbSelectedUniverses(){
        return nbSelectedUniverses;
    }

    int nbCharacters(){
        return characters.size();
    }

    Universe universe(int i){
        return universes[i];
    }

//    Assumes i is valid
    String characterName(int i){
        return characters.get(i).name;
    }

    Boolean isChecked(int i){
        return checks[i];
    }




}
