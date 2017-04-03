package com.example.aounl.comicfaceswap;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class UniversesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_universes);
    }

    public void done(View view){
        finish();
    }
}
