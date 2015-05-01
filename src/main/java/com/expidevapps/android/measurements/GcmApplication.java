package com.expidevapps.android.measurements;

import static com.expidevapps.android.measurements.BuildConfig.ACCOUNT_TYPE;
import static com.expidevapps.android.measurements.BuildConfig.NEW_RELIC_API_KEY;
import static com.expidevapps.android.measurements.BuildConfig.THEKEY_CLIENTID;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.newrelic.agent.android.NewRelic;

import net.danlew.android.joda.JodaTimeAndroid;

import io.fabric.sdk.android.Fabric;
import me.thekey.android.lib.TheKeyImpl;
import me.thekey.android.lib.TheKeyImpl.Configuration;

public class GcmApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        // initialize Android Joda-Time
        JodaTimeAndroid.init(this);

        // initialize New Relic
        NewRelic.withApplicationToken(NEW_RELIC_API_KEY).start(this);

        // configure TheKey
        Configuration config = Configuration.base().clientId(THEKEY_CLIENTID).accountType(ACCOUNT_TYPE);
        config = config.migrationSource(Configuration.base().clientId(THEKEY_CLIENTID));
        TheKeyImpl.configure(config);
    }
}
