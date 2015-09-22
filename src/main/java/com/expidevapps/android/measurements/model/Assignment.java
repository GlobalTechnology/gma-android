package com.expidevapps.android.measurements.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Objects;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Assignment extends Base implements Cloneable {
    private static final String JSON_ID = "id";
    private static final String JSON_MINISTRY_ID = "ministry_id";
    private static final String JSON_ROLE = "team_role";
    private static final String JSON_SUB_ASSIGNMENTS = "sub_ministries";

    private static final String ROLE_ADMIN = "admin";
    private static final String ROLE_INHERITED_ADMIN = "inherited_admin";
    private static final String ROLE_LEADER = "leader";
    private static final String ROLE_INHERITED_LEADER = "inherited_leader";
    private static final String ROLE_MEMBER = "member";
    private static final String ROLE_SELF_ASSIGNED = "self_assigned";
    private static final String ROLE_BLOCKED = "blocked";
    private static final String ROLE_FORMER_MEMBER = "former_member";
    public enum Role {
        ADMIN(ROLE_ADMIN), INHERITED_ADMIN(ROLE_INHERITED_ADMIN), LEADER(ROLE_LEADER),
        INHERITED_LEADER(ROLE_INHERITED_LEADER), MEMBER(ROLE_MEMBER), SELF_ASSIGNED(ROLE_SELF_ASSIGNED),
        BLOCKED(ROLE_BLOCKED), FORMER_MEMBER(ROLE_FORMER_MEMBER), UNKNOWN(null);

        @Nullable
        public final String raw;

        Role(final String raw) {
            this.raw = raw;
        }

        @NonNull
        public static Role fromRaw(@Nullable final String raw) {
            if (raw != null) {
                switch (raw.toLowerCase(Locale.US)) {
                    case ROLE_ADMIN:
                        return ADMIN;
                    case ROLE_INHERITED_ADMIN:
                        return INHERITED_ADMIN;
                    case ROLE_LEADER:
                        return LEADER;
                    case ROLE_INHERITED_LEADER:
                        return INHERITED_LEADER;
                    case ROLE_MEMBER:
                        return MEMBER;
                    case ROLE_SELF_ASSIGNED:
                        return SELF_ASSIGNED;
                    case ROLE_FORMER_MEMBER:
                        return FORMER_MEMBER;
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
    @Nullable
    private String mPersonId;
    @NonNull
    private Role role = Role.UNKNOWN;
    @NonNull
    private Ministry.Mcc mcc = Ministry.Mcc.UNKNOWN;
    @Nullable
    private Ministry ministry;

    @NonNull
    private final List<Assignment> subAssignments = new ArrayList<>();

    @NonNull
    public static List<Assignment> listFromJson(@NonNull final JSONArray json, @NonNull final String guid,
                                                @Nullable final String personId) throws JSONException {
        final List<Assignment> assignments = new ArrayList<>();
        for (int i = 0; i < json.length(); i++) {
            assignments.add(fromJson(json.getJSONObject(i), guid, personId));
        }
        return assignments;
    }

    @NonNull
    public static Assignment fromJson(@NonNull final JSONObject json, @NonNull final String guid,
                                      @Nullable final String personId) throws JSONException {
        final Assignment assignment = new Assignment(guid, json.getString(JSON_MINISTRY_ID));
        assignment.mPersonId = personId;
        assignment.id = json.optString(JSON_ID);
        assignment.role = Role.fromRaw(json.optString(JSON_ROLE));

        // parse any inherited assignments
        final JSONArray subAssignments = json.optJSONArray(JSON_SUB_ASSIGNMENTS);
        if (subAssignments != null) {
            assignment.subAssignments.addAll(listFromJson(subAssignments, guid, personId));
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
        mPersonId = assignment.mPersonId;
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

    @Nullable
    public String getPersonId() {
        return mPersonId;
    }

    public void setPersonId(@Nullable final String personId) {
        mPersonId = personId;
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
    @SuppressWarnings("CloneDoesntCallSuperClone")
    public Assignment clone() {
        return new Assignment(this);
    }

    @Override
    public String toString()
    {
        return "id: " + id;
    }

    private boolean isAdmin() {
        return getRole() == Role.ADMIN;
    }

    private boolean isInheritedAdmin() {
        return getRole() == Role.INHERITED_ADMIN;
    }

    private boolean isLeader() {
        return getRole() == Role.LEADER;
    }

    private boolean isInheritedLeader() {
        return getRole() == Role.INHERITED_LEADER;
    }

    private boolean isLeadership() {
        return isAdmin() || isInheritedAdmin() || isLeader() || isInheritedLeader();
    }

    private boolean isMember() {
        return getRole() == Role.MEMBER;
    }

    private boolean isSelfAssigned() {
        return getRole() == Role.SELF_ASSIGNED;
    }

    private boolean isFormerMember() {
        return getRole() == Role.FORMER_MEMBER;
    }

    private boolean isBlocked() {
        return getRole() == Role.BLOCKED;
    }

    public boolean can(@NonNull final Task task) {
        switch (task) {
            case CREATE_CHURCH:
                return !(isBlocked() || isFormerMember());
            case EDIT_CHURCH:
            case VIEW_CHURCH:
                throw new UnsupportedOperationException(
                        "You need to specify a church to check VIEW_CHURCH or EDIT_CHURCH permissions");
            case ADMIN_CHURCH:
                return isLeadership();
            case CREATE_TRAINING:
            case VIEW_TRAINING:
                return !(isBlocked() || isFormerMember());
            case EDIT_TRAINING:
                throw new UnsupportedOperationException(
                        "You need to specify a training to check EDIT_TRAINING permissions");
            case UPDATE_PERSONAL_MEASUREMENTS:
                return isLeadership() || isMember() || isSelfAssigned();
            case UPDATE_MINISTRY_MEASUREMENTS:
                return isLeadership();
            default:
                return false;
        }
    }

    public boolean can(@NonNull final Task task, @NonNull final Church church) {
        switch (task) {
            case EDIT_CHURCH:
                return isLeadership() ||
                        ((isMember() || isSelfAssigned()) && Objects.equal(mPersonId, church.getCreatedBy()));
            case VIEW_CHURCH:
                switch (church.getSecurity()) {
                    case PRIVATE:
                        return isMember() || isSelfAssigned() || isLeadership();
                    case REGISTERED_USERS:
                    case PUBLIC:
                        return true;
                    default:
                        return false;
                }
            default:
                return can(task);
        }
    }

    public boolean can(@NonNull final Task task, @NonNull final Training training) {
        switch (task) {
            case EDIT_TRAINING:
                return isLeadership() ||
                        ((isMember() || isSelfAssigned()) && Objects.equal(mPersonId, training.getCreatedBy()));
            default:
                return can(task);
        }
    }
}
