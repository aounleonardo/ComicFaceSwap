package com.example.aounl.comicfaceswap;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

/**
 * Created by aounl on 4/5/2017.
 */

public class CustomAdapter extends ArrayAdapter<Model> {
    public final static int CHECKBOX_POS = 0;
    Model[] modelItems = null;
    Context context;

    public CustomAdapter(Context context, Model[] resource){
        super(context, R.layout.row, resource);
        this.context = context;
        modelItems = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        convertView = inflater.inflate(R.layout.row, parent, false);
        TextView name = (TextView) convertView.findViewById(R.id.textView1);
        CheckBox cb = (CheckBox) convertView.findViewById(R.id.checkBox1);
        name.setText(modelItems[position].getName());
        cb.setChecked(modelItems[position].getValue());
        return convertView;
    }

}
