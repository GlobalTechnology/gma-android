package com.expidev.gcmapp;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.expidev.gcmapp.GcmTheKey.GcmBroadcastReceiver;
import com.expidev.gcmapp.GcmTheKey.GcmTheKeyHelper;
import com.expidev.gcmapp.http.GcmApiClient;
import com.expidev.gcmapp.http.TicketTask;
import com.expidev.gcmapp.http.TokenTask;
import com.expidev.gcmapp.model.User;
import com.expidev.gcmapp.utils.DatabaseOpenHelper;
import com.expidev.gcmapp.utils.Device;
import com.expidev.gcmapp.utils.GcmProperties;

import org.json.JSONObject;

import java.util.Properties;

import me.thekey.android.TheKey;
import me.thekey.android.lib.TheKeyImpl;
import me.thekey.android.lib.support.v4.dialog.LoginDialogFragment;


public class MainActivity extends ActionBarActivity
{
    private final String TAG = this.getClass().getSimpleName();
    private Properties properties;
    private TheKey theKey;
    private long keyClientId;
    private LocalBroadcastManager manager;
    private GcmBroadcastReceiver gcmBroadcastReceiver;
    private TextView welcome;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        welcome = (TextView) findViewById(R.id.tv_welcome);

        populateDummyMinistries();

        getProperties();

        keyClientId = Long.parseLong(properties.getProperty("TheKeyClientId", ""));

        theKey = TheKeyImpl.getInstance(getApplicationContext(), keyClientId);

        manager = LocalBroadcastManager.getInstance(getApplicationContext());
        gcmBroadcastReceiver = new GcmBroadcastReceiver(theKey);
        gcmBroadcastReceiver.registerReceiver(manager);

        if (Device.isConnected(getApplicationContext()))
        {
           handleLogin();
        }
        else
        {
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_LONG).show();
        }
    }

    private void handleLogin()
    {
        // check for previous login sessions
        if (theKey.getGuid() == null)
        {
            login();
        }
        else
        {
            GcmApiClient.getTicket(theKey, new TicketTask.TicketTaskHandler()
            {
                @Override
                public void taskComplete(String ticket)
                {
                    GcmApiClient.getToken(ticket, new TokenTask.TokenTaskHandler()
                    {
                        @Override

                        public void taskComplete(JSONObject object)
                        {
                            Log.i(TAG, "Task Complete");
                            User user = GcmTheKeyHelper.createUser(object);
                            String welcomeMessage = "Welcome " + user.getFirstName() + " " + user.getLastName();
                            welcome.setText(welcomeMessage);
                        }

                        @Override
                        public void taskFailed(String status)
                        {
                            Log.i(TAG, "Task Failed. Status: " + status);
                        }
                    });
                }

                @Override
                public void taskFailed()
                {

                }
            });
        }
    }

    private void populateDummyMinistries()
    {
        DatabaseOpenHelper databaseOpenHelper = new DatabaseOpenHelper(this);

        SQLiteDatabase database = databaseOpenHelper.getWritableDatabase();
        Cursor cursor = database.query("associated_ministries", null, null, null, null, null, null);

        // Only add the dummy rows if none exist
        if(cursor.getCount() == 0)
        {
            database.beginTransaction();

            ContentValues guatemala = new ContentValues();
            guatemala.put("ministry_id", "1");
            guatemala.put("name", "Guatemala");
            guatemala.put("team_role", "self-assigned");
            guatemala.put("last_synced", "datetime(2015-01-15 11:30:00)");
            database.insert("associated_ministries", null, guatemala);

            ContentValues bridgesUcf = new ContentValues();
            bridgesUcf.put("ministry_id", "2");
            bridgesUcf.put("name", "Bridges UCF");
            bridgesUcf.put("team_role", "member");
            bridgesUcf.put("last_synced", "datetime(2015-01-15 11:30:00)");
            database.insert("associated_ministries", null, bridgesUcf);

            ContentValues antioch21 = new ContentValues();
            antioch21.put("ministry_id", "3");
            antioch21.put("name", "Antioch21 Church");
            antioch21.put("team_role", "leader");
            antioch21.put("last_synced", "datetime(2015-01-15 11:30:00)");
            database.insert("associated_ministries", null, antioch21);

            ContentValues random = new ContentValues();
            random.put("ministry_id", "4");
            random.put("name", "Random");
            random.put("team_role", "inherited_leader");
            random.put("last_synced", "datetime(2015-01-15 11:30:00)");
            database.insert("associated_ministries", null, random);

            database.setTransactionSuccessful();
            database.endTransaction();
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
        if (id == R.id.action_settings)
        {
            Intent goToSettings = new Intent(this, SettingsActivity.class);
            startActivity(goToSettings);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        manager.unregisterReceiver(gcmBroadcastReceiver);
    }

    public void joinNewMinistry(MenuItem menuItem)
    {
        //TODO: implement join new ministry
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Join new ministry")
                .setMessage("Choose a new ministry to join:")
                .setNeutralButton("OK", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
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
                .setNeutralButton("OK", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                })
                .create();

        alertDialog.show();
    }

    public void logout(MenuItem menuItem)
    {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.logout)
                .setMessage(R.string.logout_message)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        theKey.logout();
                        dialog.dismiss();
                        login();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                })
                .create();

        alertDialog.show();  
    }

    private void getProperties()
    {
        GcmProperties gcmProperties = new GcmProperties(this);
        properties = gcmProperties.getProperties("gcm_properties.properties");
    }
    
    private void login()
    {   
        final FragmentManager fm = this.getSupportFragmentManager();
        if (fm.findFragmentByTag("loginDialog") == null)
        {
            LoginDialogFragment loginDialogFragment = LoginDialogFragment.builder().clientId(keyClientId).build();
            loginDialogFragment.show(fm.beginTransaction().addToBackStack("loginDialog"), "loginDialog");
        }
    }
}
