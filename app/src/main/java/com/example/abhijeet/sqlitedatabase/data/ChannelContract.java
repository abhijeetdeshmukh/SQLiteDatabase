package com.example.abhijeet.sqlitedatabase.data;

/*** Created by ABHIJEET on 13-01-2017.*/

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/*** API Contract for the <app_name> app.*/
public class ChannelContract {

    /*** To prevent someone from accidentally instantiating the contract class, give it an empty constructor.*/
    private ChannelContract() {
    }

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.abhijeet.sqlitedatabase";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.abhijeet.sqlitedatabase/channels/ is a valid path for
     * looking at channel data. content://com.example.abhijeet.sqlitedatabase/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_CHANNELS = "channels";


    /**
     * Inner class that defines constant values for the channels database table.
     * Each entry in the table represents a single channel.
     */
    public static final class ChannelEntry implements BaseColumns {

        /** The content URI to access the channel data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_CHANNELS);

        /*** The MIME type of the {@link #CONTENT_URI} for a list of channels.*/
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CHANNELS;

        /*** The MIME type of the {@link #CONTENT_URI} for a single channel.*/
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CHANNELS;

        /*** Name of database table for channels*/
        public final static String TABLE_NAME = "channels";

        /**
         * Unique ID number for the pet (only for use in the database table).
         * <p>
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Name of the channel.
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_CHANNEL_NAME = "name";

        /**
         * Channel ID number for the channel .
         * <p>
         * Type: INTEGER
         */
        public final static String COLUMN_CHANNEL_ID = "channel_id";
    }
}
