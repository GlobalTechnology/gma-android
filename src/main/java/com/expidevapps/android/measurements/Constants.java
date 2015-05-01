package com.expidevapps.android.measurements;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import butterknife.ButterKnife;

public class Constants {
    /* SharedPreference constants */
    public static final String PREFS_SETTINGS = "gcm_prefs";
    public static final String PREF_CURRENT_MINISTRY = "currentMinistry";

    /* common args */
    public static final String ARG_GUID = "guid";
    public static final String ARG_MINISTRY_ID = "ministry_id";
    public static final String ARG_TRAINING_ID = "training_id";
    public static final String ARG_CHURCH_ID = "church_id";
    public static final String ARG_MCC = "mcc";
    public static final String ARG_PERMLINK = "perm_link";
    public static final String ARG_PERIOD = "period";
    public static final String ARG_TYPE = "type";

    /* common extra's */
    public static final String EXTRA_GUID = "guid";
    public static final String EXTRA_CHURCH_IDS = "church_ids";
    public static final String EXTRA_MINISTRY_ID = "ministry_id";
    public static final String EXTRA_MCC = "mcc";
    public static final String EXTRA_PERIOD = "period";
    public static final String EXTRA_PERMLINK = "perm_link";
    public static final String EXTRA_PERMLINKS = "perm_links";
    public static final String EXTRA_TRAINING_IDS = "training_ids";
    public static final String EXTRA_TYPE = "type";

    /* measurements source */
    public static final String MEASUREMENTS_SOURCE = "gma-app";

    /* common ButterKnife Settings/Actions */
    public static final ButterKnife.Setter<View, Integer> VISIBILITY = new ButterKnife.Setter<View, Integer>() {
        @Override
        public void set(@Nullable final View view, @NonNull final Integer value, final int index) {
            if (view != null) {
                view.setVisibility(value);
            }
        }
    };
}
