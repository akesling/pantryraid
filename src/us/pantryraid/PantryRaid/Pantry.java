package us.pantryraid.PantryRaid;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;



public class Pantry extends ListActivity {
	private static final int ACTIVITY_CREATE=0;
	private static final int ACTIVITY_VIEW=1;

	private ItemsDbAdapter mDbHelper;
	private static Context mCtx;

	// For logging and debugging purposes
	private static final String TAG = "Pantry";
	public static final int INSERT_ID = Menu.FIRST;
	private Long selectedItemId;

	//XXX: Flag such that callbacks don't get called on first instantiation.
	//    private boolean onCreateFlag = true;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {   
		super.onCreate(savedInstanceState);
		mCtx = (Context) this;

		ActionBar bar = getActionBar();
		bar.setDisplayShowTitleEnabled(false);
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		Log.w(TAG, "Creating array adapter.");
		// Populate spinner dropdown

		Log.i(TAG, "Linking ActionBar.");
		// setup action bar for spinner
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		ArrayAdapter<CharSequence> actionBarSpinner = 
				ArrayAdapter.createFromResource(this, R.array.actionbar_view_select, 
						android.R.layout.simple_spinner_item);
		actionBarSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		bar.setSelectedNavigationItem(0);
 
		bar.setListNavigationCallbacks(actionBarSpinner, new ActionBar.OnNavigationListener() {

			@Override
			public boolean onNavigationItemSelected(int itemPosition,
					long itemId) {

				Log.w(TAG, "Item "+itemPosition+" selected.");

				switch(itemPosition) {
				case 1:
					startActivity(new Intent(mCtx, ShoppingList.class));
					return true;
				}

				return false;
			}

		});

		setContentView(R.layout.pantry_list);

		Log.w(TAG, "Hello "+TAG+".");
		mDbHelper = new ItemsDbAdapter(this);
		mDbHelper.open();
		fillData();
	}

	public void onStart(Bundle savedInstanceState) {
		
	}

	public void onResume(Bundle savedInstanceState) {

		super.onResume();
		mDbHelper.open();

	}

	public void onStop(Bundle savedInstanceState) {
		mDbHelper.close();
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);

		//menu.add(0, INSERT_ID, 0, R.string.menu_insert);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.pantry_options_menu, menu);

		
		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.add_pantry_item:
			createItem();
			return true;
//		case R.id.search:
//			onSearchRequested();
//			return true;
		default:
			return false;
		}
	}


	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		View listItemView = (View) v.getParent();

		selectedItemId = (Long) v.getTag();
		menu.setHeaderTitle(((TextView) listItemView.findViewById(R.id.pantryItemText))
				.getText().toString());


		Log.w(TAG, "Creating context menu.");
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.pantry_context, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.use_item:
			LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.entry_dialog,
			                               (ViewGroup) findViewById(R.id.layout_root));

			final EditText text = (EditText) layout.findViewById(R.id.value);
			
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setView(layout);
			
			builder.setMessage("Enter Numeric Quantity:");
			
			builder.setPositiveButton("Use this Much", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					String input = text.getText().toString();
					Double amountUsed = 0.0;
					if (input != null && !input.equals("")) {
						amountUsed = Double.parseDouble(input);
					}
					
					String toastText = "Current stock now ";
					double newQuantity = mDbHelper.useItem(selectedItemId, amountUsed);
					if (newQuantity <= 0.0) {
						toastText += "0.  Item moved to Shopping List.";
					} else {
						toastText += newQuantity+".";
					}
	                Toast.makeText(mCtx, toastText, Toast.LENGTH_LONG).show();
	        	    
					dialog.cancel();
					
	                selectedItemId = null;
	                fillData();
	           	}});
		    
		    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		    
		    	public void onClick(DialogInterface dialog, int id) {
		    		dialog.cancel();
			        selectedItemId = null;
			    }
			           
		    });
			
			builder.create().show();
			return true;
		case R.id.delete_item:
			AlertDialog.Builder confirmDeleteBuilder = new AlertDialog.Builder(this);
			confirmDeleteBuilder.setMessage("Are you sure you want to delete this item?")
					.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
					           public void onClick(DialogInterface dialog, int id) {
					                mDbHelper.deleteItem(selectedItemId);
					                selectedItemId = null;
					                fillData();
					           }})
		           .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			                dialog.cancel();
			                selectedItemId = null;
			           }});
			confirmDeleteBuilder.create().show();
			return true;
		case R.id.item_details:	
			Intent i = new Intent(this, ItemEdit.class);
			i.putExtra(ItemsDbAdapter.KEY_ROWID, selectedItemId);
			startActivityForResult(i, ACTIVITY_VIEW);
			selectedItemId = null;
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent i = new Intent(this, ItemEdit.class);
		i.putExtra(ItemsDbAdapter.KEY_ROWID, id);
		startActivityForResult(i, ACTIVITY_VIEW);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		mDbHelper.open();
		fillData();
	}

	private void createItem() {
		Intent i = new Intent(this, ItemEdit.class);
		startActivityForResult(i, ACTIVITY_CREATE);
	}


	static final String[] GROCERIES = new String[] {
		"Black Beans", "Salsa", "Flour", "Milk", "Dish Detergent",
		"Greek Yogurt", "Rotini", "Pasta Sauce"
	};
	private void fillData() {
		// Get all of the notes from the database and create the item list
		Log.w(TAG, "Fetching items.");
		Cursor ItemsCursor = mDbHelper.loadPantryItems();

		startManagingCursor(ItemsCursor);

		// Now create an array adapter and set it to display using our row
		PantryListAdapter items =
				new PantryListAdapter(this, R.layout.pantry_item, ItemsCursor);
		Log.w(TAG, "Setting list adapter.");
		setListAdapter(items);
		Log.w(TAG, "Returned from list adapter.");
	}

	private class PantryListAdapter extends ResourceCursorAdapter {

		public PantryListAdapter(Context context, int layout, Cursor c) {
			super(context, layout, c);
		}

		@Override
		public void bindView(View view, Context context, final Cursor cursor) {
			TextView pantryListText = (TextView)view.findViewById(R.id.pantryItemText);
			Button pantryItemButton = (Button)view.findViewById(R.id.pantryItemContextButton);
			Button shoppingListLock = (Button)view.findViewById(R.id.pantryItemShoplistLock);
			
			registerForContextMenu(pantryItemButton);
			pantryItemButton.setLongClickable(false);
			
			double quantity = cursor.getDouble(cursor.getColumnIndex(ItemsDbAdapter.KEY_QUANTITY));
			double threshold = cursor.getDouble(cursor.getColumnIndex(ItemsDbAdapter.KEY_THRESHOLD));
						
			//Highlight locked items
			int toggleState = cursor.getInt(cursor.getColumnIndex(ItemsDbAdapter.KEY_SHOPLIST_OVERRIDE));
			if (toggleState==1) {
				shoppingListLock.setBackgroundDrawable(
						getResources().getDrawable(R.drawable.glyphicons_202_shopping_cart_active_large));
			} else {
				if (quantity < threshold) {
					shoppingListLock.setBackgroundDrawable(
							getResources().getDrawable(R.drawable.glyphicons_202_shopping_cart_warning_large));
				} else {
					shoppingListLock.setBackgroundDrawable(
							getResources().getDrawable(R.drawable.glyphicons_202_shopping_cart_passive_large));
				}
			}

			pantryListText.setText(cursor.getString(cursor.getColumnIndex(ItemsDbAdapter.KEY_ITEM_TYPE)));

			final long rowId = cursor.getLong(cursor.getColumnIndex(ItemsDbAdapter.KEY_ROWID));
			final int cursorPos = cursor.getPosition();

			//This embeds the pantry items rowId into the button so context menus can access them.
			pantryItemButton.setTag(rowId);

			pantryListText.setOnClickListener(new View.OnClickListener() {

				public void onClick(View view) {
					Intent i = new Intent(mCtx, ItemEdit.class);
					i.putExtra(ItemsDbAdapter.KEY_ROWID, rowId);
					startActivityForResult(i, ACTIVITY_VIEW);
				}

			});
			
			shoppingListLock.setOnClickListener(new View.OnClickListener() {

				public void onClick(View view) {
					cursor.moveToPosition(cursorPos);
					int toggleState = cursor.getInt(cursor.getColumnIndex(ItemsDbAdapter.KEY_SHOPLIST_OVERRIDE));
					double quantity = cursor.getDouble(cursor.getColumnIndex(ItemsDbAdapter.KEY_QUANTITY));
					double threshold = cursor.getDouble(cursor.getColumnIndex(ItemsDbAdapter.KEY_THRESHOLD));

					Log.w(TAG, "Toggled shopLock from: "+toggleState+" At position: "+cursor.getPosition());
					mDbHelper.toggleShoppingListOverride(rowId, toggleState);
					
					if (toggleState==1) {
						Log.w(TAG, "Turn cart off.");
						if (quantity < threshold) {
							view.setBackgroundDrawable(
									getResources().getDrawable(R.drawable.glyphicons_202_shopping_cart_warning_large));
						} else {
							view.setBackgroundDrawable(
									getResources().getDrawable(R.drawable.glyphicons_202_shopping_cart_passive_large));
						}
						view.refreshDrawableState();
						Toast.makeText(mCtx, "Item Unlocked from Shopping List", Toast.LENGTH_SHORT).show();
						fillData();
					} else {
						Log.w(TAG, "Turn cart on.");
						view.setBackgroundDrawable(getResources().getDrawable(R.drawable.glyphicons_202_shopping_cart_active_large));
						view.refreshDrawableState();
						Toast.makeText(mCtx, "Item Locked to Shopping List", Toast.LENGTH_SHORT).show();
						//XXX: Shouldn't redraw the list every time...
						fillData();
					}
				}

			});

			pantryItemButton.setOnClickListener(new View.OnClickListener() {

				public void onClick(View view) {
					openContextMenu(view);
				}

			});
		}
	}
}
