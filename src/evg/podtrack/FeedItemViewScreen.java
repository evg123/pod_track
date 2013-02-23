package evg.podtrack;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class FeedItemViewScreen extends Activity 
{
	private FeedItem feedItem;
	private long itemId;
	StorageHelper sh;
	
	private PlayerService playerService = null;
	private boolean isBound = false;
    
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		Bundle extras = getIntent().getExtras();
		itemId = extras.getLong("itemId");
		
		feedItem = getFeedItemFromDb(itemId);
        setupFeedItemView();
        
        sh = new StorageHelper(this);
        
        bindService();
	}
    
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.feed_item_view_screen_action, menu);
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
            case R.id.play:
            	// dont need?
            	return true;
            case R.id.delete:
            	return true;
            case R.id.add_to_queue:
            	FeedItem.addToQueue(getApplicationContext(), itemId);
            	return true;
            case R.id.download:
            	BroadcastReceiver onDlComplete = new BroadcastReceiver() {
	    			@Override
	    			public void onReceive(Context ctxt, Intent intent) {
	    				// nothing for now
	    			}
	    		};
            	sh.downloadFeedItem(itemId, feedItem.link, feedItem.subName, onDlComplete);
            	setSource(); // TODO this doesnt work, need to wait until the download finishes
            	return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
	
    // possibly move this out to a util class
    private FeedItem getFeedItemFromDb(long itemId)
    {
        String selection = DbDefinition.FeedItemTable._ID + "=?";
        String[] selectionArgs = {""+itemId};
        
		String[] projection = {
            DbDefinition.FeedItemTable._ID,
            DbDefinition.FeedItemTable.COLUMN_NAME_TITLE,
            DbDefinition.FeedItemTable.COLUMN_NAME_FILE_LINK,
            DbDefinition.FeedItemTable.COLUMN_NAME_SUB_ID,
            DbDefinition.FeedItemTable.COLUMN_NAME_SUB_NAME,
            DbDefinition.FeedItemTable.COLUMN_NAME_PUB_DATE,
            DbDefinition.FeedItemTable.COLUMN_NAME_DESCRIPTION,
            DbDefinition.FeedItemTable.COLUMN_NAME_DOWNLOADED,
            DbDefinition.FeedItemTable.COLUMN_NAME_LISTENED,
        };
		
		Cursor cursor = getContentResolver().query(DbContentProvider.FIS_URI, projection, selection, selectionArgs, null);
		
        cursor.moveToFirst();
	    FeedItem item = FeedItem.feedItemFromCursor(cursor);
		cursor.close();
		
		return item;
    }
    
    private void setupFeedItemView()
    {
        setContentView(R.layout.feed_item_view_screen);

        TextView tv;
        ImageView iv;
        Button btn;
        
        tv = (TextView)findViewById(R.id.title);
        tv.setText(feedItem.title);
        
        tv = (TextView)findViewById(R.id.file_link);
        tv.setText(feedItem.link.toString());
        
        tv = (TextView)findViewById(R.id.pub_date);
        tv.setText(feedItem.pubDate.toString());
        
        tv = (TextView)findViewById(R.id.sub_name);
        tv.setText(feedItem.subName);
        
        tv = (TextView)findViewById(R.id.description);
        tv.setText(feedItem.description);
        
        tv = (TextView)findViewById(R.id.item_id);
        tv.setText(String.valueOf(feedItem.id));
        
        //iv = getViewById(R.id.sub_image);
        //tv.setViewImage(item.);

        btn = (Button)findViewById(R.id.play_btn);
        btn.setOnClickListener(new View.OnClickListener() {
        	@Override
        	public void onClick(View v) {
                playerPlay();
            }
        });
        
        btn = (Button)findViewById(R.id.pause_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
        	public void onClick(View v) {
                playerPause();
            }
        });
        
        btn = (Button)findViewById(R.id.stop_btn);
        btn.setOnClickListener(new View.OnClickListener() {
        	@Override
        	public void onClick(View v) {
                playerStop();
            }
        });

    }
    
    private void playerPlay() {

    	if (isBound) {
    		playerService.play();
    	}
    }
    
    private void playerPause() {
    	if (isBound) {
    		playerService.pause();
    	}
    }
    
    private void playerStop() {
    	if (isBound) {
    		playerService.stop();
    	}
    }

    void bindService() {
    	Intent bindIntent = new Intent(this, PlayerService.class);
    	bindService(bindIntent, playerServiceConnection, Context.BIND_AUTO_CREATE);
    }

    void doUnbindService() {
    	if (isBound) {
    		// Detach our existing connection.
    		unbindService(playerServiceConnection);
    		isBound = false;
    	}
    }

    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	doUnbindService();
    }

    private ServiceConnection playerServiceConnection = new ServiceConnection() {
    	public void onServiceConnected(ComponentName className, IBinder service) {
    		playerService = ((PlayerService.PlayerServiceBinder)service).getService();
    		isBound = true;
    		setSource();
    	}

    	public void onServiceDisconnected(ComponentName className) {
            playerService = null;
        }
    };
    
    private void setSource() {
    	String filename = feedItem.link.getLastPathSegment();
		String fileLoc = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS) + "/" + feedItem.subName + "/" + filename;
		playerService.setSource(fileLoc);
    }
}





