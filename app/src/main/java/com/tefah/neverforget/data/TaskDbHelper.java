package com.tefah.neverforget.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.tefah.neverforget.data.TaskContract.TaskEntry;

/**
 * Sqlite helper class
 */

public class TaskDbHelper  extends SQLiteOpenHelper {

    // The name of the database
    private static final String DATABASE_NAME = "tasksDb.db";

    // If you change the database schema, you must increment the database version
    private static final int VERSION = 1;


    // Constructor
    TaskDbHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    /**
     * Called when the tasks database is created for the first time.
     * or when scheme is changed
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        // Create tasks table (careful to follow SQL formatting rules)
        final String CREATE_TABLE = "CREATE TABLE "  + TaskEntry.TABLE_NAME + " (" +
                TaskEntry._ID                + " INTEGER PRIMARY KEY, " +
                TaskEntry.COLUMN_TEXT + " TEXT , " +
                TaskEntry.COLUMN_IMAGE    + " TEXT, " +
                TaskEntry.COLUMN_VOICE +" TEXT, " +
                TaskEntry.COLUMN_ALARM + " INTEGER NOT NULL, " +
                TaskEntry.COLUMN_DATE + " INTEGER NOT NULL " +
                " );";

        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TaskEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
