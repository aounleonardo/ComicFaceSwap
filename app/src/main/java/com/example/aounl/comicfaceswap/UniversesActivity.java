package com.example.aounl.comicfaceswap;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

public class UniversesActivity extends AppCompatActivity {
    ListView lv;
    Model[] modelItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_universes);
        lv = (ListView) findViewById(R.id.listView1);
        modelItems = new Model[2];
        modelItems[0] = new Model("heroes", false);
        modelItems[1] = new Model("villains", true);

        CustomAdapter adapter = new CustomAdapter(this, modelItems);
        lv.setAdapter(adapter);
    }

    public void done(View view){
        finish();
    }
}
