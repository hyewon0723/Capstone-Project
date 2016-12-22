package com.luke.android.travelogy;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.TaskParams;

/**
 * Created by lukekim on 12/12/16.
 */
public class FlagIntentService extends IntentService {


    public FlagIntentService(){
        super(FlagIntentService.class.getName());
    }

    public FlagIntentService(String name) {
        super(name);
    }

    @Override protected void onHandleIntent(Intent intent) {

        Bundle extras = intent.getExtras();
        Log.d(FlagIntentService.class.getSimpleName(), "Flag Intent Service");
        Log.v("Luke","FlagIntentService !!!!!!!!!!!!!!!!!!");
        FlagTaskService flagTaskService = new FlagTaskService(this);
        Bundle args = new Bundle();
        if (intent.getStringExtra("tag").equals("add")){
            args.putString("name", intent.getStringExtra("name"));
            Log.v("Luke", "FlagIntentService onHandleIntent "+intent.getStringExtra("name"));
        }
        // We can call OnRunTask from the intent service to force it to run immediately instead of
        // scheduling a task.
        flagTaskService.onRunTask(new TaskParams(intent.getStringExtra("tag"), args));
    }


}

