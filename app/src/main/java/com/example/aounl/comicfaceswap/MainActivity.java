package com.example.aounl.comicfaceswap;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonReader;
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
    private final int PICK_IMAGE = 1;
    private ProgressDialog detectionProgressDialog;
    private FaceServiceClient faceServiceClient = new FaceServiceRestClient("0ca0c2e7070f4e85864dbc2ba18c8699");
    private EmotionServiceClient emotionServiceClient = new EmotionServiceRestClient("29361194922442848f54ab8ec5f65d08");
    private static List<Bitmap> comic_faces = new ArrayList<>();
    private static JSONObject comicLib = new JSONObject();
    private static AssetManager assetManager;
    List<RecognizeResult> recos;
    Bitmap bmp = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        String[] files;
        assetManager  = getAssets();
        recos = new ArrayList<RecognizeResult>();
            try{
            files = assetManager.list("");

            InputStream input = assetManager.open("marvel_villains.json");
            JsonReader comicReader = new JsonReader(new InputStreamReader(input));
                StringWriter writer = new StringWriter();
                IOUtils.copy(input, writer, "UTF-8");
                String theString = writer.toString();

                comicLib = new JSONObject(theString);
                JSONArray characters = (JSONArray) comicLib.get("results");
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

        detectionProgressDialog = new ProgressDialog(this);
    }

    public void takePic(View view){
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }

    public void analyzeCaptured(View view){
        Bundle extras = getIntent().getExtras();
        byte[] capturedImage = extras.getByteArray("capturedImage");
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

    private void detectAndFrame(final Bitmap imageBitmap)
    {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat./*PNG*/JPEG, 100, outputStream);
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
                            System.out.println(" nb of params " + params.length);
//                            InputStream cloner = params[0]


                            ByteArrayOutputStream baos = new ByteArrayOutputStream();

// Fake code simulating the copy
// You can generally do better with nio if you need...
// And please, unlike me, do something about the Exceptions :D
                            // ----------------------

                            //BufferedInputStream bufferedInputStream = new BufferedInputStream(params[0]);
                           // /*Bitmap */bmp = BitmapFactory.decodeStream(bufferedInputStream);
                           // bmp = Bitmap.createScaledBitmap(bmp, bmp.getWidth(), bmp.getHeight(), false);
                           // System.out.println("bytecount of this bitmap" + bmp.getByteCount());
                          //  ByteArrayOutputStream bos = new ByteArrayOutputStream();
                          //  bmp.compress(Bitmap.CompressFormat.JPEG/*PNG*/, 0 /*ignored for PNG*/, bos);
                          //  byte[] bitmapdata = bos.toByteArray();
                           // ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);

                            // ------------------------


                            byte[] buffer = new byte[1024];
                            int len;
                            /* change params 0 so that it reads from a very low def image */
                            while ((len = params[0].read(buffer)) > -1 ) {
                                baos.write(buffer, 0, len);
                            }
                            baos.flush();



// Open new InputStreams using the recorded bytes
// Can be repeated as many times as you wish
                            InputStream is1 = new ByteArrayInputStream(baos.toByteArray());
                            InputStream is2 = new ByteArrayInputStream(baos.toByteArray());


                            Face[] result = faceServiceClient.detect(
                                    is1,
                                    true,         // returnFaceId
                                    true,        // returnFaceLandmarks
                                    attributes           // returnFaceAttributes: a string like "age, gender"
                            );
                            is1.close();

                                InputStream input = is2;
                                recos = emotionServiceClient.recognizeImage( input);
                            is2.close();
                            for(RecognizeResult reco : recos){
                                System.out.println(printScores(reco.scores));
                            }


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
                            System.out.println(" I went into the catch " + e.getMessage());

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
//                        imageView.setImageBitmap(drawFaceRectanglesOnBitmap(imageBitmap, result));
                        imageView.setImageBitmap(drawFacesOnFaces(/*bmp*/imageBitmap, result, recos));
                        imageBitmap/*bmp*/.recycle();
                    }
                };
        detectTask.execute(inputStream);
    }





    private Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp2, new Matrix(), null);
        return bmOverlay;
    }


    private static Bitmap drawFacesOnFaces(Bitmap originalBitmap, Face[] faces, List<RecognizeResult> recos){
        Bitmap bitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bitmap);
        int nbOfFaces = recos.size();
        if(nbOfFaces > 0){
            for(int i = 0; i< nbOfFaces; i++){
                FaceRectangle faceRectangle = faces[i].faceRectangle;
                FaceAttribute attribute = faces[i].faceAttributes;
                HeadPose headPose = attribute.headPose;
                float roll = (float)(headPose.roll);

                String imageName = findBestComicFace(faces[i], recos.get(i));


                float faceWidth = faceRectangle.width;
                float faceHeight = faceRectangle.height;
                Bitmap originalComicFace = null;
                try{
                    originalComicFace = BitmapFactory.decodeStream(assetManager.open(imageName));
                } catch (Exception e){
                    System.out.println(e.getMessage());
                }
                float originalComicFaceWidth = originalComicFace.getWidth();
                float originalComicFaceHeight = originalComicFace.getHeight();
                float widthFactor = faceWidth/originalComicFaceWidth;
                float comicFaceMult = 1.75f;
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

    public static float getCost(Scores emotions, float age, String gender, JSONObject comicChar){
        float cost = 0.0f;

        try{
        if(!gender.equals(comicChar.getJSONObject("other").get("gender"))){
                cost = 100.f;
            } else{

        cost += Math.pow(Math.abs(emotions.anger - (double)comicChar.getJSONObject("emotions").get("anger") *1.0), 2);
        cost += Math.pow(Math.abs(emotions.contempt - (double)comicChar.getJSONObject("emotions").get("contempt")*1.0), 2);
        cost += Math.pow(Math.abs(emotions.disgust - (double)comicChar.getJSONObject("emotions").get("disgust")*1.0), 2);
        cost += Math.pow(Math.abs(emotions.fear - (double)comicChar.getJSONObject("emotions").get("fear")*1.0), 2);
        cost += Math.pow(Math.abs(emotions.happiness - (double)comicChar.getJSONObject("emotions").get("happiness")*1.0), 2);
        cost += Math.pow(Math.abs(emotions.neutral - (double)comicChar.getJSONObject("emotions").get("neutral")*1.0), 2);
        cost += Math.pow(Math.abs(emotions.sadness - (double)comicChar.getJSONObject("emotions").get("sadness")*1.0), 2);
        cost += Math.pow(Math.abs(emotions.surprise - (double)comicChar.getJSONObject("emotions").get("surprise")*1.0), 2);

        cost += Math.pow(Math.abs(age - (double)comicChar.getJSONObject("other").get("age"))/50.f, 2);
        }
        }catch(Exception e){
            System.out.println("qwertyuio" + e.getMessage());
        }
        try{
            System.out.println(comicChar.getString("name") + "  costs   " + cost);
        } catch (Exception e){}
        return cost;
    }
    public static String findBestComicFace(Face face, RecognizeResult reco){
        Scores scores = reco.scores;
        float age = (float)face.faceAttributes.age;
        String gender = face.faceAttributes.gender;
        String answer = "";

        try{
            int nbCharacters = comicLib.getJSONArray("results").length();
            float minCost = Float.MAX_VALUE;
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

    public static String printScores(Scores scores){
        StringBuilder sb = new StringBuilder();
        sb.append("---------------------------- \n");
        sb.append("anger: " + scores.anger + " \n");
        sb.append("contempt: " + scores.contempt + " \n");
        sb.append("disgust: " + scores.disgust + " \n");
        sb.append("fear: " + scores.fear + " \n");
        sb.append("happiness: " + scores.happiness + " \n");
        sb.append("neutral: " + scores.neutral + " \n");
        sb.append("sadness: " + scores.sadness + " \n");
        sb.append("surprise: " + scores.surprise + " \n");
        sb.append("---------------------------- \n");

        return sb.toString();
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix,
                true);
    }

}
