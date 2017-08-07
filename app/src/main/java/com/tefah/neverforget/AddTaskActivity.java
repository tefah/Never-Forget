package com.tefah.neverforget;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tefah.neverforget.data.TaskContract;

import org.parceler.Parcels;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddTaskActivity extends AppCompatActivity implements MediaPlayer.OnSeekCompleteListener {


    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_STORAGE_PERMISSION = 1;
    public static final String FILE_PROVIDER_AUTHORITY = "com.tefah.fileprovider";
    public static final String name = "Add tAsk Activity";


    private boolean updateTask = false;
    private String audioPath;
    private String imagePath;
    private long date;
    private Bitmap mResultsBitmap;
    private String mTempPhotoPath;
    public Task task;
    MediaRecorder recorder;
    private static Tracker mTracker;

    @BindView(R.id.done)
    Button done;
    @BindView(R.id.writtenNote)
    EditText textNote;
    @BindView(R.id.imageNote)
    ImageView imageNote;
    @BindView(R.id.voiceNote)
    FloatingActionButton voiceNote;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        task = new Task();
        if (intent.getAction()!=null) {
            if (intent.getAction().equals(getString(R.string.take_picture)))
                photoNote();
            else if (intent.getAction().equals(getString(R.string.update_task)))
                updateTask = true;
        }
        if (intent.hasExtra(getString(R.string.task))) {
            task = Parcels.unwrap(intent.getParcelableExtra(getString(R.string.task)));
            imageNote.setImageBitmap(Utilities.resamplePic(this, task.getImageFilePath()));
            textNote.setText(task.getText());
        }

        voiceNote.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int action = motionEvent.getAction();
                switch (action){
                    case MotionEvent.ACTION_DOWN:
                        recorder = new MediaRecorder();
                        audioPath = Utilities.startRecording(recorder, AddTaskActivity.this);
                        break;
                    case MotionEvent.ACTION_UP:
                        Utilities.stopRecording(recorder);
                        break;
                    default:
                }
                return true;
            }
        });

        // Obtain the shared Tracker instance.
        Analytics application = (Analytics) getApplication();
        mTracker = application.getDefaultTracker();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTracker.setScreenName( name);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    /**
     * OnClick method for "camera note" Button. Launches the camera app.
     */
    @OnClick(R.id.photoNote)
    public void photoNote() {
        // Check for the external storage permission
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // If you do not have permission, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION);
        } else {
            // Launch the camera if the permission exists
            launchCamera();
        }
    }

    /**
     * Creates a temporary image file and captures a picture to store in it.
     */
    private void launchCamera() {

        // Create the capture image intent
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the temporary File where the photo should go
            File photoFile = null;
            try {
                photoFile = Utilities.createTempImageFile(this);
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {

                if (mTempPhotoPath != null)
                    Utilities.deleteFile(this, mTempPhotoPath);
                // Get the path of the temporary file
                mTempPhotoPath = photoFile.getAbsolutePath();

                // Get the content URI for the image file
                Uri photoURI = FileProvider.getUriForFile(this,
                        FILE_PROVIDER_AUTHORITY,
                        photoFile);

                // Add the URI so the camera can store the image
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                // Launch the camera activity
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // If the image capture activity was called and was successful
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Resample the saved image to fit the ImageView
            mResultsBitmap = Utilities.resamplePic(this, mTempPhotoPath);
            imageNote.setImageBitmap(mResultsBitmap);
        } else {

            // Otherwise, delete the temporary image file
            Utilities.deleteFile(this, mTempPhotoPath);
        }
    }

    @OnClick(R.id.voiceNotePlayer)
    public void play(){
        if (task.getAudioFilePath() != null)
            Utilities.startPlaying(task.getAudioFilePath(), this);
        else
            Toast.makeText(this, getString(R.string.no_audio_recorded), Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // Called when you request permission to read and write to external storage
        switch (requestCode) {
            case REQUEST_STORAGE_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // If you get permission, launch the camera
                    launchCamera();
                } else {
                    // If you do not get permission, show a Toast
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    @OnClick(R.id.done)
    public void done(){
        String text = textNote.getText().toString();
        if (text.isEmpty()) {
            text = new SimpleDateFormat("dd MM yyyy",
                    Locale.getDefault()).format(new Date());
        }
        if (mResultsBitmap!= null) {
            if (task.getImageFilePath()!= null)
                Utilities.deleteFile(this, task.getImageFilePath());
            Utilities.deleteFile(this, mTempPhotoPath);
            task.setImageFilePath(Utilities.saveImage(this, mResultsBitmap));
        }
        if (audioPath !=  null) {
            Utilities.deleteFile(this, task.getAudioFilePath());
            task.setAudioFilePath(audioPath);
        }
        task.setText(text);
        task.setTimeStamp(Utilities.timeStamp());
        if (updateTask)
            updateTask();
        else addNewTask();

        finish();
    }

    /**
     * adding new task to the db
     */
    public void addNewTask(){
        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskEntry.COLUMN_TEXT, task.getText());
        values.put(TaskContract.TaskEntry.COLUMN_VOICE, task.getAudioFilePath());
        values.put(TaskContract.TaskEntry.COLUMN_IMAGE, task.getImageFilePath());
        values.put(TaskContract.TaskEntry.COLUMN_DATE, task.getTimeStamp());
        values.put(TaskContract.TaskEntry.COLUMN_ALARM, 0);

        task.setUri((getContentResolver().insert(TaskContract.TaskEntry.CONTENT_URI, values)).toString());

        updateTask();
    }

    /**
     * updating an existing task in the db
     */
    public void updateTask() {

        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskEntry.COLUMN_TEXT, task.getText());
        values.put(TaskContract.TaskEntry.COLUMN_VOICE, task.getAudioFilePath());
        values.put(TaskContract.TaskEntry.COLUMN_IMAGE, task.getImageFilePath());
        values.put(TaskContract.TaskEntry.COLUMN_DATE, task.getTimeStamp());
        values.put(TaskContract.TaskEntry.COLUMN_ALARM, 0);
        values.put(TaskContract.TaskEntry.COLUMN_URI, task.getUri());

        Uri taskUri = Uri.parse(task.getUri());
        int tasksUpdated = getContentResolver().update(taskUri, values, null, null);
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {
        Utilities.stopPlaying(mediaPlayer);
    }
}
