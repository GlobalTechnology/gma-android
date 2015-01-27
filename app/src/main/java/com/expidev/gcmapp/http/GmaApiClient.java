package com.expidev.gcmapp.http;

import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.expidev.gcmapp.json.MinistryJsonParser;
import com.expidev.gcmapp.model.Ministry;
import com.expidev.gcmapp.service.AssociatedMinistriesService;
import com.expidev.gcmapp.utils.JsonStringReader;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
    private static final String MINISTRIES = "/ministries";
    private static final String TOKEN = "/token";
    
    private final TheKey theKey;
    
    private String ticket;
    private Context context;
    private LocalBroadcastManager broadcastManager;
    
    public GmaApiClient(final Context context)
    {
        theKey = TheKeyImpl.getInstance(context, THEKEY_CLIENTID);
        this.context = context;
        broadcastManager = LocalBroadcastManager.getInstance(context);
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

            return httpGet(url);  
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage(), e);
        }
        
        return null;
    }

    public List<Ministry> getAllMinistries(String sessionToken)
    {
        String reason;
        String urlString = BASE_URL_STAGE + MEASUREMENTS + MINISTRIES + "?token=" + sessionToken;

        try
        {
            JSONObject jsonObject = httpGet(new URL(urlString));

            if(jsonObject == null)
            {
                reason = "Failed to retrieve ministries, most likely cause is a bad session ticket";
            }
            else
            {
                reason = jsonObject.optString("reason");
            }

            if(reason != null)
            {
                Log.e(TAG, reason);
                return dummyMinistryList();
            }
            else
            {
                JSONArray names = new JSONArray();
                names.put("ministry_id");
                names.put("name");

                JSONArray jsonArray = jsonObject.toJSONArray(names);
                return MinistryJsonParser.parseMinistriesJson(jsonArray);
            }
        }
        catch(Exception e)
        {
            reason = e.getMessage();
            Log.e(TAG, "Problem occurred while retrieving ministries: " + reason);
            return dummyMinistryList();
        }
    }

    private ArrayList<Ministry> dummyMinistryList()
    {
        ArrayList<Ministry> dummyList = new ArrayList<Ministry>();

        Ministry dummy1 = new Ministry();
        dummy1.setMinistryId("37e3bb68-da0b-11e3-9786-12725f8f377c");
        dummy1.setName("Addis Ababa Campus Team (ETH)");

        dummyList.add(dummy1);

        return dummyList;
    }
    
    private JSONObject httpGet(URL url) throws IOException, JSONException
    {
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

        connection.setReadTimeout(10000);
        connection.setConnectTimeout(10000);
        connection.setRequestMethod("GET");
        connection.setDoInput(true);

        connection.connect();

        if (connection.getResponseCode() == HttpStatus.SC_OK)
        {
            InputStream inputStream = connection.getInputStream();

            if (inputStream != null)
            {
                String jsonAsString = readFully(inputStream, "UTF-8");
                Log.i(TAG, jsonAsString);

                return new JSONObject(jsonAsString);
            }
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
