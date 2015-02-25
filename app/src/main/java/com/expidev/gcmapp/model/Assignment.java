package com.expidev.gcmapp.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;

/**
 * Created by William.Randall on 1/23/2015.
 */
public class Assignment extends Base implements Serializable {
    private static final long serialVersionUID = 0L;

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

    private String id;
    @NonNull
    private Role role = Role.UNKNOWN;
    @NonNull
    private String ministryId = Ministry.INVALID_ID;
    private AssociatedMinistry ministry;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
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

    public AssociatedMinistry getMinistry()
    {
        return ministry;
    }

    public void setMinistry(AssociatedMinistry ministry)
    {
        this.ministry = ministry;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("id: " + id + ", ");
        return sb.toString();
    }
}
