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

import com.microsoft.projectoxford.emotion.EmotionServiceClient;
import com.microsoft.projectoxford.emotion.EmotionServiceRestClient;
import com.microsoft.projectoxford.emotion.contract.*;
import com.microsoft.projectoxford.face.*;
import com.microsoft.projectoxford.face.contract.*;
import com.microsoft.projectoxford.face.contract.FaceRectangle;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private ImageAnalyser imageAnalyser = ImageAnalyser.getInstance();
    private final int PICK_IMAGE = 1;
    private static List<Bitmap> comic_faces = new ArrayList<>();
    public static AssetManager assetManager;
    List<RecognizeResult> recos;
    Bitmap bmp = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        String[] files;
        imageAnalyser = ImageAnalyser.getInstance();
        imageAnalyser.assetManager  = getAssets();
        assetManager = imageAnalyser.assetManager;
        Log.w("Leo", "onCreate");


        try{
            files = assetManager.list("");

            InputStream input = assetManager.open("marvel_villains.json");
            JsonReader comicReader = new JsonReader(new InputStreamReader(input));
                StringWriter writer = new StringWriter();
                IOUtils.copy(input, writer, "UTF-8");
                String theString = writer.toString();

                imageAnalyser.comicLib = new JSONObject(theString);
                imageAnalyser.comicCharacters = (JSONArray) imageAnalyser.comicLib.get("results");
                Log.w("Leo", "characters length is " + imageAnalyser.comicCharacters.length());
//                System.out.println(((JSONArray) comicLib.get("results")).getJSONObject(0).getJSONObject("emotions").getClass());

            } catch(Exception e){
            }



//            for(String f : files){
//                if(f.endsWith(".png")){
//                    InputStream input = assetManager.open(f);
//                    Bitmap bmp = BitmapFactory.decodeStream(input);
//                    comic_faces.add(bmp);
//                }
//            }

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

                /*ExifInterface ei = new ExifInterface(uri.getPath());
                int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_UNDEFINED);

                switch(orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        bitmap = rotateImage(bitmap, 90);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        bitmap = rotateImage(bitmap, 180);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        bitmap = rotateImage(bitmap, 270);
                        break;
                    case ExifInterface.ORIENTATION_NORMAL:
                    default:
                        break;
                }*/



                //bitmap = rotateImage(bitmap, 270);

                /*if(bitmap.getHeight() > 4096 || bitmap.getWidth() > 4096){
                    int large = Math.max(bitmap.getHeight(), bitmap.getWidth());
                    float shrinkFactor = large/4096*1.f;
                    bitmap = Bitmap.createScaledBitmap(bitmap, (int)(bitmap.getWidth()/shrinkFactor),
                            (int)(bitmap.getHeight()/shrinkFactor), false);
                }*/



                ImageView imageView = (ImageView) findViewById(R.id.imageView1);
                imageView.setImageBitmap(bitmap);

                detectAndFrame(bitmap);




}
                catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Detect faces by uploading face images
// Frame faces after detection

    private void detectAndFrame(final Bitmap imageBitmap){
        imageAnalyser.detectAndFrame(imageBitmap);
        ImageView imageView = (ImageView)findViewById(R.id.imageView1);
        imageView.setImageBitmap(imageAnalyser.bitmap);
//        imageView.setImageBitmap(imageBitmap);
    }


    private Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp2, new Matrix(), null);
        return bmOverlay;
    }




    private static Bitmap drawFaceRectanglesOnBitmap(Bitmap originalBitmap, Face[] faces) {
        Bitmap bitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.RED);
        int stokeWidth = 2;
        paint.setStrokeWidth(stokeWidth);
        if (faces != null) {
            for (Face face : faces) {
                FaceRectangle faceRectangle = face.faceRectangle;
                canvas.drawRect(
                        faceRectangle.left,
                        faceRectangle.top,
                        faceRectangle.left + faceRectangle.width,
                        faceRectangle.top + faceRectangle.height,
                        paint);
            }
        }
        return bitmap;
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


    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix,
                true);
    }

}
