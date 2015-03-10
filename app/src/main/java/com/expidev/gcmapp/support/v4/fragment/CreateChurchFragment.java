package com.expidev.gcmapp.support.v4.fragment;

import static com.expidev.gcmapp.Constants.ARG_MINISTRY_ID;
import static com.expidev.gcmapp.utils.BroadcastUtils.updateChurchesBroadcast;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.TextView;

import com.expidev.gcmapp.R;
import com.expidev.gcmapp.db.GmaDao;
import com.expidev.gcmapp.model.Church;
import com.expidev.gcmapp.model.Church.Development;
import com.expidev.gcmapp.model.Ministry;
import com.expidev.gcmapp.service.GmaSyncService;
import com.google.android.gms.maps.model.LatLng;

import java.security.SecureRandom;

import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.Optional;

public class CreateChurchFragment extends BaseEditChurchDialogFragment {
    private static String ARG_LOCATION = CreateChurchFragment.class.getName() + ".ARG_LOCATION";

    private static final SecureRandom RAND = new SecureRandom();

    @NonNull
    private String mMinistryId = Ministry.INVALID_ID;
    @Nullable
    private LatLng mLocation;

    @Optional
    @Nullable
    @InjectView(R.id.save)
    TextView mSaveView;

    public static CreateChurchFragment newInstance(@NonNull final String ministryId, @NonNull final LatLng location) {
        final CreateChurchFragment fragment = new CreateChurchFragment();

        final Bundle args = new Bundle();
        args.putString(ARG_MINISTRY_ID, ministryId);
        args.putParcelable(ARG_LOCATION, location);
        fragment.setArguments(args);

        return fragment;
    }

    /* BEGIN lifecycle */

    @Override
    public void onCreate(final Bundle savedState) {
        super.onCreate(savedState);

        final Bundle args = this.getArguments();
        mMinistryId = args != null ? args.getString(ARG_MINISTRY_ID) : Ministry.INVALID_ID;
        mLocation = args != null ? args.<LatLng>getParcelable(ARG_LOCATION) : null;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateTitle("Create New Church");
        setupViews();
    }

    @Optional
    @OnClick(R.id.save)
    void onSaveChurch() {
        // create new church object
        final Church church = new Church();
        church.setNew(true);
        church.setMinistryId(mMinistryId);
        if (mLocation != null) {
            church.setLatitude(mLocation.latitude);
            church.setLongitude(mLocation.longitude);
        }
        if (mNameView != null) {
            church.setName(mNameView.getText().toString());
        }
        if (mContactNameView != null) {
            church.setContactName(mContactNameView.getText().toString());
        }
        if (mContactEmailView != null) {
            church.setContactEmail(mContactEmailView.getText().toString());
        }
        if (mDevelopmentSpinner != null) {
            final Object development = mDevelopmentSpinner.getSelectedItem();
            if (development instanceof Development) {
                church.setDevelopment((Development) development);
            }
        }
        if (mSizeView != null) {
            try {
                church.setSize(Integer.parseInt(mSizeView.getText().toString()));
            } catch (final NumberFormatException ignored) {
            }
        }

        // save new church
        final Context context = getActivity().getApplicationContext();
        final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context);
        final GmaDao dao = GmaDao.getInstance(context);

        dao.async(new Runnable() {
            @Override
            public void run() {
                boolean saved = false;
                while (!saved) {
                    // generate a new id
                    long id = 0;
                    while (id < Integer.MAX_VALUE) {
                        id = RAND.nextLong();
                    }
                    church.setId(id);

                    // update in the database
                    try {
                        dao.insert(church, SQLiteDatabase.CONFLICT_ROLLBACK);
                        saved = true;
                    } catch (final SQLException e) {
                        Log.e("CreateChurch", "insert error", e);
                    }
                }

                // broadcast that this church was created
                broadcastManager.sendBroadcast(updateChurchesBroadcast(church.getMinistryId(), church.getId()));

                // trigger a sync of dirty churches
                GmaSyncService.syncDirtyChurches(context);
            }
        });

        // dismiss the dialog
        this.dismiss();
    }

    /* END lifecycle */

    private void setupViews() {
        if(mSaveView != null) {
            mSaveView.setText(R.string.button_church_create);
        }
    }
}
