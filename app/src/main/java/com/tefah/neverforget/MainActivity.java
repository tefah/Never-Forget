package com.tefah.neverforget;

import android.Manifest;
import android.app.ActivityOptions;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.transition.Explode;
import android.transition.TransitionInflater;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tefah.neverforget.data.TaskContract;
import com.tefah.neverforget.services.ServiceUtils;
import com.tefah.neverforget.widget.TaskWidgetService;

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
    private static final String name = "Main Activity";
    public static final int TABLET_DPI = 600;

    private boolean toUpdateTask = false;
    private static String audioPath = null;
    private TaskAdapter taskAdapter;
    private MediaRecorder recorder = null;
    private MediaPlayer player;
    private List<Task> tasks;
    public Task task;
    private static Tracker mTracker;
    public CallbackManager callbackManager;
    public static AccessToken accessToken;

    @BindView(R.id.tasksList)
    RecyclerView tasksList;
    @BindView(R.id.writeNote)
    FloatingActionButton writeNote;
    @BindView(R.id.voiceNote)
    FloatingActionButton voiceNote;
    @BindView(R.id.app_name)
    TextView appNameTV;
    @BindView(R.id.login_button)
    LoginButton loginButton;
    @BindView(R.id.empty_view)
    View emptyView;

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

        // admob requesting ads
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mAdView.loadAd(adRequest);

        //custom font for title
        Typeface indie = Typeface.createFromAsset(getAssets(), "IndieFlower.ttf");
        appNameTV.setTypeface(indie);

        // http://alvinalexander.com/android/how-to-determine-android-screen-size-dimensions-orientation
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int densityDpi = metrics.densityDpi;

        GridLayoutManager layoutManager;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ||
                densityDpi >= TABLET_DPI){
            layoutManager = new GridLayoutManager(this,3);
        } else
            layoutManager = new GridLayoutManager(this, 1);
        taskAdapter = new TaskAdapter(this, this);
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
                TaskWidgetService.startActionUpdateWidget(MainActivity.this);
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

        // Obtain the shared Tracker instance.
        Analytics application = (Analytics) getApplication();
        mTracker = application.getDefaultTracker();

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        getLoaderManager().initLoader(TASK_LOADER_ID, null, this);

        //facebook login
        accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null){
            Profile profile = Profile.getCurrentProfile();
            loginButton.setEnabled(false);
        }
        callbackManager = CallbackManager.Factory.create();
        // Callback registration
        loginButton.setReadPermissions("user_events");
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                accessToken = AccessToken.getCurrentAccessToken();
                loginButton.setEnabled(false);
                Utilities.grapEvents(MainActivity.this);
                ServiceUtils.scheduleChargingReminder(MainActivity.this);
            }

            @Override
            public void onCancel() {
                // App code
                Log.i(TAG, getString(R.string.login_canceled));
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                Log.i(TAG, getString(R.string.login_error) + exception);
            }
        });

        Explode explode = null;
            explode = (Explode) TransitionInflater.from(this).inflateTransition(R.transition.main_activity_exit);
            getWindow().setExitTransition(explode);

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

@OnClick(R.id.photoNote)
public void photoNote(){
    addTask(false, true);
}

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "Setting screen name: " + name);

        // google analytics sending screen name
        mTracker.setScreenName( name);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        toUpdateTask=false;

    }

    @OnClick(R.id.writeNote)
    public void writeNote(){
        addTask(false, false);
    }

    public void addTask(boolean hasVoice, boolean takePicture){

        Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(this).toBundle();
        mTracker.send(new HitBuilders.EventBuilder().setLabel("add task").build());
        Intent intent = new Intent(this,AddTaskActivity.class);
        if (takePicture)
            intent.setAction(getString(R.string.take_picture));
        else if (hasVoice)
            intent.putExtra(getString(R.string.task), Parcels.wrap(task));
        if (toUpdateTask)
            intent.setAction(getString(R.string.update_task));
        startActivity(intent, bundle);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this, TaskContract.TaskEntry.CONTENT_URI,
                null, null, null, TaskContract.TaskEntry.COLUMN_DATE);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        taskAdapter.swapCursor(cursor);
        tasks =  new ArrayList<>();
        for (int i =0; i< cursor.getCount(); i++){
            if (cursor.moveToPosition(i)){
                String text         = cursor.getString(TaskContract.TEXT_INDEX);
                String imagePath    = cursor.getString(TaskContract.IMAGE_INDEX);
                String voicePath    = cursor.getString(TaskContract.VOICE_INDEX);
                boolean alarm       = cursor.getInt(TaskContract.ALARM_INDEX) == 1 ? true : false;
                long date           = cursor.getLong(TaskContract.DATE_INDEX);
                String uri          = cursor.getString(TaskContract.URI_INDEX);
                tasks.add(new Task(uri, date, alarm, text, voicePath, imagePath));
            }
        }
        if (tasks.size() == 0)
            emptyView.setVisibility(View.VISIBLE);
        else emptyView.setVisibility(View.GONE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        taskAdapter.swapCursor(null);
    }

    @Override
    public void onClick(View view, int position) {
        if (view.getId() == R.id.playVoiceNote) {
            String audioPath = tasks.get(position).getAudioFilePath();
            if (audioPath == null){
                Toast.makeText(this, getString(R.string.no_audio_recorded), Toast.LENGTH_SHORT).show();
                return;
            }
            if (player != null){
                onSeekComplete(player);
            }
            player = new MediaPlayer();
            Utilities.startPlaying(player, audioPath, this);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null){
            onSeekComplete(player);
        }
    }
}