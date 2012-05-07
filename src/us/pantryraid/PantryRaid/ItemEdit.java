package us.pantryraid.PantryRaid;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

public class ItemEdit extends Activity {
	private Long mRowId;
	EditText mItemTypeText;
	EditText mStoreText;
	EditText mQuantityText;
	EditText mThresholdText;

	// For logging and debugging purposes
	private static final String TAG = "ItemEdit";
	public static final int INSERT_ID = Menu.FIRST;
	//private static final String RETURN_ACTION = "Return Action";
	private ItemsDbAdapter mDbHelper;


	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDbHelper = new ItemsDbAdapter(this);

		mDbHelper.open();

		ActionBar bar = getActionBar();
		bar.setTitle("Edit Item");
		setContentView(R.layout.item_edit);

		mItemTypeText = (EditText) findViewById(R.id.itemType);
		mStoreText = (EditText) findViewById(R.id.store);
		mQuantityText = (EditText) findViewById(R.id.quantity);
		mThresholdText = (EditText) findViewById(R.id.threshold);
		Bundle extras = getIntent().getExtras();

		mRowId = (savedInstanceState == null) ? null :
			(Long) savedInstanceState.getSerializable(ItemsDbAdapter.KEY_ROWID);
		if (mRowId == null) {
			mRowId = extras != null ? extras.getLong(ItemsDbAdapter.KEY_ROWID)
					: null;
		}


		populateFields();


	}


	public boolean onCreateOptionsMenu(Menu menu){

		Log.w(TAG, "Entering OptionMenu creation.");
		// boolean result = super.onCreateOptionsMenu(menu);
		//menu.add(0, INSERT_ID, 0, R.string.menu_insert);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.item_edit_menu, menu);

		//         if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
		//             SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		//         	SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
		//         	searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		//             searchView.setIconifiedByDefault(false);
		//         }
		Log.w(TAG, "OptionsMenu built.");
		return true;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.cancel:
			cancelItemChanges();
			return true;
		case R.id.save:
			saveItemChanges();
			return true;
		default:
			return false;

		}
	}

	private void populateFields() {
		if (mRowId != null) {
			Cursor note = mDbHelper.loadItem(mRowId);

			startManagingCursor(note);

			mItemTypeText.setText(note.getString(
					note.getColumnIndexOrThrow(ItemsDbAdapter.KEY_ITEM_TYPE)));
			mStoreText.setText(note.getString(
					note.getColumnIndexOrThrow(ItemsDbAdapter.KEY_STORE)));
			mQuantityText.setText(note.getString(
					note.getColumnIndexOrThrow(ItemsDbAdapter.KEY_QUANTITY)));
			mThresholdText.setText(note.getString(
					note.getColumnIndexOrThrow(ItemsDbAdapter.KEY_THRESHOLD)));
		}


	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		//    saveState();
		outState.putSerializable(ItemsDbAdapter.KEY_ROWID, mRowId);
	}

	protected void onPause(){
		super.onPause();
		mDbHelper.close();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mDbHelper.open();
		//    populateFields();
	}

	private void saveState() {

		String itemType = mItemTypeText.getText().toString();
		Double quantity = Double.parseDouble(mQuantityText.getText().toString());
		Double threshold = Double.parseDouble(mThresholdText.getText().toString());
		String store = mStoreText.getText().toString();

		if (mRowId == null) {
			long id = mDbHelper.createItem(itemType, store, quantity, threshold, System.currentTimeMillis());
			if (id > 0) {
				mRowId = id;
			}
		} else {
			mDbHelper.updateItem(mRowId, itemType, store, quantity, threshold, System.currentTimeMillis());
		}

	}

	private void saveItemChanges() {
		if(mItemTypeText.getText().toString().equals("")){
			AlertDialog.Builder noNameBuilder = new AlertDialog.Builder(this);
			noNameBuilder.setMessage("Item must have a name")
			.setNeutralButton("Okay", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}});
			noNameBuilder.create().show();

		}else{
			if(( mQuantityText.getText().toString().equals( "")) ){

				AlertDialog.Builder invalidQuantityBuilder = new AlertDialog.Builder(this);
				invalidQuantityBuilder.setMessage("Must enter item Quantity")
				.setNeutralButton("Okay", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}});
				invalidQuantityBuilder.create().show();
				
			}else{
				if ((mThresholdText.getText().toString().equals( ""))){

					AlertDialog.Builder invalidThresholdBuilder = new AlertDialog.Builder(this);
					invalidThresholdBuilder.setMessage("Must enter item threshold")
					.setNeutralButton("Okay", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}});
					invalidThresholdBuilder.create().show();
				}else{
					saveState();
					setResult(RESULT_OK);//, mIntent);
					finish();
				}
			}


		}
	}

	private void cancelItemChanges() {
		//	Bundle bundle = new Bundle();
		//	bundle.putInt(RETURN_ACTION, 0);
		//	Intent mIntent = new Intent();
		//	mIntent.putExtras(bundle);
		setResult(RESULT_CANCELED);//, mIntent);
		finish();
	}
}