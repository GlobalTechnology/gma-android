package com.expidev.gcmapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.expidev.gcmapp.utils.Device;


public class Login extends ActionBarActivity
{
    private static final String TAG = "MainActivity";
    private static final String SETTINGS_NAME = "GCMapp";
    
    Button login_button;
    EditText userName_ET;
    EditText password_ET;
    ProgressBar progressBar;
    TextView loading_TV;
    SharedPreferences settings;
    String userName;
    String password;
    

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        Log.i(TAG, "Starting activity");
        
        setUIComponents();
        
        // Check if credentials are saved
        settings = getSharedPreferences(SETTINGS_NAME, MODE_PRIVATE);
        userName = settings.getString("userName", "");
        password = settings.getString("password", "");
        
        // if connected to internet have user sign in
        if (Device.isConnected(getApplicationContext()))
        {
            if (!userName.equalsIgnoreCase(""))
            {
                userName_ET.setText(userName);
                password_ET.setText(password);
                login(null);
            }
        }
        else
        {
            // skip to next activity. (to support offline viewing)
            goToHomeScreen();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_exit)
        {
            showExitAlert();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    
    public void login(View view)
    {
        userName = userName_ET.getText().toString();
        password = password_ET.getText().toString();
        
        if (userName.isEmpty() || password.isEmpty())
        {
            Toast.makeText(this, "Username and Password fields cannot be empty", Toast.LENGTH_LONG).show();
        }
        else
        {   
            progressBar.setVisibility(View.VISIBLE);
            loading_TV.setVisibility(View.VISIBLE);
            goToHomeScreen();
        }
    }
    
    private void goToHomeScreen()
    {
        //Intent intent = new Intent(this, HomeScreen.class);
        //startActivity(intent);

        // we don't want to come back to this page
        finish();
    }
    
    private void setUIComponents()
    {
        login_button = (Button) findViewById(R.id.button_login);
        userName_ET = (EditText) findViewById(R.id.et_username);
        password_ET = (EditText) findViewById(R.id.et_password);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        loading_TV = (TextView) findViewById(R.id.tv_loading);
    }
    
    private void showExitAlert()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false)
                .setMessage(R.string.exit_message)
                .setPositiveButton(R.string.exit, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        finish();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
