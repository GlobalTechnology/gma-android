package com.expidevapps.android.measurements.model;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.expidevapps.android.measurements.R;
import com.google.common.base.Objects;

import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Church extends Location implements Cloneable {
    public static final long INVALID_ID = -1;

    private static final int DEVELOPMENT_UNKNOWN = 0;
    private static final int DEVELOPMENT_TARGET = 1;
    private static final int DEVELOPMENT_GROUP = 2;
    private static final int DEVELOPMENT_CHURCH = 3;
    private static final int DEVELOPMENT_MULTIPLYING_CHURCH = 5;

    public enum Development {
        UNKNOWN(DEVELOPMENT_UNKNOWN, R.drawable.ic_church_church),
        TARGET(DEVELOPMENT_TARGET, R.drawable.ic_church_target), GROUP(DEVELOPMENT_GROUP, R.drawable.ic_church_group),
        CHURCH(DEVELOPMENT_CHURCH, R.drawable.ic_church_church),
        MULTIPLYING_CHURCH(DEVELOPMENT_MULTIPLYING_CHURCH, R.drawable.ic_church_multiplying);

        public final int id;
        @DrawableRes
        public final int image;

        Development(final int id, int image) {
            this.id = id;
            this.image = image;
        }

        @NonNull
        public static Development fromRaw(final int index) {
            switch (index) {
                case DEVELOPMENT_TARGET:
                    return TARGET;
                case DEVELOPMENT_GROUP:
                    return GROUP;
                case DEVELOPMENT_CHURCH:
                    return CHURCH;
                case DEVELOPMENT_MULTIPLYING_CHURCH:
                    return MULTIPLYING_CHURCH;
                default:
                    return UNKNOWN;
            }
        }
    }

    @Deprecated
    private static final int SECURITY_LOCAL_PRIVATE = 0;
    private static final int SECURITY_PRIVATE = 1;
    private static final int SECURITY_REGISTERED_USERS = 2;
    private static final int SECURITY_PUBLIC = 3;

    public enum Security {
        PRIVATE(SECURITY_PRIVATE), REGISTERED_USERS(SECURITY_REGISTERED_USERS), PUBLIC(SECURITY_PUBLIC);

        public static Security DEFAULT = Security.REGISTERED_USERS;

        public final int id;

        Security(final int id) {
            this.id = id;
        }

        @NonNull
        public static Security fromRaw(final int id) {
            switch (id) {
                case SECURITY_LOCAL_PRIVATE:
                case SECURITY_PRIVATE:
                    return PRIVATE;
                case SECURITY_PUBLIC:
                    return PUBLIC;
                case SECURITY_REGISTERED_USERS:
                    return REGISTERED_USERS;
                default:
                    return DEFAULT;
            }
        }
    }

    public static final String JSON_ID = "id";
    private static final String JSON_PARENT = "parent_id";
    private static final String JSON_PARENTS = "parents";
    public static final String JSON_MINISTRY_ID = "ministry_id";
    public static final String JSON_NAME = "name";
    public static final String JSON_CONTACT_EMAIL = "contact_email";
    public static final String JSON_CONTACT_MOBILE = "contact_mobile";
    public static final String JSON_CONTACT_NAME = "contact_name";
    public static final String JSON_JESUS_FILM_ACTIVITY = "jf_contrib";

    public static final String JSON_DEVELOPMENT = "development";
    public static final String JSON_SIZE = "size";
    public static final String JSON_SECURITY = "security";
    public static final String JSON_END_DATE = "end_date";
    public static final String JSON_CREATED_BY = "created_by";

    private long id = INVALID_ID;
    private long mParentId = INVALID_ID;
    @NonNull
    private String ministryId = Ministry.INVALID_ID;
    @Nullable
    private String name;
    @Nullable
    private String contactEmail;
    @Nullable
    private String contactMobile;
    @Nullable
    private String contactName;
    @NonNull
    private Development development = Development.UNKNOWN;
    @NonNull
    private Security security = Security.PUBLIC;
    private boolean mJesusFilmActivity = false;
    private int size = 0;
    @Nullable
    private String createdBy;
    @Nullable
    private LocalDate mEndDate;

    @NonNull
    public static List<Church> listFromJson(@NonNull final JSONArray json) throws JSONException {
        final List<Church> churches = new ArrayList<>();
        for (int i = 0; i < json.length(); i++) {
            churches.add(fromJson(json.getJSONObject(i)));
        }
        return churches;
    }

    @NonNull
    public static Church fromJson(@NonNull final JSONObject json) throws JSONException {
        final Church church = new Church();
        church.id = json.getLong(JSON_ID);

        final JSONArray parents = json.optJSONArray(JSON_PARENTS);
        if (parents != null) {
            church.mParentId = parents.optLong(0, church.mParentId);
        }
        church.mParentId = json.optLong(JSON_PARENT, church.mParentId);

        church.ministryId = json.getString(JSON_MINISTRY_ID);
        church.name = json.optString(JSON_NAME, null);
        church.contactEmail = json.optString(JSON_CONTACT_EMAIL, null);
        church.contactMobile = json.optString(JSON_CONTACT_MOBILE, null);
        church.mJesusFilmActivity = json.optInt(JSON_JESUS_FILM_ACTIVITY, 0) > 0;
        church.contactName = json.optString(JSON_CONTACT_NAME, null);
        church.setLatitude(json.optDouble(JSON_LATITUDE, Double.NaN));
        church.setLongitude(json.optDouble(JSON_LONGITUDE, Double.NaN));
        church.development = Development.fromRaw(json.optInt(JSON_DEVELOPMENT, DEVELOPMENT_UNKNOWN));
        church.security = Security.fromRaw(json.optInt(JSON_SECURITY, Security.DEFAULT.id));
        church.size = json.optInt(JSON_SIZE, 0);
        church.createdBy = json.optString(JSON_CREATED_BY, null);
        return church;
    }

    public Church() {
    }

    private Church(@NonNull final Church church) {
        super(church);
        this.id = church.id;
        mParentId = church.mParentId;
        this.ministryId = church.ministryId;
        this.name = church.name;
        this.contactEmail = church.contactEmail;
        this.contactMobile = church.contactMobile;
        mJesusFilmActivity = church.mJesusFilmActivity;
        this.contactName = church.contactName;
        this.development = church.development;
        this.size = church.size;
        this.security = church.security;
        this.createdBy = church.createdBy;
        mEndDate = church.mEndDate;
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public long getParentId() {
        return mParentId;
    }

    public void setParentId(final long parent) {
        mParentId = parent;
    }

    public boolean hasParent() {
        return mParentId != INVALID_ID;
    }

    @NonNull
    public String getMinistryId() {
        return ministryId;
    }

    public void setMinistryId(@NonNull final String ministryId) {
        if (mTrackingChanges && !TextUtils.equals(this.ministryId, ministryId)) {
            mDirty.add(JSON_MINISTRY_ID);
        }
        this.ministryId = ministryId;
    }

    @Nullable
    public String getName() {
        return name;
    }

    public void setName(@Nullable final String name) {
        if (mTrackingChanges && !TextUtils.equals(this.name, name)) {
            mDirty.add(JSON_NAME);
        }
        this.name = name;
    }

    @Nullable
    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(@Nullable final String email) {
        if (mTrackingChanges && !TextUtils.equals(this.contactEmail, email)) {
            mDirty.add(JSON_CONTACT_EMAIL);
        }
        this.contactEmail = email;
    }

    @Nullable
    public String getContactMobile() {
        return contactMobile;
    }

    public void setContactMobile(@Nullable final String mobile) {
        if (mTrackingChanges && !TextUtils.equals(this.contactMobile, mobile)) {
            mDirty.add(JSON_CONTACT_MOBILE);
        }
        this.contactMobile = mobile;
    }

    public boolean isJesusFilmActivity() {
        return mJesusFilmActivity;
    }

    public void setJesusFilmActivity(final boolean state) {
        if (mTrackingChanges && mJesusFilmActivity != state) {
            mDirty.add(JSON_JESUS_FILM_ACTIVITY);
        }
        mJesusFilmActivity = state;
    }

    @Nullable
    public String getContactName() {
        return contactName;
    }

    public void setContactName(@Nullable final String name) {
        if (mTrackingChanges && !TextUtils.equals(this.contactName, name)) {
            mDirty.add(JSON_CONTACT_NAME);
        }
        this.contactName = name;
    }

    @NonNull
    public Development getDevelopment() {
        return development;
    }

    public void setDevelopment(@NonNull final Development development) {
        if (mTrackingChanges && this.development != development) {
            mDirty.add(JSON_DEVELOPMENT);
        }
        this.development = development;
    }

    public int getSize() {
        return size;
    }

    public void setSize(final int size) {
        if (mTrackingChanges && this.size != size) {
            mDirty.add(JSON_SIZE);
        }
        this.size = size;
    }

    @NonNull
    public Security getSecurity() {
        return security;
    }

    public void setSecurity(@NonNull final Security security) {
        this.security = security;
    }

    @Nullable
    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(@Nullable final String createdBy) {
        this.createdBy = createdBy;
    }

    @Nullable
    public LocalDate getEndDate() {
        return mEndDate;
    }

    public void setEndDate(@Nullable final LocalDate date) {
        if (mTrackingChanges && !Objects.equal(mEndDate, date)) {
            mDirty.add(JSON_END_DATE);
        }
        mEndDate = date;
    }

    public void setDeletedEndDate() {
        setEndDate(LocalDate.now().withDayOfMonth(1).minusDays(1));
    }

    @Override
    public boolean canEdit(@Nullable final Assignment assignment) {
        return assignment != null && assignment.can(Task.EDIT_CHURCH, this);
    }

    @Override
    @SuppressWarnings("CloneDoesntCallSuperClone")
    public Church clone() {
        return new Church(this);
    }

    @NonNull
    @Override
    public JSONObject toJson() throws JSONException {
        final JSONObject json = super.toJson();
        if (mParentId != INVALID_ID) {
            json.put(JSON_PARENT, mParentId);
        }
        json.put(JSON_MINISTRY_ID, this.ministryId);
        json.put(JSON_NAME, this.name);
        json.put(JSON_CONTACT_NAME, this.contactName);
        json.put(JSON_CONTACT_EMAIL, this.contactEmail);
        json.put(JSON_CONTACT_MOBILE, this.contactMobile);
        json.put(JSON_JESUS_FILM_ACTIVITY, mJesusFilmActivity);

        if (this.development != Development.UNKNOWN) {
            json.put(JSON_DEVELOPMENT, this.development.id);
        }
        json.put(JSON_SIZE, this.size);
        json.put(JSON_SECURITY, this.security.id);
        if (mEndDate != null) {
            json.put(JSON_END_DATE, mEndDate.toString());
        }
        return json;
    }
}
