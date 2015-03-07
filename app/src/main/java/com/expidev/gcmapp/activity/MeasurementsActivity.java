package com.expidev.gcmapp.activity;

import static com.expidev.gcmapp.Constants.EXTRA_GUID;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;

import com.expidev.gcmapp.R;
import com.expidev.gcmapp.support.v4.fragment.measurement.ColumnsListFragment;

public class MeasurementsActivity extends ActionBarActivity {
    private String mGuid;

    public static void start(@NonNull final Context context, @NonNull final String guid) {
        final Intent intent = new Intent(context, MeasurementsActivity.class);
        intent.putExtra(EXTRA_GUID, guid);
        context.startActivity(intent);
    }


    /* BEGIN lifecycle */

    @Override
    protected void onCreate(@Nullable final Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.activity_measurements_frags);

        final Intent intent = this.getIntent();
        mGuid = intent.getStringExtra(EXTRA_GUID);

        loadMeasurementColumnsFragment();
    }

    /* END lifecycle */

    private void loadMeasurementColumnsFragment() {
        final ColumnsListFragment fragment = ColumnsListFragment.newInstance(mGuid);
        this.getSupportFragmentManager().beginTransaction().replace(R.id.frame_content, fragment).commit();
    }
}
