package com.expidev.gcmapp.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.expidev.gcmapp.BuildConfig;
import com.expidev.gcmapp.db.MinistriesDao;
import com.expidev.gcmapp.http.GmaApiClient;
import com.expidev.gcmapp.json.AssignmentsJsonParser;
import com.expidev.gcmapp.model.Assignment;

import org.ccci.gto.android.common.api.ApiException;
import org.json.JSONException;
import org.json.JSONObject;

import me.thekey.android.TheKey;
import me.thekey.android.TheKeySocketException;
import me.thekey.android.lib.TheKeyImpl;

public class CreateAssignmentTask extends AsyncTask<Void, Void, Assignment> {
    private final GmaApiClient mApi;
    protected final MinistriesDao mDao;
    private final TheKey mTheKey;

    @Nullable
    private final String mEmail;
    @NonNull
    private final String mMinistryId;
    private final Assignment.Role mRole;

    public CreateAssignmentTask(@NonNull final Context context, @NonNull final String ministryId,
                                @NonNull final Assignment.Role role) {
        this(context, null, ministryId, role);
    }

    public CreateAssignmentTask(@NonNull final Context context, @Nullable final String email,
                                @NonNull final String ministryId, @NonNull final Assignment.Role role) {
        mApi = GmaApiClient.getInstance(context);
        mDao = MinistriesDao.getInstance(context);
        mTheKey = TheKeyImpl.getInstance(context, BuildConfig.THEKEY_CLIENTID);
        mEmail = email;
        mMinistryId = ministryId;
        mRole = role;
    }

    @Override
    protected Assignment doInBackground(final Void... params) {
        // use current user's email if one is not specified
        String email = mEmail;
        if (email == null) {
            // try loading the current user's email
            TheKey.Attributes attrs = mTheKey.getAttributes();
            if (attrs.getEmail() == null) {
                try {
                    if (mTheKey.loadAttributes()) {
                        attrs = mTheKey.getAttributes();
                    }
                } catch (final TheKeySocketException ignored) {
                }
            }
            email = attrs.getEmail();
        }

        // only create the assignment if we have a valid email
        if (email != null) {
            try {
                final JSONObject json = mApi.createAssignment(email, mMinistryId, mRole);
                if (json != null) {
                    // save created assignment
                    final Assignment assignment = AssignmentsJsonParser.parseAssignment(json);
                    mDao.updateOrInsertAssignment(assignment);

                    // return assignment
                    return assignment;
                }
            } catch (final ApiException | JSONException ignored) {
            }
        }

        // something failed, return null
        return null;
    }
}
