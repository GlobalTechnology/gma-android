package com.expidevapps.android.measurements.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.expidevapps.android.measurements.R;
import com.expidevapps.android.measurements.service.GoogleAnalyticsManager;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;
import me.thekey.android.lib.support.v4.dialog.LoginDialogFragment;

public class LoginActivity extends FragmentActivity implements LoginDialogFragment.Listener {
    @NonNull
    private /* final */ GoogleAnalyticsManager mGoogleAnalytics;

    /* BEGIN lifecycle */

    @Override
    protected void onCreate(@Nullable final Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.activity_login);

        mGoogleAnalytics = GoogleAnalyticsManager.getInstance(this);

        ButterKnife.inject(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleAnalytics.sendLoginScreen();
    }

    @Optional
    @OnClick(R.id.login)
    void onShowLogin() {
        final FragmentManager fm = this.getSupportFragmentManager();
        if (fm.findFragmentByTag("loginDialog") == null) {
            LoginDialogFragment loginDialogFragment = LoginDialogFragment.builder().build();
            loginDialogFragment.show(fm.beginTransaction().addToBackStack("loginDialog"), "loginDialog");
        }
    }

    @Override
    public void onLoginSuccess(final LoginDialogFragment dialog, final String guid) {
        mGoogleAnalytics.sendLoginEvent(guid);
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onLoginFailure(final LoginDialogFragment dialog) {
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
    }

    /* END lifecycle */
}
