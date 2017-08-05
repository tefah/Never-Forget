package com.tefah.neverforget.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by TEFA on 7/30/2017.
 */

public class TaskContract {

    public static final String AUTHORITY = "com.tefah.neverforget";

    // The base content URI = "content://" + <authority>
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_TASKS = "tasks";

    public static final int ID_INDEX = 0;
    public static final int TEXT_INDEX  = ID_INDEX + 1;
    public static final int IMAGE_INDEX = TEXT_INDEX + 1;
    public static final int VOICE_INDEX = IMAGE_INDEX + 1;
    public static final int ALARM_INDEX = VOICE_INDEX + 1;
    public static final int DATE_INDEX  = ALARM_INDEX + 1;
    public static final int URI_INDEX   = DATE_INDEX + 1;


    /* TaskEntry is an inner class that defines the contents of the task table */
    public static final class TaskEntry implements BaseColumns {

        // TaskEntry content URI = base content URI + path
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TASKS).build();


        // Task table and column names
        public static final String TABLE_NAME = "tasks";

        // Since TaskEntry implements the interface "BaseColumns", it has an automatically produced
        // "_ID" column in addition to the two below
        public static final String COLUMN_TEXT          = "text";
        public static final String COLUMN_IMAGE         = "image";
        public static final String COLUMN_VOICE         = "voice";
        public static final String COLUMN_DATE          = "date";
        public static final String COLUMN_ALARM         = "alarm";
        public static final String COLUMN_URI           = "uri";

    }

}
