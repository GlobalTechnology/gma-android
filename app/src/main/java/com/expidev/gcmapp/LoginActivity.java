package com.expidev.gcmapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

/**
 * Created by Sami on 3/9/15.
 */
public class LoginActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }






}
