package com.expidev.gcmapp;

public class Constants {
    /* SharedPreference constants */
    public static final String PREFS_SETTINGS = "gcm_prefs";
    public static final String PREF_CURRENT_MINISTRY = "currentMinistry";

    /* common args */
    public static final String ARG_GUID = "guid";
    public static final String ARG_MINISTRY_ID = "ministry_id";
    public static final String ARG_TRAINING_ID = "training_id";
    public static final String ARG_CHURCH_ID = "church_id";
    public static final String ARG_MEASUREMENT_ID = "measurementId";
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
    public static final String EXTRA_TRAINING_IDS = "training_ids";
    public static final String EXTRA_TYPE = "type";

    /* result codes */
    public static final int BLOCKED_MINISTRY = 1;

    /* request codes */
    public static final int REQUEST_EXIT = 1;
}
