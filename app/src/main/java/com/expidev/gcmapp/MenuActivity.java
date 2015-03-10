package com.expidev.gcmapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

/**
 * Created by Sami on 3/10/15.
 */
public class MenuActivity extends Activity {

    ImageButton btperson,btgroup,btchurch,btppl_group,btgra_hat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dasboard_layout);
         btperson = (ImageButton) findViewById(R.id.btperson);
        btgroup  = (ImageButton) findViewById(R.id.btgroup);
        btchurch  = (ImageButton) findViewById(R.id.btchurch);
        btppl_group  = (ImageButton) findViewById(R.id.btppl_group);
        btgra_hat = (ImageButton) findViewById(R.id.btgrad_hat);

        btperson.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent());
            }
        });
        btgroup.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent());
            }
        });
        btchurch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent());
            }
        });
        btppl_group.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent());
            }
        });
        btgra_hat.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent());
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }






}
