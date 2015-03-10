package com.expidev.gcmapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.android.gms.common.images.ImageManager;

/**
 * Created by Sami on 3/9/15.
 */
public class LoginActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        ImageButton btsign_in = (ImageButton) findViewById(R.id.btsing_in);

        btsign_in.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, MenuActivity.class));
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }






}
