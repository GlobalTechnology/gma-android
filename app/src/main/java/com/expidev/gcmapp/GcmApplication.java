package com.expidev.gcmapp;

import static com.expidev.gcmapp.BuildConfig.NEW_RELIC_API_KEY;

import android.app.Application;

import com.newrelic.agent.android.NewRelic;

public class GcmApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        NewRelic.withApplicationToken(NEW_RELIC_API_KEY).start(this);
    }
}
