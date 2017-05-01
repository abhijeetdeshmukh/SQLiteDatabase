package com.example.abhijeet.sqlitedatabase.data;

/*** Created by ABHIJEET on 14-01-2017.*/

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

/*** {@link ContentProvider} for <app_name> app.*/
public class ChannelProvider extends ContentProvider {

    /** Tag for the log messages */
    public static final String LOG_TAG = ChannelProvider.class.getSimpleName();

    /** URI matcher code for the content URI for the channels table */
    private static final int CHANNELS = 100;

    /** URI matcher code for the content URI for a single channel in the channels table */
    private static final int CHANNEL_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // The content URI of the form "content://com.example.abhijeet.sqlitedatabase.channels/channels" will map to the
        // integer code {@link #CHANNELS}. This URI is used to provide access to MULTIPLE rows
        // of the channels table.
        sUriMatcher.addURI(ChannelContract.CONTENT_AUTHORITY, ChannelContract.PATH_CHANNELS, CHANNELS);

        // The content URI of the form "content://com.example.abhijeet.sqlitedatabase.channels/channels/#" will map to the
        // integer code {@link #CHANNEL_ID}. This URI is used to provide access to ONE single row
        // of the channels table.
        //
        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        // For example, "content://com.example.android.pets/pets/3" matches, but
        // "content://com.example.android.pets/pets" (without a number at the end) doesn't match.
        sUriMatcher.addURI(ChannelContract.CONTENT_AUTHORITY, ChannelContract.PATH_CHANNELS + "/#", CHANNEL_ID);
    }

    /** Database helper object */
    private ChannelDbHelper mDbHelper;

    /*** Initialize the provider and the database helper object.*/
    @Override
    public boolean onCreate() {
        // TO-DO: Create and initialize a PetDbHelper object to gain access to the pets database.
        // Make sure the variable is a global variable, so it can be referenced from other
        // ContentProvider methods.

        mDbHelper = new ChannelDbHelper(getContext());
        return true;
    }

    /*** Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.*/
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case CHANNELS:
                // For the CHANNELS code, query the channels table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the pets table.
                // TO-DO: Perform database query on pets table
                cursor = database.query(ChannelContract.ChannelEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case CHANNEL_ID:
                // For the CHANNEL_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.pets/pets/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = ChannelContract.ChannelEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the pets table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(ChannelContract.ChannelEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the cursor
        return cursor;
    }

    /*** Insert new data into the provider with the given ContentValues.*/
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CHANNELS:
                return insertChannel(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a channel into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertChannel(Uri uri, ContentValues values) {

        // Check that the name is not null
        String name = values.getAsString(ChannelContract.ChannelEntry.COLUMN_CHANNEL_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Channel requires a name");
        }

        // If the channel id is provided, check that it's greater than or equal to 0 kg
        Integer channelId = values.getAsInteger(ChannelContract.ChannelEntry.COLUMN_CHANNEL_ID);
        if (channelId != null && channelId < 0) {
            throw new IllegalArgumentException("Channel requires valid channel Id");
        }

        // TO-DO: Insert a new channel into the channels database table with the given ContentValues
        // Get write-able database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new channel with the given values
        long id = database.insert(ChannelContract.ChannelEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the channel content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    /*** Updates the data at the given selection and selection arguments, with the new ContentValues.*/
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CHANNELS:
                return updateChannel(uri, contentValues, selection, selectionArgs);
            case CHANNEL_ID:
                // For the CHANNEL_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = ChannelContract.ChannelEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateChannel(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update channels in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more channels).
     * Return the number of rows that were successfully updated.
     */
    private int updateChannel(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // TO-DO: Update the selected channels in the channels database table with the given ContentValues

        // If the {@link ChannelEntry#COLUMN_CGANNEL_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(ChannelContract.ChannelEntry.COLUMN_CHANNEL_NAME)) {
            String name = values.getAsString(ChannelContract.ChannelEntry.COLUMN_CHANNEL_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Channel requires a name");
            }
        }

        // If the {@link ChannelEntry#COLUMN_CHANNEL_ID} key is present,
        // check that the channel id value is valid.
        if (values.containsKey(ChannelContract.ChannelEntry.COLUMN_CHANNEL_ID)) {
            // Check that the id is greater than or equal to 0
            Integer id = values.getAsInteger(ChannelContract.ChannelEntry.COLUMN_CHANNEL_ID);
            if (id != null && id < 0) {
                throw new IllegalArgumentException("Channel requires valid id");
            }
        }

        // TO-DO: Return the number of rows that were affected

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        /// Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(ChannelContract.ChannelEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }

    /*** Delete the data at the given selection and selection arguments.*/
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CHANNELS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(ChannelContract.ChannelEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case CHANNEL_ID:
                // Delete a single row given by the ID in the URI
                selection = ChannelContract.ChannelEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(ChannelContract.ChannelEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;

    }

    /*** Returns the MIME type of data for the content URI.*/
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CHANNELS:
                return ChannelContract.ChannelEntry.CONTENT_LIST_TYPE;
            case CHANNEL_ID:
                return ChannelContract.ChannelEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}