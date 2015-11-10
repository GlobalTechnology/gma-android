package com.expidevapps.android.measurements.support.v4.fragment;

import static com.expidevapps.android.measurements.Constants.ARG_GUID;
import static com.expidevapps.android.measurements.Constants.ARG_MINISTRY_ID;
import static com.expidevapps.android.measurements.Constants.INVALID_GUID;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.widget.CheckBox;
import android.widget.TextView;

import com.expidevapps.android.measurements.R;
import com.expidevapps.android.measurements.db.GmaDao;
import com.expidevapps.android.measurements.model.Ministry;
import com.expidevapps.android.measurements.model.Story;
import com.expidevapps.android.measurements.model.Story.Privacy;
import com.expidevapps.android.measurements.model.Story.State;
import com.expidevapps.android.measurements.sync.BroadcastUtils;
import com.expidevapps.android.measurements.sync.GmaSyncService;

import org.ccci.gto.android.common.util.AsyncTaskCompat;
import org.ccci.gto.android.common.util.BundleCompat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;

import butterknife.InjectView;
import butterknife.Optional;

public class CreateStoryDialogFragment extends BaseEditStoryDialogFragment {
    private static final Logger LOG = LoggerFactory.getLogger(CreateStoryDialogFragment.class);
    @SuppressLint("TrulyRandom")
    private static final SecureRandom RAND = new SecureRandom();

    @Nullable
    @Optional
    @InjectView(R.id.title)
    TextView mTitle;
    @Nullable
    @Optional
    @InjectView(R.id.content)
    TextView mContent;
    @Nullable
    @Optional
    @InjectView(R.id.privacy)
    CheckBox mPrivacy;
    @Nullable
    @Optional
    @InjectView(R.id.published)
    CheckBox mPublished;

    @NonNull
    private /*final*/ String mGuid = INVALID_GUID;
    @NonNull
    private /*final*/ String mMinistryId = Ministry.INVALID_ID;

    public static Bundle buildArgs(@NonNull final String guid, @NonNull final String ministryId) {
        final Bundle args = new Bundle();
        args.putString(ARG_GUID, guid);
        args.putString(ARG_MINISTRY_ID, ministryId);
        return args;
    }

    public static CreateStoryDialogFragment newInstance(@NonNull final String guid, @NonNull final String ministryId) {
        final CreateStoryDialogFragment fragment = new CreateStoryDialogFragment();
        fragment.setArguments(buildArgs(guid, ministryId));
        return fragment;
    }

    /* BEGIN lifecycle */

    @Override
    public void onCreate(@Nullable final Bundle savedState) {
        super.onCreate(savedState);

        final Bundle args = this.getArguments();
        if (args != null) {
            mGuid = BundleCompat.getString(args, ARG_GUID, mGuid);
            mMinistryId = BundleCompat.getString(args, ARG_MINISTRY_ID, mMinistryId);
        }
    }

    @NonNull
    @Override
    protected AlertDialog.Builder onCreateDialogBuilder(final Bundle savedState) {
        final AlertDialog.Builder builder = super.onCreateDialogBuilder(savedState);
        builder.setTitle(R.string.title_dialog_stories_create);
        builder.setView(R.layout.fragment_edit_story);
        builder.setPositiveButton(R.string.btn_stories_create, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                onCreateStory();
            }
        });
        return builder;
    }

    void onCreateStory() {
        final Context context = getActivity().getApplicationContext();

        final Story story = new Story();
        story.setNew(true);
        story.setMinistryId(mMinistryId);
        story.setTitle(mTitle != null ? mTitle.getText().toString() : "");
        story.setContent(mContent != null ? mContent.getText().toString() : "");
        story.setPendingImage(mImage);
        story.setPrivacy(mPrivacy != null && !mPrivacy.isChecked() ? Privacy.TEAM : Privacy.PUBLIC);
        story.setState(mPublished != null && mPublished.isChecked() ? State.PUBLISHED : State.DRAFT);

        AsyncTaskCompat.execute(new Runnable() {
            @Override
            public void run() {
                final GmaDao dao = GmaDao.getInstance(context);
                while (true) {
                    story.setId(RAND.nextLong());
                    try {
                        dao.insert(story);
                        break;
                    } catch (final SQLiteConstraintException e) {
                        LOG.debug("Constraint conflict, let's retry", e);
                    }
                }

                // broadcast that we created (updated) a story
                LocalBroadcastManager.getInstance(context)
                        .sendBroadcast(BroadcastUtils.updateStoriesBroadcast(story.getId()));

                // trigger a backend sync of this story to the API
                GmaSyncService.syncDirtyStories(context, mGuid);
            }
        });
    }

    /* END lifecycle */
}
