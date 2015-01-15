package com.expidev.gcmapp.http;

import android.os.AsyncTask;
import android.util.Log;

import com.expidev.gcmapp.utils.JsonStringReader;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by William.Randall on 1/9/2015.
 */
public class MinistriesTask extends AsyncTask<Object, Void, String>
{
    private final String TAG = this.getClass().getSimpleName();

    private MinistriesTaskHandler taskHandler;
    private JSONArray jsonArray;
    private String reason;

    public static interface MinistriesTaskHandler
    {
        void taskComplete(JSONArray jsonArray);
        void taskFailed(String reason);
    }

    public MinistriesTask(MinistriesTaskHandler taskHandler)
    {
        this.taskHandler = taskHandler;
    }

    @Override
    protected String doInBackground(Object... params)
    {
        String urlString = params[0].toString();
        Log.i(TAG, urlString);

        try
        {
            URL url = new URL(urlString);

            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setReadTimeout(1000);
            urlConnection.setConnectTimeout(10000);
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoInput(true);

            urlConnection.connect();
            int statusCode = urlConnection.getResponseCode();

            if (statusCode == HttpStatus.SC_OK)
            {
                InputStream inputStream = urlConnection.getInputStream();

                if (inputStream != null)
                {
                    String jsonAsString = JsonStringReader.readFully(inputStream, "UTF-8");
                    Log.i(TAG, jsonAsString);

                    // A successful response will return an array, an error response will return an object
                    if(jsonAsString.substring(0,1).equals("["))
                    {
                        jsonArray = new JSONArray(jsonAsString);
                    }
                    else
                    {
                        JSONObject jsonObject = new JSONObject(jsonAsString);
                        reason = jsonObject.optString("reason");
                    }
                }
            }
            else
            {
                reason = "Status Code: " + statusCode + " returned";
                Log.e(TAG, "Status Code: " + statusCode);
            }
        }
        catch(Exception e)
        {
            //Do stuff
            Log.e(TAG, "Problem occurred while retrieving ministries: " + e.getMessage());
            reason = e.getMessage();
            return null;
        }

        return null;
    }

    @Override
    protected void onPreExecute()
    {
        //TODO: Make sure logged in
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String s)
    {
        super.onPostExecute(s);
        if(reason == null || reason.length() == 0)
        {
            taskHandler.taskComplete(jsonArray);
//            try
//            {
//                taskHandler.taskComplete(
//                    new JSONArray("[{\"ministry_id\": \"37e3bb68-da0b-11e3-9786-12725f8f377c\",\"name\": \"Addis Ababa Campus Team (ETH)\"}]")
//                );
//            }
//            catch(JSONException e)
//            {
//                Log.e(TAG, "Failed to create JSONObject: " + e.getMessage());
//            }
        }
        else
        {
            taskHandler.taskFailed(reason);
        }
    }
}
