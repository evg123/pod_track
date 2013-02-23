package evg.podtrack;

import evg.podtrack.NewSubDialogFragment.NewSubDialogListener;

import android.app.FragmentManager;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.ComponentName;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class SubsListScreen extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor>, NewSubDialogListener, OnItemLongClickListener
{
	private static final int LOADER_ID = 7;
	
	private SimpleCursorAdapter adapter;
	private ActionMode actionMode;
	private long selectedSubId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setSubsListAdapter();
	}
	
	private void setSubsListAdapter() {
		String[] fromColumns = {
				DbDefinition.SubscriptionTable.COLUMN_NAME_NAME,
				DbDefinition.SubscriptionTable.COLUMN_NAME_SUB_IMAGE_PATH,
		};
		int[] toViews = {
				R.id.sub_name,
				R.id.sub_icon,
		};
		
		adapter = new SimpleCursorAdapter(this, R.layout.subscription_list_item, null, fromColumns, toViews, 0) {
			@Override
			public void setViewImage (ImageView v, String value) {
				super.setViewImage(v, value);
				String imgPath = getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES) + StorageHelper.SUB_IMAGE_PATH + value;
				Bitmap bitm = BitmapFactory.decodeFile(imgPath);
				v.setImageBitmap(bitm);

			}
		};
		
		setListAdapter(adapter);
		getLoaderManager().initLoader(LOADER_ID, null, this);
		
		ListView lv = getListView();
		lv.setOnItemLongClickListener(this);
	}
	
	@Override
	public void onListItemClick(ListView lv, View view, int pos, long id) 
	{
		Intent intent = new Intent();
		intent.putExtra("subId", id);
		intent.setComponent(new ComponentName(this, SubscriptionScreen.class));
		startActivity(intent);
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> av, View view, int pos, long id) 
	{
        if (actionMode != null) {
            return false;
        }
        
        actionMode = startActionMode(actionModeCallback);
        selectedSubId = id;
        return true;
	}
	
	@Override
	public void onPositiveClick(NewSubDialogFragment dialog) {
		// dont need anymore, might be useful later
	}
	
	@Override
    public void onNegativeClick(NewSubDialogFragment dialog) {
    	// nothing to do
    }
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.subslist_screen_action, menu);
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
            case R.id.newSub:
            	FragmentManager fm = getFragmentManager();
            	NewSubDialogFragment diag = new NewSubDialogFragment();
            	diag.show(fm, "new_sub_dialog");
            	return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {

    	@Override
    	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
    		MenuInflater inflater = mode.getMenuInflater();
    		inflater.inflate(R.menu.subs_list_screen_context, menu);
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
    		case R.id.unsubscribe:
    			Subscription.unsubscribe(getApplicationContext(), selectedSubId);
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
    	String[] projection = {
    			DbDefinition.SubscriptionTable._ID,
    			DbDefinition.SubscriptionTable.COLUMN_NAME_NAME,
    			DbDefinition.SubscriptionTable.COLUMN_NAME_SUB_IMAGE_URL,
    			DbDefinition.SubscriptionTable.COLUMN_NAME_SUB_IMAGE_PATH,
    	};

    	CursorLoader cursorLoader = new CursorLoader(this, DbContentProvider.SUBS_URI, projection, null, null, null);
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
