package us.pantryraid.PantryRaid;


import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.content.Intent;
import android.database.Cursor;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SimpleCursorAdapter;

public class Pantry extends ListActivity {
    private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_VIEW=1;
    
    private ItemsDbAdapter mDbHelper;
    private Cursor mItemsCursor;
    
    // For logging and debugging purposes
    private static final String TAG = "Pantry";
    public static final int INSERT_ID = Menu.FIRST;
    private int mItemNumber = 1;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pantry_list);
        mDbHelper = new ItemsDbAdapter(this);
        mDbHelper.open();
        fillData();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        menu.add(0, INSERT_ID, 0, R.string.menu_insert);
        
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        	SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        	searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setIconifiedByDefault(false);
        }
    	return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case INSERT_ID:
            createItem();
            return true;
            case R.id.search:
                onSearchRequested();
                return true;
            default:
                return false;
        
        }
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
    
    
    static final String[] GROCERIES = new String[] {
        "Black Beans", "Salsa", "Flour", "Milk", "Dish Detergent",
        "Greek Yogurt", "Rotini", "Pasta Sauce"
    };
    private void fillData() {
        // Get all of the notes from the database and create the item list
        mItemsCursor = mDbHelper.fetchAllItems();
        startManagingCursor(mItemsCursor);

        String[] from = new String[] { ItemsDbAdapter.KEY_ITEM_TYPE };
        int[] to = new int[] { R.id.text1 };
        
        // Now create an array adapter and set it to display using our row
        SimpleCursorAdapter notes =
            new SimpleCursorAdapter(this, R.layout.pantry_item, mItemsCursor, from, to);
        setListAdapter(notes);
    }
}