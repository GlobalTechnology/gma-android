package com.expidev.gcmapp.http;

import static com.expidev.gcmapp.BuildConfig.THEKEY_CLIENTID;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.expidev.gcmapp.BuildConfig;
import com.expidev.gcmapp.http.GmaApiClient.Session;
import com.expidev.gcmapp.json.MinistryJsonParser;
import com.expidev.gcmapp.model.Assignment;
import com.expidev.gcmapp.model.Ministry;

import org.ccci.gto.android.common.api.AbstractApi;
import org.ccci.gto.android.common.api.AbstractApi.Request.MediaType;
import org.ccci.gto.android.common.api.AbstractTheKeyApi;
import org.ccci.gto.android.common.api.ApiException;
import org.ccci.gto.android.common.api.ApiSocketException;
import org.ccci.gto.android.common.util.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.thekey.android.TheKey;
import me.thekey.android.TheKeySocketException;
import me.thekey.android.lib.TheKeyImpl;

/**
 * Created by matthewfrederick on 1/23/15.
 */
public final class GmaApiClient extends AbstractTheKeyApi<AbstractTheKeyApi.Request<Session>, Session> {
    private final String TAG = getClass().getSimpleName();

    private static final String ASSIGNMENTS = "assignments";
    private static final String MEASUREMENTS = "measurements";
    private static final String MINISTRIES = "ministries";
    private static final String TOKEN = "token";
    private static final String TRAINING = "training";

    private static final Object LOCK_INSTANCE = new Object();
    private static GmaApiClient INSTANCE;

    private GmaApiClient(final Context context) {
        super(context, TheKeyImpl.getInstance(context, THEKEY_CLIENTID), BuildConfig.GCM_BASE_URI, "gcm_api_sessions");
    }

    public static GmaApiClient getInstance(final Context context) {
        synchronized (LOCK_INSTANCE) {
            if(INSTANCE == null) {
                INSTANCE = new GmaApiClient(context.getApplicationContext());
            }
        }

        return INSTANCE;
    }

    @Nullable
    @Override
    protected String getDefaultService() {
        return mBaseUri.buildUpon().appendPath(TOKEN).toString();
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
        // XXX: this should go away once we remove the cookie requirement on the API
        if (request.useSession && request.session != null) {
            conn.addRequestProperty("Cookie", TextUtils.join("; ", request.session.cookies));
        }
    }

    @Nullable
    private Pair<HttpURLConnection, String> getTokenInternal(final boolean refresh) throws ApiException {
        HttpURLConnection conn = null;
        boolean successful = false;
        try {
            final String service = getService();
            final TheKey.TicketAttributesPair ticket = mTheKey.getTicketAndAttributes(service);

            // issue request only if we have a ticket
            if (ticket != null && ticket.attributes.getGuid() != null) {
                // build request
                final Request<Session> request = new Request<>(TOKEN);
                request.accept = MediaType.APPLICATION_JSON;
                request.params.add(param("st", ticket.ticket));
                request.params.add(param("refresh", refresh));
                request.useSession = false;

                // send request (tickets are one time use only, so we can't retry)
                conn = this.sendRequest(request, 0);

                // parse valid responses
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    successful = true;
                    return Pair.create(conn, ticket.attributes.getGuid());
                } else {
                    // authentication with the ticket failed, let's clear the cached service in case that caused the issue
                    if (service != null && service.equals(getCachedService())) {
                        setCachedService(null);
                    }
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

    @Nullable
    public List<Ministry> getAllMinistries() throws ApiException {
        return this.getAllMinistries(false);
    }

    @Nullable
    public List<Ministry> getAllMinistries(final boolean refresh) throws ApiException {
        // build request
        final Request<Session> request = new Request<>(MINISTRIES);
        request.params.add(param("refresh", refresh));

        // process request
        HttpURLConnection conn = null;
        try
        {
            conn = this.sendRequest(request);

            Log.i(TAG, "response code: " + Integer.toString(conn.getResponseCode()));

            // is this a successful response?
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                final JSONArray json = new JSONArray(IOUtils.readString(conn.getInputStream()));
                return MinistryJsonParser.parseMinistriesJson(json);
            }
        } catch (final JSONException e) {
            Log.e(TAG, "error parsing getAllMinistries response", e);
        } catch (final IOException e) {
            throw new ApiSocketException(e);
        } finally {
            IOUtils.closeQuietly(conn);
        }

        return null;
    }

    @Nullable
    public JSONArray searchMeasurements(@NonNull final String ministryId, @NonNull final String mcc,
                                        @Nullable final String period) throws ApiException {
        // build request
        final Request<Session> request = new Request<>(MEASUREMENTS);
        request.params.add(param("ministry_id", ministryId));
        request.params.add(param("mcc", mcc));
        if (period != null) {
            request.params.add(param("period", period));
        }

        // process request
        HttpURLConnection conn = null;
        try {
            conn = this.sendRequest(request);

            // is this a successful response?
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return new JSONArray(IOUtils.readString(conn.getInputStream()));
            }
        } catch (final JSONException e) {
            Log.e(TAG, "error parsing getAllMinistries response", e);
        } catch (final IOException e) {
            throw new ApiSocketException(e);
        } finally {
            IOUtils.closeQuietly(conn);
        }

        return null;
    }

    @Nullable
    public JSONObject getDetailsForMeasurement(@NonNull final String measurementId, @NonNull final String ministryId,
                                               @NonNull final String mcc, @Nullable final String period)
            throws ApiException {
        // build request
        final Request<Session> request = new Request<>(MEASUREMENTS + "/" + measurementId);
        request.params.add(param("ministry_id", ministryId));
        request.params.add(param("mcc", mcc));
        if (period != null) {
            request.params.add(param("period", period));
        }

        // process request
        HttpURLConnection conn = null;
        try {
            conn = this.sendRequest(request);

            // is this a successful response?
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return new JSONObject(IOUtils.readString(conn.getInputStream()));
            }
        } catch (final JSONException e) {
            Log.e(TAG, "error parsing getAllMinistries response", e);
        } catch (final IOException e) {
            throw new ApiSocketException(e);
        } finally {
            IOUtils.closeQuietly(conn);
        }

        return null;
    }

    @Nullable
    public JSONArray getAssignments() throws ApiException {
        return this.getAssignments(false);
    }

    @Nullable
    public JSONArray getAssignments(final boolean refresh) throws ApiException {
        // build request
        final Request<Session> request = new Request<>(ASSIGNMENTS);
        request.params.add(param("refresh", refresh));

        // process request
        HttpURLConnection conn = null;
        try {
            conn = this.sendRequest(request);

            // is this a successful response?
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return new JSONArray(IOUtils.readString(conn.getInputStream()));
            }
        } catch (final JSONException e) {
            Log.e(TAG, "error parsing getAllMinistries response", e);
        } catch (final IOException e) {
            throw new ApiSocketException(e);
        } finally {
            IOUtils.closeQuietly(conn);
        }

        return null;
    }

    @Nullable
    public JSONObject createAssignment(@NonNull final String userEmail, @NonNull final String ministryId,
                                       @NonNull final Assignment.Role role) throws ApiException {
        // build request
        final Request<Session> request = new Request<>(ASSIGNMENTS);
        request.method = AbstractApi.Request.Method.POST;

        // generate POST data
        final Map<String, Object> data = new HashMap<>();
        data.put("username", userEmail);
        data.put("ministry_id", ministryId);
        data.put("team_role", role.raw);
        request.setContent(new JSONObject(data));

        // issue request and process response
        HttpURLConnection conn = null;
        try {
            conn = this.sendRequest(request);

            // if successful return parsed response
            if (conn.getResponseCode() == HttpURLConnection.HTTP_CREATED) {
                return new JSONObject(IOUtils.readString(conn.getInputStream()));
            }
        } catch (final IOException e) {
            throw new ApiSocketException(e);
        } catch (final JSONException e) {
            Log.i(TAG, "invalid response json for createAssignment", e);
        } finally {
            IOUtils.closeQuietly(conn);
        }

        return null;
    }

    @Nullable
    public JSONArray searchTraining(@NonNull final String ministryId, @NonNull final String mcc) throws ApiException {
        // build request
        final Request<Session> request = new Request<>(TRAINING);
        request.params.add(param("ministry_id", ministryId));
        request.params.add(param("mcc", mcc));

        // process request
        HttpURLConnection conn = null;
        try
        {
            conn = this.sendRequest(request);

            // is this a successful response?
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return new JSONArray(IOUtils.readString(conn.getInputStream()));
            }
        } catch (final JSONException e) {
            Log.e(TAG, "error parsing searchTraining response", e);
        } catch (final IOException e) {
            throw new ApiSocketException(e);
        } finally {
            IOUtils.closeQuietly(conn);
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
