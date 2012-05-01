/*
 * Copyright (C) 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package us.pantryraid.PantryRaid;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Simple items database access helper class. Defines the basic CRUD operations
 * for the itempad example, and gives the ability to list all items as well as
 * retrieve or modify a specific item.
 * 
 * This has been improved from the first version of this tutorial through the
 * addition of better error handling and also using returning a Cursor instead
 * of using a collection of inner classes (which is less scalable and not
 * recommended).
 */
public class ItemsDbAdapter {

    public static final String KEY_ITEM_TYPE = "item_type";
    public static final String KEY_STORE = "store";
    public static final String KEY_QUANTITY = "quantity";
    public static final String KEY_THRESHOLD = "threshold";
    public static final String KEY_LAST_UPDATED = "last_updated";
    public static final String KEY_ROWID = "_id";

    private static final String TAG = "ItemsDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE =
        "create table items (_id integer primary key autoincrement, " +
        "item_type text not null, " +
        "store text," +
        "quantity real not null, " +
        "threshold real," +
        "last_updated integer not null);";

    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "items";
    private static final int DATABASE_VERSION = 1;

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
            db.execSQL("DROP TABLE IF EXISTS items");
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public ItemsDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the items database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public ItemsDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }


    /**
     * Create a new item using the title and body provided. If the item is
     * successfully created return the new rowId for that item, otherwise return
     * a -1 to indicate failure.
     * 
     * @param item_type the type of the item
     * @param store the store at which the item was purchased
     * @param quantity how much of the item is in the pantry
     * @param threshold quantity at which to warn user item is getting low
     * @param last_updated date row was last updated
     * @return rowId or -1 if failed
     */


    public long createItem(String item_type, String store, 
    		double quantity, double threshold, long last_updated) {
        ContentValues initialValues = new ContentValues();

        initialValues.put(KEY_ITEM_TYPE, item_type);
        initialValues.put(KEY_STORE, store);
        initialValues.put(KEY_QUANTITY, quantity);
        initialValues.put(KEY_THRESHOLD, threshold);
        initialValues.put(KEY_LAST_UPDATED, last_updated);
        
        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Delete the item with the given rowId
     * 
     * @param rowId id of item to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteItem(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all items in the database
     * 
     * @return Cursor over all items
     */
    public Cursor fetchAllItems() {

        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_ITEM_TYPE,
                KEY_STORE, KEY_QUANTITY, KEY_THRESHOLD, KEY_LAST_UPDATED},
                null, null, null, null, null);
    }

    /**
     * Return a Cursor positioned at the item that matches the given rowId
     * 
     * @param rowId id of item to retrieve
     * @return Cursor positioned to matching item, if found
     * @throws SQLException if item could not be found/retrieved
     */
    public Cursor fetchItem(long rowId) throws SQLException {

        Cursor mCursor =
            mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, KEY_ITEM_TYPE,
                    KEY_STORE, KEY_QUANTITY, KEY_THRESHOLD, KEY_LAST_UPDATED}, 
                    KEY_ROWID + "=" + rowId, null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    /**
     * Update the item using the details provided. The item to be updated is
     * specified using the rowId, and it is altered to use the title and body
     * values passed in
     * 
     * @param rowId id of item to update
     * @param item_type the type of the item
     * @param store the store at which the item was purchased
     * @param quantity how much of the item is in the pantry
     * @param threshold quantity at which to warn user item is getting low
     * @param last_updated date row was last updated
     * @return true if the item was successfully updated, false otherwise
     */
    public boolean updateItem(long rowId, String item_type, String store, 
    		double quantity, double threshold, long last_updated) {
        ContentValues args = new ContentValues();
        args.put(KEY_ITEM_TYPE, item_type);
        args.put(KEY_STORE, store);
        args.put(KEY_QUANTITY, quantity);
        args.put(KEY_THRESHOLD, threshold);
        args.put(KEY_LAST_UPDATED, last_updated);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
}
