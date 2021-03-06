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

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leonardo Aoun on 4/2/2017.
 */

 class ImageAnalyser {
    private static ImageAnalyser instance;
    private FaceServiceClient faceServiceClient;
    private EmotionServiceClient emotionServiceClient;
    ProgressDialog detectionProgressDialog;
    private List<RecognizeResult> recos;
    byte[] capturedImage;
    Bitmap bitmap;

    private ImageAnalyser(){
        faceServiceClient = new FaceServiceRestClient("0ca0c2e7070f4e85864dbc2ba18c8699");
        emotionServiceClient = new EmotionServiceRestClient("29361194922442848f54ab8ec5f65d08");
    }

    static ImageAnalyser getInstance(){
        if(instance == null){instance = new ImageAnalyser();}
        return instance;
    }

    void detectAndFrame(final Bitmap imageBitmap, final ImageView imageView, final Librarian librarian)
    {
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

//                            InputStream input = is2;
                            recos = emotionServiceClient.recognizeImage(is2);
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
                        imageView.setImageBitmap(drawFacesOnFaces(imageBitmap, result, recos, librarian));
                        imageBitmap/*bmp*/.recycle();
                    }
                };
        detectTask.execute(inputStream);
    }

    private Bitmap drawFacesOnFaces(Bitmap originalBitmap, Face[] faces, List<RecognizeResult> recos, Librarian librarian){
        Bitmap bitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bitmap);
        int nbOfFaces = recos.size();
        if(nbOfFaces > 0){
            for(int i = 0; i< nbOfFaces; i++){
                FaceRectangle faceRectangle = faces[i].faceRectangle;
                FaceAttribute attribute = faces[i].faceAttributes;
                HeadPose headPose = attribute.headPose;
                float roll = (float)(headPose.roll);

                String imageName = findBestComicFace(faces[i], recos.get(i), librarian);

                float faceWidth = faceRectangle.width;
                float faceHeight = faceRectangle.height;
                Bitmap originalComicFace = null;
                try{
                    originalComicFace = BitmapFactory.decodeStream(librarian.assetManager.open(imageName));
                } catch (Exception e){
                    System.out.println(e.getMessage());
                }
                float originalComicFaceWidth = originalComicFace.getWidth();
                float originalComicFaceHeight = originalComicFace.getHeight();
                float widthFactor = faceWidth/originalComicFaceWidth;
                float comicFaceMult = 1.25f;
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

    private String findBestComicFace(Face face, RecognizeResult reco, Librarian librarian){
        Scores scores = reco.scores;
        float age = (float)face.faceAttributes.age;
        String gender = face.faceAttributes.gender;
        String answer = "";

        try{
            int nbCharacters = librarian.nbCharacters();
            double minCost = Float.MAX_VALUE;
            for(int i = 0; i < nbCharacters; i++){
                double thisCost = librarian.getCost(scores, age, gender, i);
                if(thisCost < minCost){
                    minCost = thisCost;
                    answer = librarian.characterName(i);
                }
            }

        } catch (Exception e){
            System.out.println(e.getMessage());
        }
        return answer;
    }


    private String printScores(Scores scores){
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


    private Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix,
                true);
    }


    private Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp2, new Matrix(), null);
        return bmOverlay;
    }


}
