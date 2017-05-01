package com.example.abhijeet.sqlitedatabase;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.abhijeet.sqlitedatabase.data.ChannelContract.ChannelEntry;

/*** Displays list of channels that were entered and stored in the app.*/
public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>{

    /** Identifier for the channel data loader */
    private static final int CHANNEL_LOADER = 0;

    /** Adapter for the ListView */
    ChannelCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the channel data
        ListView channelListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        channelListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of channel data in the Cursor.
        // There is no channel data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new ChannelCursorAdapter(this, null);
        channelListView.setAdapter(mCursorAdapter);

        // Setup the item click listener
        channelListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);

                // Form the content URI that represents the specific channel that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link PetEntry#CONTENT_URI}.
                // For example, the URI would be "content://com.example.abhijeet.sqlitedatabase/channels/2"
                // if the pet with ID 2 was clicked on.
                Uri currentChannelUri = ContentUris.withAppendedId(ChannelEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentChannelUri);

                // Launch the {@link EditorActivity} to display the data for the current channel.
                startActivity(intent);
            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(CHANNEL_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertChannel();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllChannels();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*** Helper method to insert hardcoded channel data into the database. For debugging purposes only.*/
    private void insertChannel() {
        // Create a ContentValues object where column names are the keys,
        // and channel Id 9 thingSpeak Channel attributes are the values.
        ContentValues values = new ContentValues();
        values.put(ChannelEntry.COLUMN_CHANNEL_NAME, "My channel");
        values.put(ChannelEntry.COLUMN_CHANNEL_ID, 9);

        // Insert a new row for 'My channel' into the provider using the ContentResolver.
        // Use the {@link ChannelEntry#CONTENT_URI} to indicate that we want to insert
        // into the channels database table.
        // Receive the new content URI that will allow us to access My Channels's data in the future.
        Uri newUri = getContentResolver().insert(ChannelEntry.CONTENT_URI, values);
    }

    /**
     * Helper method to delete all channels in the database.
     */
    private void deleteAllChannels() {
        int rowsDeleted = getContentResolver().delete(ChannelEntry.CONTENT_URI, null, null);
        Log.v("MainActivity", rowsDeleted + " rows deleted from channel database");
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                ChannelEntry._ID,
                ChannelEntry.COLUMN_CHANNEL_NAME,
                ChannelEntry.COLUMN_CHANNEL_ID };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                ChannelEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link ChannelCursorAdapter} with this new cursor containing updated channel data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}
