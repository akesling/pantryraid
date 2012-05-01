package us.pantryraid.PantryRaid;


import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

public class Pantry extends ListActivity {
    // For logging and debugging purposes
    private static final String TAG = "Pantry";
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	setListAdapter(new ArrayAdapter<String>(this, R.layout.pantry_item, GROCERIES));
    	
    	ListView lv = getListView();
    	lv.setTextFilterEnabled(true);
    	
    	lv.setOnItemClickListener(new OnItemClickListener() {
    		public void onItemClick(AdapterView<?> parent, View view, 
    				int position, long id) {
    			// When clicked, show a toast with the TextView text
    			Toast.makeText(getApplicationContext(), ((TextView) view).getText(),
    					Toast.LENGTH_SHORT).show();
    		}
    	});
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
		  
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
            case R.id.search:
                onSearchRequested();
                return true;
            default:
                return false;
        }
    }
    
    static final String[] GROCERIES = new String[] {
        "Black Beans", "Salsa", "Flour", "Milk", "Dish Detergent",
        "Greek Yogurt", "Rotini", "Pasta Sauce"
    };
}