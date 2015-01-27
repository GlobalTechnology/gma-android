package com.expidev.gcmapp.http;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import me.thekey.android.TheKey;
import me.thekey.android.lib.TheKeyImpl;

import static com.expidev.gcmapp.BuildConfig.THEKEY_CLIENTID;

/**
 * Created by matthewfrederick on 1/23/15.
 */
public class GmaApiClient
{
    private final String TAG = getClass().getSimpleName();

    private static final String BASE_URL_STAGE = "https://stage.sbr.global-registry.org/api";
    private static final String BASE_URL_PROD = "https://sbr.global-registry.org/api";
    private static final String MEASUREMENTS = "/measurements";
    private static final String TOKEN = "/token";
    private static final String TRAINING = "/training";

    private final String PREF_NAME = "gcm_prefs";

    private final TheKey theKey;

    private String ticket;
    private Context context;
    private LocalBroadcastManager broadcastManager;
    
    private SharedPreferences preferences;
    private SharedPreferences.Editor prefEditor;

    public GmaApiClient(final Context context)
    {
        theKey = TheKeyImpl.getInstance(context, THEKEY_CLIENTID);
        this.context = context;
        broadcastManager = LocalBroadcastManager.getInstance(context);

        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefEditor = preferences.edit();
    }

    private HttpsURLConnection prepareRequest(HttpsURLConnection connection)
    {
        String cookie = preferences.getString("Cookie", "");
        
        if (!cookie.isEmpty())
        {
            connection.addRequestProperty("Cookie", cookie.toString());
        }
        
        return connection;
    }

    private HttpsURLConnection processResponse(HttpsURLConnection connection) throws IOException
    {
        if (connection.getHeaderFields() != null)
        {
            String headerName = null;
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 1; (headerName = connection.getHeaderFieldKey(i)) != null; i++)
            {
                if (headerName.equals("Set-Cookie"))
                {
                    String cookie = connection.getHeaderField(i);
                    cookie = cookie.split("\\;")[0] + "; ";
                    stringBuilder.append(cookie);
                }
            }
            Log.i(TAG, "Full Cookie: " + stringBuilder.toString());

            // cookie store is not retrieving cookie so it will be saved to preferences
            prefEditor.putString("Cookie", stringBuilder.toString());
            prefEditor.commit();
            
        }
        return connection;
    }

    public JSONObject authorizeUser()
    {
        try
        {
            ticket = theKey.getTicket(BASE_URL_STAGE + MEASUREMENTS + TOKEN);
            Log.i(TAG, "Ticket: " + ticket);

            if (ticket == null) return null;

            String urlString = BASE_URL_STAGE + MEASUREMENTS + TOKEN + "?st=" + ticket + "&refresh=true";
            Log.i(TAG, "URL: " + urlString);

            URL url = new URL(urlString);

            return new JSONObject(httpGet(url));
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage(), e);
        }

        return null;
    }

    public JSONArray searchTraining(String ministryId, String sessionTicket)
    {
        try
        {
            String urlString = BASE_URL_STAGE + MEASUREMENTS +TRAINING +
                    "?token=" + sessionTicket + "&ministry_id=" + ministryId +
                    "&show_all=false&show_tree=false&mcc=slm";

            Log.i(TAG, "Url: " + urlString);

            URL url = new URL(urlString);

            return new JSONArray(httpGet(url));
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage(), e);
        }

        return null;
    }

    private String httpGet(URL url) throws IOException, JSONException, URISyntaxException
    {
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        prepareRequest(connection);

        connection.setReadTimeout(10000);
        connection.setConnectTimeout(10000);

        connection.connect();
        processResponse(connection);

        if (connection.getResponseCode() == HttpStatus.SC_OK)
        {
            InputStream inputStream = connection.getInputStream();

            if (inputStream != null)
            {
                String jsonAsString = readFully(inputStream, "UTF-8");
                Log.i(TAG, jsonAsString);

                // instead of returning a JSONObject, a string will be returned. This is
                // because some endpoints return an object and some return an array.
                return jsonAsString;
            }
        }
        else
        {
            Log.d(TAG, "Status: " + connection.getResponseCode());
        }

        return null;
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
