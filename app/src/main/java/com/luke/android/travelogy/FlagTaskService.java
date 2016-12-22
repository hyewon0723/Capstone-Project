package com.luke.android.travelogy;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.luke.android.travelogy.data.FlagContract;
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

public class FlagTaskService extends GcmTaskService {

    private static String LOG_TAG = FlagTaskService.class.getSimpleName();

    private OkHttpClient client = new OkHttpClient();

    private Context mContext;
    private StringBuilder mStoredSymbols = new StringBuilder();
    private boolean mIsUpdate;

    public FlagTaskService(Context context) {
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
    public FlagTaskService() {
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
            Log.v("Luke", "FlagTaskService onRunTask " + flagInput);
            StringBuilder urlStringBuilder = new StringBuilder();
            urlStringBuilder.append("https://restcountries.eu/rest/v1/name/"+flagInput);

            String urlString;
            String getResponse;

            if (urlStringBuilder != null){

                urlString = urlStringBuilder.toString();
                Log.v("Luke", "FlagTaskService #### urlString "+urlString);
                try{
                    getResponse = fetchData(urlString);
                    result = GcmNetworkManager.RESULT_SUCCESS;
                        ArrayList list = quoteJsonToContentVals(getResponse);

                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        }

        return result;
    }


    public ArrayList quoteJsonToContentVals(String JSON){
        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
        JSONObject jsonObject = null;
        JSONArray resultsArray = null;
        try{
            resultsArray = new JSONArray(JSON);
            Log.v("Luke", "FlagTaskService #### resultsArray.length()  "+resultsArray.length());
            if (resultsArray != null && resultsArray.length() != 0){

                JSONObject countryCode = resultsArray.getJSONObject(0);
                Log.v("Luke", "FlagTaskService #### countryCode.getString(\"name\")  "+countryCode.getString("name"));
                Log.v("Luke", "FlagTaskService #### countryCode.getString(\"alpha2Code\")  "+countryCode.getString("alpha2Code"));

                ContentValues movieValues = new ContentValues();
                movieValues.put(FlagContract.FlagEntry.COLUMN_FLAG_ID,
                        0);
                movieValues.put(FlagContract.FlagEntry.COLUMN_FLAG_TITLE,
                        countryCode.getString("name"));
                movieValues.put(FlagContract.FlagEntry.COLUMN_FLAG_POSTER_PATH,
                        countryCode.getString("alpha2Code"));
                movieValues.put(FlagContract.FlagEntry.COLUMN_FLAG_BACKDROP_PATH,
                        countryCode.getString("alpha2Code"));
                mContext.getContentResolver().insert(
                        FlagContract.FlagEntry.CONTENT_URI,
                        movieValues
                );

                Log.v("Luke", "FlagTaskService #### mContextInSERTED !!!!! ");


            }
        } catch (JSONException e){
            Log.e(LOG_TAG, "String to JSON failed: " + e);
        }
        return batchOperations;
    }



}
