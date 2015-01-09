package com.botornot.demo.botornot;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.sql.SQLException;

public class RatingsDbAdapter {

    public static final String KEY_IMAGE_ID = "image_id";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_RATING = "rating";

    private static final String TAG = "QUYEN";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation SQL statement
     */
    private static final String DATABASE_CREATE =
            "create table ratings (image_id integer primary key, "
            + "user_id integer, rating real);";

    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "ratings";
    private static final int DATABASE_VERSION = 2;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE);
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be opened/created
     *
     * @param ctx the Context within which to work
     */
    public RatingsDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw and exception to
     * signal the failure
     *
     * @return this (self reference, allowing this to be chained in an
     *          initialization call)
     *
     * @throws SQLException if the database could be neither opened or created
     */
    public RatingsDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }

    /**
     * If successfully created return the new rowId for inserted row, otherwise return
     * a -1 to indicate failure.
     *
     * @param imageId the unique id of the image
     * @param userId the id of the user
     * @param rating the decimal rating of the image
     * @return rowId or -1 if failed
     */
    public long addRating(long imageId, long userId, double rating) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_IMAGE_ID, imageId);
        initialValues.put(KEY_USER_ID, userId);
        initialValues.put(KEY_RATING, rating);

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Delete the rating with the given image id
     *
     * @param imageId id of image to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteRating(long imageId) {
        return mDb.delete(DATABASE_TABLE, KEY_IMAGE_ID + "=" + imageId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all ratings in the database
     *
     * @return Cursor over all ratings
     */
    public Cursor fetchAllRatings() {
        return mDb.query(DATABASE_TABLE, new String[] {KEY_IMAGE_ID, KEY_USER_ID,
            KEY_RATING}, null, null, null, null, null);
    }

    /**
     * Return a Cursor positioned at the rating that matches the given image id
     *
     * @param imageId id of rating to retrieve
     * @return Cursor positioned to matching rating, if found
     * @throws SQLException if rating could not be found/retrieved
     */
    public Cursor fetchRating(long imageId) throws SQLException {
        Cursor mCursor = mDb.query(true, DATABASE_TABLE, new String[] {KEY_IMAGE_ID,
            KEY_USER_ID, KEY_RATING}, KEY_IMAGE_ID + "=" + imageId, null,
                null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    /**
     * Update the rating using the details provided. The rating to be updated is
     * specified using the image id.
     *
     * @param imageId id of rating to update
     * @param rating value to set the rating to
     * @return true if the rating was successfully updated, false otherwise
     */
    public boolean updateRating(long imageId, double rating) {
        ContentValues args = new ContentValues();
        args.put(KEY_RATING, rating);

        return mDb.update(DATABASE_TABLE, args, KEY_IMAGE_ID + "=" + imageId, null) > 0;
    }
}










