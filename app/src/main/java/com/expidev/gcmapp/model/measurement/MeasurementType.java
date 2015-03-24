package com.expidev.gcmapp.model.measurement;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.expidev.gcmapp.model.Base;

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

    public static final String ARG_COLUMN = MeasurementType.class.getName() + ".ARG_COLUMN";

    static final String JSON_PERM_LINK_STUB = "perm_link_stub";
    private static final String JSON_PERSONAL_ID = "person_id";
    private static final String JSON_LOCAL_ID = "local_id";
    private static final String JSON_TOTAL_ID = "total_id";
    private static final String JSON_NAME = "name";
    private static final String JSON_DESCRIPTION = "description";
    private static final String JSON_SECTION = "section";
    private static final String JSON_COLUMN = "column";
    private static final String JSON_SORT_ORDER = "sort_order";

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
        FAITH, FRUIT, OUTCOME, UNKNOWN;

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
                }
            }

            return UNKNOWN;
        }
    }

    @NonNull
    private String personalId = INVALID_ID;
    @NonNull
    private String localId = INVALID_ID;
    @NonNull
    private String totalId = INVALID_ID;
    @Nullable
    private String name;
    @NonNull
    private String permLinkStub;
    @Nullable
    private String description;
    @NonNull
    private Section section = Section.UNKNOWN;
    @NonNull
    private Column column = Column.UNKNOWN;
    private int sortOrder = DEFAULT_SORT_ORDER;

    @NonNull
    public static List<MeasurementType> listFromJson(@NonNull final JSONArray json) throws JSONException {
        final List<MeasurementType> types = new ArrayList<>();
        for (int i = 0; i < json.length(); i++) {
            types.add(MeasurementType.fromJson(json.getJSONObject(i)));
        }
        return types;
    }

    @NonNull
    public static MeasurementType fromJson(@NonNull final JSONObject json) throws JSONException {
        final MeasurementType type = new MeasurementType();
        type.personalId = json.optString(JSON_PERSONAL_ID, INVALID_ID);
        type.localId = json.optString(JSON_LOCAL_ID, INVALID_ID);
        type.totalId = json.optString(JSON_TOTAL_ID, INVALID_ID);
        type.name = json.optString(JSON_NAME, null);
        type.permLinkStub = json.getString(JSON_PERM_LINK_STUB);
        type.description = json.getString(JSON_DESCRIPTION);
        type.section = Section.fromRaw(json.getString(JSON_SECTION));
        type.column = Column.fromRaw(json.getString(JSON_COLUMN));
        type.sortOrder = json.optInt(JSON_SORT_ORDER, DEFAULT_SORT_ORDER);
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

    public void setPermLinkStub(@NonNull final String permLinkStub) {
        this.permLinkStub = permLinkStub;
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

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(final int order) {
        this.sortOrder = order;
    }
}
