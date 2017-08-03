package com.tefah.neverforget;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.tefah.neverforget.data.TaskContract;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddTaskActivity extends AppCompatActivity implements MediaPlayer.OnSeekCompleteListener {


    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_STORAGE_PERMISSION = 1;
    public static final String FILE_PROVIDER_AUTHORITY = "com.tefah.fileprovider";


    private String audioFileName;
    private long date;
    private Bitmap mResultsBitmap;
    private String mTempPhotoPath;

    @BindView(R.id.addTask)
    Button addTask;
    @BindView(R.id.writtenNote)
    EditText textNote;
    @BindView(R.id.imageNote)
    ImageView imageNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        if (intent.hasExtra("fileName")) {
            audioFileName = intent.getStringExtra("fileName");
        }
        date = intent.getLongExtra("date", 0);
        if (intent.getBooleanExtra("takePicture", false))
            photoNote();


        addTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                done();
            }
        });
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
            Utilities.deleteImageFile(this, mTempPhotoPath);
        }
    }

    private void done(){
        String text = textNote.getText().toString();
        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskEntry.COLUMN_TEXT, text);
        values.put(TaskContract.TaskEntry.COLUMN_DATE, date);
        values.put(TaskContract.TaskEntry.COLUMN_ALARM, 0);

        Uri uri = getContentResolver().insert(TaskContract.TaskEntry.CONTENT_URI, values);

//        Toast.makeText(this, uri.toString(), Toast.LENGTH_SHORT).show();
        Utilities.saveImage(this, mResultsBitmap);
        finish();
    }


    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {
        Utilities.stopPlaying(mediaPlayer);
    }
}
