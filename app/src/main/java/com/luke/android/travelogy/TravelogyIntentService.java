package com.luke.android.travelogy;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.gcm.TaskParams;

/**
 * Created by lukekim on 12/12/16.
 */
public class TravelogyIntentService extends IntentService {


    public TravelogyIntentService(){
        super(TravelogyIntentService.class.getName());
    }

    public TravelogyIntentService(String name) {
        super(name);
    }

    @Override protected void onHandleIntent(Intent intent) {

        Bundle extras = intent.getExtras();

        TravelogyTaskService flagTaskService = new TravelogyTaskService(this);
        Bundle args = new Bundle();
        if (intent.getStringExtra("tag").equals("add")){
            args.putString("name", intent.getStringExtra("name"));
        }
        else if (intent.getStringExtra("tag").equals("addPhoto")) {
            args.putString("name", intent.getStringExtra("name"));
            args.putString("flagName", intent.getStringExtra("flagName"));
        }

        // We can call OnRunTask from the intent service to force it to run immediately instead of
        // scheduling a task.
        flagTaskService.onRunTask(new TaskParams(intent.getStringExtra("tag"), args));
    }


}

