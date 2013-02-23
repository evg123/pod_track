package evg.podtrack;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.ComponentName;
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
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class QueueScreen extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor>, OnItemLongClickListener
{
	private static final int LOADER_ID = 9;
	
	private ActionMode mActionMode;
	private SimpleCursorAdapter adapter;
	private long selectedItemId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setQueueListAdapter();
	}	
	
	private void setQueueListAdapter() 
    {
		String[] fromColumns = {
			DbDefinition.QueueTable.COLUMN_NAME_QUEUE_POS,
            DbDefinition.FeedItemTable.COLUMN_NAME_TITLE,
            DbDefinition.FeedItemTable.COLUMN_NAME_PUB_DATE,
            DbDefinition.FeedItemTable.COLUMN_NAME_FILE_LINK,
        };
		
        int[] toViews = {
        	R.id.queue_pos,
            R.id.item_name,
            R.id.item_date,
            R.id.item_file_link,
        };
        
		adapter = new SimpleCursorAdapter(this, R.layout.queue_list_item, null, fromColumns, toViews, 0);
		setListAdapter(adapter);
		getLoaderManager().initLoader(LOADER_ID, null, this);
		
		ListView lv = getListView();
		lv.setOnItemLongClickListener(this);
    }
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.queue_screen_action, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                Intent intent = new Intent(this, TitleScreen.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            case R.id.clearQueue:
            	FeedItem.clearQueue(this);
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
	public boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id) 
	{
		
        if (mActionMode != null) {
            return false;
        }

        // Start the CAB using the ActionMode.Callback defined above
        mActionMode = startActionMode(actionModeCallback);
        selectedItemId = id;
        return true;
	}
	
	private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {

	    // Called when the action mode is created; startActionMode() was called
	    @Override
	    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
	        // Inflate a menu resource providing context menu items
	        MenuInflater inflater = mode.getMenuInflater();
	        inflater.inflate(R.menu.queue_screen_context, menu);
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
	    	long newPos = -1;
	        switch (item.getItemId()) {
		        case R.id.play:
	                play(selectedItemId);
	                mode.finish();
	                return true;    
		        case R.id.removeFromQueue:
		        	FeedItem.removeFromQueue(getApplicationContext(), selectedItemId);
	                mode.finish();
	                return true;
	            case R.id.moveToTop:
	            	FeedItem.changeQueuePos(getApplicationContext(), selectedItemId, 1);
	                mode.finish();
	                return true;
	            case R.id.moveToBottom:
	            	newPos = adapter.getCount();
	            	FeedItem.changeQueuePos(getApplicationContext(), selectedItemId, newPos);
	                mode.finish();
	                return true;
	            default:
	                return false;
	        }
	    }

	    // Called when the user exits the action mode
	    @Override
	    public void onDestroyActionMode(ActionMode mode) {
	        mActionMode = null;
	    }
	};
	
	private void play(long itemId) {
		// later
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		String sortOrder = DbDefinition.QueueTable.COLUMN_NAME_QUEUE_POS;
        
    	CursorLoader cursorLoader = new CursorLoader(this, DbContentProvider.QUEUE_ITEMS_URI, null, null, null, sortOrder);
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
