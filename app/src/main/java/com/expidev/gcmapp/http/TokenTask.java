package com.expidev.gcmapp.http;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

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
        void taskFailed(String status);
    }
    
    public TokenTask(TokenTaskHandler listener)
    {
        taskHandler = listener;    
    }
    
    @Override
    protected String doInBackground(Object... params)
    {
        String urlString = params[0].toString();
        String sessionTicket = params[1].toString();

        String fullUrl = urlString + "/token?st=" + sessionTicket + "&refresh=true";
        Log.i(TAG, fullUrl);

        try
        {
            URL url = new URL(fullUrl);

            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(10000);
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoInput(true);

            urlConnection.connect();
            statusCode = urlConnection.getResponseCode();

            InputStream inputStream = urlConnection.getInputStream();

            if (inputStream != null)
            {
                String jsonAsString = readFully(inputStream, "UTF-8");
                Log.i(TAG, jsonAsString);
                jsonObject = new JSONObject(jsonAsString);
                status = jsonObject.getString("status");
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage(), e);
            return null;
        }

        return null;
    }

    @Override
    protected void onPostExecute(String s)
    {
        super.onPostExecute(s);

        if ("success".equalsIgnoreCase(status)) taskHandler.taskComplete(jsonObject);
        else taskHandler.taskFailed(status);
    }
    
    private String readFully(InputStream inputStream, String encoding) throws IOException
    {
        return new String(readFully(inputStream), encoding);
    }
    
    private byte[] readFully(InputStream inputStream) throws IOException
    {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length = 0;
        while ((length = inputStream.read(buffer)) != -1)
        {
            byteArrayOutputStream.write(buffer, 0, length);
        }
        return byteArrayOutputStream.toByteArray();
    }
}
