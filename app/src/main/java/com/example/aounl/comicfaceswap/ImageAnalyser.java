package com.example.aounl.comicfaceswap;

import android.app.ProgressDialog;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.microsoft.projectoxford.emotion.EmotionServiceClient;
import com.microsoft.projectoxford.emotion.EmotionServiceRestClient;
import com.microsoft.projectoxford.emotion.contract.RecognizeResult;
import com.microsoft.projectoxford.emotion.contract.Scores;
import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.contract.FaceAttribute;
import com.microsoft.projectoxford.face.contract.FaceRectangle;
import com.microsoft.projectoxford.face.contract.HeadPose;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leonardo Aoun on 4/2/2017.
 */

public class ImageAnalyser {
    private static ImageAnalyser instance;
    public static FaceServiceClient faceServiceClient;
    public static EmotionServiceClient emotionServiceClient;
    public static ProgressDialog detectionProgressDialog;
    private static List<RecognizeResult> recos;
    public static JSONObject comicLib;
    public static JSONArray comicCharacters;
    public static byte[] capturedImage;
    public static Bitmap bitmap;
    public static AssetManager assetManager;

    private ImageAnalyser(){
        faceServiceClient = new FaceServiceRestClient("0ca0c2e7070f4e85864dbc2ba18c8699");
        emotionServiceClient = new EmotionServiceRestClient("29361194922442848f54ab8ec5f65d08");
    }

    public static ImageAnalyser getInstance(){
        if(instance == null){instance = new ImageAnalyser();}
        return instance;
    }


    public static void detectAndFrame_dumb(final Bitmap imageBitmap){
        bitmap = imageBitmap.copy(imageBitmap.getConfig(), true);
    }

    public static void detectAndFrame(final Bitmap imageBitmap)
    {
        Log.w("Leo", "detectAndFrame");
        recos = new ArrayList<>();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG/*JPEG*/, 100, outputStream);
        ByteArrayInputStream inputStream =
                new ByteArrayInputStream(outputStream.toByteArray());
        bitmap = imageBitmap.copy(imageBitmap.getConfig(), true);

        AsyncTask<InputStream, String, Face[]> detectTask =
                new AsyncTask<InputStream, String, Face[]>() {
                    @Override
                    protected Face[] doInBackground(InputStream... params) {
                        Log.w("Leo", "doInBackground");

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
                            recos = emotionServiceClient.recognizeImage(input);
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
//                        ImageView imageView = (ImageView)findViewById(R.id.imageView1);
//                        imageView.setImageBitmap(drawFaceRectanglesOnBitmap(imageBitmap, result));
//                        bitmap = imageBitmap.copy(imageBitmap.getConfig(), true);
                        Bitmap temp = bitmap.copy(bitmap.getConfig(), true);
                        bitmap = drawFacesOnFaces(imageBitmap, result, recos);
                        //bitmap = temp.copy(temp.getConfig(), true);
                        imageBitmap/*bmp*/.recycle();
                    }
                };
        detectTask.execute(inputStream);
    }

    private static Bitmap drawFacesOnFaces(Bitmap originalBitmap, Face[] faces, List<RecognizeResult> recos){
        Log.w("Leo", "drawFacesOnFaces");
        Bitmap bitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bitmap);
        int nbOfFaces = recos.size();
        if(nbOfFaces > 0){
            for(int i = 0; i< nbOfFaces; i++){
                Log.w("Leo", "new face");
                FaceRectangle faceRectangle = faces[i].faceRectangle;
                FaceAttribute attribute = faces[i].faceAttributes;
                HeadPose headPose = attribute.headPose;
                float roll = (float)(headPose.roll);

                String imageName = findBestComicFace(faces[i], recos.get(i));
                Log.w("Leo", "image name is " + imageName);


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

    private static String findBestComicFace(Face face, RecognizeResult reco){
        Log.w("Leo", "findBestComicFace");
        Scores scores = reco.scores;
        float age = (float)face.faceAttributes.age;
        String gender = face.faceAttributes.gender;
        String answer = "";

        try{
            Log.w("Leo", "inside try, comic lib is null? " + (comicLib == null));
            Log.w("Leo", "comicCharacters length is " + comicCharacters.length());
            int nbCharacters = comicCharacters.length();
            Log.w("Leo", "after getJSONArray");
            Log.w("Leo", "nbCharacters is " + nbCharacters);
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
            Log.w("Leo", "ERROR: " + e.getMessage());
            System.out.println(e.getMessage());
        }
        return answer;
    }

    private static float getCost(Scores emotions, float age, String gender, JSONObject comicChar){
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

    private static String printScores(Scores scores){
        return "---------------------------- \n"
                + "anger: " + scores.anger + " \n"
                + "contempt: " + scores.contempt + " \n"
                + "disgust: " + scores.disgust + " \n"
                + "fear: " + scores.fear + " \n"
                + "happiness: " + scores.happiness + " \n"
                + "neutral: " + scores.neutral + " \n"
                + "sadness: " + scores.sadness + " \n"
                + "surprise: " + scores.surprise + " \n"
                + "---------------------------- \n";
    }


}