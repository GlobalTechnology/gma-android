package com.expidev.gcmapp.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.expidev.gcmapp.json.MinistryJsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Assignment extends Base implements Serializable {
    private static final long serialVersionUID = 0L;

    public static final String JSON_ID = "id";
    public static final String JSON_MINISTRY_ID = "ministry_id";
    public static final String JSON_ROLE = "team_role";

    private static final String ROLE_LEADER = "leader";
    private static final String ROLE_INHERITED_LEADER = "inherited_leader";
    private static final String ROLE_MEMBER = "member";
    private static final String ROLE_SELF_ASSIGNED = "self_assigned";
    private static final String ROLE_BLOCKED = "blocked";
    public enum Role {
        LEADER(ROLE_LEADER), INHERITED_LEADER(ROLE_INHERITED_LEADER), MEMBER(ROLE_MEMBER),
        SELF_ASSIGNED(ROLE_SELF_ASSIGNED), BLOCKED(ROLE_BLOCKED), UNKNOWN(null);

        @Nullable
        public final String raw;

        private Role(final String raw) {
            this.raw = raw;
        }

        @NonNull
        public static Role fromRaw(@Nullable final String raw) {
            if (raw != null) {
                switch (raw) {
                    case ROLE_LEADER:
                        return LEADER;
                    case ROLE_INHERITED_LEADER:
                        return INHERITED_LEADER;
                    case ROLE_MEMBER:
                        return MEMBER;
                    case ROLE_SELF_ASSIGNED:
                        return SELF_ASSIGNED;
                    case ROLE_BLOCKED:
                        return BLOCKED;
                }
            }

            return UNKNOWN;
        }
    }

    @NonNull
    private String guid;
    @Nullable
    private String id;
    @NonNull
    private Role role = Role.UNKNOWN;
    @NonNull
    private String ministryId = Ministry.INVALID_ID;
    @NonNull
    private Ministry.Mcc mcc = Ministry.Mcc.UNKNOWN;
    @Nullable
    private AssociatedMinistry ministry;

    @NonNull
    public static List<Assignment> listFromJson(@NonNull final JSONArray json) throws JSONException {
        final List<Assignment> assignments = new ArrayList<>();
        for (int i = 0; i < json.length(); i++) {
            assignments.add(fromJson(json.getJSONObject(i)));
        }
        return assignments;
    }

    @NonNull
    public static Assignment fromJson(@NonNull final JSONObject json) throws JSONException {
        final Assignment assignment = new Assignment();
        assignment.id = json.optString(JSON_ID);
        assignment.ministryId = json.getString(JSON_MINISTRY_ID);
        assignment.role = Role.fromRaw(json.optString(JSON_ROLE));

        // parse the embedded ministry
        final AssociatedMinistry ministry = MinistryJsonParser.parseAssociatedMinistry(json);
        assignment.setMinistry(ministry);

        return assignment;
    }

    @NonNull
    public String getGuid() {
        return guid;
    }

    public void setGuid(@NonNull String guid) {
        this.guid = guid;
    }

    @Nullable
    public String getId()
    {
        return id;
    }

    public void setId(@Nullable final String id) {
        this.id = id;
    }

    @NonNull
    public Role getRole() {
        return role;
    }

    public void setRole(@Nullable final String role) {
        this.role = Role.fromRaw(role);
    }

    public void setRole(@NonNull final Role role) {
        this.role = role;
    }

    @NonNull
    public String getMinistryId() {
        return ministryId;
    }

    public void setMinistryId(@NonNull final String ministryId) {
        this.ministryId = ministryId;
    }

    @NonNull
    public Ministry.Mcc getMcc() {
        return mcc;
    }

    public void setMcc(@Nullable final Ministry.Mcc mcc) {
        this.mcc = mcc != null ? mcc : Ministry.Mcc.UNKNOWN;
    }

    public AssociatedMinistry getMinistry()
    {
        return ministry;
    }

    public void setMinistry(AssociatedMinistry ministry)
    {
        this.ministry = ministry;
    }

    public JSONObject toJson() throws JSONException {
        final JSONObject json = new JSONObject();
        json.put(JSON_ROLE, this.role.raw);
        json.put(JSON_MINISTRY_ID, this.ministryId);
        return json;
    }

    @Override
    public String toString()
    {
        return "id: " + id;
    }
}
