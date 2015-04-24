package com.expidev.gcmapp;

import static com.expidev.gcmapp.Constants.EXTRA_GUID;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;

import com.expidev.gcmapp.support.v4.fragment.FindMinistryFragment;
import com.expidev.gcmapp.support.v4.fragment.JoinMinistryDialogFragment;

public class JoinMinistryActivity extends ActionBarActivity
        implements JoinMinistryDialogFragment.OnJoinMinistryListener {
    private static final String TAG_FIND_MINISTRY = "findMinistry";

    @NonNull
    private String mGuid = "";

    public static void start(@NonNull final Context context, @NonNull final String guid) {
        final Intent intent = new Intent(context, JoinMinistryActivity.class);
        intent.putExtra(EXTRA_GUID, guid);
        context.startActivity(intent);
    }

    /* BEGIN lifecycle */

    @Override
    public void onCreate(@Nullable final Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.activity_join_ministry);

        final Intent intent = this.getIntent();
        mGuid = intent.getStringExtra(EXTRA_GUID);

        loadFindMinistryFragment(false);
    }

    @Override
    public void onJoinedMinistry(@NonNull final String ministryId) {
        finish();
    }

    /* END lifecycle */

    private void loadFindMinistryFragment(final boolean force) {
        // only load the findMinistry fragment if it's not currently loaded
        final FragmentManager fm = getSupportFragmentManager();
        if (force || fm.findFragmentByTag(TAG_FIND_MINISTRY) == null) {
            fm.beginTransaction().replace(R.id.findMinistry, FindMinistryFragment.newInstance(mGuid), TAG_FIND_MINISTRY)
                    .commit();
        }
    }
}
