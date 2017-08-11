package com.tefah.neverforget;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.tefah.neverforget.data.TaskContract;
import com.tefah.neverforget.widget.TaskWidgetService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Utility class
 */

public class Utilities {

    public static void startPlaying(MediaPlayer player, String audioFileName, MediaPlayer.OnSeekCompleteListener listener) {
        try {
            player.setDataSource(audioFileName);
            player.prepare();
            player.start();
            player.setOnSeekCompleteListener(listener);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void stopPlaying(MediaPlayer player) {
        player.release();
        player = null;
    }

    public static String startRecording(MediaRecorder recorder, Context context) {
        String savedAudioPath = null;

        File storageDir = mainStorageDir();
        boolean success = true;
        storageDir = new File(storageDir.getPath() + "/audio");
        if (!storageDir.exists()) {
            success = storageDir.mkdirs();
        }
        if (success) {
            String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss",
                    Locale.getDefault()).format(new Date());
            // Record to the external cache directory for visibility
            String audioFileName = timeStamp + " audioNote.3gp";
            File imageFile = new File(storageDir, audioFileName);
            savedAudioPath = imageFile.getAbsolutePath();

            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setOutputFile(savedAudioPath);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            try {
                recorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }

            recorder.start();
        }
        return savedAudioPath;
    }

    public static void stopRecording(MediaRecorder recorder) {
        recorder.stop();
        recorder.release();
        recorder = null;
    }

    public static long timeStamp(){
        Calendar c = Calendar.getInstance();
        return c.getTime().getTime();
    }

    /**
     * Resamples the captured photo to fit the screen for better memory usage.
     *
     * @param context   The application context.
     * @param imagePath The path of the photo to be resampled.
     * @return The resampled bitmap
     */
    static Bitmap resamplePic(Context context, String imagePath) {

        // Get device screen size information
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        manager.getDefaultDisplay().getMetrics(metrics);

        int targetH = metrics.heightPixels;
        int targetW = metrics.widthPixels;

        // Get the dimensions of the original bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        return BitmapFactory.decodeFile(imagePath);
    }

    /**
     * Creates the temporary image file in the cache directory.
     *
     * @return The temporary image file.
     * @throws IOException Thrown if there is an error creating the file
     */
    static File createTempImageFile(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalCacheDir();

        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

    /**
     * Deletes image file for a given path.
     *
     * @param context   The application context.
     * @param path The path of the photo to be deleted.
     */
    static boolean deleteFile(Context context, String path) {
        if (path == null)
            return false;
        // Get the file
        File imageFile = new File(path);

        // Delete the image
        boolean deleted = imageFile.delete();

        // If there is an error deleting the file, show a Toast
        if (!deleted) {
            String errorMessage = context.getString(R.string.error);
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
        }

        return deleted;
    }

    /**
     * Helper method for adding the photo to the system photo gallery so it can be accessed
     * from other apps.
     *
     * @param imagePath The path of the saved image
     */
    private static void galleryAddPic(Context context, String imagePath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(imagePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }


    /**
     * Helper method for saving the image.
     *
     * @param context The application context.
     * @param image   The image to be saved.
     * @return The path of the saved image.
     */
    static String saveImage(Context context, Bitmap image) {

        String savedImagePath = null;

        // Create the new file in the external storage
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss",
                Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".jpg";
        File storageDir = mainStorageDir();
        boolean success = true;
        storageDir = new File(storageDir.getPath() + "/pics");
        if (!storageDir.exists()) {
            success = storageDir.mkdirs();
        }
        if (success) {
            File imageFile = new File(storageDir, imageFileName);
            savedImagePath = imageFile.getAbsolutePath();
            try {
                OutputStream fOut = new FileOutputStream(imageFile);
                image.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                fOut.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Add the image to the system gallery
            galleryAddPic(context, savedImagePath);

        }
        return savedImagePath;
    }

    private static File mainStorageDir(){
        File storageDir = new File(
                Environment.getExternalStorageDirectory()
                        + "/NeverForget");

        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        return storageDir;
    }

    public static void grapEvents(final Context context){
        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        try {
                            updateDB(context, fetchFacebookEvents(object));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "events");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private static void updateDB(Context context, List<Task> tasks) {

        for (int i =0; i<tasks.size(); i++){
            addNewTask(context, tasks.get(i));
        }
    }

    private static List<Task> fetchFacebookEvents( JSONObject root) throws JSONException {
        if (root.get("events") == null)
            return null;
        List<Task> tasks = new ArrayList<>();
        Log.i("ROOT", root.toString());
        JSONObject events = root.getJSONObject("events");
        JSONArray data = events.getJSONArray("data");
        for (int i =0; i<data.length(); i++){
            JSONObject event = (JSONObject) data.get(i);
            String name = event.getString("name");
            String time = event.getString("start_time");
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd",
                    Locale.getDefault()).format(new Date());
            Log.i("TIME STAMP", time + "  time stamp:  "+ timeStamp);
            time = time.substring(0, 10);
            Log.i("TIME STAMP", time + "  time stamp:  "+ timeStamp);
            if (time.equals(timeStamp)){
                Task task = new Task(timeStamp(),false, name);
                tasks.add(task);
            }
        }

        return tasks;
    }
    /**
     * adding new task to the db
     */
    public static void addNewTask(Context context, Task task){
        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskEntry.COLUMN_TEXT, task.getText());
        values.put(TaskContract.TaskEntry.COLUMN_VOICE, task.getAudioFilePath());
        values.put(TaskContract.TaskEntry.COLUMN_IMAGE, task.getImageFilePath());
        values.put(TaskContract.TaskEntry.COLUMN_DATE, task.getTimeStamp());
        values.put(TaskContract.TaskEntry.COLUMN_ALARM, 0);

        task.setUri((context.getContentResolver().insert(TaskContract.TaskEntry.CONTENT_URI, values)).toString());

        updateTask(context, task);
    }

    /**
     * updating an existing task in the db
     */
    public static void updateTask(Context context, Task task) {

        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskEntry.COLUMN_TEXT, task.getText());
        values.put(TaskContract.TaskEntry.COLUMN_VOICE, task.getAudioFilePath());
        values.put(TaskContract.TaskEntry.COLUMN_IMAGE, task.getImageFilePath());
        values.put(TaskContract.TaskEntry.COLUMN_DATE, task.getTimeStamp());
        values.put(TaskContract.TaskEntry.COLUMN_ALARM, 0);
        values.put(TaskContract.TaskEntry.COLUMN_URI, task.getUri());

        Uri taskUri = Uri.parse(task.getUri());
        int tasksUpdated = context.getContentResolver().update(taskUri, values, null, null);
        TaskWidgetService.startActionUpdateWidget(context);
    }
}
