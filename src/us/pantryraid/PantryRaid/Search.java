package us.pantryraid.PantryRaid;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

public class Search extends Activity {
    // For logging and debugging purposes
    private static final String TAG = "Search";
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);
        
     // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
          String query = intent.getStringExtra(SearchManager.QUERY);
          searchFor(query);
        }
        
        

    }

	private void searchFor(String query) {
		ItemsDbAdapter dbAdapter = new ItemsDbAdapter(this);
		Cursor results = dbAdapter.searchDatabase(query);
		
		
	}
}