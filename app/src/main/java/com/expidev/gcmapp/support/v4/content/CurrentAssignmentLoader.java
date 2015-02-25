package com.expidev.gcmapp.support.v4.content;

import android.content.Context;
import android.support.annotation.NonNull;

import com.expidev.gcmapp.db.Contract;
import com.expidev.gcmapp.db.MinistriesDao;
import com.expidev.gcmapp.model.Assignment;
import com.expidev.gcmapp.utils.BroadcastUtils;

import org.ccci.gto.android.common.support.v4.content.AsyncTaskBroadcastReceiverSharedPreferencesChangeLoader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.expidev.gcmapp.Constants.PREFS_SETTINGS;
import static com.expidev.gcmapp.Constants.PREF_CURRENT_ASSIGNMENT;
import static com.expidev.gcmapp.Constants.PREF_CURRENT_MINISTRY;

/**
 * Created by William.Randall on 2/25/2015.
 */
public class CurrentAssignmentLoader extends AsyncTaskBroadcastReceiverSharedPreferencesChangeLoader<Assignment>
{
    private final MinistriesDao ministriesDao;

    public CurrentAssignmentLoader(@NonNull final Context context)
    {
        super(context, context.getSharedPreferences(PREFS_SETTINGS, Context.MODE_PRIVATE));
        this.addIntentFilter(BroadcastUtils.updateAssignmentsFilter());
        this.addPreferenceKey(PREF_CURRENT_MINISTRY);
        ministriesDao = MinistriesDao.getInstance(context);
    }

    @Override
    public Assignment loadInBackground()
    {
        // Load from the chosen ministry, if there is one
        String ministryId = mPrefs.getString(PREF_CURRENT_MINISTRY, null);

        if(ministryId != null)
        {
            return loadAssignmentFromMinistryId(ministryId);
        }
        else
        {
            // load the current assignment
            final String assignmentId = mPrefs.getString(PREF_CURRENT_ASSIGNMENT, null);

            final Assignment currentAssignment = assignmentId != null
                ? ministriesDao.find(Assignment.class, assignmentId)
                : null;

            if(currentAssignment != null)
            {
                return currentAssignment;
            }

            // if no current assignment is set, retrieve a default
            return initCurrentAssignment();
        }
    }

    private Assignment loadAssignmentFromMinistryId(String ministryId)
    {
        List<Assignment> allAssignments = ministriesDao.get(
            Assignment.class,
            Contract.Assignment.SQL_WHERE_MINISTRY,
            new String[] { ministryId });

        switch(allAssignments.size())
        {
            case 0:
                return null;
            case 1:
                return allAssignments.get(0);
            default:
                return getAssignmentBasedOnRole(allAssignments);
        }
    }

    private Assignment initCurrentAssignment()
    {
        List<Assignment> allAssignments;

        final String currentMinistry = mPrefs.getString(PREF_CURRENT_MINISTRY, null);

        if(currentMinistry != null)
        {
            allAssignments = ministriesDao.get(
                Assignment.class,
                Contract.Assignment.SQL_WHERE_MINISTRY,
                new String[] { currentMinistry });
        }
        else
        {
            allAssignments = ministriesDao.get(Assignment.class);
        }

        switch(allAssignments.size())
        {
            case 0:
                return null;
            case 1:
                return allAssignments.get(0);
            default:
                for(Assignment assignment : allAssignments)
                {
                    // if there is a current ministry already selected, use the assignment associated with that
                    if(currentMinistry != null && assignment.getMinistryId() != null)
                    {
                        if(assignment.getMinistryId().equals(currentMinistry))
                        {
                            return assignment;
                        }
                    }
                }

                return getAssignmentBasedOnRole(allAssignments);
        }
    }

    private Assignment getAssignmentBasedOnRole(List<Assignment> allAssignments)
    {
        Map<String, Assignment.Role> assignmentRoleMapping = new HashMap<>();
        for(Assignment assignment : allAssignments)
        {
            assignmentRoleMapping.put(assignment.getId(), assignment.getRole());
        }

        if(assignmentRoleMapping.containsValue(Assignment.Role.LEADER))
        {
            return getAssignmentFromMap(allAssignments, assignmentRoleMapping, Assignment.Role.LEADER);
        }
        else if(assignmentRoleMapping.containsValue(Assignment.Role.INHERITED_LEADER))
        {
            return getAssignmentFromMap(allAssignments, assignmentRoleMapping, Assignment.Role.INHERITED_LEADER);
        }
        else if(assignmentRoleMapping.containsValue(Assignment.Role.MEMBER))
        {
            return getAssignmentFromMap(allAssignments, assignmentRoleMapping, Assignment.Role.MEMBER);
        }
        else if(assignmentRoleMapping.containsValue(Assignment.Role.SELF_ASSIGNED))
        {
            return getAssignmentFromMap(allAssignments, assignmentRoleMapping, Assignment.Role.SELF_ASSIGNED);
        }
        else
        {
            return getAssignmentFromMap(allAssignments, assignmentRoleMapping, Assignment.Role.BLOCKED);
        }
    }

    private Assignment getAssignmentFromMap(
        List<Assignment> allAssignments,
        Map<String, Assignment.Role> assignmentRoleMapping,
        Assignment.Role role)
    {
        for(Map.Entry<String, Assignment.Role> entry : assignmentRoleMapping.entrySet())
        {
            if(entry.getValue() == role)
            {
                return getAssignmentFromList(allAssignments, entry.getKey());
            }
        }

        return null;
    }

    private Assignment getAssignmentFromList(List<Assignment> assignments, String assignmentId)
    {
        for(Assignment assignment : assignments)
        {
            if(assignment.getId().equals(assignmentId))
            {
                return assignment;
            }
        }

        return null;
    }
}
