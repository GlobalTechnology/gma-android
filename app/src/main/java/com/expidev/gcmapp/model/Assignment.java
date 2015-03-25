package com.expidev.gcmapp.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Assignment extends Base implements Cloneable {
    public static final String JSON_ID = "id";
    public static final String JSON_MINISTRY_ID = "ministry_id";
    public static final String JSON_ROLE = "team_role";
    public static final String JSON_SUB_ASSIGNMENTS = "sub_ministries";

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
                switch (raw.toLowerCase(Locale.US)) {
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
    private final String guid;
    @NonNull
    private final String ministryId;
    @Nullable
    private String id;
    @NonNull
    private Role role = Role.UNKNOWN;
    @NonNull
    private Ministry.Mcc mcc = Ministry.Mcc.UNKNOWN;
    @Nullable
    private Ministry ministry;

    @NonNull
    private final List<Assignment> subAssignments = new ArrayList<>();

    @NonNull
    public static List<Assignment> listFromJson(@NonNull final JSONArray json, @NonNull final String guid) throws JSONException {
        final List<Assignment> assignments = new ArrayList<>();
        for (int i = 0; i < json.length(); i++) {
            assignments.add(fromJson(json.getJSONObject(i), guid));
        }
        return assignments;
    }

    @NonNull
    public static Assignment fromJson(@NonNull final JSONObject json, @NonNull final String guid) throws JSONException {
        final Assignment assignment = new Assignment(guid, json.getString(JSON_MINISTRY_ID));
        assignment.id = json.optString(JSON_ID);
        assignment.role = Role.fromRaw(json.optString(JSON_ROLE));

        // parse any inherited assignments
        final JSONArray subAssignments = json.optJSONArray(JSON_SUB_ASSIGNMENTS);
        if (subAssignments != null) {
            assignment.subAssignments.addAll(listFromJson(subAssignments, guid));
        }

        // parse the merged ministry object
        assignment.setMinistry(Ministry.fromJson(json));

        return assignment;
    }

    public Assignment(@NonNull final String guid, @NonNull final String ministryId) {
        this.guid = guid;
        this.ministryId = ministryId;
    }

    protected Assignment(@NonNull final Assignment assignment) {
        super(assignment);
        this.guid = assignment.guid;
        this.ministryId = assignment.ministryId;
        this.id = assignment.id;
        this.role = assignment.role;
        this.mcc = assignment.mcc;
        for(final Assignment sub : assignment.subAssignments) {
            this.subAssignments.add(sub.clone());
        }

        //TODO: clone ministry
        this.ministry = assignment.ministry;
    }

    @NonNull
    public String getGuid() {
        return guid;
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

    @NonNull
    public Ministry.Mcc getMcc() {
        return mcc;
    }

    public void setMcc(@Nullable final Ministry.Mcc mcc) {
        this.mcc = mcc != null ? mcc : Ministry.Mcc.UNKNOWN;
    }

    public Ministry getMinistry() {
        return ministry;
    }

    public void setMinistry(final Ministry ministry) {
        this.ministry = ministry;
    }

    @NonNull
    public List<Assignment> getSubAssignments() {
        return subAssignments;
    }

    public JSONObject toJson() throws JSONException {
        final JSONObject json = new JSONObject();
        json.put(JSON_ROLE, this.role.raw);
        json.put(JSON_MINISTRY_ID, this.ministryId);
        return json;
    }

    @Override
    public Assignment clone() {
        return new Assignment(this);
    }

    @Override
    public String toString()
    {
        return "id: " + id;
    }

    public boolean isLeader()
    {
        return getRole() == Role.LEADER;
    }

    public boolean isInheritedLeader()
    {
        return getRole() == Role.INHERITED_LEADER;
    }

    public boolean isLeadership()
    {
        return isLeader() || isInheritedLeader();
    }

    public boolean isMember()
    {
        return getRole() == Role.MEMBER;
    }

    public boolean isSelfAssigned()
    {
        return getRole() == Role.SELF_ASSIGNED;
    }

    public boolean isBlocked()
    {
        return getRole() == Role.BLOCKED;
    }

    public boolean can(@NonNull final Task task) {
        switch (task) {
            case CREATE_CHURCH:
            case EDIT_CHURCH:
                return isLeadership();
            case VIEW_CHURCH:
                return true;
            case EDIT_TRAINING:
                return isLeadership();
            case VIEW_TRAINING:
                return !isSelfAssigned() && !isBlocked();
            default:
                return false;
        }
    }

    public boolean can(@NonNull final Task task, @NonNull final Church church) {
        switch (task) {
            case VIEW_CHURCH:
                switch (church.getSecurity()) {
                    case PRIVATE:
                        return isMember() || isLeadership();
                    case LOCAL_PRIVATE:
                        return isMember() || isLeader();
                    case PUBLIC:
                        return true;
                    default:
                        return false;
                }
            default:
                return can(task);
        }
    }
}
