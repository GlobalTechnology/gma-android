package com.expidev.gcmapp.http;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.expidev.gcmapp.json.MinistryJsonParser;
import com.expidev.gcmapp.model.Ministry;
import com.expidev.gcmapp.utils.JsonStringReader;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
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
            connection.addRequestProperty("Cookie", cookie);
        }
        
        return connection;
    }

    private HttpsURLConnection processResponse(HttpsURLConnection connection) throws IOException
    {
        if (connection.getHeaderFields() != null)
        {
            String headerName;
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

    public List<Ministry> getAllMinistries(String sessionToken)
    {
        String reason;
        String urlString = BASE_URL_STAGE + MEASUREMENTS + MINISTRIES + "?token=" + sessionToken;

        try
        {
            String json = httpGet(new URL(urlString));

            if(json == null)
            {
                Log.e(TAG, "Failed to retrieve ministries, most likely cause is a bad session ticket");
                return dummyMinistryList();
            }
            else
            {
                JSONObject jsonObject = new JSONObject(json);
                reason = jsonObject.optString("reason");

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

    public JSONArray searchTraining(String ministryId, String sessionTicket)
    {
        try
        {
            String urlString = BASE_URL_STAGE + MEASUREMENTS +TRAINING +
                    "?token=" + sessionTicket + "&ministry_id=" + ministryId +
                    "&mcc=slm";

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
                String jsonAsString = JsonStringReader.readFully(inputStream, "UTF-8");
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
}
