package com.example.aounl.comicfaceswap;

import android.content.res.AssetManager;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.graphics.*;
import android.widget.*;
import android.provider.*;
import com.microsoft.projectoxford.emotion.contract.*;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private ImageAnalyser imageAnalyser = ImageAnalyser.getInstance();
    private final int PICK_IMAGE = 1;
    public static AssetManager assetManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        String[] files;
        imageAnalyser = ImageAnalyser.getInstance();
        imageAnalyser.assetManager  = getAssets();
        assetManager = imageAnalyser.assetManager;


        try{
            files = assetManager.list("");

            InputStream input = assetManager.open("marvel_villains.json");
            JsonReader comicReader = new JsonReader(new InputStreamReader(input));
                StringWriter writer = new StringWriter();
                IOUtils.copy(input, writer, "UTF-8");
                String theString = writer.toString();

                imageAnalyser.comicLib = new JSONObject(theString);
                imageAnalyser.comicCharacters = (JSONArray) imageAnalyser.comicLib.get("results");

            } catch(Exception e){
            }


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button1 = (Button)findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallIntent = new Intent(Intent.ACTION_GET_CONTENT);
                gallIntent.setType("image/*");
                startActivityForResult(Intent.createChooser(gallIntent, "Select Picture"), PICK_IMAGE);
            }
        });

        imageAnalyser.detectionProgressDialog = new ProgressDialog(this);
    }

    public void takePic(View view){
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }

    public void analyzeCaptured(View view){
        Bundle extras = getIntent().getExtras();
        byte[] capturedImage = imageAnalyser.capturedImage;
        if(capturedImage != null){
            Bitmap bitmap = BitmapFactory.decodeByteArray(capturedImage, 0, capturedImage.length);

            ImageView imageView = (ImageView) findViewById(R.id.imageView1);
            imageView.setImageBitmap(bitmap);
            detectAndFrame(bitmap);
        }

    }

    public void gotoMultiverse(View view){
        Intent intent = new Intent(this, UniversesActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

                ImageView imageView = (ImageView) findViewById(R.id.imageView1);
                imageView.setImageBitmap(bitmap);

                detectAndFrame(bitmap);

            }
                catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void detectAndFrame(final Bitmap imageBitmap){
        ImageView imageView = (ImageView)findViewById(R.id.imageView1);
        imageAnalyser.detectAndFrame(imageBitmap, imageView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



}
