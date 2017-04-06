package com.example.aounl.comicfaceswap;

import android.widget.ImageView;

/**
 * Created by Leonardo Aoun on 4/5/2017.
 */

public class Model {
    private String name;
    private boolean checkboxEnabled;

    public Model(String name, boolean value){
        this.name = name;
        this.checkboxEnabled = value;
    }

    public String getName(){
        return name;
    }

    public boolean getValue(){
        return checkboxEnabled;
    }
}
