package us.pantryraid.PantryRaid;

import android.app.ActionBar;
import android.app.ListActivity; 
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

public class ShoppingList extends ListActivity {
    private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_VIEW=1;
    
    private ItemsDbAdapter mDbHelper;
    private Cursor mItemsCursor;
    private static Context mCtx;
    
    // For logging and debugging purposes
    private static final String TAG = "ShoppingList";
    public static final int INSERT_ID = Menu.FIRST;
    private int mItemNumber = 1;
    
    //XXX: Flag such that callbacks don't get called on first instantiation.
    private boolean onCreateFlag = true;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);
        mCtx = (Context) this;
        
        ActionBar bar = getActionBar();
        bar.setDisplayShowTitleEnabled(false);

        // setup action bar for spinner
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        ArrayAdapter<CharSequence> actionBarSpinner = 
        		ArrayAdapter.createFromResource(this, R.array.actionbar_view_select, 
        				android.R.layout.simple_spinner_item);
        actionBarSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bar.setSelectedNavigationItem(1);
        bar.setListNavigationCallbacks(actionBarSpinner, new ActionBar.OnNavigationListener() {

			@Override
			public boolean onNavigationItemSelected(int itemPosition,
					long itemId) {
				
				if (onCreateFlag) {
					onCreateFlag = false;
					return true;
				}
				
				switch(itemPosition) {
				case 0:
			    	startActivity(new Intent(mCtx, Pantry.class));
			    	return true;
				}		    	
				
				return false;
			}
        	
        });
        
        //Populate list
        setContentView(R.layout.pantry_list);
        mDbHelper = new ItemsDbAdapter(this);
        mDbHelper.open();
        fillData();
        Log.w(TAG, "Hello "+TAG+".");
    }
    
    public void onStart(Bundle savedInstanceState) {
    	ActionBar bar = getActionBar();
        bar.setSelectedNavigationItem(1);
    }
    
    public void onResume(Bundle savedInstanceState) {
    	ActionBar bar = getActionBar();
        bar.setSelectedNavigationItem(1);
    }
    
    public void onStop(Bundle savedInstanceState) {
    	mDbHelper.close();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        menu.add(0, INSERT_ID, 0, R.string.menu_insert);
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case INSERT_ID:
            getActionBar().setSelectedNavigationItem(1);
            return true;
        }
       
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, position, id);
    	Cursor c = mItemsCursor;
    	c.moveToPosition(position);
    	Intent i = new Intent(this, ItemEdit.class);
    	i.putExtra(ItemsDbAdapter.KEY_ROWID, id);
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
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	super.onActivityResult(requestCode, resultCode, intent);
    	Bundle extras = intent.getExtras();
    	
	    String item_type;
	    String store;
	    Double quantity;
	    Double threshold;
	    Long last_updated;

    	switch(requestCode) {
    	case ACTIVITY_CREATE:
    	    item_type = extras.getString(ItemsDbAdapter.KEY_ITEM_TYPE);
    	    store = extras.getString(ItemsDbAdapter.KEY_STORE);
    	    quantity = extras.getDouble(ItemsDbAdapter.KEY_QUANTITY);
    	    threshold = extras.getDouble(ItemsDbAdapter.KEY_THRESHOLD);
    	    last_updated = extras.getLong(ItemsDbAdapter.KEY_LAST_UPDATED);
    	    mDbHelper.createItem(item_type, store, quantity, threshold, last_updated);
    	    fillData();
    	    break;
    	case ACTIVITY_VIEW:
    	    Long mRowId = extras.getLong(ItemsDbAdapter.KEY_ROWID);
    	    if (mRowId != null) {
        	    item_type = extras.getString(ItemsDbAdapter.KEY_ITEM_TYPE);
        	    store = extras.getString(ItemsDbAdapter.KEY_STORE);
        	    quantity = extras.getDouble(ItemsDbAdapter.KEY_QUANTITY);
        	    threshold = extras.getDouble(ItemsDbAdapter.KEY_THRESHOLD);
        	    last_updated = extras.getLong(ItemsDbAdapter.KEY_LAST_UPDATED);
        	    mDbHelper.updateItem(mRowId, item_type, store, quantity, threshold, last_updated);
    	    }
    	    fillData();
    	    break;
    	}
    }

    private void createItem() {
    	Intent i = new Intent(this, ItemEdit.class);
    	startActivityForResult(i, ACTIVITY_CREATE);
    }
    
    private void fillData() {
        // Get all of the notes from the database and create the item list
        mItemsCursor = mDbHelper.fetchAllItems();
        startManagingCursor(mItemsCursor);
        
        // Now create an array adapter and set it to display using our row
        ShoppingListAdapter items =
            new ShoppingListAdapter(this, R.layout.shopping_list_item, mItemsCursor);
        setListAdapter(items);
    }
    
    private class ShoppingListAdapter extends ResourceCursorAdapter {

		public ShoppingListAdapter(Context context, int layout, Cursor c) {
			super(context, layout, c);
		}

		@Override
		public void bindView(View view, Context context, final Cursor cursor) {
			CheckBox shoppingListCheckBox = (CheckBox)view.findViewById(R.id.shoppingListCheckbox);
			TextView shoppingListText = (TextView)view.findViewById(R.id.shoppingListText);
			
			shoppingListCheckBox.setChecked(
					(cursor.getInt(cursor.getColumnIndex(ItemsDbAdapter.KEY_CHECKED))==0? false:true));
			shoppingListText.setText(cursor.getString(cursor.getColumnIndex(ItemsDbAdapter.KEY_ITEM_TYPE)));
			
			final long rowId = cursor.getLong(cursor.getColumnIndex(ItemsDbAdapter.KEY_ROWID));
			final int cursorPos = cursor.getPosition();
			
			shoppingListCheckBox.setOnClickListener(new View.OnClickListener() {

			    public void onClick(View view) {
	        	    mDbHelper.setItemChecked(rowId, ((CheckBox) view).isChecked());
			    }
			    
			});
			
			shoppingListText.setOnClickListener(new View.OnClickListener() {

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
		}
    	
    }
}