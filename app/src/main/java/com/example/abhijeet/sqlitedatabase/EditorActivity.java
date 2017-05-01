package com.example.abhijeet.sqlitedatabase;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.abhijeet.sqlitedatabase.data.ChannelContract;
import com.example.abhijeet.sqlitedatabase.data.ChannelContract.ChannelEntry;

/*** Allows user to import a new channel or edit an existing one.*/
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    /** Identifier for the channel data loader */
    private static final int EXISTING_CHANNEL_LOADER = 0;

    /** Content URI for the existing channel (null if it's a new channel) */
    private Uri mCurrentChannelUri;

    /*** EditText field to enter the channel's name*/
    private EditText mNameEditText;

    /*** EditText field to enter the channel's Id*/
    private EditText mIdEditText;

    /** Boolean flag that keeps track of whether the channel has been edited (true) or not (false) */
    private boolean mChannelHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mChannelHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mChannelHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new channel or editing an existing one.
        Intent intent = getIntent();
        mCurrentChannelUri = intent.getData();

        // If the intent DOES NOT contain a channel content URI, then we know that we are
        // creating a new channel.
        if (mCurrentChannelUri == null) {
            // This is a new channel, so change the app bar to say "Add a Channel"
            setTitle(getString(R.string.editor_activity_title_new_channel));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a pet that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing channel, so change app bar to say "Edit channel"
            setTitle(getString(R.string.editor_activity_title_edit_channel));

            // Initialize a loader to read the channel data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_CHANNEL_LOADER, null, this);
        }

        //Find all the relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_channel_name);
        mIdEditText = (EditText) findViewById(R.id.edit_channel_id);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mIdEditText.setOnTouchListener(mTouchListener);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }
    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new channel, hide the "Delete" menu item.
        if (mCurrentChannelUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save channel to database
                saveChannel();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
             showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the channel hasn't changed, continue with navigating up to parent activity
                // which is the {@link MainActivity}.
                if (!mChannelHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*** Get user input from editor and save new channel into database.*/
    private void saveChannel() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String idString = mIdEditText.getText().toString().trim();

        // Check if this is supposed to be a new channel
        // and check if all the fields in the editor are blank
        if (mCurrentChannelUri == null &&
                TextUtils.isEmpty(nameString)  &&
                TextUtils.isEmpty(idString) ) {
            // Since no fields were modified, we can return early without creating a new channel.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and pet attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(ChannelContract.ChannelEntry.COLUMN_CHANNEL_NAME, nameString);
        // If the id is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int id = 0;
        if (!TextUtils.isEmpty(idString)) {
            id = Integer.parseInt(idString);
        }
        values.put(ChannelContract.ChannelEntry.COLUMN_CHANNEL_ID, id);


        // Determine if this is a new or existing channel by checking if mCurrentChannelUri is null or not
        if (mCurrentChannelUri == null) {
            // This is a NEW channel, so insert a new channel into the provider,
            // returning the content URI for the new channel.
            Uri newUri = getContentResolver().insert(ChannelEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_channel_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_channel_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING channel, so update the channel with content URI: mCurrentChannelUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentChannelUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentChannelUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_channel_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_channel_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /*** This method is called when the back button is pressed.*/
    @Override
    public void onBackPressed() {
        // If the channel hasn't changed, continue with handling back button press
        if (!mChannelHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all pet attributes, define a projection that contains
        // all columns from the pet table
        String[] projection = {
                ChannelEntry._ID,
                ChannelEntry.COLUMN_CHANNEL_NAME,
                ChannelEntry.COLUMN_CHANNEL_ID};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentChannelUri,         // Query the content URI for the current pet
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of channel attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(ChannelEntry.COLUMN_CHANNEL_NAME);
            int idtColumnIndex = cursor.getColumnIndex(ChannelEntry.COLUMN_CHANNEL_ID);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            int id = cursor.getInt(idtColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mIdEditText.setText(Integer.toString(id));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mIdEditText.setText("");
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the channel.
                deleteChannel();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the channel.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /*** Perform the deletion of the channel in the database.*/
    private void deleteChannel() {
        // Only perform the delete if this is an existing channel.
        if (mCurrentChannelUri != null) {
            // Call the ContentResolver to delete the channel at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentChannelUri
            // content URI already identifies the channel that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentChannelUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_channel_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_channel_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

}
