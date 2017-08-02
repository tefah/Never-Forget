package com.tefah.neverforget;

import android.content.ContentValues;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.tefah.neverforget.data.TaskContract;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddTaskActivity extends AppCompatActivity {

    @BindView(R.id.addTask)
    Button addTask;
    @BindView(R.id.writtenNote)
    EditText textNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        ButterKnife.bind(this);
        addTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                done();
            }
        });
    }
    private void done(){
        String text = textNote.getText().toString();
        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskEntry.COLUMN_TEXT, text);
        values.put(TaskContract.TaskEntry.COLUMN_DATE, 1);
        values.put(TaskContract.TaskEntry.COLUMN_ALARM, 0);

        Uri uri = getContentResolver().insert(TaskContract.TaskEntry.CONTENT_URI, values);

        Toast.makeText(this, uri.toString(), Toast.LENGTH_SHORT).show();
        finish();
    }
}
