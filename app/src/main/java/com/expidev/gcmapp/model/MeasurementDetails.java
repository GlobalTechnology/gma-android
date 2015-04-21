package com.expidev.gcmapp.model;

import static com.expidev.gcmapp.Constants.MEASUREMENTS_SOURCE;
import static com.expidev.gcmapp.http.GmaApiClient.V4;
import static com.expidev.gcmapp.model.measurement.MeasurementValue.TYPE_LOCAL;
import static com.expidev.gcmapp.model.measurement.MeasurementValue.TYPE_PERSONAL;
import static com.expidev.gcmapp.model.measurement.MeasurementValue.TYPE_TOTAL;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.expidev.gcmapp.model.Ministry.Mcc;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;

import org.joda.time.YearMonth;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class MeasurementDetails extends Base {
    private static final Logger LOG = LoggerFactory.getLogger(MeasurementDetails.class);

    private static final String JSON_HISTORY_TOTAL = "total";
    private static final String JSON_HISTORY_LOCAL = "local";
    private static final String JSON_HISTORY_PERSONAL = "my_measurements";
    private static final String JSON_BREAKDOWN_SELF = "self_breakdown";
    private static final String JSON_BREAKDOWN_LOCAL = "local_breakdown";
    private static final String JSON_BREAKDOWN_TEAM = "team";
    private static final String JSON_BREAKDOWN_SELF_ASSIGNED = "self_assigned";
    private static final String JSON_BREAKDOWN_SUB_MINISTRIES = "sub_ministries";

    @NonNull
    private final String guid;
    @NonNull
    private final String ministryId;
    @NonNull
    private final Mcc mcc;
    @NonNull
    private final String permLink;
    @NonNull
    private final YearMonth period;

    @Nullable
    private String rawJson;
    @Nullable
    private JSONObject json;
    private int version;

    @Nullable
    private transient Table<Integer, YearMonth, Integer> history;
    @Nullable
    private transient Breakdown[] local;
    private transient int localTotal = 0;
    @Nullable
    private transient Breakdown[] team;
    private transient int teamTotal = 0;
    @Nullable
    private transient AssignmentBreakdown[] selfAssigned;
    private transient int selfAssignedTotal = 0;
    @Nullable
    private transient MinistryBreakdown[] subMinistries;
    private transient int subMinistriesTotal = 0;

    public MeasurementDetails(@NonNull final String guid, @NonNull final String ministryId, @NonNull final Mcc mcc,
                              @NonNull final String permLink, @NonNull final YearMonth period) {
        this.guid = guid;
        this.ministryId = ministryId;
        this.mcc = mcc;
        this.permLink = permLink;
        this.period = period;
    }

    @NonNull
    public String getGuid() {
        return guid;
    }

    @NonNull
    public String getMinistryId() {
        return ministryId;
    }

    @NonNull
    public Mcc getMcc() {
        return mcc;
    }

    @NonNull
    public String getPermLink() {
        return permLink;
    }

    public YearMonth getPeriod() {
        return period;
    }

    @Nullable
    public JSONObject getJson() {
        // try parsing raw JSON if we don't have parsed JSON, but we have raw JSON
        if (json == null && rawJson != null) {
            try {
                json = new JSONObject(rawJson);
            } catch (final JSONException e) {
                LOG.error("Error parsing MeasurementDetails JSON", e);
                resetTransients();
            }
        }

        return json;
    }

    @Nullable
    public String getRawJson() {
        if (rawJson == null && json != null) {
            rawJson = json.toString();
        }

        return rawJson;
    }

    public void setJson(@Nullable final String json, final int version) {
        resetTransients();
        this.rawJson = json;
        this.version = version;
    }

    public void setJson(@Nullable final JSONObject json, final int version) {
        resetTransients();
        this.json = json;
        this.version = version;
    }

    public int getVersion() {
        return version;
    }

    @NonNull
    public Table<Integer, YearMonth, Integer> getHistory() {
        if (this.history == null) {
            this.history = parseHistory();
        }

        return this.history;
    }

    @NonNull
    public Breakdown[] getLocalBreakdown() {
        if (this.local == null) {
            this.local = parseLocalBreakdown();
        }

        return this.local;
    }

    public int getLocalBreakdownTotal() {
        return this.localTotal;
    }

    @NonNull
    public Breakdown[] getTeamBreakdown() {
        if (this.team == null) {
            this.team = parseTeamBreakdown();

            int total = 0;
            for (final Breakdown breakdown : this.team) {
                total += breakdown.getValue();
            }
            this.teamTotal = total;
        }

        return this.team;
    }

    public int getTeamBreakdownTotal() {
        return this.teamTotal;
    }

    @NonNull
    public Breakdown[] getSubMinistriesBreakdown() {
        if (this.subMinistries == null) {
            this.subMinistries = parseSubMinistriesBreakdown();

            int total = 0;
            for (final Breakdown breakdown : this.subMinistries) {
                total += breakdown.getValue();
            }
            this.subMinistriesTotal = total;
        }

        return this.subMinistries;
    }

    public int getSubMinistriesBreakdownTotal() {
        return this.subMinistriesTotal;
    }

    @NonNull
    public Breakdown[] getSelfAssignedBreakdown() {
        if (this.selfAssigned == null) {
            this.selfAssigned = parseSelfAssignedBreakdown();

            int total = 0;
            for (final Breakdown breakdown : this.selfAssigned) {
                total += breakdown.getValue();
            }
            this.selfAssignedTotal = total;
        }

        return this.selfAssigned;
    }

    public int getSelfAssignedBreakdownTotal() {
        return this.selfAssignedTotal;
    }

    private void resetTransients() {
        this.json = null;
        this.rawJson = null;
        this.version = 0;
        this.history = null;
        this.local = null;
        this.team = null;
        this.selfAssigned = null;
        this.subMinistries = null;

        this.localTotal = 0;
        this.teamTotal = 0;
        this.selfAssignedTotal = 0;
        this.subMinistriesTotal = 0;
    }

    @NonNull
    private Table<Integer, YearMonth, Integer> parseHistory() {
        final ImmutableTable.Builder<Integer, YearMonth, Integer> table = ImmutableTable.builder();
        table.orderColumnsBy(YearMonthComparator.INSTANCE);

        final JSONObject json = getJson();
        if (json != null) {
            if (this.version >= V4) {
                for (final int type : new int[] {TYPE_LOCAL, TYPE_PERSONAL, TYPE_TOTAL}) {
                    // extract the history for the current measurement type
                    final JSONObject history;
                    switch (type) {
                        case TYPE_LOCAL:
                            history = json.optJSONObject(JSON_HISTORY_LOCAL);
                            break;
                        case TYPE_PERSONAL:
                            history = json.optJSONObject(JSON_HISTORY_PERSONAL);
                            break;
                        case TYPE_TOTAL:
                            history = json.optJSONObject(JSON_HISTORY_TOTAL);
                            break;
                        default:
                            history = null;
                            break;
                    }

                    // if we have history, parse it into the table
                    if (history != null) {
                        final Iterator<String> periods = history.keys();
                        while (periods.hasNext()) {
                            try {
                                final String period = periods.next();
                                table.put(type, YearMonth.parse(period), history.optInt(period, 0));
                            } catch (final Exception e) {
                                LOG.debug("Error processing MeasurementDetails history", e);
                            }
                        }
                    }
                }
            }
        }

        return table.build();
    }

    private SimpleBreakdown[] parseLocalBreakdown() {
        final JSONObject json = getJson();
        if (json != null) {
            final JSONObject localJson = json.optJSONObject(JSON_BREAKDOWN_LOCAL);
            if (localJson != null) {
                final List<SimpleBreakdown> data = new ArrayList<>();
                final Iterator<String> keys = localJson.keys();
                while (keys.hasNext()) {
                    final String key = keys.next();
                    final int value = localJson.optInt(key, 0);
                    switch (key) {
                        case MEASUREMENTS_SOURCE:
                            data.add(new SimpleBreakdown("Local", value));
                            break;
                        case "total":
                            this.localTotal = value;
                            break;
                        default:
                            data.add(new SimpleBreakdown(key, value));
                            break;
                    }
                }
                return data.toArray(new SimpleBreakdown[data.size()]);
            }
        }

        return new SimpleBreakdown[0];
    }

    @NonNull
    private Breakdown[] parseTeamBreakdown() {
        final Breakdown[] raw;
        final JSONObject json = getJson();
        final int me;
        if (json != null) {
            raw = parseAssignmentBreakdown(json.optJSONArray(JSON_BREAKDOWN_TEAM));
            final JSONObject selfJson = json.optJSONObject(JSON_BREAKDOWN_SELF);
            if (selfJson != null) {
                me = selfJson.optInt(MEASUREMENTS_SOURCE, 0);
            } else {
                me = 0;
            }
        } else {
            raw = new Breakdown[0];
            me = 0;
        }

        final Breakdown[] team = new Breakdown[raw.length + 1];
        team[0] = new SimpleBreakdown("You", me);
        System.arraycopy(raw, 0, team, 1, raw.length);
        return team;
    }

    @NonNull
    private AssignmentBreakdown[] parseSelfAssignedBreakdown() {
        final JSONObject json = getJson();
        if (json != null) {
            return parseAssignmentBreakdown(json.optJSONArray(JSON_BREAKDOWN_SELF_ASSIGNED));
        }

        return new AssignmentBreakdown[0];
    }

    @NonNull
    private AssignmentBreakdown[] parseAssignmentBreakdown(@Nullable final JSONArray json) {
        if (json != null) {
            // parse all the provided assignment breakdowns
            final AssignmentBreakdown[] breakdowns = new AssignmentBreakdown[json.length()];
            for (int i = 0; i < breakdowns.length; i++) {
                breakdowns[i] = new AssignmentBreakdown(json.optJSONObject(i));
            }
            return breakdowns;
        }

        return new AssignmentBreakdown[0];
    }

    @NonNull
    private MinistryBreakdown[] parseSubMinistriesBreakdown() {
        final JSONObject json = getJson();
        if (json != null) {
            final JSONArray ministriesJson = json.optJSONArray(JSON_BREAKDOWN_SUB_MINISTRIES);
            if (ministriesJson != null) {
                // parse all the provided ministry breakdowns
                final MinistryBreakdown[] breakdowns = new MinistryBreakdown[ministriesJson.length()];
                for (int i = 0; i < breakdowns.length; i++) {
                    breakdowns[i] = new MinistryBreakdown(ministriesJson.optJSONObject(i));
                }
                return breakdowns;
            }
        }

        return new MinistryBreakdown[0];
    }

    public interface Breakdown {
        @Nullable
        Object getId();

        @Nullable
        String getName();

        int getValue();
    }

    static final class AssignmentBreakdown implements Breakdown {
        private static final Joiner JOINER_NAME = Joiner.on(", ").skipNulls();

        private static final String JSON_LAST_NAME = "last_name";
        private static final String JSON_FIRST_NAME = "first_name";
        private static final String JSON_ID = "assignment_id";
        private static final String JSON_VALUE = "total";

        @Nullable
        private final String id;
        @Nullable
        private final String firstName;
        @Nullable
        private final String lastName;
        private final int value;

        AssignmentBreakdown(@Nullable final JSONObject json) {
            if (json != null) {
                this.id = json.optString(JSON_ID, null);
                this.firstName = json.optString(JSON_FIRST_NAME, null);
                this.lastName = json.optString(JSON_LAST_NAME, null);
                this.value = json.optInt(JSON_VALUE, 0);
            } else {
                this.id = null;
                this.firstName = null;
                this.lastName = null;
                this.value = 0;
            }
        }

        @Nullable
        @Override
        public String getId() {
            return id;
        }

        @NonNull
        @Override
        public String getName() {
            return JOINER_NAME.join(lastName, firstName);
        }

        @Override
        public int getValue() {
            return this.value;
        }
    }

    static final class MinistryBreakdown implements Breakdown {
        private static final String JSON_NAME = "name";
        private static final String JSON_ID = "ministry_id";
        private static final String JSON_VALUE = "total";

        @Nullable
        private final String id;
        @Nullable
        private final String name;
        private final int value;

        public MinistryBreakdown(@Nullable final JSONObject json) {
            if (json != null) {
                id = json.optString(JSON_ID, null);
                name = json.optString(JSON_NAME, null);
                value = json.optInt(JSON_VALUE, 0);
            } else {
                id = null;
                name = null;
                value = 0;
            }
        }

        @Nullable
        @Override
        public Object getId() {
            return id;
        }

        @Nullable
        @Override
        public String getName() {
            return name;
        }

        @Override
        public int getValue() {
            return value;
        }
    }

    static final class SimpleBreakdown implements Breakdown {
        @NonNull
        private final String name;
        private final int value;

        public SimpleBreakdown(@NonNull final String name, final int value) {
            this.name = name;
            this.value = value;
        }

        @NonNull
        @Override
        public Object getId() {
            return name;
        }

        @NonNull
        @Override
        public String getName() {
            return name;
        }

        @Override
        public int getValue() {
            return value;
        }
    }

    static final class YearMonthComparator implements Comparator<YearMonth> {
        static final YearMonthComparator INSTANCE = new YearMonthComparator();

        @Override
        public int compare(final YearMonth lhs, final YearMonth rhs) {
            return lhs.compareTo(rhs);
        }
    }
}
