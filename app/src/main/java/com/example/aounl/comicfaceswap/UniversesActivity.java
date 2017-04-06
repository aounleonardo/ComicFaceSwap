package com.example.aounl.comicfaceswap;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.Adapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class UniversesActivity extends AppCompatActivity {
    ListView lv;
    Model[] modelItems;
    Librarian librarian;
    List<Integer> selectedModels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_universes);
        lv = (ListView) findViewById(R.id.listView1);
        librarian = Librarian.getInstance();
        modelItems = new Model[librarian.multiverseSize()];
        selectedModels = new LinkedList<>();
        for(int i = 0; i < librarian.multiverseSize(); i++){
            modelItems[i] = new Model(librarian.universe(i).name, librarian.isChecked(i));
        }

        CustomAdapter adapter = new CustomAdapter(this, modelItems);
        lv.setAdapter(adapter);
    }

    public void done(View view){
        Log.w("Leo", "inside done");
        try{
            int count = lv.getCount();
            boolean[] checks = new boolean[count];

            for(int i = 0; i < count; i++){
                View child = lv.getChildAt(i);
                CheckBox cb = (CheckBox) ((LinearLayout) child).getChildAt(CustomAdapter.CHECKBOX_POS);
                checks[i] = cb.isChecked();
            }

            librarian.updateLibrary(checks);

//            SparseBooleanArray checkedItemPositions = list.getCheckedItemPositions();
//            Log.w("Leo", "checkedItemPositions is " + (checkedItemPositions == null));
        //final int checkedItemCount = checkedItemPositions.size();
        //Log.w("Leo", "size is " + checkedItemCount);
        //Log.w("Leo", "0 is " + checkedItemPositions.get(checkedItemPositions.keyAt(0)));
        //Log.w("Leo", "1 is " + checkedItemPositions.get(checkedItemPositions.keyAt(1)));

        } catch (Exception e){
            Log.w("Leo", "Exception " + e.getMessage(), e);
        }


        finish();
    }
}
