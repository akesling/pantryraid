package us.pantryraid.PantryRaid;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
    
    static final String[] GROCERIES = new String[] {
        "Black Beans", "Salsa", "Flour", "Milk", "Dish Detergent",
        "Greek Yogurt", "Rotini", "Pasta Sauce"
    };
}