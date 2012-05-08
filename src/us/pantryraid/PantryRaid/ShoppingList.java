package us.pantryraid.PantryRaid;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;



public class ShoppingList extends ListActivity {
	//	private static final String RETURN_ACTION = "Return Action";
	//    private static final int ITEM_CANCELED=0;
	//    private static final int ITEM_SAVED=1;
	private static final int ACTIVITY_CREATE = 0;
	private static final int ACTIVITY_VIEW = 1;



	private ItemsDbAdapter mDbHelper;
	//    private Cursor mItemsCursor;
	private static Context mCtx;
	private Long selectedItemId;

	// For logging and debugging purposes
	private static final String TAG = "ShoppingList";
	public static final int INSERT_ID = Menu.FIRST;
	//    private int mItemNumber = 1;

	//XXX: Flag such that callbacks don't get called on first instantiation.
//	private boolean onCreateFlag = true;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {  
		super.onCreate(savedInstanceState);
		mCtx = (Context) this;

		ActionBar bar = getActionBar();
		bar.setDisplayShowTitleEnabled(false);

		Log.w(TAG, "Setting up ActionBar");

		// setup action bar for spinner
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		ArrayAdapter<CharSequence> actionBarSpinner = 
				ArrayAdapter.createFromResource(this, R.array.actionbar_view_select, 
						android.R.layout.simple_spinner_item);
		actionBarSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//		bar.setSelectedNavigationItem(1);

		Log.w(TAG, "Navigation list setup.");
		bar.setListNavigationCallbacks(actionBarSpinner, new ActionBar.OnNavigationListener() {

			@Override
			public boolean onNavigationItemSelected(int itemPosition,
					long itemId) {
				//
				//				if (onCreateFlag) {
				//					onCreateFlag = false;
				//					return true;
				//				}

				switch(itemPosition) {
				case 0:
					startActivity(new Intent(mCtx, Pantry.class));
					return true;
				}		    	

				return false;
			}

		});

		bar.setSelectedNavigationItem(1);
		Log.w(TAG, "Navigation callback set.");

		//Populate list
		setContentView(R.layout.pantry_list);

		mDbHelper = new ItemsDbAdapter(this);
		mDbHelper.open();
		fillData();
		Log.w(TAG, "Data filled.");
	}

	public void onStart(Bundle savedInstanceState) {
		Log.w(TAG, "Calling onStart");
		//		ActionBar bar = getActionBar();
		//		bar.setSelectedNavigationItem(1);
	}

	public void onResume(Bundle savedInstanceState) {
		Log.w(TAG, "Calling onResume");
		super.onResume();
		mDbHelper.open();
		//		ActionBar bar = getActionBar();
		//		bar.setSelectedNavigationItem(1);
	}

	public void onPause(){
		super.onPause();
		//mDbHelper.close();
	}
	
	public void onStop(Bundle savedInstanceState) {
		Log.w(TAG, "Calling onStop");
		mDbHelper.close();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.w(TAG, "Entering OptionMenu creation.");
		// boolean result = super.onCreateOptionsMenu(menu);
		//menu.add(0, INSERT_ID, 0, R.string.menu_insert);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.shopping_list_options_menu, menu);
 
		
		Log.w(TAG, "OptionsMenu built.");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.w(TAG, "Calling onOptionsItemSelected");
			return false;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		
		Log.w(TAG, "Creating context menu.");
		View listItemView = (View) v.getParent();
		selectedItemId = (Long) v.getTag();
		menu.setHeaderTitle(((TextView) listItemView.findViewById(R.id.shoppingListName))
				.getText().toString());
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.shopping_list_context, menu);
	}
 
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.update_quantity:
			LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.entry_dialog,
			                               (ViewGroup) findViewById(R.id.layout_root));

			final EditText text = (EditText) layout.findViewById(R.id.value);
			
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setView(layout);
			
			builder.setMessage("Enter Numeric Quantity:");
			
			builder.setPositiveButton("Add to Pantry", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					String input = text.getText().toString();
					Double addition = 0.0;
					if (input != null && !input.equals("")) {
						addition = Double.parseDouble(input);
					}
					
	                Toast.makeText(mCtx, "Current Stock now "+mDbHelper.addQuantityToItem(selectedItemId, addition), 
	                		Toast.LENGTH_LONG).show();
	        	    
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
		case R.id.remove_from_shopping_list:
			//How to deal with this? If you simply toggle SL override,
			//the item may still be a candidate for the shopping list
			//if quantity is less than threshold.
			//For now, just toggle override if 1.
			Cursor cursor = mDbHelper.loadItem(selectedItemId);
			int ovrrd = cursor.getInt(cursor.getColumnIndex(ItemsDbAdapter.KEY_SHOPLIST_OVERRIDE));

			if((ovrrd == 0)){
				//Dialogue: Item already in shopping list
				AlertDialog.Builder notLockedToListBuilder = new AlertDialog.Builder(this);
				notLockedToListBuilder.setMessage("Item unlocked from shopping list")
						.setCancelable(false).setNeutralButton("Okay", new DialogInterface.OnClickListener() {
					           public void onClick(DialogInterface dialog, int id) {
					                dialog.cancel();
					           }
					       });
				notLockedToListBuilder.create().show();
				//Log.w(TAG, "Already in Shopping List");
			}else{ 
				mDbHelper.toggleShoppingListOverride(selectedItemId, ovrrd);
				fillData();
			}	
			
			selectedItemId = null;
			return true;
		case R.id.item_details:
			Intent i = new Intent(this, ItemEdit.class);
			i.putExtra(ItemsDbAdapter.KEY_ROWID, selectedItemId);
			startActivityForResult(i, ACTIVITY_VIEW);
			selectedItemId = null;
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
		default:
			return super.onContextItemSelected(item);
		}
	}
	
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

	private void fillData() {
		Log.w(TAG, "Calling fillData");
		// Get all of the notes from the database and create the item list
		Cursor ItemsCursor = mDbHelper.loadShoppingListItems();
		//mItemsCursor = mDbHelper.fetchAllItems();
		startManagingCursor(ItemsCursor);

		// Now create an array adapter and set it to display using our row
		ShoppingListAdapter items =
				new ShoppingListAdapter(this, R.layout.shopping_list_item, ItemsCursor);
		Log.w(TAG, "Setting list adapter.");
		setListAdapter(items);
	}

	private class ShoppingListAdapter extends ResourceCursorAdapter {

		public ShoppingListAdapter(Context context, int layout, Cursor c) {
			super(context, layout, c);
			Log.w(TAG, "Calling Constructor for ShoppingListAdapter");
		}

		@Override
		public void bindView(View view, Context context, final Cursor cursor) {
			Log.w(TAG, "Calling bindView");
			final long rowId = cursor.getLong(cursor.getColumnIndex(ItemsDbAdapter.KEY_ROWID));
			final int cursorPos = cursor.getPosition();
			
			final CheckBox shoppingListCheckBox = (CheckBox)view.findViewById(R.id.shoppingListCheckbox);
			final TextView shoppingListName = (TextView)view.findViewById(R.id.shoppingListName);
			final TextView shoppingListQuantity = (TextView)view.findViewById(R.id.shoppingListQuantity);
			final Button shoppingListLock = (Button)view.findViewById(R.id.shoppingListLock);
			final Button shoppingListItemButton = (Button)view.findViewById(R.id.shoppingListItemContextButton);

			Log.w(TAG, "Creating button... is it null?: "+(shoppingListItemButton == null));
			registerForContextMenu(shoppingListItemButton);
			shoppingListItemButton.setLongClickable(false);
			
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
			
			int itemChecked = cursor.getInt(cursor.getColumnIndex(ItemsDbAdapter.KEY_CHECKED));
			shoppingListCheckBox.setChecked((itemChecked!=1? false:true));
			
			shoppingListName.setText(cursor.getString(cursor.getColumnIndex(ItemsDbAdapter.KEY_ITEM_TYPE)));
			shoppingListQuantity.setText("Current Stock: " + cursor.getString(cursor.getColumnIndex(ItemsDbAdapter.KEY_QUANTITY)));

			
			/*
			if(itemChecked == 0) {
				shoppingListText.setAlpha((float) 0.5);
				shoppingListText.setPaintFlags(
						shoppingListText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
			}*/
			
			shoppingListName.setAlpha((float) (1-0.5*itemChecked));
			shoppingListQuantity.setAlpha((float) (1-0.5*itemChecked));

			shoppingListItemButton.setTag(rowId);
			
			shoppingListCheckBox.setOnClickListener(new View.OnClickListener() {

				public void onClick(View view) {
					boolean itemChecked = ((CheckBox) view).isChecked();
					mDbHelper.setItemChecked(rowId, itemChecked);
					
					if(itemChecked) {
						shoppingListName.setAlpha((float) 0.5);
						shoppingListQuantity.setAlpha((float) 0.5);
					} else {
						shoppingListName.setAlpha((float) 1);
						shoppingListQuantity.setAlpha((float) 1);
					}
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

			view.findViewById(R.id.shoppingListText).setOnClickListener(new View.OnClickListener() {

				public void onClick(View view) {
					Cursor c = cursor;
					c.moveToPosition(cursorPos);
					Intent i = new Intent(mCtx, ItemEdit.class);
					i.putExtra(ItemsDbAdapter.KEY_ROWID, rowId);
					i.putExtra(ItemsDbAdapter.KEY_ITEM_TYPE, c.getString(
							c.getColumnIndexOrThrow(ItemsDbAdapter.KEY_ITEM_TYPE)));
					i.putExtra(ItemsDbAdapter.KEY_STORE, c.getString(
							c.getColumnIndexOrThrow(ItemsDbAdapter.KEY_STORE)));
					i.putExtra(ItemsDbAdapter.KEY_QUANTITY, c.getDouble(
							c.getColumnIndexOrThrow(ItemsDbAdapter.KEY_QUANTITY)));
					i.putExtra(ItemsDbAdapter.KEY_THRESHOLD, c.getDouble(
							c.getColumnIndexOrThrow(ItemsDbAdapter.KEY_THRESHOLD)));
					i.putExtra(ItemsDbAdapter.KEY_LAST_UPDATED, c.getLong(
							c.getColumnIndexOrThrow(ItemsDbAdapter.KEY_LAST_UPDATED)));
					i.putExtra("intent", "view");
					startActivityForResult(i, ACTIVITY_VIEW);
				}

			});

			shoppingListItemButton.setOnClickListener(new View.OnClickListener() {

				public void onClick(View view) {
					openContextMenu(view);
				}

			});
		}

	}
}
