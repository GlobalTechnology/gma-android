package com.expidevapps.android.measurements;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import butterknife.ButterKnife;

public final class Constants {
    /* SharedPreference constants */
    private static final String PREFS_SETTINGS = "prefs";
    public static final String PREF_ACTIVE_MINISTRY = "ministry.active";
    public static final String PREF_ACTIVE_MCC = "ministry.active.mcc";
    public static final String PREF_MAP_LAYER_TRAINING = "map.layer.training";
    public static final String PREF_MAP_LAYER_CHURCH_TARGET = "map.layer.church.target";
    public static final String PREF_MAP_LAYER_CHURCH_GROUP = "map.layer.church.group";
    public static final String PREF_MAP_LAYER_CHURCH_CHURCH = "map.layer.church.church";
    public static final String PREF_MAP_LAYER_CHURCH_MULTIPLYING = "map.layer.church.multiplying";
    public static final String PREF_MAP_LAYER_CHURCH_PARENTS = "map.layer.church.parents";

    @NonNull
    public static String PREFS_SETTINGS(@NonNull final String guid) {
        return PREFS_SETTINGS + "_" + guid;
    }

    /* common args */
    public static final String ARG_GUID = "guid";
    public static final String ARG_MINISTRY_ID = "ministry_id";
    public static final String ARG_TRAINING_ID = "training_id";
    public static final String ARG_CHURCH_ID = "church_id";
    public static final String ARG_MCC = "mcc";
    public static final String ARG_PERMLINK = "perm_link";
    public static final String ARG_PERIOD = "period";
    public static final String ARG_TYPE = "type";
    public static final String ARG_LOCATION = "location";
    public static final String ARG_PERSON_ID = "person_id";
    public static final String ARG_FAVORITES_ONLY = "favorites_only";

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
    public static final String EXTRA_PREFERENCES = "prefs";
    public static final String EXTRA_FAVORITES_ONLY = "favorites_only";

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
