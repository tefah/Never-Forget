package com.tefah.neverforget;

import android.Manifest;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.tefah.neverforget.data.TaskContract;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static  int TASK_LOADER_ID = 0;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String audioFileName = null;
    private TaskAdapter taskAdapter;
    private MediaRecorder recorder = null;
    private MediaPlayer player = null;


    @BindView(R.id.tasksList)
    RecyclerView tasksList;
    @BindView(R.id.writeNote)
    FloatingActionButton writeNote;
    @BindView(R.id.voiceNote)
    FloatingActionButton voiceNote;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        taskAdapter = new TaskAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        tasksList.setLayoutManager(layoutManager);
        tasksList.setAdapter(taskAdapter);
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }
            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int id = (int) viewHolder.itemView.getTag();
                // Build appropriate uri with String row id appended
                String stringId = Integer.toString(id);
                Uri uri = TaskContract.TaskEntry.CONTENT_URI;
                uri = uri.buildUpon().appendPath(stringId).build();
                getContentResolver().delete(uri, null, null);
                getLoaderManager().restartLoader(TASK_LOADER_ID, null, MainActivity.this);
            }
        }).attachToRecyclerView(tasksList);

        writeNote.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                }
                else if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                    addTask();
                }
                return true;
            }
        });
        voiceNote.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int action = motionEvent.getAction();
                switch (action){
                    case MotionEvent.ACTION_DOWN:
                        recorder = new MediaRecorder();
                        audioFileName = Utilities.startRecording(recorder, MainActivity.this);
                        break;
                    case MotionEvent.ACTION_UP:
                        Utilities.stopRecording(recorder);
                        Toast.makeText(MainActivity.this, audioFileName, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
                        intent.putExtra("fileName", audioFileName);
                        intent.putExtra("date", Utilities.getUnixTime());
                        startActivity(intent);
                        break;
                    default:
                }
                return true;
            }
        });

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

    }

@OnClick(R.id.photoNote)
public void photoNote(){
    Intent intent = new Intent(this, AddTaskActivity.class);
    intent.putExtra("takePicture", true);
    intent.putExtra("date", Utilities.getUnixTime());
    startActivity(intent);
}

    @Override
    protected void onResume() {
        super.onResume();
        getLoaderManager().initLoader(TASK_LOADER_ID, null, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        TASK_LOADER_ID +=1;
    }

    public void addTask(){
        Intent intent = new Intent(this,AddTaskActivity.class);
        intent.putExtra("date", Utilities.getUnixTime());
        startActivity(intent);
    }

    private Cursor query(){
        return getContentResolver().query(TaskContract.TaskEntry.CONTENT_URI,
                null, null, null, TaskContract.TaskEntry.COLUMN_DATE);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new AsyncTaskLoader<Cursor>(this) {
            Cursor taskData = null;

            // onStartLoading() is called when a loader first starts loading data
            @Override
            protected void onStartLoading() {
                if (taskData != null) {
                    // Delivers any previously loaded data immediately
                    deliverResult(taskData);
                } else {
                    // Force a new load
                    forceLoad();
                }
            }

            @Override
            public Cursor loadInBackground() {
                try {
                    return query();

                } catch (Exception e) {
                    Log.e(TAG, "Failed to asynchronously load data.");
                    e.printStackTrace();
                    return null;
                }
            }

            // deliverResult sends the result of the load, a Cursor, to the registered listener
            public void deliverResult(Cursor data) {
                taskData = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        taskAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        taskAdapter.swapCursor(null);
    }

    private void startPlaying() {
        player = new MediaPlayer();
        try {
            player.setDataSource(audioFileName);
            player.prepare();
            player.start();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        player.release();
        player = null;
    }

}
