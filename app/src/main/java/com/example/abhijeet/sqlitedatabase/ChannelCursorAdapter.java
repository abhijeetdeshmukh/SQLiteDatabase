package com.example.abhijeet.sqlitedatabase;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.abhijeet.sqlitedatabase.data.ChannelContract.ChannelEntry;

/*** Created by ABHIJEET on 15-01-2017.*/

/**
 * {@link ChannelCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of channel data as its data source. This adapter knows
 * how to create list items for each row of channel data in the {@link Cursor}.
 */
public class ChannelCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link ChannelCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public ChannelCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the channel data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current channel can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView summaryTextView = (TextView) view.findViewById(R.id.summary);

        // Find the columns of channel attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(ChannelEntry.COLUMN_CHANNEL_NAME);
        int idColumnIndex = cursor.getColumnIndex(ChannelEntry.COLUMN_CHANNEL_ID);

        // Read the channel attributes from the Cursor for the current channel
        String channelName = cursor.getString(nameColumnIndex);
        String channelId = cursor.getString(idColumnIndex);

        // Update the TextViews with the attributes for the current channel
        nameTextView.setText(channelName);
        summaryTextView.setText(channelId);
    }
}