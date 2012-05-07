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
	public static final String KEY_CHECKED = "checked";
	public static final String KEY_ROWID = "_id";
	public static final String KEY_SHOPLIST_OVERRIDE = "shop_list_override";
	public static final String[] ALL_COLUMNS = new String[] {KEY_ROWID, KEY_ITEM_TYPE,
		KEY_STORE, KEY_QUANTITY, KEY_THRESHOLD, KEY_CHECKED, KEY_LAST_UPDATED, KEY_SHOPLIST_OVERRIDE};

	private static final String TAG = "ItemsDbAdapter";
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	/**
	 * Database creation sql statement
	 */
	//    private static final String DATABASE_CREATE =
	//        "create virtual table items using fts3 (" +
	//        "item_type text not null, " +
	//        "store text," +
	//        "quantity real not null, " +
	//        "threshold real," +
	//        "checked boolean default 0 not null, " +
	//        "last_updated integer not null);";
	private static final String DATABASE_CREATE =
			"create table items (_id integer primary key autoincrement, " +
					"item_type text not null, " +
					"store text," +
					"quantity real not null, " +
					"threshold real," +
					"checked boolean default 0 not null, " +
					"last_updated integer not null, " + 
					"shop_list_override boolean default 0 not null);";


	private static final String DATABASE_NAME = "data";
	private static final String DATABASE_TABLE = "items";
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
			db.execSQL("DROP TABLE IF EXISTS items");
			onCreate(db);
		}
	}

	public void changeDatabase(){
		Log.w(TAG, "Altering table.");
		//For refactoring database
		mDb.execSQL( "BEGIN TRANSACTION;" +
		" CREATE TEMPORARY TABLE backup(_id integer primary key autoincrement, " +
					"item_type text not null, " +
					"store text," +
					"quantity real not null, " +
					"threshold real," +
					"checked boolean default 0 not null, " +
					"last_updated integer not null, " + 
					"shop_list_override boolean default 0 not null);" +
		" INSERT INTO backup SELECT " +  KEY_ROWID + ", " + KEY_ITEM_TYPE +
		", " + KEY_STORE + ", " + KEY_QUANTITY + ", " + KEY_THRESHOLD + ", " + 
		KEY_CHECKED + ", " + KEY_LAST_UPDATED + ", " + KEY_SHOPLIST_OVERRIDE  + " FROM " + DATABASE_TABLE + ";" +
		" DROP TABLE " + DATABASE_TABLE + ";" +
		DATABASE_CREATE +
		" INSERT INTO " + DATABASE_TABLE + " SELECT * FROM backup;" +
		" DROP TABLE backup;" +
		" COMMIT;");
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
//		changeDatabase();
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
	public Cursor loadAllItems() {

		return mDb.query(DATABASE_TABLE, ALL_COLUMNS,
				null, null, null, null, null);
	}

	public Cursor searchDatabase(String query){



		return mDb.query(DATABASE_TABLE, ALL_COLUMNS, 
				(DATABASE_TABLE + " MATCHES '?'") , new String[] {query}, null, null, null);
	}

	/**
	 * Return a Cursor positioned at the item that matches the given rowId
	 * 
	 * @param rowId id of item to retrieve
	 * @return Cursor positioned to matching item, if found
	 * @throws SQLException if item could not be found/retrieved
	 */
	public Cursor loadItem(long rowId) throws SQLException {

		Cursor mCursor =
				mDb.query(true, DATABASE_TABLE, ALL_COLUMNS, 
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

	public boolean setItemChecked(long rowId, boolean value) {
		ContentValues args = new ContentValues();
		args.put(KEY_CHECKED, value);

		return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
	}

	public Cursor loadPantryItems() {

		return mDb.query(DATABASE_TABLE, ALL_COLUMNS, 
				KEY_QUANTITY + " > 0;", null, null, null, null);
	}

	public Cursor loadShoppingListItems() {
		return mDb.query(DATABASE_TABLE, ALL_COLUMNS, 
				"(" + KEY_QUANTITY + " < " + KEY_THRESHOLD + 
				") OR (" + KEY_SHOPLIST_OVERRIDE + " = " + "1);", 
				null, null, null, null);
	}

	public void toggleShoppingListOverride(Long itemID, int currentValue) {

		ContentValues values = new ContentValues();
		values.put(KEY_SHOPLIST_OVERRIDE, ((currentValue == 0) ? 1 : 0));

		mDb.update(DATABASE_TABLE, values, (KEY_ROWID + " = " + itemID),null);
	}

	public void useItem(Long rowId) {
		//get current quantity of item by id
		//put quantity - 1 in database
		//If already 0, throw error.
		
		Cursor cursor = mDb.query(DATABASE_TABLE, new String[]{KEY_QUANTITY}, 
				KEY_ROWID + " = " + rowId, null, null, null, null);
		cursor.moveToFirst();
		long currentQuantity = cursor.getLong(cursor.getColumnIndex(KEY_QUANTITY)) ;
		
		if (currentQuantity >= 1){
			ContentValues cv = new ContentValues();
			cv.put(KEY_QUANTITY, (currentQuantity - 1));
			mDb.update(DATABASE_TABLE, cv, KEY_ROWID + " = " + rowId, null);
			
		}else{
			//Throw error.
		}
		
	}

}
