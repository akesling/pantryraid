package us.pantryraid.PantryRaid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ItemEdit extends Activity {
	private Long mRowId;
	EditText mItemTypeText;
	EditText mStoreText;
	EditText mQuantityText;
	EditText mThresholdText;
	
    // For logging and debugging purposes
    private static final String TAG = "ItemEdit";
    
    /** Called when the activity is first created. */
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.item_edit);
		
		mItemTypeText = (EditText) findViewById(R.id.itemType);
		mStoreText = (EditText) findViewById(R.id.store);
		mQuantityText = (EditText) findViewById(R.id.quantity);
		mThresholdText = (EditText) findViewById(R.id.threshold);
		Button confirmButton = (Button) findViewById(R.id.confirm);
		
		mRowId = null;
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
		    String itemType = extras.getString(ItemsDbAdapter.KEY_ITEM_TYPE);
		    String store = extras.getString(ItemsDbAdapter.KEY_STORE);
		    Double quantity = extras.getDouble(ItemsDbAdapter.KEY_QUANTITY);
		    Double threshold = extras.getDouble(ItemsDbAdapter.KEY_THRESHOLD);
		    mRowId = extras.getLong(ItemsDbAdapter.KEY_ROWID);
		    
		    String intentState = extras.getString("intent");
		    if (intentState != null && intentState.equals("view")) {
		    	confirmButton.setText("Save Item");
		    }

		    if (itemType != null) {
		        mItemTypeText.setText(itemType);
	            Log.w(TAG, "Setting ItemType to " + itemType);
		    }
		    if (store != null) {
		        mStoreText.setText(store);
	            Log.w(TAG, "Setting Store to " + store);
		    }
		    if (quantity != null) {
	            Log.w(TAG, "Setting Quantity to " + Double.toString(quantity));
		        mQuantityText.setText(Double.toString(quantity));
		    }
		    if (threshold != null) {
	            Log.w(TAG, "Setting Threshold to " + Double.toString(threshold));
		        mThresholdText.setText(Double.toString(threshold));
		    }
		}
		
		confirmButton.setOnClickListener(new View.OnClickListener() {

		    public void onClick(View view) {
		    	Bundle bundle = new Bundle();

		    	bundle.putString(ItemsDbAdapter.KEY_ITEM_TYPE, mItemTypeText.getText().toString());
		    	bundle.putString(ItemsDbAdapter.KEY_STORE, mStoreText.getText().toString());
		    	bundle.putDouble(ItemsDbAdapter.KEY_QUANTITY, 
		    			Double.parseDouble(mQuantityText.getText().toString()));
		    	bundle.putDouble(ItemsDbAdapter.KEY_THRESHOLD, 
		    			Double.parseDouble(mThresholdText.getText().toString()));
		    	if (mRowId != null) {
		    	    bundle.putLong(ItemsDbAdapter.KEY_ROWID, mRowId);
		    	}
		    	
		    	Intent mIntent = new Intent();
		    	mIntent.putExtras(bundle);
		    	setResult(RESULT_OK, mIntent);
		    	finish();
		    }
		    
		});
	}
}