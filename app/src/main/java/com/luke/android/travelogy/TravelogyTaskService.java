package com.luke.android.travelogy;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.luke.android.travelogy.data.TravelogyContract;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by lukekim on 12/12/16.
 */

public class TravelogyTaskService extends GcmTaskService {

    private static String LOG_TAG = TravelogyTaskService.class.getSimpleName();

    private OkHttpClient client = new OkHttpClient();

    private Context mContext;
    private StringBuilder mStoredSymbols = new StringBuilder();
    private boolean mIsUpdate;

    public TravelogyTaskService(Context context) {
        mContext = context;
    }

    String fetchData(String url) throws IOException{
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    interface Listener {
        void onFetchFinished(String countryCode);
    }

    @SuppressWarnings("unused")
    public TravelogyTaskService() {
    }

    @Override
    public int onRunTask(TaskParams params) {

        if (mContext == null) {
            return GcmNetworkManager.RESULT_FAILURE;
        }

        int result = GcmNetworkManager.RESULT_FAILURE;

        if (params.getTag().equals("add")) {
            // get symbol from params.getExtra and build query
            String flagInput = params.getExtras().getString("name");
            Log.v("Luke", "TravelogyTaskService onRunTask 1" + flagInput);
            StringBuilder urlStringBuilder = new StringBuilder();
            urlStringBuilder.append("https://restcountries.eu/rest/v1/name/"+flagInput);

            String urlString;
            String getResponse;

            if (urlStringBuilder != null){

                urlString = urlStringBuilder.toString();
                Log.v("Luke", "TravelogyTaskService #### urlString "+urlString);
                try{
                    getResponse = fetchData(urlString);
                    result = GcmNetworkManager.RESULT_SUCCESS;
                        ArrayList list = quoteJsonToContentVals(getResponse);

                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
        else if (params.getTag().equals("addPhoto")) {
            String PhotoInput = params.getExtras().getString("name");
            String flagName = params.getExtras().getString("flagName");
            Log.v("Luke", "TravelogyTaskService onRunTask @@@@@@@@@@  @@@@@@@@@@  flagName: " + flagName );
            long flagId = -1;
            Cursor flagCursor = mContext.getContentResolver().query(
                    TravelogyContract.FlagEntry.CONTENT_URI,
                    new String[]{TravelogyContract.FlagEntry._ID},
                    TravelogyContract.FlagEntry.COLUMN_FLAG_SETTING + " = ?",
                    new String[]{flagName},
                    null);

            if (flagCursor.moveToFirst()) {
                int locationIdIndex = flagCursor.getColumnIndex(TravelogyContract.FlagEntry._ID);
                flagId = flagCursor.getLong(locationIdIndex);
            }
            Log.v("Luke", "TravelogyTaskService onRunTask @@@@@@@@@@  @@@@@@@@@@" + PhotoInput + " flagId "+flagId);
            ContentValues photoValues = new ContentValues();
            photoValues.put(TravelogyContract.PhotoEntry.COLUMN_FLAG_KEY,
                    flagId);
            photoValues.put(TravelogyContract.PhotoEntry.COLUMN_PHOTO_ID,
                    0);
            photoValues.put(TravelogyContract.PhotoEntry.COLUMN_PHOTO_TITLE,
                    PhotoInput);
            photoValues.put(TravelogyContract.PhotoEntry.COLUMN_PHOTO_PATH,
                    PhotoInput);
            mContext.getContentResolver().insert(
                    TravelogyContract.PhotoEntry.CONTENT_URI,
                    photoValues
            );
        }

        return result;
    }


    public ArrayList quoteJsonToContentVals(String JSON){
        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
        JSONObject jsonObject = null;
        JSONArray resultsArray = null;
        try{
            resultsArray = new JSONArray(JSON);
            Log.v("Luke", "TravelogyTaskService #### resultsArray.length()  "+resultsArray.length());
            if (resultsArray != null && resultsArray.length() != 0){

                JSONObject countryCode = resultsArray.getJSONObject(0);
                Log.v("Luke", "TravelogyTaskService #### countryCode.getString(\"name\")  "+countryCode.getString("name"));
                Log.v("Luke", "TravelogyTaskService #### countryCode.getString(\"alpha2Code\")  "+countryCode.getString("alpha2Code"));

                ContentValues flagValues = new ContentValues();
                flagValues.put(TravelogyContract.FlagEntry.COLUMN_FLAG_ID,
                        0);
                flagValues.put(TravelogyContract.FlagEntry.COLUMN_FLAG_TITLE,
                        countryCode.getString("name"));
                flagValues.put(TravelogyContract.FlagEntry.COLUMN_FLAG_POSTER_PATH,
                        countryCode.getString("alpha2Code"));
                flagValues.put(TravelogyContract.FlagEntry.COLUMN_FLAG_BACKDROP_PATH,
                        countryCode.getString("alpha2Code"));
                flagValues.put(TravelogyContract.FlagEntry.COLUMN_FLAG_SETTING,
                        countryCode.getString("name"));
                mContext.getContentResolver().insert(
                        TravelogyContract.FlagEntry.CONTENT_URI,
                        flagValues
                );

                Log.v("Luke", "TravelogyTaskService #### mContextInSERTED !!!!! ");


            }
        } catch (JSONException e){
            Log.e(LOG_TAG, "String to JSON failed: " + e);
        }
        return batchOperations;
    }



}
