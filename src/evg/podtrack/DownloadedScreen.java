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

public class DownloadedScreen extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor>, OnItemLongClickListener
{
	private static final int LOADER_ID = 33;
	
	private SimpleCursorAdapter adapter;
	private ActionMode actionMode;
	private long selectedItemId = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setDownloadedListAdapter();
	}
	
	private void setDownloadedListAdapter() 
    {
		String[] fromColumns = {
            DbDefinition.FeedItemTable.COLUMN_NAME_TITLE,
            DbDefinition.FeedItemTable.COLUMN_NAME_PUB_DATE,
            DbDefinition.FeedItemTable.COLUMN_NAME_FILE_LINK,
            DbDefinition.FeedItemTable.COLUMN_NAME_DOWNLOADED,
        };
		
        int[] toViews = {
            R.id.item_name,
            R.id.item_date,
            R.id.item_file_link,
            R.id.downloaded,
        };

		adapter = new SimpleCursorAdapter(this, R.layout.downloaded_list_item, null, fromColumns, toViews, 0);
		setListAdapter(adapter);
		getLoaderManager().initLoader(LOADER_ID, null, this);
		
		ListView lv = getListView();
		lv.setOnItemLongClickListener(this);
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
        return true;
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.downloaded_screen_action, menu);
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {

    	@Override
    	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
    		MenuInflater inflater = mode.getMenuInflater();
    		inflater.inflate(R.menu.downloaded_screen_context, menu);
    		return true;
    	}

    	// Called each time the action mode is shown. Always called after onCreateActionMode, but
    	// may be called multiple times if the mode is invalidated.
    	@Override
    	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
    		return false;
    	}
    	
    	@Override
    	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
    		switch (item.getItemId()) {
    		case R.id.delete:
    			FeedItem.delete(getApplicationContext(), selectedItemId);
    			mode.finish();
    			return true;
    		case R.id.addToQueue:
    			
    			mode.finish();
    			return true;
    		case R.id.markAsListened:
    			
    			mode.finish();
    			return true;
    		case R.id.removeFromQueue:
    			
    			mode.finish();
    			return true;
    		case R.id.viewSub:
    			
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

		String selection = DbDefinition.FeedItemTable.COLUMN_NAME_DOWNLOADED + "=1";

		String[] projection = {
            DbDefinition.FeedItemTable._ID,
            DbDefinition.FeedItemTable.COLUMN_NAME_TITLE,
            DbDefinition.FeedItemTable.COLUMN_NAME_PUB_DATE,
            DbDefinition.FeedItemTable.COLUMN_NAME_FILE_LINK,
            DbDefinition.FeedItemTable.COLUMN_NAME_DOWNLOADED,
        };
        
    	CursorLoader cursorLoader = new CursorLoader(this, DbContentProvider.FIS_URI, projection, selection, null, null);
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
