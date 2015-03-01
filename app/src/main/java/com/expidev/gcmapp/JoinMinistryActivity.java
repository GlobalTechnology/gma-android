package com.expidev.gcmapp;

import static org.ccci.gto.android.common.util.ViewUtils.findView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.FilterQueryProvider;

import com.expidev.gcmapp.activity.SettingsActivity;
import com.expidev.gcmapp.db.Contract;
import com.expidev.gcmapp.db.MinistriesDao;
import com.expidev.gcmapp.model.Assignment;
import com.expidev.gcmapp.model.Ministry;
import com.expidev.gcmapp.service.MinistriesService;
import com.expidev.gcmapp.tasks.CreateAssignmentTask;

import org.ccci.gto.android.common.util.CursorUtils;

import java.util.List;


public class JoinMinistryActivity extends ActionBarActivity
{
    private final String TAG = this.getClass().getSimpleName();

    private MinistriesDao mDao;

    private AutoCompleteTextView mMinistriesTextView = null;
    SimpleCursorAdapter mMinistriesAdapter = null;

    /* BEGIN lifecycle */

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_ministry);
        mDao = MinistriesDao.getInstance(this);
        findViews();
        setupAdapters();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cleanupAdapters();
        clearViews();
    }

    /* END lifecycle */

    private void findViews() {
        mMinistriesTextView = findView(this, AutoCompleteTextView.class, R.id.ministry_team_autocomplete);
    }

    private void setupAdapters() {
        if (mMinistriesTextView != null) {
            // create & attach adapter
            mMinistriesAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_dropdown_item_1line, null,
                                                         new String[] {Contract.AssociatedMinistry.COLUMN_NAME},
                                                         new int[] {android.R.id.text1}, 0);
            final MinistriesCursorProvider provider = new MinistriesCursorProvider(this);
            mMinistriesAdapter.setFilterQueryProvider(provider);
            mMinistriesAdapter.setCursorToStringConverter(provider);
            mMinistriesTextView.setAdapter(mMinistriesAdapter);
        }
    }

    private void cleanupAdapters() {
        if (mMinistriesTextView != null) {
            mMinistriesTextView.setAdapter(null);
        }

        mMinistriesAdapter = null;
    }

    private void clearViews() {
        mMinistriesTextView = null;
    }

    public void joinMinistry(View view)
    {
        AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView)findViewById(R.id.ministry_team_autocomplete);

        String ministryName = autoCompleteTextView.getText().toString();
        final Ministry chosenMinistry = getMinistryByName(ministryName);
        String ministryId = chosenMinistry != null ? chosenMinistry.getMinistryId() : null;

        new CreateAssignmentTask(this, ministryId, Assignment.Role.SELF_ASSIGNED) {
            @Override
            protected void onPostExecute(final Assignment assignment) {
                super.onPostExecute(assignment);

                if (assignment != null) {
                    // trigger a forced background sync of all assignments
                    MinistriesService.syncAssignments(JoinMinistryActivity.this, mTheKey.getGuid(), true);

                    // display dialog on success
                    // TODO: we should display the dialog when starting and change state to complete when we finish
                    final Ministry ministry = assignment.getMinistry();
                    new AlertDialog.Builder(JoinMinistryActivity.this).setTitle("Join Ministry")
                            .setMessage("You have joined " + ministry.getName() + " with a ministry ID of: " +
                                                assignment.getMinistryId()).setNeutralButton(
                            "OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    finish();
                                }
                            }).create().show();
                }

                // TODO: we need to handle failed requests
            }
        }.execute();
    }

    @Nullable
    private Ministry getMinistryByName(final String name) {
        //TODO: we shouldn't be using the DB on the UI Thread
        final List<Ministry> ministries =
                mDao.get(Ministry.class, Contract.AssociatedMinistry.COLUMN_NAME + "=?", new String[] {name});
        if (ministries.size() > 0) {
            return ministries.get(0);
        }

        return null;
    }

    public void cancel(View view)
    {
        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_join_ministry, menu);
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
        if(id == R.id.action_settings)
        {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private static final class MinistriesCursorProvider implements FilterQueryProvider,
            SimpleCursorAdapter.CursorToStringConverter {
        @NonNull
        private final MinistriesDao mDao;

        private MinistriesCursorProvider(final Context context) {
            mDao = MinistriesDao.getInstance(context);
        }

        private static final String[] PROJECTION_FIELDS =
                new String[] {Contract.AssociatedMinistry.COLUMN_ROWID, Contract.AssociatedMinistry.COLUMN_NAME};
        private static final String ORDER_BY_NAME = Contract.AssociatedMinistry.COLUMN_NAME;
        private static final String WHERE_NAME_LIKE = Contract.AssociatedMinistry.COLUMN_NAME + " LIKE ?";

        @Override
        public Cursor runQuery(final CharSequence constraint) {
            if (TextUtils.isEmpty(constraint)) {
                return mDao.getCursor(Ministry.class, PROJECTION_FIELDS, null, null, ORDER_BY_NAME);
            } else {
                return mDao.getCursor(Ministry.class, PROJECTION_FIELDS, WHERE_NAME_LIKE,
                                      new String[] {"%" + constraint + "%"}, ORDER_BY_NAME);
            }
        }

        @Override
        public CharSequence convertToString(final Cursor cursor) {
            return CursorUtils.getString(cursor, Contract.AssociatedMinistry.COLUMN_NAME, "");
        }
    }
}
