package com.expidev.gcmapp.http;

import static com.expidev.gcmapp.BuildConfig.THEKEY_CLIENTID;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;

import com.expidev.gcmapp.BuildConfig;
import com.expidev.gcmapp.http.GmaApiClient.Session;
import com.expidev.gcmapp.json.MinistryJsonParser;
import com.expidev.gcmapp.model.Ministry;
import com.expidev.gcmapp.utils.JsonStringReader;

import org.apache.http.HttpStatus;
import org.ccci.gto.android.common.api.AbstractTheKeyApi;
import org.ccci.gto.android.common.api.ApiException;
import org.ccci.gto.android.common.api.ApiSocketException;
import org.ccci.gto.android.common.util.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

import me.thekey.android.TheKey;
import me.thekey.android.TheKeySocketException;
import me.thekey.android.lib.TheKeyImpl;

/**
 * Created by matthewfrederick on 1/23/15.
 */
public class GmaApiClient extends AbstractTheKeyApi<AbstractTheKeyApi.Request<Session>, Session> {
    private final String TAG = getClass().getSimpleName();

    private static final String MINISTRIES = "ministries";
    private static final String TOKEN = "token";
    private static final String TRAINING = "training";

    private static final String PREF_NAME = "gcm_prefs";

    private static final Object LOCK_INSTANCE = new Object();
    private static GmaApiClient INSTANCE;

    private SharedPreferences preferences;
    private SharedPreferences.Editor prefEditor;

    public GmaApiClient(final Context context)
    {
        super(context, TheKeyImpl.getInstance(context, THEKEY_CLIENTID), BuildConfig.GCM_BASE_URI, "gcm_api_sessions");

        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefEditor = preferences.edit();
    }

    public static GmaApiClient getInstance(final Context context) {
        synchronized (LOCK_INSTANCE) {
            if(INSTANCE == null) {
                INSTANCE = new GmaApiClient(context.getApplicationContext());
            }
        }

        return INSTANCE;
    }

    @Override
    protected Session loadSession(@NonNull final SharedPreferences prefs, @NonNull final Request request) {
        return new Session(prefs, request.guid);
    }

    @Nullable
    @Override
    protected Session establishSession(@NonNull final Request<Session> request) throws ApiException {
        HttpURLConnection conn = null;
        try {
            final Pair<HttpURLConnection, String> tokenPair = this.getTokenInternal(false);
            if (tokenPair != null) {
                conn = tokenPair.first;

                // extract cookies
                // XXX: this won't be needed once Jon removes the cookie requirement from the API
                final Set<String> cookies = new HashSet<>();
                for (final Map.Entry<String, List<String>> header : conn.getHeaderFields().entrySet()) {
                    final String key = header.getKey();
                    if ("Set-Cookie".equalsIgnoreCase(key) || "Set-Cookie2".equals(key)) {
                        for (final String value : header.getValue()) {
                            for (final HttpCookie cookie : HttpCookie.parse(value)) {
                                if (cookie != null) {
                                    cookies.add(cookie.toString());
                                }
                            }
                        }
                    }
                }

                // create session object
                final JSONObject json = new JSONObject(IOUtils.readString(conn.getInputStream()));
                return new Session(json.optString("session_ticket", null), cookies, tokenPair.second);
            }
        } catch (final IOException e) {
            throw new ApiSocketException(e);
        } catch (final JSONException e) {
            Log.i(TAG, "invalid json for getToken", e);
        } finally {
            IOUtils.closeQuietly(conn);
        }

        // unable to get a session
        return null;
    }

    @Override
    protected void onPrepareUri(@NonNull final Uri.Builder uri, @NonNull final Request<Session> request)
            throws ApiException {
        super.onPrepareUri(uri, request);

        // append the session_token when using the session
        if (request.useSession && request.session != null) {
            uri.appendQueryParameter("token", request.session.id);
        }
    }

    @Override
    protected void onPrepareRequest(@NonNull final HttpURLConnection conn, @NonNull final Request<Session> request)
    throws ApiException, IOException {
        super.onPrepareRequest(conn, request);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        // attach cookies when using the session
        if (request.useSession && request.session != null) {
            for (final String cookie : request.session.cookies) {
                conn.addRequestProperty("Cookie", cookie);
            }
        }
    }

    private String getService() {
        return mBaseUri.buildUpon().appendPath(TOKEN).toString();
    }

    @Nullable
    private Pair<HttpURLConnection, String> getTokenInternal(final boolean refresh) throws ApiException {
        HttpURLConnection conn = null;
        boolean successful = false;
        try {
            final TheKey.TicketAttributesPair ticket = mTheKey.getTicketAndAttributes(getService());

            // issue request only if we have a ticket
            if (ticket != null && ticket.attributes.getGuid() != null) {
                // build request
                final Request<Session> request = new Request<>(TOKEN);
                request.accept = Request.MediaType.APPLICATION_JSON;
                request.params.add(param("st", ticket.ticket));
                request.params.add(param("refresh", refresh));
                request.useSession = false;

                // send request (tickets are one time use only, so we can't retry)
                conn = this.sendRequest(request, 0);

                // parse valid responses
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    successful = true;
                    return Pair.create(conn, ticket.attributes.getGuid());
                }
            }
        } catch (final TheKeySocketException | IOException e) {
            throw new ApiSocketException(e);
        } finally {
            if (!successful) {
                IOUtils.closeQuietly(conn);
            }
        }

        // error retrieving token
        return null;
    }

    private HttpURLConnection prepareRequest(HttpURLConnection connection) {
        String cookie = preferences.getString("Cookie", "");

        if (!cookie.isEmpty())
        {
            Log.i(TAG, "Cookie added: " + cookie);
            connection.addRequestProperty("Cookie", cookie);
        }
        else
        {
            Log.w(TAG, "No Cookies found");
        }
        
        return connection;
    }

    private HttpURLConnection processResponse(HttpURLConnection connection) throws IOException {
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
            if (!stringBuilder.toString().isEmpty())
            {
                prefEditor.putString("Cookie", stringBuilder.toString());
                prefEditor.apply();
            }
            
        }
        return connection;
    }

    @Nullable
    public JSONObject authorizeUser()
    {
        HttpURLConnection conn = null;
        try
        {
            final Pair<HttpURLConnection, String> tokenPair = this.getTokenInternal(true);
            if (tokenPair != null) {
                conn = tokenPair.first;
                return new JSONObject(IOUtils.readString(conn.getInputStream()));
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(conn);
        }
        
        return null;
    }

    public List<Ministry> getAllMinistries(String sessionToken)
    {
        String reason;
        String urlString = BuildConfig.GCM_BASE_URI + MINISTRIES + "?token=" + sessionToken;

        try
        {
            String json = httpGet(new URL(urlString));

            if(json == null)
            {
                Log.e(TAG, "Failed to retrieve ministries, most likely cause is a bad session ticket");
                return null;
            }
            else
            {
                if(json.startsWith("["))
                {
                    JSONArray jsonArray = new JSONArray(json);
                    return MinistryJsonParser.parseMinistriesJson(jsonArray);
                }
                else
                {
                    JSONObject jsonObject = new JSONObject(json);
                    reason = jsonObject.optString("reason");
                    Log.e(TAG, reason);
                    return null;
                }
            }
        }
        catch(Exception e)
        {
            reason = e.getMessage();
            Log.e(TAG, "Problem occurred while retrieving ministries: " + reason);
            return null;
        }
    }

    public JSONArray searchTraining(String ministryId, String mcc, String sessionTicket)
    {
        try
        {
            String urlString = BuildConfig.GCM_BASE_URI + TRAINING +
                    "?token=" + sessionTicket + "&ministry_id=" + ministryId +
                    "&mcc=" + mcc;

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

    public JSONArray searchMeasurements(String ministryId, String mcc, String period, String sessionTicket)
    {
        try
        {
            String urlString = BuildConfig.GCM_BASE_URI + "measurements" +
                "?token=" + sessionTicket + "&ministry_id=" + ministryId + "&mcc=" + mcc;

            if(period != null)
            {
                urlString += "&period=" + period;
            }

            Log.i(TAG, "Url: " + urlString);

            return new JSONArray(httpGet(new URL(urlString)));
        }
        catch(Exception e)
        {
            Log.e(TAG, e.getMessage(), e);
        }

        return null;
    }

    public JSONObject getDetailsForMeasurement(
        String measurementId,
        String sessionTicket,
        String ministryId,
        String mcc,
        String period)
    {
        try
        {
            String urlString = BuildConfig.GCM_BASE_URI + "measurements/" + measurementId +
                "?token=" + sessionTicket +
                "&ministry_id=" + ministryId +
                "&mcc=" + mcc;

            if(period != null)
            {
                urlString += "&period=" + period;
            }

            Log.i(TAG, "Url: " + urlString);

            return new JSONObject(httpGet(new URL(urlString)));
        }
        catch(Exception e)
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

    protected static class Session extends AbstractTheKeyApi.Session {
        @NonNull
        final Set<String> cookies;

        Session(@Nullable final String id, @Nullable final Collection<String> cookies, @NonNull final String guid) {
            super(id, guid);
            this.cookies = Collections.unmodifiableSet(new HashSet<>(cookies));
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        Session(@NonNull final SharedPreferences prefs, @NonNull final String guid) {
            super(prefs, guid);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                this.cookies = Collections.unmodifiableSet(new HashSet<>(
                        prefs.getStringSet(this.getPrefAttrName("cookies"), Collections.<String>emptySet())));
            } else {
                // work around missing getStringSet
                final Set<String> cookies = new HashSet<>();
                try {
                    final JSONArray json =
                            new JSONArray(prefs.getString(this.getPrefAttrName("cookies"), null));
                    for (int i = 0; i < json.length(); i++) {
                        cookies.add(json.getString(i));
                    }
                } catch (final JSONException ignored) {
                }
                this.cookies = Collections.unmodifiableSet(cookies);
            }
        }

        @Override
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        protected void save(@NonNull final SharedPreferences.Editor prefs) {
            super.save(prefs);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                prefs.putStringSet(this.getPrefAttrName("cookies"), this.cookies);
            } else {
                // work around missing putStringSet
                prefs.putString(this.getPrefAttrName("cookies"), new JSONArray(this.cookies).toString());
            }
        }

        @Override
        protected void delete(@NonNull SharedPreferences.Editor prefs) {
            super.delete(prefs);
            prefs.remove(this.getPrefAttrName("cookies"));
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final Session that = (Session) o;
            return super.equals(o) && this.cookies.equals(that.cookies);
        }

        @Override
        protected boolean isValid() {
            return super.isValid() && this.cookies.size() > 0;
        }
    }
}
