package com.tefah.neverforget;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.tefah.neverforget.data.TaskContract;

/**
 * custom adapter
 */

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private Cursor cursor;
    private Context context;
    OnTaskClickListener clickListener;

    public interface OnTaskClickListener {
        public void onClick(View view, int position);
    }

    public TaskAdapter(Context context, OnTaskClickListener clickListener){
        this.context = context;
        this.clickListener = clickListener;
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tasks_list_item, null);

        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {

        cursor.moveToPosition(position);
        String text = cursor.getString(TaskContract.TEXT_INDEX);
        int id = cursor.getInt(TaskContract.ID_INDEX);
        String imagePath = cursor.getString(TaskContract.IMAGE_INDEX);
        Bitmap bitmap = Utilities.resamplePic(context, imagePath);

        holder.textNote.setText(text);
        holder.itemView.setTag(id);
        if (bitmap == null)
            holder.imageNote.setImageResource(R.mipmap.note);
        else
            holder.imageNote.setImageBitmap(bitmap);

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

    public class TaskViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView textNote;
        ImageView imageNote;
        ImageButton play;
        public TaskViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            textNote = (TextView) itemView.findViewById(R.id.textNote);
            imageNote = (ImageView) itemView.findViewById(R.id.imageNote);
            play = (ImageButton) itemView.findViewById(R.id.playVoiceNote);
            play.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            clickListener.onClick(view, getAdapterPosition());
        }
    }
}
