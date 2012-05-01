package us.pantryraid.PantryRaid;

import java.util.Date;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SimpleCursorAdapter;

public class Pantry extends ListActivity {
    // For logging and debugging purposes
    private static final String TAG = "Pantry";
    private ItemsDbAdapter mDbHelper;
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
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case INSERT_ID:
            createItem();
            return true;
        }
       
        return super.onOptionsItemSelected(item);
    }

    private void createItem() {
        String itemType = "ItemType " + mItemNumber++;
        Date time = new Date();
        mDbHelper.createItem(itemType, "Harris Teeter", 4.0, 2.0, time.getTime());
        fillData();
    }
    
    private void fillData() {
        // Get all of the notes from the database and create the item list
        Cursor c = mDbHelper.fetchAllItems();
        startManagingCursor(c);

        String[] from = new String[] { ItemsDbAdapter.KEY_ITEM_TYPE };
        int[] to = new int[] { R.id.text1 };
        
        // Now create an array adapter and set it to display using our row
        SimpleCursorAdapter notes =
            new SimpleCursorAdapter(this, R.layout.list_item, c, from, to);
        setListAdapter(notes);
    }
}