package com.expidevapps.android.measurements.model;

import static com.expidevapps.android.measurements.model.MeasurementType.JSON_PERM_LINK_STUB;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.ccci.gto.android.common.util.LocaleCompat;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class MeasurementTypeLocalization extends Base {
    public static final String JSON_LOCALE_NAME = "locale";
    private static final String JSON_LOCALIZED_NAME = "localized_name";
    private static final String JSON_LOCALIZED_DESCRIPTION = "localized_description";

    @Deprecated
    private static final String JSON_LOCALIZE_NAME = "localize_name";
    @Deprecated
    private static final String JSON_LOCALIZE_DESCRIPTION = "localize_description";

    @NonNull
    private final String mPermLinkStub;
    @NonNull
    private final String mMinistryId;
    @NonNull
    private final Locale mLocale;
    @Nullable
    private String mName = null;
    @Nullable
    private String mDescription = null;

    public MeasurementTypeLocalization(@NonNull final String permLinkStub, @NonNull final String ministryId,
                                       @NonNull final Locale locale) {
        mPermLinkStub = permLinkStub;
        mMinistryId = ministryId;
        mLocale = locale;
    }

    @NonNull
    public static MeasurementTypeLocalization fromJson(@NonNull final JSONObject json, @NonNull final String ministryId)
            throws JSONException {
        final Locale locale = LocaleCompat.forLanguageTag(json.getString(JSON_LOCALE_NAME));
        final MeasurementTypeLocalization localization =
                new MeasurementTypeLocalization(json.getString(JSON_PERM_LINK_STUB), ministryId, locale);

        // first look for misspelled localize_name & localize_description
        localization.mName = json.optString(JSON_LOCALIZE_NAME, localization.mName);
        localization.mDescription = json.optString(JSON_LOCALIZE_DESCRIPTION, localization.mDescription);

        // prefer localized_name & localized_description
        localization.mName = json.optString(JSON_LOCALIZED_NAME, localization.mName);
        localization.mDescription = json.optString(JSON_LOCALIZED_DESCRIPTION, localization.mDescription);

        return localization;
    }

    @NonNull
    public String getPermLinkStub() {
        return mPermLinkStub;
    }

    @NonNull
    public String getMinistryId() {
        return mMinistryId;
    }

    @NonNull
    public Locale getLocale() {
        return mLocale;
    }

    @Nullable
    public String getName() {
        return mName;
    }

    public void setName(@Nullable final String name) {
        mName = name;
    }

    @Nullable
    public String getDescription() {
        return mDescription;
    }

    public void setDescription(@Nullable final String description) {
        mDescription = description;
    }
}
