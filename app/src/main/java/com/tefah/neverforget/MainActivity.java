package com.tefah.neverforget;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.tasksList)
    RecyclerView tasksList;
    @BindView(R.id.writeNote)
    FloatingActionButton writeNote;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        TaskAdapter taskAdapter = new TaskAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        tasksList.setLayoutManager(layoutManager);
        tasksList.setAdapter(taskAdapter);

        writeNote.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                }
                else if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                        Log.i("ACTIONdOWN", "ENOUGH IDIOT " );
                    addTask();
                }
                return true;
            }
        });
    }
    public void addTask(){
        Intent intent = new Intent(this,AddTaskActivity.class);
        startActivity(intent);
    }
}
