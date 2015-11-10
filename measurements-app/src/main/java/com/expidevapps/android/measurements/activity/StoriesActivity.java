package com.expidevapps.android.measurements.activity;

import static com.expidevapps.android.measurements.Constants.EXTRA_GUID;
import static com.expidevapps.android.measurements.Constants.EXTRA_MINISTRY_ID;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.expidevapps.android.measurements.R;
import com.expidevapps.android.measurements.model.Ministry;
import com.expidevapps.android.measurements.support.v4.fragment.CreateStoryDialogFragment;
import com.expidevapps.android.measurements.support.v4.fragment.StoriesFragment;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;

public class StoriesActivity extends AppCompatActivity {
    private static final String TAG_FRAGMENT_STORIES = "stories";
    private static final String TAG_FRAGMENT_CREATE_STORY = "createStory";

    @NonNull
    private /* final */ String mGuid;
    @NonNull
    private /* final */ String mMinistryId = Ministry.INVALID_ID;

    private boolean mPaused = false;

    public static void start(@NonNull final Context context, @NonNull final String guid,
                             @NonNull final String ministryId) {
        final Intent intent = new Intent(context, StoriesActivity.class);
        populateIntent(intent, guid, ministryId);
        context.startActivity(intent);
    }

    public static void populateIntent(@NonNull final Intent intent, @NonNull final String guid,
                                      @NonNull final String ministryId) {
        intent.putExtra(EXTRA_GUID, guid);
        intent.putExtra(EXTRA_MINISTRY_ID, ministryId);
    }

    /* BEGIN lifecycle */

    @Override
    protected void onCreate(final Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.activity_stories);
        ButterKnife.inject(this);

        // get intent extras
        final Intent intent = getIntent();
        mGuid = intent.getStringExtra(EXTRA_GUID);
        mMinistryId = intent.getStringExtra(EXTRA_MINISTRY_ID);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPaused = false;
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        // we create the fragment here to guarantee state has been restored
        // see: http://www.androiddesignpatterns.com/2013/08/fragment-transaction-commit-state-loss.html
        createStoriesFragmentIfNeeded();
    }

    @Optional
    @OnClick(R.id.action_create)
    void onCreateStory() {
        final FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentByTag(TAG_FRAGMENT_CREATE_STORY) == null) {
            final CreateStoryDialogFragment fragment = CreateStoryDialogFragment.newInstance(mGuid, mMinistryId);
            fragment.show(fm.beginTransaction().addToBackStack(null), TAG_FRAGMENT_CREATE_STORY);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPaused = true;
    }

    /* END lifecycle */

    private void createStoriesFragmentIfNeeded() {
        // short-circuit if we are currently paused to prevent state loss
        if (mPaused) {
            // fragment state will be updated the next time we resume
            return;
        }

        // check for the current fragment
        final FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentByTag(TAG_FRAGMENT_STORIES) instanceof StoriesFragment) {
            // no need to create the fragment
            return;
        }

        // create a new StoriesFragment
        // XXX: we use commitAllowingStateLoss because we already prevent state loss by checking mPaused
        final StoriesFragment fragment = StoriesFragment.newInstance(mGuid, mMinistryId);
        fm.beginTransaction().replace(R.id.frame_content, fragment, TAG_FRAGMENT_STORIES)
                .commitAllowingStateLoss();
    }
}
