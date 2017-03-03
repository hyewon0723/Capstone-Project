package com.luke.android.travelogy.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.luke.android.travelogy.R;
import com.luke.android.travelogy.data.TravelogyContract;

/**
 * Created by lukekim on 2/22/17.
 */

public class WidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsService.RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new WidgetDataFactory(getApplicationContext(), intent);
    }
}

class WidgetDataFactory implements RemoteViewsService.RemoteViewsFactory {

    private Cursor mCursor;
    private Context mContext;
    int mWidgetId;

    public WidgetDataFactory(Context context, Intent intent) {
        mWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        mContext = context;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public int getCount() {
        return mCursor.getCount();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews remoteView = new RemoteViews(mContext.getPackageName(), R.layout.widget_flag_list);
        if (mCursor.moveToPosition(position)) {

            String flagName = mCursor.getString(TravelogyContract.FlagEntry.COL_FLAG_TITLE);

            Cursor photoCursor = mContext.getContentResolver().query(TravelogyContract.PhotoEntry.buildPhotoUriByFlag(flagName),TravelogyContract.PhotoEntry.PHOTO_COLUMNS,
                    null,null,null);
            remoteView.setTextViewText(R.id.flag_name, flagName);
            remoteView.setTextViewText(R.id.photo_count, Integer.toString(photoCursor.getCount()));

        }
        return remoteView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public void onDestroy() {
        if (mCursor != null) {
            mCursor.close();
        }
    }

    @Override
    public void onDataSetChanged() {
        if (mCursor != null) {
            mCursor.close();
        }

        mCursor = mContext.getContentResolver().query(
                TravelogyContract.FlagEntry.CONTENT_URI,
                TravelogyContract.FlagEntry.FLAG_COLUMNS,
                null,
                null,
                null);
    }

}
