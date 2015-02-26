package com.expidev.gcmapp.http;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.expidev.gcmapp.BuildConfig;
import com.expidev.gcmapp.http.GmaApiClient.Session;
import com.expidev.gcmapp.json.MeasurementsJsonParser;
import com.expidev.gcmapp.json.MinistryJsonParser;
import com.expidev.gcmapp.model.Assignment;
import com.expidev.gcmapp.model.Church;
import com.expidev.gcmapp.model.Ministry;
import com.expidev.gcmapp.model.Training;
import com.expidev.gcmapp.model.measurement.MeasurementDetails;
import com.expidev.gcmapp.service.MinistriesService;

import org.ccci.gto.android.common.api.AbstractApi.Request.MediaType;
import org.ccci.gto.android.common.api.AbstractApi.Request.Method;
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
import java.util.ArrayList;
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
    private static final String CHURCHES = "churches";
    private static final String MEASUREMENTS = "measurements";
    private static final String MINISTRIES = "ministries";
    private static final String TOKEN = "token";
    private static final String TRAINING = "training";

    private static final Object LOCK_INSTANCE = new Object();
    private static GmaApiClient INSTANCE;

    // XXX: temporary until mContext from AbstractApi is visible
    private final Context mContext;

    private GmaApiClient(final Context context) {
        super(context, TheKeyImpl.getInstance(context), BuildConfig.GCM_BASE_URI, "gcm_api_sessions");
        mContext = context;
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
            final String service = getService();
            if (service != null) {
                // issue request only if we get a ticket for the user making this request
                final TheKey.TicketAttributesPair ticket = mTheKey.getTicketAndAttributes(service);
                assert request.guid != null;
                if (ticket != null && request.guid.equals(ticket.attributes.getGuid())) {
                    // issue getToken request
                    conn = this.getToken(ticket.ticket, false);

                    // parse valid responses
                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        // extract cookies
                        // XXX: this won't be needed once Jon removes the cookie requirement from the API
                        final Set<String> cookies = this.extractCookies(conn);

                        // parse response JSON
                        final JSONObject json = new JSONObject(IOUtils.readString(conn.getInputStream()));

                        // save the returned associated ministries
                        // XXX: this isn't ideal and crosses logical components, but I can't think of a cleaner way to do it currently -DF
                        MinistriesService.saveAssociatedMinistriesFromServer(mContext, request.guid,
                                                                             json.optJSONArray("assignments"));

                        // create session object
                        return new Session(json.optString("session_ticket", null), cookies,
                                           ticket.attributes.getGuid());
                    } else {
                        // authentication with the ticket failed, let's clear the cached service in case that caused the issue
                        if (service.equals(getCachedService())) {
                            setCachedService(null);
                        }
                    }
                }
            }
        } catch (final TheKeySocketException | IOException e) {
            throw new ApiSocketException(e);
        } catch (final JSONException e) {
            Log.i(TAG, "invalid json for getToken", e);
        } finally {
            IOUtils.closeQuietly(conn);
        }

        // unable to get a session
        return null;
    }

    @NonNull
    private Set<String> extractCookies(@NonNull final HttpURLConnection conn) {
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
        return cookies;
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

    /* API methods */

    @NonNull
    private HttpURLConnection getToken(@NonNull final String ticket, final boolean refresh) throws ApiException {
        // build login request
        final Request<Session> login = new Request<>(TOKEN);
        login.accept = MediaType.APPLICATION_JSON;
        login.params.add(param("st", ticket));
        login.params.add(param("refresh", refresh));
        login.useSession = false;

        // send request (tickets are one time use only, so we can't retry)
        return this.sendRequest(login, 0);
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

    /* BEGIN church methods */

    @Nullable
    public List<Church> getChurches(@NonNull final String ministryId) throws ApiException {
        // build request
        final Request<Session> request = new Request<>(CHURCHES);
        request.accept = MediaType.APPLICATION_JSON;
        request.params.add(param("ministry_id", ministryId));

        // process request
        HttpURLConnection conn = null;
        try {
            conn = this.sendRequest(request);

            // is this a successful response?
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return Church.listFromJson(new JSONArray(IOUtils.readString(conn.getInputStream())));
            }
        } catch (final JSONException e) {
            Log.e(TAG, "error parsing getChurches response", e);
        } catch (final IOException e) {
            throw new ApiSocketException(e);
        } finally {
            IOUtils.closeQuietly(conn);
        }

        return null;
    }

    public boolean createChurch(@NonNull final Church church) throws ApiException, JSONException {
        return this.createChurch(church.toJson());
    }

    public boolean createChurch(@NonNull final JSONObject church) throws ApiException {
        // build request
        final Request<Session> request = new Request<>(CHURCHES);
        request.method = Method.POST;
        request.setContent(church);

        // process request
        HttpURLConnection conn = null;
        try {
            conn = this.sendRequest(request);

            // is this a successful response?
            return conn.getResponseCode() == HttpURLConnection.HTTP_CREATED;
        } catch (final IOException e) {
            throw new ApiSocketException(e);
        } finally {
            IOUtils.closeQuietly(conn);
        }
    }

    public boolean updateChurch(@NonNull final Church church) throws ApiException, JSONException {
        return this.updateChurch(church.getId(), church.toJson());
    }

    public boolean updateChurch(final long id, @NonNull final JSONObject church) throws ApiException {
        // short-circuit if we are trying to update an invalid church
        if (id == Church.INVALID_ID) {
            return false;
        }

        // build request
        final Request<Session> request = new Request<>(CHURCHES + "/" + id);
        request.method = Method.PUT;
        request.setContent(church);

        // process request
        HttpURLConnection conn = null;
        try {
            conn = this.sendRequest(request);

            // is this a successful response?
            return conn.getResponseCode() == HttpURLConnection.HTTP_CREATED;
        } catch (final IOException e) {
            throw new ApiSocketException(e);
        } finally {
            IOUtils.closeQuietly(conn);
        }
    }

    /* END church methods */

    @Nullable
    public JSONArray searchMeasurements(@NonNull final String ministryId, @NonNull final String mcc,
                                        @Nullable final String period) throws ApiException {
        // build request
        final Request<Session> request = new Request<>(MEASUREMENTS);
        request.params.add(param("ministry_id", ministryId));
        request.params.add(param("mcc", mcc.toLowerCase()));
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
        request.params.add(param("mcc", mcc.toLowerCase()));
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

    public boolean updateMeasurementDetails(List<MeasurementDetails> measurementDetailsList, String assignmentId)
        throws JSONException, ApiException
    {
        List<JSONObject> data = new ArrayList<>();
        for(MeasurementDetails measurementDetails : measurementDetailsList)
        {
            // Can be positive or negative
            if(measurementDetails.getLocalValue() != 0)
            {
                data.add(MeasurementsJsonParser.createJsonForMeasurementDetails(measurementDetails, "local", assignmentId));
            }
            if(measurementDetails.getPersonalValue() != 0)
            {
                data.add(MeasurementsJsonParser.createJsonForMeasurementDetails(measurementDetails, "personal", assignmentId));
            }
        }

        return updateMeasurementDetails(MeasurementsJsonParser.createPostJsonForMeasurementDetails(data));
    }

    public boolean updateMeasurementDetails(JSONArray data) throws ApiException
    {
        // build request
        final Request<Session> request = new Request<>(MEASUREMENTS);
        request.method = Method.POST;
        request.setContent(data);

        // process request
        HttpURLConnection conn = null;
        try
        {
            conn = this.sendRequest(request);
            Log.i(TAG, "Response Code: " + conn.getResponseCode());
            Log.i(TAG, "Data POSTed: " + data.toString());

            // is this a successful response?
            return conn.getResponseCode() == HttpURLConnection.HTTP_CREATED;
        }
        catch (final IOException e)
        {
            throw new ApiSocketException(e);
        }
        finally
        {
            IOUtils.closeQuietly(conn);
        }
    }

    @Nullable
    public List<Assignment> getAssignments() throws ApiException {
        return this.getAssignments(false);
    }

    @Nullable
    public List<Assignment> getAssignments(final boolean refresh) throws ApiException {
        // build request
        final Request<Session> request = new Request<>(ASSIGNMENTS);
        request.params.add(param("refresh", refresh));

        // process request
        HttpURLConnection conn = null;
        try {
            conn = this.sendRequest(request);

            // is this a successful response?
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return Assignment.listFromJson(new JSONArray(IOUtils.readString(conn.getInputStream())));
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
    public Assignment createAssignment(@NonNull final String userEmail, @NonNull final String ministryId,
                                       @NonNull final Assignment.Role role) throws ApiException {
        // build request
        final Request<Session> request = new Request<>(ASSIGNMENTS);
        request.method = Method.POST;

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
                return Assignment.fromJson(new JSONObject(IOUtils.readString(conn.getInputStream())));
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
    
    public boolean updateTraining(final long id, @NonNull final JSONObject training) throws ApiException
    {
        if (id == Training.INVALID_ID) return false;
        
        final Request<Session> request = new Request<>(TRAINING + "/" + id);
        request.method = Method.PUT;
        request.setContent(training);

        HttpURLConnection connn = null;
        try
        {
            connn = this.sendRequest(request);
            return connn.getResponseCode() == HttpURLConnection.HTTP_CREATED;
        } catch (final IOException e)
        {
            throw new ApiSocketException(e);
        }
        finally
        {
            IOUtils.closeQuietly(connn);
        }
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
