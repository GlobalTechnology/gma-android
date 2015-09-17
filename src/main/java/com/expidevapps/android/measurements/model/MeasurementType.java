package com.expidevapps.android.measurements.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Function;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MeasurementType extends Base {
    public static final String INVALID_ID = "";
    public static final String INVALID_PERM_LINK_STUB = "";
    public static final int DEFAULT_SORT_ORDER = -1;
    public static final boolean DEFAULT_CUSTOM = true;

    public static final String ARG_COLUMN = MeasurementType.class.getName() + ".ARG_COLUMN";

    static final String JSON_PERM_LINK_STUB = "perm_link_stub";
    static final String JSON_TYPE_ID = "measurement_type_id";
    private static final String JSON_NAME = "name";
    private static final String JSON_ENGLISH = "english";
    private static final String JSON_DESCRIPTION = "description";
    private static final String JSON_SECTION = "section";
    private static final String JSON_COLUMN = "column";
    private static final String JSON_CUSTOM = "is_custom";
    private static final String JSON_SORT_ORDER = "sort_order";

    private static final String JSON_TYPE_IDS = "measurement_type_ids";
    private static final String JSON_TYPE_IDS_PERSONAL = "person";
    private static final String JSON_TYPE_IDS_LOCAL = "local";
    private static final String JSON_TYPE_IDS_TOTAL = "total";
    private static final String JSON_PERSONAL_ID = "person_id";
    private static final String JSON_LOCAL_ID = "local_id";
    private static final String JSON_TOTAL_ID = "total_id";

    public static final Function<MeasurementType, String> FUNCTION_PERMLINK = new Function<MeasurementType, String>() {
        @NonNull
        @Override
        public String apply(@NonNull final MeasurementType type) {
            return type.getPermLinkStub();
        }
    };

    public enum Section {
        WIN, BUILD, SEND, OTHER, UNKNOWN;

        @NonNull
        public static Section fromRaw(@Nullable final String raw) {
            if (raw != null) {
                switch (raw.toLowerCase(Locale.US)) {
                    case "win":
                        return WIN;
                    case "build":
                        return BUILD;
                    case "send":
                        return SEND;
                    case "other":
                        return OTHER;
                }
            }
            return UNKNOWN;
        }
    }

    public enum Column {
        FAITH, FRUIT, OUTCOME, OTHER, UNKNOWN;

        @NonNull
        public static Column fromRaw(@Nullable final String raw) {
            if (raw != null) {
                switch (raw.toLowerCase(Locale.US)) {
                    case "faith":
                        return FAITH;
                    case "fruit":
                        return FRUIT;
                    case "outcome":
                        return OUTCOME;
                    case "other":
                        return OTHER;
                }
            }

            return UNKNOWN;
        }
    }

    @NonNull
    private final String permLinkStub;
    @NonNull
    private String personalId = INVALID_ID;
    @NonNull
    private String localId = INVALID_ID;
    @NonNull
    private String totalId = INVALID_ID;
    @Nullable
    private String name;
    @Nullable
    private String description;
    @NonNull
    private Section section = Section.UNKNOWN;
    @NonNull
    private Column column = Column.UNKNOWN;
    private boolean custom = DEFAULT_CUSTOM;
    private int sortOrder = DEFAULT_SORT_ORDER;
    @Nullable
    private MeasurementTypeLocalization mLocalization;

    public MeasurementType(@NonNull final String permLinkStub) {
        this.permLinkStub = permLinkStub;
    }

    @NonNull
    public static List<MeasurementType> listFromJson(@NonNull final JSONArray json, @NonNull final String ministryId)
            throws JSONException {
        final List<MeasurementType> types = new ArrayList<>();
        for (int i = 0; i < json.length(); i++) {
            types.add(MeasurementType.fromJson(json.getJSONObject(i), ministryId));
        }
        return types;
    }

    @NonNull
    public static MeasurementType fromJson(@NonNull final JSONObject json, @NonNull final String ministryId)
            throws JSONException {
        final MeasurementType type = new MeasurementType(json.getString(JSON_PERM_LINK_STUB));

        if (json.has(JSON_NAME)) {
            type.name = json.optString(JSON_NAME, null);
        } else if (json.has(JSON_ENGLISH)) {
            type.name = json.optString(JSON_ENGLISH, null);
        }
        type.description = json.optString(JSON_DESCRIPTION, null);
        type.section = Section.fromRaw(json.getString(JSON_SECTION));
        type.column = Column.fromRaw(json.getString(JSON_COLUMN));
        type.custom = json.optBoolean(JSON_CUSTOM, DEFAULT_CUSTOM);
        type.sortOrder = json.optInt(JSON_SORT_ORDER, DEFAULT_SORT_ORDER);

        final JSONObject typeIds = json.optJSONObject(JSON_TYPE_IDS);
        if (typeIds != null) {
            type.personalId = typeIds.optString(JSON_TYPE_IDS_PERSONAL, INVALID_ID);
            type.localId = typeIds.optString(JSON_TYPE_IDS_LOCAL, INVALID_ID);
            type.totalId = typeIds.optString(JSON_TYPE_IDS_TOTAL, INVALID_ID);
        } else {
            type.personalId = json.optString(JSON_PERSONAL_ID, INVALID_ID);
            type.localId = json.optString(JSON_LOCAL_ID, INVALID_ID);
            type.totalId = json.optString(JSON_TOTAL_ID, INVALID_ID);
        }

        // parse MeasurementType localization if we have a valid ministry ID and a locale
        if (!ministryId.equals(Ministry.INVALID_ID) && json.has(MeasurementTypeLocalization.JSON_LOCALE_NAME)) {
            type.mLocalization = MeasurementTypeLocalization.fromJson(json, ministryId);
        }

        return type;
    }

    @NonNull
    public String getPersonalId() {
        return personalId;
    }

    public void setPersonalId(@NonNull final String personalId) {
        this.personalId = personalId;
    }

    @NonNull
    public String getLocalId() {
        return localId;
    }

    public void setLocalId(@NonNull final String localId) {
        this.localId = localId;
    }

    @NonNull
    public String getTotalId() {
        return totalId;
    }

    public void setTotalId(@NonNull final String totalId) {
        this.totalId = totalId;
    }

    @Nullable
    public String getName() {
        return name;
    }

    public void setName(@Nullable final String name) {
        this.name = name;
    }

    @NonNull
    public String getPermLinkStub() {
        return permLinkStub;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    public void setDescription(@Nullable final String description) {
        this.description = description;
    }

    @NonNull
    public Section getSection() {
        return section;
    }

    public void setSection(@NonNull final Section section) {
        this.section = section;
    }

    @NonNull
    public Column getColumn() {
        return column;
    }

    public void setColumn(@NonNull final Column column) {
        this.column = column;
    }

    public boolean isCustom() {
        return custom;
    }

    public void setCustom(final boolean custom) {
        this.custom = custom;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(final int order) {
        this.sortOrder = order;
    }

    @Nullable
    public MeasurementTypeLocalization getLocalization() {
        return mLocalization;
    }

    public void setLocalization(@Nullable final MeasurementTypeLocalization localization) {
        mLocalization = localization;
    }
}
