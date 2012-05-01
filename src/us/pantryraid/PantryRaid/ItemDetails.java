package us.pantryraid.PantryRaid;

import android.app.Activity;
import android.os.Bundle;

public class ItemDetails extends Activity {
    // For logging and debugging purposes
    private static final String TAG = "ItemDetails";
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_details);
    }
}