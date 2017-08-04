package com.tefah.neverforget;

import android.Manifest;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
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

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        TaskAdapter.OnTaskClickListener, MediaPlayer.OnSeekCompleteListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static  int TASK_LOADER_ID = 0;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    private boolean toUpdateTask = false;
    private static String audioPath = null;
    private TaskAdapter taskAdapter;
    private MediaRecorder recorder = null;
    private List<Task> tasks;
    public Task task;

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

        tasks = new ArrayList<>();
        taskAdapter = new TaskAdapter(this, this);
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

        voiceNote.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int action = motionEvent.getAction();
                switch (action){
                    case MotionEvent.ACTION_DOWN:
                        recorder = new MediaRecorder();
                        audioPath = Utilities.startRecording(recorder, MainActivity.this);
                        break;
                    case MotionEvent.ACTION_UP:
                        Utilities.stopRecording(recorder);
                        task = new Task();
                        task.setAudioFilePath(audioPath);
                        addTask(true, false);
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
    addTask(false, true);
}

    @Override
    protected void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(TASK_LOADER_ID, null, this);
    }

    @OnClick(R.id.writeNote)
    public void writeNote(){
        addTask(false, false);
    }



    public void addTask(boolean hasVoice, boolean takePicture){
        Intent intent = new Intent(this,AddTaskActivity.class);
        if (takePicture)
            intent.setAction(getString(R.string.take_picture));
        else if (hasVoice)
            intent.putExtra(getString(R.string.task), Parcels.wrap(task));
        if (toUpdateTask)
            intent.setAction(getString(R.string.update_task));
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
                super.deliverResult(data);
                taskData = data;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        taskAdapter.swapCursor(cursor);
        for (int i =0; i< cursor.getCount(); i++){
            if (cursor.moveToNext()){
                int id = cursor.getInt(TaskContract.ID_INDEX);
                String text         = cursor.getString(TaskContract.TEXT_INDEX);
                String imagePath    = cursor.getString(TaskContract.IMAGE_INDEX);
                String voicePath    = cursor.getString(TaskContract.VOICE_INDEX);
                boolean alarm       = cursor.getInt(TaskContract.ALARM_INDEX) == 1 ? true : false;
                long date           = cursor.getLong(TaskContract.DATE_INDEX);
                tasks.add(new Task(id, date, alarm, text, voicePath, imagePath));
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        taskAdapter.swapCursor(null);
    }

    @Override
    public void onClick(View view, int position) {
        Log.i("MAIN ACTIVITY", view.getId() + " ");
        if (view.getId() == R.id.playVoiceNote) {
            String audioPath = tasks.get(position).getAudioFilePath();
            if (audioPath == null){
                Toast.makeText(this, getString(R.string.no_audio_recorded), Toast.LENGTH_SHORT).show();
                return;
            }
            Utilities.startPlaying(audioPath, this);
        }else {
            task = tasks.get(position);
            toUpdateTask = true;
            addTask(true, false);
        }

    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {
        Utilities.stopPlaying(mediaPlayer);
    }
}