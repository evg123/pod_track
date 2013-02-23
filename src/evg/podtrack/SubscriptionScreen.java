package evg.podtrack;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.TextView;
import android.net.Uri;

public class SubscriptionScreen extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor>, OnItemLongClickListener
{
	private static final int LOADER_ID = 2;
	
	ActionMode actionMode;
	Subscription sub;
    long subId;
    long selectedItemId = -1;
    View selectedView = null;
    private SimpleCursorAdapter adapter;
    StorageHelper sh;
    
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		Bundle extras = getIntent().getExtras();
		subId = extras.getLong("subId");
        
		sh = new StorageHelper(this);
		
        sub = getSubFromDb(subId);
		
		setFeedItemListAdapter();
	}
	
	private Subscription getSubFromDb(long subId) {
		String[] projection = {
    			DbDefinition.SubscriptionTable._ID,
    			DbDefinition.SubscriptionTable.COLUMN_NAME_NAME,
    			DbDefinition.SubscriptionTable.COLUMN_NAME_SUB_IMAGE_URL,
    			DbDefinition.SubscriptionTable.COLUMN_NAME_DESCRIPTION,
    			DbDefinition.SubscriptionTable.COLUMN_NAME_SUB_IMAGE_URL,
    			DbDefinition.SubscriptionTable.COLUMN_NAME_SUB_IMAGE_PATH,
    	};
		
		String selection = DbDefinition.SubscriptionTable._ID + "=?";
        String[] selectionArgs = {""+subId};
		
		Cursor cursor = getContentResolver().query(DbContentProvider.SUBS_URI, projection, selection, selectionArgs, null);
		cursor.moveToFirst();
		Subscription sub = Subscription.subscriptionFromCursor(cursor, this);
		cursor.close();
		return sub;
	}
	
    private void setFeedItemListAdapter() 
    {
		String[] fromColumns = {
            DbDefinition.FeedItemTable.COLUMN_NAME_TITLE,
            DbDefinition.FeedItemTable.COLUMN_NAME_PUB_DATE,
            DbDefinition.FeedItemTable.COLUMN_NAME_FILE_LINK,
        };
		
        int[] toViews = {
            R.id.item_name,
            R.id.item_date,
            R.id.item_file_link,
        };

		adapter = new SimpleCursorAdapter(this, R.layout.feed_item_list_item, null, fromColumns, toViews, 0);
		setListAdapter(adapter);
		getLoaderManager().initLoader(LOADER_ID, null, this);
		
		ListView lv = getListView();
		lv.setOnItemLongClickListener(this);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.subscription_screen_action, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(this, TitleScreen.class));
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            case R.id.refresh:
            	ProgressDialog progDialog = ProgressDialog.show(this, "test", "Refreshing Subscription", true);
                progDialog.show();
                
                // TODO: update the existing subscription
                // this probably just means checking for new items, and reading sub info again
                
            	progDialog.dismiss();
            	return true;
            case R.id.unsubscribe:
            	Subscription.unsubscribe(this, subId);
            	// we no longer want to be on this screen
            	finish();
            	return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
	@Override
	public void onListItemClick(ListView lv, View view, int pos, long id) 
	{
		Intent intent = new Intent();
		intent.putExtra("itemId", id);
		intent.setComponent(new ComponentName(this, FeedItemViewScreen.class));
		startActivity(intent);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> av, View view, int pos, long id) 
	{
        if (actionMode != null) {
            return false;
        }
        
        actionMode = startActionMode(actionModeCallback);
        selectedItemId = id;
        selectedView = view;
        return true;
	}
	
	private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
		
	    @Override
	    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
	        // Inflate a menu resource providing context menu items
	        MenuInflater inflater = mode.getMenuInflater();
	        inflater.inflate(R.menu.subscription_screen_context, menu);
	        return true;
	    }

	    // Called each time the action mode is shown. Always called after onCreateActionMode, but
	    // may be called multiple times if the mode is invalidated.
	    @Override
	    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
	        return false;
	    }

	    // Called when the user selects a contextual menu item
	    @Override
	    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
	        switch (item.getItemId()) {
	        case R.id.addToQueue:
                FeedItem.addToQueue(getApplicationContext(), selectedItemId);
                mode.finish();
                return true;
	        case R.id.download:
	        	BroadcastReceiver onDlComplete = new BroadcastReceiver() {
	    			@Override
	    			public void onReceive(Context ctxt, Intent intent) {
	    				// nothing for now
	    			}
	    		};
	    		TextView linkView = (TextView)selectedView.findViewById(R.id.item_file_link);
	    		String linkStr = linkView.getText().toString();
	    		Uri link = Uri.parse(linkStr);
	        	sh.downloadFeedItem(selectedItemId, link, sub.name, onDlComplete);
                mode.finish();
                return true;
	            default:
	                return false;
	        }
	    }

	    // Called when the user exits the action mode 
	    @Override
	    public void onDestroyActionMode(ActionMode mode) {
	        actionMode = null;
	    }
	};

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {

		String selection = DbDefinition.FeedItemTable.COLUMN_NAME_SUB_ID + "=?";
		String[] selectionArgs = {""+subId};

		String[] projection = {
            DbDefinition.FeedItemTable._ID,
            DbDefinition.FeedItemTable.COLUMN_NAME_TITLE,
            DbDefinition.FeedItemTable.COLUMN_NAME_PUB_DATE,
            DbDefinition.FeedItemTable.COLUMN_NAME_FILE_LINK,
        };
        
    	CursorLoader cursorLoader = new CursorLoader(this, DbContentProvider.FIS_URI, projection, selection, selectionArgs, null);
    	return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    	adapter.swapCursor(data);
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
		
	}


}
