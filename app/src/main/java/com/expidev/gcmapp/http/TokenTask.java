package com.expidev.gcmapp.http;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.util.Objects;
import java.util.UUID;

/**
 * Created by matthewfrederick on 1/13/15.
 */
public class TokenTask extends AsyncTask<Object, Void, String>
{
    private final String TAG = this.getClass().getSimpleName();
    
    private TokenTaskHandler taskHandler;
    private int statusCode = 0;
    private JSONObject jsonObject;
    private String status;
    
    public static interface TokenTaskHandler
    {
        void taskComplete(JSONObject object);
        void taskFailed();
    }
    
    public TokenTask(TokenTaskHandler listener)
    {
        taskHandler = listener;    
    }
    
    @Override
    protected String doInBackground(Object... params)
    {
        String url = params[0].toString();
        String guid = params[1].toString();
        
        String fullUrl = url + "/token?st=" + guid + "&refresh=true";

        try
        {
            HttpGet request = new HttpGet(fullUrl);
            Log.i(TAG, fullUrl);

            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
            HttpConnectionParams.setSoTimeout(httpParams, 10000);

            HttpClient httpClient = new DefaultHttpClient(httpParams);
            HttpResponse response = httpClient.execute(request);
            statusCode = response.getStatusLine().getStatusCode();

            HttpEntity entity = response.getEntity();
            if (entity != null)
            {
                String jsonString = EntityUtils.toString(entity);
                jsonObject = new JSONObject(jsonString);
                status = jsonObject.getString("status");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

        return null;
    }

    @Override
    protected void onPostExecute(String s)
    {
        super.onPostExecute(s);
        
        if (statusCode == HttpStatus.SC_OK && status.equalsIgnoreCase("success")) taskHandler.taskComplete(jsonObject);
        else taskHandler.taskFailed();
    }
}
