package com.example.aounl.comicfaceswap;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

public class UniversesActivity extends AppCompatActivity {
    ListView lv;
    Model[] modelItems;
    Librarian librarian;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_universes);
        lv = (ListView) findViewById(R.id.listView1);
        librarian = Librarian.getInstance();
        modelItems = new Model[librarian.multiverseSize()];
        for(int i = 0; i < librarian.multiverseSize(); i++){
            modelItems[i] = new Model(librarian.universe(i).name, false);
        }

        CustomAdapter adapter = new CustomAdapter(this, modelItems);
        lv.setAdapter(adapter);
    }

    public void done(View view){
        
        finish();
    }
}
