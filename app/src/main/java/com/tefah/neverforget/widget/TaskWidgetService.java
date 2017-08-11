package com.tefah.neverforget.widget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.tefah.neverforget.R;

public class TaskWidgetService extends IntentService{

    public static final String ACTION_UPDATE_WIDGET = "update widget";

    public TaskWidgetService(){
        super("TaskWidgetService");
    }

    public static void startActionUpdateWidget(Context context){
        Intent intent = new Intent(context, TaskWidgetService.class);
        intent.setAction(ACTION_UPDATE_WIDGET);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPDATE_WIDGET.equals(action)) {
                updateWidget();
            }
        }
    }

    private void updateWidget() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, TaskWidgetProvider.class));
        //Trigger data update to handle the GridView widgets and force a data refresh
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_grid_view);
        //Now update all widgets
        TaskWidgetProvider.updateTaskWidgets(this, appWidgetManager, appWidgetIds);
    }
}