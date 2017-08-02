package com.tefah.neverforget;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tefah.neverforget.data.TaskContract;

/**
 * custom adapter
 */

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private Cursor cursor;
    private Context context;

    public TaskAdapter(Context context){
        this.context = context;
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tasks_list_item, null);

        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {

        int idIndex = cursor.getColumnIndex(TaskContract.TaskEntry._ID);
        int textInd = cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_TEXT);

        cursor.moveToPosition(position);
        String text = cursor.getString(textInd);
        int id = cursor.getInt(idIndex);

        holder.textNote.setText(text);
        holder.itemView.setTag(id);

    }

    @Override
    public int getItemCount() {
        if (cursor == null)
            return 0;
        return cursor.getCount();
    }

    /**
     * When data changes and a re-query occurs, this function swaps the old Cursor
     * with a newly updated Cursor (Cursor c) that is passed in.
     */
    public Cursor swapCursor(Cursor c) {
        // check if this cursor is the same as the previous cursor (mCursor)
        if (cursor == c) {
            return null; // bc nothing has changed
        }
        Cursor temp = cursor;
        this.cursor = c; // new cursor value assigned

        //check if this is a valid cursor, then update the cursor
        if (c != null) {
            this.notifyDataSetChanged();
        }
        return temp;
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder {

        TextView textNote;
        public TaskViewHolder(View itemView) {
            super(itemView);
            textNote = (TextView) itemView.findViewById(R.id.textNote);
        }
    }
}
