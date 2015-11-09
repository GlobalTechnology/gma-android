package com.expidevapps.android.measurements.model;

import static com.expidevapps.android.measurements.model.PagedList.JSON_META;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.expidevapps.android.measurements.model.Ministry.Mcc;

import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Story extends Location {
    public static final long INVALID_ID = -1;

    public static final String ARG_SELF_ONLY = Story.class.getName() + ".ARG_SELF_ONLY";

    private static final String JSON_STORIES = "stories";
    private static final String JSON_ID = "story_id";
    private static final String JSON_MINISTRY_ID = "ministry_id";
    private static final String JSON_MCC = "mcc";
    private static final String JSON_TITLE = "title";
    private static final String JSON_CONTENT = "content";
    private static final String JSON_IMAGE_URL = "image_url";
    private static final String JSON_CREATED_BY = "created_by";
    private static final String JSON_CREATED = "created_at";
    private static final String JSON_UPDATED = "updated_at";
    private static final String JSON_STATE = "state";
    private static final String JSON_PRIVACY = "privacy";

    private static final String JSON_STATE_DRAFT = "draft";
    private static final String JSON_STATE_PUBLISHED = "published";
    private static final String JSON_STATE_REMOVED = "removed";

    private static final String JSON_PRIVACY_PUBLIC = "public";
    private static final String JSON_PRIVACY_TEAM = "team_only";

    public enum Privacy {
        PUBLIC(JSON_PRIVACY_PUBLIC), TEAM(JSON_PRIVACY_TEAM);

        public static final Privacy DEFAULT = PUBLIC;

        @NonNull
        public final String mJson;

        Privacy(@NonNull final String json) {
            mJson = json;
        }

        @NonNull
        public static Privacy fromJson(@Nullable final String json) {
            if (json != null) {
                switch (json) {
                    case JSON_PRIVACY_PUBLIC:
                        return PUBLIC;
                    case JSON_PRIVACY_TEAM:
                        return TEAM;
                }
            }
            return DEFAULT;
        }
    }

    public enum State {
        DRAFT(JSON_STATE_DRAFT), PUBLISHED(JSON_STATE_PUBLISHED), REMOVED(JSON_STATE_REMOVED), UNKNOWN(null);

        public static final State DEFAULT = DRAFT;

        @Nullable
        public final String mJson;

        State(@Nullable final String json) {
            mJson = json;
        }

        @NonNull
        public static State fromJson(@Nullable final String json) {
            if (json != null) {
                switch (json) {
                    case JSON_STATE_DRAFT:
                        return DRAFT;
                    case JSON_STATE_PUBLISHED:
                        return PUBLISHED;
                    case JSON_STATE_REMOVED:
                        return REMOVED;
                }
            }
            return UNKNOWN;
        }
    }

    private long mId = INVALID_ID;
    @NonNull
    private String mMinistryId = Ministry.INVALID_ID;
    @NonNull
    private Mcc mMcc = Mcc.UNKNOWN;
    @NonNull
    private String mTitle = "";
    @NonNull
    private String mContent = "";
    @Nullable
    private String mImageUrl;
    @NonNull
    private Privacy mPrivacy = Privacy.DEFAULT;
    @NonNull
    private State mState = State.UNKNOWN;
    @Nullable
    private String mCreatedBy;
    @NonNull
    private LocalDate mCreated = LocalDate.now();

    public Story() {
    }

    @NonNull
    public static PagedList<Story> listFromJson(@NonNull final JSONObject json) throws JSONException {
        final PagedList<Story> stories = PagedList.fromMetaJson(json.getJSONObject(JSON_META));
        stories.addAll(listFromJson(json.getJSONArray(JSON_STORIES)));
        return stories;
    }

    @NonNull
    public static List<Story> listFromJson(@NonNull final JSONArray json) throws JSONException {
        final List<Story> stories = new ArrayList<>(json.length());
        for (int i = 0; i < json.length(); i++) {
            stories.add(fromJson(json.getJSONObject(i)));
        }
        return stories;
    }

    @NonNull
    public static Story fromJson(@NonNull final JSONObject json) throws JSONException {
        final Story story = new Story();
        story.populateFromJson(json);
        return story;
    }

    @Override
    void populateFromJson(@NonNull final JSONObject json) throws JSONException {
        super.populateFromJson(json);

        mId = json.getLong(JSON_ID);
        mMinistryId = json.getString(JSON_MINISTRY_ID);
        mMcc = Mcc.fromJson(json.optString(JSON_MCC, Mcc.UNKNOWN.mJson));
        mTitle = json.getString(JSON_TITLE);
        mContent = json.getString(JSON_CONTENT);
        mImageUrl = json.optString(JSON_IMAGE_URL, null);
        mPrivacy = Privacy.fromJson(json.optString(JSON_PRIVACY, null));
        mState = State.fromJson(json.optString(JSON_STATE, State.UNKNOWN.mJson));
        mCreated = LocalDate.parse(json.getString(JSON_CREATED));
        mCreatedBy = json.optString(JSON_CREATED_BY, null);
    }

    @NonNull
    @Override
    public JSONObject toJson() throws JSONException {
        final JSONObject json = super.toJson();

        json.put(JSON_MINISTRY_ID, mMinistryId);
        json.put(JSON_MCC, mMcc.mJson);
        json.put(JSON_TITLE, mTitle);
        json.put(JSON_CONTENT, mContent);
        json.put(JSON_PRIVACY, mPrivacy.mJson);
        json.put(JSON_STATE, mState.mJson);

        // move latitude & longitude to location node
        if (json.has(JSON_LATITUDE) || json.has(JSON_LONGITUDE)) {
            final JSONObject location = new JSONObject();
            location.putOpt(JSON_LATITUDE, json.remove(JSON_LATITUDE));
            location.putOpt(JSON_LONGITUDE, json.remove(JSON_LONGITUDE));
            json.put(JSON_LOCATION, location);
        }

        return json;
    }

    public long getId() {
        return mId;
    }

    public void setId(final long id) {
        mId = id;
    }

    @NonNull
    public String getMinistryId() {
        return mMinistryId;
    }

    public void setMinistryId(@NonNull final String ministryId) {
        mMinistryId = ministryId;
    }

    @NonNull
    public Mcc getMcc() {
        return mMcc;
    }

    public void setMcc(@NonNull final Mcc mcc) {
        mMcc = mcc;
    }

    @NonNull
    public String getTitle() {
        return mTitle;
    }

    public void setTitle(@NonNull final String title) {
        mTitle = title;
    }

    @NonNull
    public String getContent() {
        return mContent;
    }

    public void setContent(@NonNull final String content) {
        mContent = content;
    }

    @Nullable
    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(@Nullable final String url) {
        mImageUrl = url;
    }

    @NonNull
    public Privacy getPrivacy() {
        return mPrivacy;
    }

    public void setPrivacy(@NonNull final Privacy privacy) {
        mPrivacy = privacy;
    }

    @NonNull
    public State getState() {
        return mState;
    }

    public void setState(@NonNull final State state) {
        mState = state;
    }

    @Nullable
    public String getCreatedBy() {
        return mCreatedBy;
    }

    public void setCreatedBy(@Nullable final String userId) {
        mCreatedBy = userId;
    }

    @NonNull
    public LocalDate getCreated() {
        return mCreated;
    }

    public void setCreated(@NonNull final LocalDate date) {
        mCreated = date;
    }
}
