package com.expidevapps.android.measurements.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.v4.util.SimpleArrayMap;
import android.text.TextUtils;

import com.crashlytics.android.Crashlytics;
import com.expidevapps.android.measurements.api.GmaApiClient.Request;
import com.expidevapps.android.measurements.api.GmaApiClient.Session;
import com.expidevapps.android.measurements.model.Assignment;
import com.expidevapps.android.measurements.model.Church;
import com.expidevapps.android.measurements.model.Measurement;
import com.expidevapps.android.measurements.model.MeasurementDetails;
import com.expidevapps.android.measurements.model.MeasurementType;
import com.expidevapps.android.measurements.model.MeasurementValue;
import com.expidevapps.android.measurements.model.Ministry;
import com.expidevapps.android.measurements.model.Ministry.Mcc;
import com.expidevapps.android.measurements.model.PagedList;
import com.expidevapps.android.measurements.model.Story;
import com.expidevapps.android.measurements.model.Training;
import com.expidevapps.android.measurements.model.Training.Completion;
import com.expidevapps.android.measurements.model.UserPreference;
import com.google.common.base.Objects;

import org.ccci.gto.android.common.api.AbstractApi.Request.MediaType;
import org.ccci.gto.android.common.api.AbstractApi.Request.Method;
import org.ccci.gto.android.common.api.AbstractTheKeyApi;
import org.ccci.gto.android.common.api.AbstractTheKeyApi.ExecutionContext;
import org.ccci.gto.android.common.api.ApiException;
import org.ccci.gto.android.common.api.ApiSocketException;
import org.ccci.gto.android.common.util.GenericKey;
import org.ccci.gto.android.common.util.IOUtils;
import org.ccci.gto.android.common.util.LocaleCompat;
import org.ccci.gto.android.common.util.SharedPreferencesUtils;
import org.joda.time.YearMonth;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import io.fabric.sdk.android.Fabric;
import me.thekey.android.TheKeySocketException;
import me.thekey.android.lib.TheKeyImpl;

public class GmaApiClient extends AbstractTheKeyApi<Request, ExecutionContext<Session>, Session> {
    private static final Logger LOG = LoggerFactory.getLogger(GmaApiClient.class);

    public static final int V4 = 4;
    public static final int V5 = 5;

    public static final int DEFAULT_STORIES_PER_PAGE = 20;

    private static final String PREF_COOKIES = "cookies";
    private static final String PREF_PERSON_ID = "user.person_id";

    private static final String ASSIGNMENTS = "assignments";
    private static final String CHURCHES = "churches";
    private static final String MEASUREMENTS = "measurements";
    private static final String MEASUREMENT_TYPES = "measurement_types";
    private static final String MINISTRIES = "ministries";
    private static final String STORIES = "stories";
    private static final String IMAGES = "images";
    private static final String TOKEN = "token";
    private static final String TRAINING = "training";
    private static final String TRAINING_COMPLETION = "training_completion";
    private static final String USER_PREFERENCES = "user_preferences";

    private static final SimpleArrayMap<GenericKey, GmaApiClient> INSTANCES = new SimpleArrayMap<>();

    private final int mApiVersion;
    @NonNull
    private final String mSource;

    private GmaApiClient(@NonNull final Context context, @NonNull final String apiUri, final int apiVersion,
                         @NonNull final String source, @Nullable final String guid) {
        super(context, TheKeyImpl.getInstance(context), apiUri + (apiUri.endsWith("/") ? "v" : "/v") + apiVersion + "/",
              "gma_api_sessions", guid);
        mApiVersion = apiVersion;
        mSource = source;
    }

    @NonNull
    public static GmaApiClient getInstance(@NonNull final Context context, @NonNull final String apiUri,
                                           final int apiVersion, @NonNull final String source,
                                           @NonNull final String guid) {
        final GenericKey key = new GenericKey(apiUri, apiVersion, source, guid);
        synchronized (INSTANCES) {
            if (!INSTANCES.containsKey(key)) {
                INSTANCES.put(key, new GmaApiClient(context.getApplicationContext(), apiUri, apiVersion, source, guid));
            }

            return INSTANCES.get(key);
        }
    }

    @NonNull
    public static GmaApiClient getInstance(@NonNull final Context context, @NonNull final String apiUri,
                                           @NonNull final String source, @NonNull final String guid) {
        return getInstance(context, apiUri, BuildConfig.GMA_API_VERSION, source, guid);
    }

    @NonNull
    public static GmaApiClient getInstance(@NonNull final Context context, @NonNull final String source,
                                           @NonNull final String guid) {
        return getInstance(context, BuildConfig.GMA_API_BASE_URI, source, guid);
    }

    @Nullable
    @Override
    protected String getDefaultService() {
        return mBaseUri.buildUpon().appendPath(TOKEN).toString();
    }

    @Override
    protected Session loadSession(@NonNull final SharedPreferences prefs, @NonNull final Request request) {
        assert request.context != null;
        return new Session(prefs, request.context.guid);
    }

    @Nullable
    @Override
    protected Session establishSession(@NonNull final Request request) throws ApiException {
        assert request.context != null;

        HttpURLConnection conn = null;
        try {
            if (request.context.guid != null) {
                final String service = getService();
                if (service != null) {
                    // get a ticket for this user
                    final String ticket = mTheKey.getTicket(request.context.guid, service);
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

                            // get the user's person_id for this session
                            final JSONObject user = json.optJSONObject("user");
                            final String personId = user != null ? user.optString("person_id") : null;

                            // create session object
                            return new Session(json.optString("session_ticket", null), cookies, personId,
                                               request.context.guid);
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
            reportJSONException(request, e);
            LOG.debug("invalid json for getToken", e);
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
    protected void onPrepareRequest(@NonNull final HttpURLConnection conn, @NonNull final Request request)
            throws ApiException, IOException {
        super.onPrepareRequest(conn, request);
        assert request.context != null;

        final CookieHandler handler = CookieHandler.getDefault();
        if (handler instanceof CookieManager) {
            final CookieStore cookies = ((CookieManager) handler).getCookieStore();
            try {
                final URI uri = conn.getURL().toURI();
                for (final HttpCookie cookie : cookies.get(uri)) {
                    cookies.remove(uri, cookie);
                }
            } catch (URISyntaxException e) {
            }
        }

        // attach cookies when using the session
        // XXX: this should go away once we remove the cookie requirement on the API
        if (request.useSession && request.context.session != null) {
            conn.addRequestProperty("Cookie", TextUtils.join("; ", request.context.session.cookies));
        }
        //Add bearer token code to replace URI token id [MMAND-12]
        if (request.useSession && request.context.session != null) {
            conn.addRequestProperty("Authorization", "Bearer " + request.context.session.id);
        }
    }

    @NonNull
    protected final Request.Parameter param(@NonNull final String name, @NonNull final Locale value) {
        return param(name, LocaleCompat.toLanguageTag(value));
    }

    @NonNull
    protected final Request.Parameter param(@NonNull final String name, @NonNull final Mcc value) {
        if (BuildConfig.DEBUG) {
            if (value == Mcc.UNKNOWN) {
                throw new AssertionError("param(name, Mcc.UNKNOWN) is invalid");
            }
        }
        return param(name, value.mJson);
    }

    private void reportJSONException(@NonNull final Request request, @NonNull final JSONException e) {
        // log JSON exception in Crashlytics
        if (Fabric.isInitialized() && Crashlytics.getInstance() != null) {
            if (request.context != null && request.context.url != null) {
                Crashlytics.setString("url", request.context.url.toString());
            }
            Crashlytics.logException(e);
        }
    }

    /* API methods */

    @NonNull
    private HttpURLConnection getToken(@NonNull final String ticket, final boolean refresh) throws ApiException {
        // build login request
        final Request login = new Request(TOKEN);
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
        final Request request = new Request(MINISTRIES);
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
            reportJSONException(request, e);
            LOG.error("error parsing getMinistries response", e);
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
        final Request request = new Request(CHURCHES);
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
            reportJSONException(request, e);
            LOG.error("error parsing getChurches response", e);
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
        final Request request = new Request(CHURCHES);
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
            reportJSONException(request, e);
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
        final Request request = new Request(CHURCHES + "/" + id);
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
    public List<MeasurementType> getMeasurementTypes(@NonNull final String ministryId) throws ApiException {
        return getMeasurementTypes(ministryId, Locale.getDefault());
    }

    @Nullable
    public List<MeasurementType> getMeasurementTypes(@NonNull final String ministryId, @NonNull final Locale locale)
            throws ApiException {
        // build request
        final Request request = new Request(MEASUREMENT_TYPES);

        // only use locale and ministryId if we have a valid ministryId
        if (!ministryId.equals(Ministry.INVALID_ID)) {
            request.params.add(param("ministry_id", ministryId));
            request.params.add(param("locale", locale));
        }

        // process request
        HttpURLConnection conn = null;
        try {
            conn = sendRequest(request);

            // is this a successful response?
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return MeasurementType
                        .listFromJson(new JSONArray(IOUtils.readString(conn.getInputStream())), ministryId);
            }
        } catch (final JSONException e) {
            reportJSONException(request, e);
            LOG.error("error parsing getMeasurementTypes response", e);
        } catch (final IOException e) {
            throw new ApiSocketException(e);
        } finally {
            IOUtils.closeQuietly(conn);
        }

        return null;
    }

    @Nullable
    public List<Measurement> getMeasurements(@NonNull final String ministryId, @NonNull final Mcc mcc,
                                             @NonNull final YearMonth period) throws ApiException {
        return getMeasurements(ministryId, mcc, period, Locale.getDefault());
    }

    @Nullable
    public List<Measurement> getMeasurements(@NonNull final String ministryId, @NonNull final Mcc mcc,
                                             @NonNull final YearMonth period, @NonNull final Locale locale)
            throws ApiException {
        // short-circuit if we don't have a valid ministryId or mcc
        if (ministryId.equals(Ministry.INVALID_ID) || mcc == Mcc.UNKNOWN) {
            return null;
        }

        // build request
        final Request request = new Request(MEASUREMENTS);
        request.params.add(param("source", mSource));
        request.params.add(param("ministry_id", ministryId));
        request.params.add(param("mcc", mcc));
        request.params.add(param("period", period.toString()));
        request.params.add(param("locale", locale));

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
            reportJSONException(request, e);
            LOG.error("error parsing getMeasurements response", e);
        } catch (final IOException e) {
            throw new ApiSocketException(e);
        } finally {
            IOUtils.closeQuietly(conn);
        }

        return null;
    }

    @Nullable
    public MeasurementDetails getMeasurementDetails(@NonNull final String ministryId, @NonNull final Mcc mcc,
                                                    @NonNull final String permLink, @NonNull final YearMonth period)
            throws ApiException {
        // short-circuit if we don't have a valid ministryId or mcc
        if (ministryId.equals(Ministry.INVALID_ID) || mcc == Mcc.UNKNOWN) {
            return null;
        }

        // build request
        final Request request = new Request(MEASUREMENTS + "/" + permLink);
        request.params.add(param("ministry_id", ministryId));
        request.params.add(param("mcc", mcc));
        request.params.add(param("period", period.toString()));

        // process request
        HttpURLConnection conn = null;
        try {
            conn = this.sendRequest(request);

            // is this a successful response?
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                if (request.context != null && request.context.guid != null) {
                    final MeasurementDetails details =
                            new MeasurementDetails(request.context.guid, ministryId, mcc, permLink, period);
                    details.setSource(mSource);
                    details.setJson(new JSONObject(IOUtils.readString(conn.getInputStream())), mApiVersion);
                    return details;
                }
            }
        } catch (final JSONException e) {
            reportJSONException(request, e);
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
        final Request request = new Request(MEASUREMENTS);
        request.method = Method.POST;
        try {
            final JSONArray json = new JSONArray();
            for (final MeasurementValue value : measurements) {
                json.put(value.toUpdateJson(mSource));
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

    /* BEGIN Assignment endpoints */

    @Nullable
    public List<Assignment> getAssignments() throws ApiException {
        return this.getAssignments(false);
    }

    @Nullable
    public List<Assignment> getAssignments(final boolean refresh) throws ApiException {
        // build request
        final Request request = new Request(ASSIGNMENTS);
        request.params.add(param("refresh", refresh));

        // process request
        HttpURLConnection conn = null;
        try {
            conn = this.sendRequest(request);

            // is this a successful response?
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                assert request.context != null && request.context.guid != null :
                        "request.context.guid should be non-null because the request was successful";
                return Assignment
                        .listFromJson(new JSONArray(IOUtils.readString(conn.getInputStream())), request.context.guid,
                                      request.context.session != null ? request.context.session.mPersonId : null);
            }
        } catch (final JSONException e) {
            reportJSONException(request, e);
            LOG.error("error parsing getAllMinistries response", e);
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
        final Request request = new Request(ASSIGNMENTS);
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
                return Assignment.fromJson(new JSONObject(IOUtils.readString(conn.getInputStream())), guid,
                                           request.context != null && request.context.session != null ?
                                                   request.context.session.mPersonId : null);
            }
        } catch (final IOException e) {
            throw new ApiSocketException(e);
        } catch (final JSONException e) {
            reportJSONException(request, e);
            LOG.error("invalid response json for createAssignment", e);
        } finally {
            IOUtils.closeQuietly(conn);
        }

        return null;
    }

    /* END Assignment endpoints */

    /* BEGIN Story endpoints */

    @Nullable
    @WorkerThread
    public Story createStory(@NonNull final Story story) throws ApiException, JSONException {
        return createStory(story.toJson());
    }

    @Nullable
    @WorkerThread
    public Story createStory(@NonNull final JSONObject story) throws ApiException {
        // build request
        final Request request = new Request(STORIES);
        request.method = Method.POST;
        request.setContent(story);

        // process request
        HttpURLConnection conn = null;
        try {
            conn = this.sendRequest(request);

            // is this a successful response?
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return Story.fromJson(new JSONObject(IOUtils.readString(conn.getInputStream())));
            }
        } catch (final JSONException e) {
            reportJSONException(request, e);
            LOG.error("error parsing createStory response", e);
        } catch (final IOException e) {
            throw new ApiSocketException(e);
        } finally {
            IOUtils.closeQuietly(conn);
        }

        return null;
    }

    @Nullable
    @WorkerThread
    public PagedList<Story> getStories(@NonNull final String ministryId, @Nullable final Bundle filters, final int page,
                                       final int pageSize) throws ApiException {
        if (Ministry.INVALID_ID.equals(ministryId)) {
            return null;
        }

        // build request
        final Request request = new Request(STORIES);
        request.params.add(param("ministry_id", ministryId));
        request.params.add(param("page", page));
        request.params.add(param("per_page", pageSize));

        // process any filters
        if (filters != null) {
            final Mcc mcc = Mcc.fromBundle(filters);
            if (mcc != Mcc.UNKNOWN) {
                request.params.add(param("mcc", mcc));
            }
            final long churchId = filters.getLong(Church.ARG_ID, Church.INVALID_ID);
            if (churchId != Church.INVALID_ID) {
                request.params.add(param("church_id", churchId));
            }
            final long trainingId = filters.getLong(Training.ARG_ID, Training.INVALID_ID);
            if (trainingId != Training.INVALID_ID) {
                request.params.add(param("training_id", trainingId));
            }
            request.params.add(param("self_only", filters.getBoolean(Story.ARG_SELF_ONLY, false)));
        }

        // process request
        HttpURLConnection conn = null;
        try {
            conn = this.sendRequest(request);

            // is this a successful response?
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return Story.listFromJson(new JSONObject(IOUtils.readString(conn.getInputStream())));
            }
        } catch (final JSONException e) {
            reportJSONException(request, e);
            LOG.error("error parsing getStories response", e);
        } catch (final IOException e) {
            throw new ApiSocketException(e);
        } finally {
            IOUtils.closeQuietly(conn);
        }

        return null;
    }

    @Nullable
    public Story storeImage(final long storyId, @NonNull final File image) throws ApiException {
        // build request
        final Request request = new Request(IMAGES);
        request.method = Method.POST;
        request.params.add(param("story_id", storyId));
        request.setContentType(MediaType.FORM_MULTIPART);
        request.form.add(param("file", image));

        // process request
        HttpURLConnection conn = null;
        try {
            conn = sendRequest(request);

            // is this a successful response?
            if (conn.getResponseCode() == HttpURLConnection.HTTP_CREATED) {
                return Story.fromJson(new JSONObject(IOUtils.readString(conn.getInputStream())));
            }
        } catch (final JSONException e) {
            reportJSONException(request, e);
            LOG.error("error parsing storeImage response", e);
        } catch (final IOException e) {
            throw new ApiSocketException(e);
        } finally {
            IOUtils.closeQuietly(conn);
        }

        return null;
    }

    /* END Story endpoints */

    /* BEGIN Training endpoints */

    @Nullable
    public List<Training> getTrainings(@NonNull final String ministryId, @NonNull final Mcc mcc) throws ApiException {
        return getTrainings(ministryId, mcc, true, false);
    }

    @Nullable
    public List<Training> getTrainings(@NonNull final String ministryId, @NonNull final Mcc mcc, final boolean all,
                                       final boolean includeDescendents) throws ApiException {
        // short-circuit on invalid requests
        if (ministryId.equals(Ministry.INVALID_ID) || mcc == Mcc.UNKNOWN) {
            return null;
        }

        // build request
        final Request request = new Request(TRAINING);
        request.params.add(param("ministry_id", ministryId));
        request.params.add(param("mcc", mcc));
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
            reportJSONException(request, e);
            LOG.error("error parsing getTrainings response", e);
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
        final Request request = new Request(TRAINING);
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
            reportJSONException(request, e);
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

        final Request request = new Request(TRAINING + "/" + id);
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

        final Request request = new Request(TRAINING + "/" + id);
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
        final Request request = new Request(TRAINING_COMPLETION);
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
            reportJSONException(request, e);
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

        final Request request = new Request(TRAINING_COMPLETION + "/" + id);
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
        final Request request = new Request(TRAINING_COMPLETION + "/" + completionId);
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
            reportJSONException(request, e);
            LOG.error("error parsing createTraining response", e);
        } catch (final IOException e) {
            throw new ApiSocketException(e);
        } finally {
            IOUtils.closeQuietly(conn);
        }

        return null;
    }

    /* END Training endpoints */

    /* BEGIN User Preference endpoints */

    @Nullable
    @WorkerThread
    public Map<String, UserPreference> getPreferences() throws ApiException {
        final Request request = new Request(USER_PREFERENCES);
        HttpURLConnection conn = null;
        try {
            conn = this.sendRequest(request);
            assert request.context != null && request.context.guid != null;

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return UserPreference
                        .mapFromJson(new JSONObject(IOUtils.readString(conn.getInputStream())), request.context.guid);
            }
        } catch (final JSONException e) {
            LOG.error("error parsing getPreferences response", e);
        } catch (final IOException e) {
            throw new ApiSocketException(e);
        } finally {
            IOUtils.closeQuietly(conn);
        }

        return null;
    }

    @Nullable
    @WorkerThread
    public Map<String, UserPreference> updatePreferences(@NonNull final UserPreference... prefs)
            throws ApiException, JSONException {
        // generate JSON for these preferences
        final JSONObject json = new JSONObject();
        for (final UserPreference pref : prefs) {
            json.put(pref.getName(), pref.getValue());
        }

        // issue API request
        return updatePreferences(json);
    }

    @Nullable
    @WorkerThread
    public Map<String, UserPreference> updatePreferences(@NonNull final JSONObject json) throws ApiException {
        final Request request = new Request(USER_PREFERENCES);
        request.method = Method.POST;
        request.setContent(json);

        HttpURLConnection conn = null;
        try {
            conn = this.sendRequest(request);
            assert request.context != null && request.context.guid != null;

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return UserPreference
                        .mapFromJson(new JSONObject(IOUtils.readString(conn.getInputStream())), request.context.guid);
            }
        } catch (final JSONException e) {
            LOG.error("error parsing updatePreferences response", e);
        } catch (final IOException e) {
            throw new ApiSocketException(e);
        } finally {
            IOUtils.closeQuietly(conn);
        }

        return null;
    }

    /* END User Preference endpoints */

    static final class Request extends AbstractTheKeyApi.Request<ExecutionContext<Session>, Session> {
        Request(@NonNull final String path) {
            super(path);
        }
    }

    protected static class Session extends AbstractTheKeyApi.Session {
        @NonNull
        final Set<String> cookies;
        @Nullable
        final String mPersonId;

        Session(@Nullable final String id, @Nullable final Collection<String> cookies, @Nullable final String personId,
                @Nullable final String guid) {
            super(id, guid);
            this.cookies = Collections.unmodifiableSet(new HashSet<>(cookies));
            mPersonId = personId;
        }

        Session(@NonNull final SharedPreferences prefs, @Nullable final String guid) {
            super(prefs, guid);
            this.cookies = Collections.unmodifiableSet(SharedPreferencesUtils.getStringSet(
                    prefs, getPrefAttrName(PREF_COOKIES), Collections.<String>emptySet()));
            mPersonId = prefs.getString(getPrefAttrName(PREF_PERSON_ID), null);
        }

        @Override
        public void save(@NonNull final SharedPreferences.Editor prefs) {
            super.save(prefs);
            SharedPreferencesUtils.putStringSet(prefs, getPrefAttrName(PREF_COOKIES), this.cookies);
            prefs.putString(getPrefAttrName(PREF_PERSON_ID), mPersonId);
        }

        @Override
        public void delete(@NonNull SharedPreferences.Editor prefs) {
            super.delete(prefs);
            prefs.remove(getPrefAttrName(PREF_COOKIES));
            prefs.remove(getPrefAttrName(PREF_PERSON_ID));
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
            return super.equals(o) && this.cookies.equals(that.cookies) && Objects.equal(mPersonId, that.mPersonId);
        }

        @Override
        public boolean isValid() {
            return super.isValid() && this.cookies.size() > 0;
        }
    }
}
