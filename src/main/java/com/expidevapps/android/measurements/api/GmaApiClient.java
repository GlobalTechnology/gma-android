package com.expidevapps.android.measurements.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.expidevapps.android.measurements.BuildConfig;
import com.expidevapps.android.measurements.api.GmaApiClient.Session;
import com.expidevapps.android.measurements.model.Assignment;
import com.expidevapps.android.measurements.model.Church;
import com.expidevapps.android.measurements.model.Measurement;
import com.expidevapps.android.measurements.model.MeasurementDetails;
import com.expidevapps.android.measurements.model.MeasurementType;
import com.expidevapps.android.measurements.model.MeasurementValue;
import com.expidevapps.android.measurements.model.Ministry;
import com.expidevapps.android.measurements.model.Training;
import com.expidevapps.android.measurements.model.Training.Completion;
import com.expidevapps.android.measurements.sync.GmaSyncService;

import org.ccci.gto.android.common.api.AbstractApi.Request.MediaType;
import org.ccci.gto.android.common.api.AbstractApi.Request.Method;
import org.ccci.gto.android.common.api.AbstractTheKeyApi;
import org.ccci.gto.android.common.api.ApiException;
import org.ccci.gto.android.common.api.ApiSocketException;
import org.ccci.gto.android.common.util.IOUtils;
import org.ccci.gto.android.common.util.SharedPreferencesUtils;
import org.joda.time.YearMonth;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

import me.thekey.android.TheKeySocketException;
import me.thekey.android.lib.TheKeyImpl;

import static com.expidevapps.android.measurements.Constants.MEASUREMENTS_SOURCE;

import static com.expidevapps.android.measurements.Constants.PREFS_USER;
import static com.expidevapps.android.measurements.Constants.PREF_PERSON_ID;

public final class GmaApiClient extends AbstractTheKeyApi<AbstractTheKeyApi.Request<Session>, Session> {
    private static final Logger LOG = LoggerFactory.getLogger(GmaApiClient.class);
    private final String TAG = getClass().getSimpleName();

    public static final int V2 = 2;
    public static final int V4 = 4;
    public static final int V5 = 5;

    private static final String PREF_COOKIES = "cookies";

    private static final String ASSIGNMENTS = "assignments";
    private static final String CHURCHES = "churches";
    private static final String MEASUREMENTS = "measurements";
    private static final String MEASUREMENT_TYPES = "measurement_types";
    private static final String MINISTRIES = "ministries";
    private static final String TOKEN = "token";
    private static final String TRAINING = "training";
    private static final String TRAINING_COMPLETION = "training_completion";
    private static final String USER_PREFERENCES = "user_preferences";

    private static final Map<String, GmaApiClient> INSTANCES = new HashMap<>();

    private GmaApiClient(final Context context, final String guid) {
        super(context, TheKeyImpl.getInstance(context),
              BuildConfig.GMA_API_BASE_URI + "v" + BuildConfig.GMA_API_VERSION + "/", "gma_api_sessions", guid);
    }

    @NonNull
    public static GmaApiClient getInstance(@NonNull final Context context, @NonNull final String guid) {
        synchronized (INSTANCES) {
            if (!INSTANCES.containsKey(guid)) {
                INSTANCES.put(guid, new GmaApiClient(context.getApplicationContext(), guid));
            }

            return INSTANCES.get(guid);
        }
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
            if (request.guid != null) {
                final String service = getService();
                if (service != null) {
                    // get a ticket for this user
                    final String ticket = mTheKey.getTicket(request.guid, service);
                    if (ticket != null) {
                        // issue getToken request
                        conn = this.getToken(ticket, false);

                        // parse valid responses
                        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            // extract cookies
                            // XXX: this won't be needed once Jon removes the cookie requirement from the API
                            final Set<String> cookies = this.extractCookies(conn);

                            // parse response JSON
                            final JSONObject json = new JSONObject(IOUtils.readString(conn.getInputStream()));

                            // save the returned associated ministries
                            // XXX: this isn't ideal and crosses logical components, but I can't think of a cleaner way to do it currently -DF
                            GmaSyncService.saveAssignments(mContext, request.guid, json.optJSONArray("assignments"));

                            saveUser(json.optJSONObject("user"));

                            // create session object
                            return new Session(json.optString("session_ticket", null), cookies, request.guid);
                        } else {
                            // authentication with the ticket failed, let's clear the cached service in case that caused the issue
                            if (service.equals(getCachedService())) {
                                setCachedService(null);
                            }
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
    protected void onPrepareRequest(@NonNull final HttpURLConnection conn, @NonNull final Request<Session> request)
    throws ApiException, IOException {
        super.onPrepareRequest(conn, request);

        // attach cookies when using the session
        // XXX: this should go away once we remove the cookie requirement on the API
        if (request.useSession && request.session != null) {
            conn.addRequestProperty("Cookie", TextUtils.join("; ", request.session.cookies));
        }
        //Add bearer token code to replace URI token id [MMAND-12]
        if(request.useSession && request.session != null) {
            conn.addRequestProperty("Authorization", "Bearer " + request.session.id);
        }
    }

    @Override
    protected void onCleanupRequest(@NonNull final Request<Session> request) {
        // we don't call the super method to prevent wiping the guid on a successful request
        // super.onCleanupRequest(request);
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

    /* BEGIN Ministry Endpoints */

    @Nullable
    public List<Ministry> getMinistries() throws ApiException {
        return this.getMinistries(false);
    }

    @Nullable
    public List<Ministry> getMinistries(final boolean refresh) throws ApiException {
        // build request
        final Request<Session> request = new Request<>(MINISTRIES);
        request.params.add(param("refresh", refresh));

        // process request
        HttpURLConnection conn = null;
        try
        {
            conn = this.sendRequest(request);

            // is this a successful response?
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return Ministry.listFromJson(new JSONArray(IOUtils.readString(conn.getInputStream())));
            }
        } catch (final JSONException e) {
            Log.e(TAG, "error parsing getMinistries response", e);
        } catch (final IOException e) {
            throw new ApiSocketException(e);
        } finally {
            IOUtils.closeQuietly(conn);
        }

        return null;
    }

    /* END Ministries Endpoints */

    /* BEGIN church methods */

    @Nullable
    public List<Church> getChurches(@NonNull final String ministryId) throws ApiException {
        // short-circuit if this is an invalid request
        if (ministryId.equals(Ministry.INVALID_ID)) {
            return null;
        }

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

    @Nullable
    public Church createChurch(@NonNull final Church church) throws ApiException, JSONException {
        return this.createChurch(church.toJson());
    }

    @Nullable
    public Church createChurch(@NonNull final JSONObject church) throws ApiException {
        // build request
        final Request<Session> request = new Request<>(CHURCHES);
        request.method = Method.POST;
        request.setContent(church);

        // process request
        HttpURLConnection conn = null;
        try {
            conn = this.sendRequest(request);

            // is this a successful response?
            if (conn.getResponseCode() == HttpURLConnection.HTTP_CREATED) {
                return Church.fromJson(new JSONObject(IOUtils.readString(conn.getInputStream())));
            }
        } catch (final JSONException e) {
            LOG.error("error parsing createChurch response", e);
        } catch (final IOException e) {
            throw new ApiSocketException(e);
        } finally {
            IOUtils.closeQuietly(conn);
        }

        return null;
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
            return conn.getResponseCode() == HttpURLConnection.HTTP_OK;
        } catch (final IOException e) {
            throw new ApiSocketException(e);
        } finally {
            IOUtils.closeQuietly(conn);
        }
    }

    /* END church methods */

    /* BEGIN measurements methods */

    @Nullable
    public List<MeasurementType> getMeasurementTypes() throws ApiException {
        // build & process request
        HttpURLConnection conn = null;
        try {
            conn = sendRequest(new Request<Session>(MEASUREMENT_TYPES));

            // is this a successful response?
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return MeasurementType.listFromJson(new JSONArray(IOUtils.readString(conn.getInputStream())));
            }
        } catch (final JSONException e) {
            Log.e(TAG, "error parsing getMeasurementTypes response", e);
        } catch (final IOException e) {
            throw new ApiSocketException(e);
        } finally {
            IOUtils.closeQuietly(conn);
        }

        return null;
    }

    @Nullable
    public List<Measurement> getMeasurements(@NonNull final String ministryId, @NonNull final Ministry.Mcc mcc,
                                             @NonNull final YearMonth period) throws ApiException {
        // short-circuit if we don't have a valid ministryId or mcc
        if(ministryId.equals(Ministry.INVALID_ID) || mcc == Ministry.Mcc.UNKNOWN) {
            return null;
        }
        assert mcc.raw != null : "only Mcc.UNKNOWN has a null raw value";

        // build request
        final Request<Session> request = new Request<>(MEASUREMENTS);
        request.params.add(param("source", MEASUREMENTS_SOURCE));
        request.params.add(param("ministry_id", ministryId));
        request.params.add(param("mcc", mcc.raw));
        request.params.add(param("period", period.toString()));

        // process request
        HttpURLConnection conn = null;
        try {
            conn = this.sendRequest(request);

            // is this a successful response?
            final String guid = getActiveGuid();
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK && guid != null) {
                return Measurement.listFromJson(
                        new JSONArray(IOUtils.readString(conn.getInputStream())), guid, ministryId, mcc, period);
            }
        } catch (final JSONException e) {
            Log.e(TAG, "error parsing getMeasurements response", e);
        } catch (final IOException e) {
            throw new ApiSocketException(e);
        } finally {
            IOUtils.closeQuietly(conn);
        }

        return null;
    }

    @Nullable
    public MeasurementDetails getMeasurementDetails(@NonNull final String ministryId, @NonNull final Ministry.Mcc mcc,
                                                    @NonNull final String permLink, @NonNull final YearMonth period)
            throws ApiException {
        // short-circuit if we don't have a valid ministryId or mcc
        if (ministryId.equals(Ministry.INVALID_ID) || mcc == Ministry.Mcc.UNKNOWN) {
            return null;
        }
        assert mcc.raw != null : "only Mcc.UNKNOWN has a null raw value";

        // build request
        final Request<Session> request = new Request<>(MEASUREMENTS + "/" + permLink);
        request.params.add(param("ministry_id", ministryId));
        request.params.add(param("mcc", mcc.raw));
        request.params.add(param("period", period.toString()));

        // process request
        HttpURLConnection conn = null;
        try {
            conn = this.sendRequest(request);

            // is this a successful response?
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                if (request.guid != null) {
                    final MeasurementDetails details =
                            new MeasurementDetails(request.guid, ministryId, mcc, permLink, period);
                    details.setJson(new JSONObject(IOUtils.readString(conn.getInputStream())),
                                    BuildConfig.GMA_API_VERSION);
                    return details;
                }
            }
        } catch (final JSONException e) {
            LOG.error("error parsing getMeasurementDetails JSON response", e);
        } catch (final IOException e) {
            throw new ApiSocketException(e);
        } finally {
            IOUtils.closeQuietly(conn);
        }

        return null;
    }

    public boolean updateMeasurements(@NonNull final MeasurementValue... measurements) throws ApiException {
        // short-circuit if we don't have any measurements to update
        if (measurements.length == 0) {
            return false;
        }

        // build request
        final Request<Session> request = new Request<>(MEASUREMENTS);
        request.method = Method.POST;
        try {
            final JSONArray json = new JSONArray();
            for (final MeasurementValue value : measurements) {
                json.put(value.toUpdateJson(MEASUREMENTS_SOURCE));
            }
            request.setContent(json);
        } catch (final JSONException e) {
            LOG.error("Error generating update JSON", e);
            return false;
        }

        // process request
        HttpURLConnection conn = null;
        try {
            conn = this.sendRequest(request);

            // is this a successful response?
            if (conn.getResponseCode() == HttpURLConnection.HTTP_CREATED) {
                return true;
            }
        } catch (final IOException e) {
            throw new ApiSocketException(e);
        } finally {
            IOUtils.closeQuietly(conn);
        }

        return false;
    }

    /* END measurement methods */

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
                assert request.guid != null : "request.guid should be non-null because the request was successful";
                return Assignment.listFromJson(new JSONArray(IOUtils.readString(conn.getInputStream())), request.guid);
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
    public Assignment createAssignment(@NonNull final String ministryId, @NonNull final Assignment.Role role,
                                       @NonNull final String guid) throws ApiException {
        // short-circuit if this is an invalid request
        if (ministryId.equals(Ministry.INVALID_ID) || role == Assignment.Role.UNKNOWN) {
            return null;
        }

        // build request
        final Request<Session> request = new Request<>(ASSIGNMENTS);
        request.method = Method.POST;

        // generate POST data
        final Map<String, Object> data = new HashMap<>();
        data.put("ministry_id", ministryId);
        data.put("team_role", role.raw);
        data.put("key_guid", guid);
        request.setContent(new JSONObject(data));

        // issue request and process response
        HttpURLConnection conn = null;
        try {
            conn = this.sendRequest(request);

            // if successful return parsed response
            if (conn.getResponseCode() == HttpURLConnection.HTTP_CREATED) {
                return Assignment.fromJson(new JSONObject(IOUtils.readString(conn.getInputStream())), guid);
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

    /* BEGIN Training endpoints */

    @Nullable
    public List<Training> getTrainings(@NonNull final String ministryId, @NonNull final Ministry.Mcc mcc)
            throws ApiException {
        return getTrainings(ministryId, mcc, true, false);
    }

    @Nullable
    public List<Training> getTrainings(@NonNull final String ministryId, @NonNull final Ministry.Mcc mcc,
                                       final boolean all, final boolean includeDescendents) throws ApiException {
        // short-circuit on invalid requests
        if(ministryId.equals(Ministry.INVALID_ID) || mcc == Ministry.Mcc.UNKNOWN) {
            return null;
        }
        assert mcc.raw != null : "Only Mcc.UNKNOWN should have a null raw value";

        // build request
        final Request<Session> request = new Request<>(TRAINING);
        request.params.add(param("ministry_id", ministryId));
        request.params.add(param("mcc", mcc.raw));
        request.params.add(param("show_all", all));
        request.params.add(param("show_tree", includeDescendents));

        // process request
        HttpURLConnection conn = null;
        try {
            conn = this.sendRequest(request);

            // is this a successful response?
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return Training.listFromJson(new JSONArray(IOUtils.readString(conn.getInputStream())));
            }
        } catch (final JSONException e) {
            Log.e(TAG, "error parsing getTrainings response", e);
        } catch (final IOException e) {
            throw new ApiSocketException(e);
        } finally {
            IOUtils.closeQuietly(conn);
        }

        return null;
    }

    @Nullable
    public Training createTraining(@NonNull final Training training) throws ApiException, JSONException {
        return this.createTraining(training.toJson());
    }

    @Nullable
    public Training createTraining(@NonNull final JSONObject training) throws ApiException {
        // build request
        final Request<Session> request = new Request<>(TRAINING);
        request.method = Method.POST;
        request.setContent(training);

        // process request
        HttpURLConnection conn = null;
        try {
            conn = this.sendRequest(request);

            // is this a successful response?
            if (conn.getResponseCode() == HttpURLConnection.HTTP_CREATED) {
                return Training.fromJson(new JSONObject(IOUtils.readString(conn.getInputStream())));
            }
        } catch (final JSONException e) {
            LOG.error("error parsing createTraining response", e);
        } catch (final IOException e) {
            throw new ApiSocketException(e);
        } finally {
            IOUtils.closeQuietly(conn);
        }

        return null;
    }

    public boolean updateTraining(final long id, @NonNull final JSONObject training) throws ApiException {
        if (id == Training.INVALID_ID) {
            return false;
        }

        final Request<Session> request = new Request<>(TRAINING + "/" + id);
        request.method = Method.PUT;
        request.setContent(training);

        HttpURLConnection conn = null;
        try {
            conn = this.sendRequest(request);
            return conn.getResponseCode() == HttpURLConnection.HTTP_CREATED;
        } catch (final IOException e) {
            throw new ApiSocketException(e);
        } finally {
            IOUtils.closeQuietly(conn);
        }
    }

    public boolean deleteTraining(final long id) throws ApiException {
        if (id == Training.INVALID_ID) {
            return false;
        }

        final Request<Session> request = new Request<>(TRAINING + "/" + id);
        request.method = Method.DELETE;

        HttpURLConnection conn = null;
        try {
            conn = this.sendRequest(request);
            return conn.getResponseCode() == HttpURLConnection.HTTP_NO_CONTENT;
        } catch (final IOException e) {
            throw new ApiSocketException(e);
        } finally {
            IOUtils.closeQuietly(conn);
        }
    }

    @Nullable
    public Completion createTrainingCompletion(final long trainingId, @NonNull final Completion completion)
            throws ApiException, JSONException {
        return this.createTrainingCompletion(trainingId, completion.toJson());
    }

    @Nullable
    public Completion createTrainingCompletion(final long trainingId, @NonNull final JSONObject completion)
            throws ApiException {
        // build request
        final Request<Session> request = new Request<>(TRAINING_COMPLETION);
        request.method = Method.POST;
        request.setContent(completion);

        // process request
        HttpURLConnection conn = null;
        try {
            conn = this.sendRequest(request);

            // is this a successful response?
            if (conn.getResponseCode() == HttpURLConnection.HTTP_CREATED) {
                return Completion.fromJson(trainingId, new JSONObject(IOUtils.readString(conn.getInputStream())));
            }
        } catch (final JSONException e) {
            LOG.error("error parsing createTraining response", e);
        } catch (final IOException e) {
            throw new ApiSocketException(e);
        } finally {
            IOUtils.closeQuietly(conn);
        }

        return null;
    }

    public boolean deleteTrainingCompletion(final long id) throws ApiException {
        if (id == Completion.INVALID_ID) {
            return false;
        }

        final Request<Session> request = new Request<>(TRAINING_COMPLETION + "/" + id);
        request.method = Method.DELETE;

        HttpURLConnection conn = null;
        try {
            conn = this.sendRequest(request);
            return conn.getResponseCode() == HttpURLConnection.HTTP_NO_CONTENT;
        } catch (final IOException e) {
            throw new ApiSocketException(e);
        } finally {
            IOUtils.closeQuietly(conn);
        }
    }

    @Nullable
    public Completion updateTrainingCompletion(final long trainingId, final long completionId,
                                               @NonNull final JSONObject completion) throws ApiException {
        // build request
        final Request<Session> request = new Request<>(TRAINING_COMPLETION + "/" + completionId);
        request.method = Method.PUT;
        request.setContent(completion);

        // process request
        HttpURLConnection conn = null;
        try {
            conn = this.sendRequest(request);

            // is this a successful response?
            if (conn.getResponseCode() == HttpURLConnection.HTTP_CREATED) {
                return Completion.fromJson(trainingId, new JSONObject(IOUtils.readString(conn.getInputStream())));
            }
        } catch (final JSONException e) {
            LOG.error("error parsing createTraining response", e);
        } catch (final IOException e) {
            throw new ApiSocketException(e);
        } finally {
            IOUtils.closeQuietly(conn);
        }

        return null;
    }

    public boolean updatePreference(final JSONObject preference) throws ApiException {

        final Request<Session> request = new Request<>(USER_PREFERENCES);
        request.method = Method.POST;
        request.setContent(preference);

        HttpURLConnection conn = null;
        try {
            conn = this.sendRequest(request);

            return conn.getResponseCode() == HttpURLConnection.HTTP_OK;
        }
        catch (final IOException e) {
            throw new ApiSocketException(e);
        } finally {
            IOUtils.closeQuietly(conn);
        }
    }

    /* END Training endpoints */

    private void saveUser(@Nullable final JSONObject user) {
        if(user != null) {
            String persionId = user.optString("person_id", null);
            SharedPreferences.Editor editor = mContext.getSharedPreferences(PREFS_USER, Context.MODE_PRIVATE).edit();
            editor.putString(PREF_PERSON_ID, persionId);
            editor.apply();
        }
    }

    public static String getUserId(@NonNull final Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_USER, Context.MODE_PRIVATE);
        String persionId = prefs.getString(PREF_PERSON_ID, null);
        return persionId;
    }

    protected static class Session extends AbstractTheKeyApi.Session {
        @NonNull
        final Set<String> cookies;

        Session(@Nullable final String id, @Nullable final Collection<String> cookies, @Nullable final String guid) {
            super(id, guid);
            this.cookies = Collections.unmodifiableSet(new HashSet<>(cookies));
        }

        Session(@NonNull final SharedPreferences prefs, @Nullable final String guid) {
            super(prefs, guid);
            this.cookies = Collections.unmodifiableSet(SharedPreferencesUtils.getStringSet(
                    prefs, getPrefAttrName(PREF_COOKIES), Collections.<String>emptySet()));
        }

        @Override
        protected void save(@NonNull final SharedPreferences.Editor prefs) {
            super.save(prefs);
            SharedPreferencesUtils.putStringSet(prefs, getPrefAttrName(PREF_COOKIES), this.cookies);
        }

        @Override
        protected void delete(@NonNull SharedPreferences.Editor prefs) {
            super.delete(prefs);
            prefs.remove(getPrefAttrName(PREF_COOKIES));
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
