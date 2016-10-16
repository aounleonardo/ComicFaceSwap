package com.example.aounl.comicfaceswap;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.JsonReader;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.view.*;
import android.graphics.*;
import android.widget.*;
import android.provider.*;

import com.microsoft.projectoxford.emotion.EmotionServiceClient;
import com.microsoft.projectoxford.emotion.EmotionServiceRestClient;
import com.microsoft.projectoxford.emotion.contract.RecognizeResult;
import com.microsoft.projectoxford.emotion.contract.Scores;
import com.microsoft.projectoxford.face.*;
import com.microsoft.projectoxford.face.contract.*;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private final int PICK_IMAGE = 1;
    private ProgressDialog detectionProgressDialog;
    private FaceServiceClient faceServiceClient = new FaceServiceRestClient("0ca0c2e7070f4e85864dbc2ba18c8699");
    private EmotionServiceClient emotionServiceClient = new EmotionServiceRestClient("29361194922442848f54ab8ec5f65d08");
    private static List<Bitmap> comic_faces = new ArrayList<>();
    private static JSONObject comicLib = new JSONObject();
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        AssetManager assetManager = getAssets();
        String[] files;


            try{
            files = assetManager.list("");

            InputStream input = assetManager.open("comicFaces.json");
            JsonReader comicReader = new JsonReader(new InputStreamReader(input));
                StringWriter writer = new StringWriter();
                IOUtils.copy(input, writer, "UTF-8");
                String theString = writer.toString();

                comicLib = new JSONObject(theString);
                JSONArray characters = (JSONArray) comicLib.get("results");
                System.out.println(((JSONArray) comicLib.get("results")).getJSONObject(0).getJSONObject("emotions").getClass());

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

        detectionProgressDialog = new ProgressDialog(this);
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


//                ByteArrayOutputStream bos = new ByteArrayOutputStream();
//                bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
//                byte[] bitmapdata = bos.toByteArray();
//                ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);
//
//                try(InputStream input = bs){
//                    System.out.println(" pre emotions ");
//                    List<RecognizeResult> emotions = emotionServiceClient.recognizeImage(input);
//                    System.out.println(" emotions " + emotions.size());
//
//                } catch(Exception e){
//
//                }


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Detect faces by uploading face images
// Frame faces after detection

    private void detectAndFrame(final Bitmap imageBitmap)
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        ByteArrayInputStream inputStream =
                new ByteArrayInputStream(outputStream.toByteArray());
        AsyncTask<InputStream, String, Face[]> detectTask =
                new AsyncTask<InputStream, String, Face[]>() {
                    @Override
                    protected Face[] doInBackground(InputStream... params) {
                        try {
                            FaceServiceClient.FaceAttributeType[] attributes = new FaceServiceClient.FaceAttributeType[]{
                                    FaceServiceClient.FaceAttributeType.Age,
                                    FaceServiceClient.FaceAttributeType.Gender,
                                    FaceServiceClient.FaceAttributeType.HeadPose
                            };
                            publishProgress("Detecting...");
                            Face[] result = faceServiceClient.detect(
                                    params[0],
                                    true,         // returnFaceId
                                    true,        // returnFaceLandmarks
                                    attributes           // returnFaceAttributes: a string like "age, gender"
                            );

                            if (result == null)
                            {
                                publishProgress("Detection Finished. Nothing detected");
                                return null;
                            }
                            publishProgress(
                                    String.format("Detection Finished. %d face(s) detected",
                                            result.length));
                            return result;
                        } catch (Exception e) {
                            publishProgress("Detection failed");
                            return null;
                        }
                    }
                    @Override
                    protected void onPreExecute() {
                        //TODO: show progress dialog
                        detectionProgressDialog.show();
                    }
                    @Override
                    protected void onProgressUpdate(String... progress) {
                        //TODO: update progress
                        detectionProgressDialog.setMessage(progress[0]);
                    }
                    @Override
                    protected void onPostExecute(Face[] result) {
                        //TODO: update face frames
                        detectionProgressDialog.dismiss();
                        if (result == null) return;
                        ImageView imageView = (ImageView)findViewById(R.id.imageView1);
                        imageView.setImageBitmap(drawFaceRectanglesOnBitmap(imageBitmap, result));
//                        imageView.setImageBitmap(drawFacesOnFaces(imageBitmap, result));
                        imageBitmap.recycle();
                    }
                };
        detectTask.execute(inputStream);
    }

    /*
    * int len = result.length;
                            FaceRectangle[] faceRectangles = new FaceRectangle[len];

                            for(int i = 0; i < len; i++){
                                faceRectangles[i] = result[i].faceRectangle;
                            }
                            try(InputStream input = params[0]){
                            List<RecognizeResult> emotions = emotionServiceClient.recognizeImage(input, faceRectangles);

                            } catch(Exception e){

                            }
    * */


    private Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp2, new Matrix(), null);
        return bmOverlay;
    }


    private static Bitmap drawFacesOnFaces(Bitmap originalBitmap, Face[] faces){
        Bitmap bitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bitmap);
        if(faces != null){
            for(Face face: faces){
                FaceRectangle faceRectangle = face.faceRectangle;
                FaceAttribute attribute = face.faceAttributes;
                HeadPose headPose = attribute.headPose;
                float roll = (float)(headPose.roll);

                float faceWidth = faceRectangle.width;
                float faceHeight = faceRectangle.height;
                Bitmap originalComicFace = comic_faces.get(2);
                float originalComicFaceWidth = originalComicFace.getWidth();
                float originalComicFaceHeight = originalComicFace.getHeight();
                float widthFactor = faceWidth/originalComicFaceWidth;
                float comicFaceMult = 2.5f;
                int comicWidth = (int)(originalComicFaceWidth*comicFaceMult*widthFactor);
                int comicHeight = (int)(originalComicFaceHeight*comicFaceMult*widthFactor);
                Bitmap comicFace = Bitmap.createScaledBitmap(originalComicFace, comicWidth, comicHeight, false);
                float centerX = faceRectangle.left + faceRectangle.width/2.f;
                float centerY = faceRectangle.top + faceRectangle.height/2.f;
                Matrix matrix = new Matrix();
                matrix.setTranslate(centerX - comicWidth/2.f, centerY - comicHeight/2.f);
                matrix.preRotate(roll, comicWidth/2.f, comicHeight/2.f);

                canvas.drawBitmap(comicFace, matrix, null);
            }
        }
        return bitmap;
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

    public float getCost(Scores emotions, float age, String gender, JSONObject comicChar){
        float cost = 0.0f;

        try{
        cost += Math.abs(emotions.anger - (double)comicChar.getJSONObject("emotions").get("anger"));
        cost += Math.abs(emotions.contempt - (double)comicChar.getJSONObject("emotions").get("contempt"));
        cost += Math.abs(emotions.disgust - (double)comicChar.getJSONObject("emotions").get("disgust"));
        cost += Math.abs(emotions.fear - (double)comicChar.getJSONObject("emotions").get("fear"));
        cost += Math.abs(emotions.happiness - (double)comicChar.getJSONObject("emotions").get("happiness"));
        cost += Math.abs(emotions.neutral - (double)comicChar.getJSONObject("emotions").get("neutral"));
        cost += Math.abs(emotions.sadness - (double)comicChar.getJSONObject("emotions").get("sadness"));
        cost += Math.abs(emotions.surprise - (double)comicChar.getJSONObject("emotions").get("surprise"));

        cost += Math.abs(age - (double)comicChar.getJSONObject("other").get("age"))/50;
        if(!gender.equals(comicChar.getJSONObject("other").get("gender"))){
            cost += 1.0;
        }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
        return cost;
    }
    public String findBestComicFace(Face face, RecognizeResult reco){
        Scores scores = reco.scores;
        float age = (float)face.faceAttributes.age;
        String gender = face.faceAttributes.gender;
        String answer = "";

        try{
            int nbCharacters = comicLib.getJSONArray("results").length();
            float minCost = Integer.MAX_VALUE;
            for(int i = 0; i < nbCharacters; i++){
                JSONObject character = comicLib.getJSONArray("results").getJSONObject(i);
                float thisCost = getCost(scores, age, gender, character);
                if(thisCost < minCost){
                    minCost = thisCost;
                    answer = character.getString("name");
                }
            }

        } catch (Exception e){
            System.out.println(e.getMessage());
        }
        return answer;
    }

}
