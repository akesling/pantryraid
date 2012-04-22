package us.pantryraid.PantryRaid;

import android.app.Activity;
import android.os.Bundle;

public class Search extends Activity {
    // For logging and debugging purposes
    private static final String TAG = "Pantry";
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);
    }
}