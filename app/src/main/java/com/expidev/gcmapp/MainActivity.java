package com.expidev.gcmapp;

import android.app.AlertDialog;
import android.support.v4.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.expidev.gcmapp.utils.Device;

import me.thekey.android.lib.support.v4.dialog.LoginDialogFragment;


public class MainActivity extends ActionBarActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        if (Device.isConnected(getApplicationContext()))
        {
            final FragmentManager fm = this.getSupportFragmentManager();
            if (fm.findFragmentByTag("loginDialog") == null)
            {
                LoginDialogFragment.builder().clientId(THEKEY_CLIENTID).build().show(fm.beginTransaction().addToBackStack("loginDialog"), "loginDialog");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent goToSettings = new Intent(this, SettingsActivity.class);
            startActivity(goToSettings);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void joinNewMinistry(MenuItem menuItem)
    {
        //TODO: implement join new ministry
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Join new ministry")
                .setMessage("Choose a new ministry to join:")
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();

        alertDialog.show();
    }

    public void reset(MenuItem menuItem)
    {
        //TODO: implement reset: clear local data-model, download from server
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Reset")
                .setMessage("Re-downloading information...")
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();

        alertDialog.show();
    }

    public void logout(MenuItem menuItem)
    {
        //TODO: implement logout: actually log user out, redirect to TheKey login page
        AlertDialog alertDialog = new AlertDialog.Builder(this)
            .setTitle("Logged Out")
            .setMessage("You have been logged out! (not really)")
            .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            })
            .create();

        alertDialog.show();
    }
}
