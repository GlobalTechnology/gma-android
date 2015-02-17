package com.expidev.gcmapp.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.expidev.gcmapp.model.measurement.TeamMemberDetails;

/**
 * Created by William.Randall on 2/12/2015.
 */
public class TeamMemberDetailsMapper extends MeasurementDetailsDataMapper<TeamMemberDetails>
{
    @Override
    protected void mapField(
        @NonNull ContentValues values,
        @NonNull String field,
        @NonNull TeamMemberDetails teamMemberDetails)
    {
        switch(field)
        {
            case Contract.TeamMemberDetails.COLUMN_ASSIGNMENT_ID:
                values.put(field, teamMemberDetails.getAssignmentId());
                break;
            case Contract.TeamMemberDetails.COLUMN_TEAM_ROLE:
                values.put(field, teamMemberDetails.getTeamRole());
                break;
            case Contract.TeamMemberDetails.COLUMN_FIRST_NAME:
                values.put(field, teamMemberDetails.getFirstName());
                break;
            case Contract.TeamMemberDetails.COLUMN_LAST_NAME:
                values.put(field, teamMemberDetails.getLastName());
                break;
            case Contract.TeamMemberDetails.COLUMN_PERSON_ID:
                values.put(field, teamMemberDetails.getPersonId());
                break;
            case Contract.TeamMemberDetails.COLUMN_TOTAL:
                values.put(field, teamMemberDetails.getTotal());
                break;
            case Contract.TeamMemberDetails.COLUMN_TYPE:
                values.put(field, teamMemberDetails.getType());
                break;
            default:
                super.mapField(values, field, teamMemberDetails);
                break;

        }
    }

    @NonNull
    @Override
    protected TeamMemberDetails newObject(@NonNull Cursor c)
    {
        return new TeamMemberDetails();
    }

    @NonNull
    @Override
    public TeamMemberDetails toObject(@NonNull Cursor cursor)
    {
        final TeamMemberDetails teamMemberDetails = super.toObject(cursor);

        teamMemberDetails.setAssignmentId(this.getString(cursor, Contract.TeamMemberDetails.COLUMN_ASSIGNMENT_ID, null));
        teamMemberDetails.setTeamRole(this.getString(cursor, Contract.TeamMemberDetails.COLUMN_TEAM_ROLE, null));
        teamMemberDetails.setFirstName(this.getString(cursor, Contract.TeamMemberDetails.COLUMN_FIRST_NAME, null));
        teamMemberDetails.setLastName(this.getString(cursor, Contract.TeamMemberDetails.COLUMN_LAST_NAME, null));
        teamMemberDetails.setPersonId(this.getString(cursor, Contract.TeamMemberDetails.COLUMN_PERSON_ID, null));
        teamMemberDetails.setTotal(this.getInt(cursor, Contract.TeamMemberDetails.COLUMN_TOTAL, 0));
        teamMemberDetails.setType(this.getString(cursor, Contract.TeamMemberDetails.COLUMN_TYPE, null));

        return teamMemberDetails;
    }
}
