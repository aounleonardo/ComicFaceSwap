package com.example.aounl.comicfaceswap;

import android.util.Log;

import org.json.JSONObject;

/**
 * Created by aounl on 4/5/2017.
 */

class Character {

    String name;
    int age;
    String gender; //male for Male, female for Female, apache for Apache

    double anger;
    double contempt;
    double disgust;
    double fear;
    double happiness;
    double neutral;
    double sadness;
    double surprise;

    double roll;
    double yaw;
    double pitch;

    private Character(){
        name = "";
        age = 0;
        gender = "";
        anger = 0d;
        contempt = 0d;
        disgust = 0d;
        fear = 0d;
        happiness = 0d;
        neutral = 0d;
        sadness = 0d;
        surprise = 0d;
        roll = 0d;
        yaw = 0d;
        pitch = 0d;
    }

    Character(JSONObject character){
        try{
            JSONObject emotions = character.getJSONObject("emotions");
            JSONObject other = character.getJSONObject("other");
            JSONObject headPose = other.getJSONObject("headPose");

            name = character.getString("name");
            age = other.getInt("age");
            gender = other.getString("gender");
            anger = emotions.getDouble("anger");
            contempt = emotions.getDouble("contempt");
            disgust = emotions.getDouble("disgust");
            fear = emotions.getDouble("fear");
            happiness = emotions.getDouble("happiness");
            neutral = emotions.getDouble("neutral");
            sadness = emotions.getDouble("sadness");
            surprise = emotions.getDouble("surprise");
            roll = headPose.getDouble("roll");
            yaw = headPose.getDouble("yaw");
            pitch = headPose.getDouble("pitch");


        } catch(Exception e){
            Log.w("Leo", "Exception in Character constructor " + e.getMessage());
        }

    }

}
