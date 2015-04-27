package com.expidevapps.android.measurements;

import static com.expidevapps.android.measurements.BuildConfig.NEW_RELIC_API_KEY;
import static com.expidevapps.android.measurements.BuildConfig.THEKEY_CLIENTID;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.newrelic.agent.android.NewRelic;

import net.danlew.android.joda.JodaTimeAndroid;

import io.fabric.sdk.android.Fabric;
import me.thekey.android.TheKey;
import me.thekey.android.TheKeyContext;
import me.thekey.android.lib.TheKeyImpl;

public class GcmApplication extends Application implements TheKeyContext {
    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        // initialize Android Joda-Time
        JodaTimeAndroid.init(this);

        // initialize New Relic
        NewRelic.withApplicationToken(NEW_RELIC_API_KEY).start(this);
    }

    @Override
    public TheKey getTheKey() {
        return TheKeyImpl.getInstance(this, THEKEY_CLIENTID);
    }
}
