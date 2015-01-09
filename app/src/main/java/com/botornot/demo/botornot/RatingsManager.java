package com.botornot.demo.botornot;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.sql.SQLException;

/**
 * This class extends Activity only to use the Cursor management features
 */
public class RatingsManager extends ActionBarActivity {

    private static final String LOG_TAG = "QUYEN";
    private RatingsDbAdapter mDbManager;

    public RatingsManager(Context context) {
        mDbManager = new RatingsDbAdapter(context);
        try {
            mDbManager.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveRating(Rating rating) {
        long rowId = getDbManager().addRating(rating.getImageId(), rating.getUserId(),
                rating.getRating());
        if (rowId <=0 ) {
            Log.d(LOG_TAG, "FAILED TO ADD IN DB, TRYING UPDATE: " + rating.toString());
            updateRating(rating);
        } else {
            Log.i(LOG_TAG, "SUCCESSFULLY ADD RATING IN DB: "+rating.toString());
        }
    }

    public void updateRating(Rating rating) {
        boolean success = getDbManager().updateRating(rating.getImageId(), rating.getRating());
        if (success) {
            Log.i(LOG_TAG, "UPDATED RATING IN DB: "+rating.toString());
        } else {
            Log.e(LOG_TAG, "FAILED TO UPDATE RATING IN DB: "+rating.toString());
        }
    }

    public double getRating(long imageId) {
        Cursor note = null;
        try {
            note = getDbManager().fetchRating(imageId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        startManagingCursor(note);
        double result = 0.0;
        if (note.getCount() > 0) {
            result = note.getDouble(note.getColumnIndexOrThrow(RatingsDbAdapter.KEY_RATING));
            Log.i(LOG_TAG, "RETRIEVED RATING IN DB FOR: "+imageId+" , Rating: "+result);
        } else {
            Log.i(LOG_TAG, "NO RATING EXIST IN DB FOR IMAGE ID: "+imageId);
        }
        return result;
    }

    public Rating[] getAllUserRatings(long userId) {
        return new Rating[0];
    }

    public Rating[] getAllRatings() {
        //mDbManager.clearDB(); // Wipe out the DB table if need to start fresh
        return new Rating[0];
    }

    private RatingsDbAdapter getDbManager() {
        return mDbManager;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bot_or_not, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}














