package com.expidev.gcmapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import me.thekey.android.lib.activity.LoginActivity;


public class SplashActivity extends ActionBarActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Thread timer=new Thread(){
            public  void run(){
                try{
                    sleep(3000);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }finally {
                    startActivity(new Intent(SplashActivity.this,LoginActivity.class));
                }
            }
        };
        timer.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }





}
