package evg.podtrack;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class TitleScreen extends Activity 
{
	/*
	 * TODO go here
	 * ------------
	 * 
	 * add triggers to delete feed items and queue items when a sub is deleted
	 * add triggers to reorder queue when queue items are deleted
	 * 
	 */
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.title_screen);
		initButtons();
		
		//addSubscription("http://www.giantbomb.com/podcast-xml/");
		//addSubscription("http://www.tested.com/podcast-xml/this-is-only-a-test/");
		//addSubscription("http://www.tested.com/podcast-xml/still-untitled-the-adam-savage-project/");
		//addSubscription("http://feeds.feedburner.com/mbmbam");
		
	}
	
	public void initButtons()
    {
		Button btn;
		OnClickListener listener;
		
    	btn = (Button)findViewById(R.id.queueBtn);
    	listener = new View.OnClickListener()
    	{	
			@Override
			public void onClick(View v) 
			{
				startQueueScreen();
			}
		};
    	btn.setOnClickListener(listener);
    	
    	btn = (Button)findViewById(R.id.downloadedBtn);
    	listener = new View.OnClickListener()
    	{	
			@Override
			public void onClick(View v) 
			{
				startDownloadedScreen();
			}
		};
    	btn.setOnClickListener(listener);
    	
    	btn = (Button)findViewById(R.id.subsBtn);
    	listener = new View.OnClickListener()
    	{	
			@Override
			public void onClick(View v) 
			{
				startSubsScreen();
			}
		};
		btn.setOnClickListener(listener);
		
    	btn = (Button)findViewById(R.id.optionsBtn);
    	listener = new View.OnClickListener()
    	{	
			@Override
			public void onClick(View v) 
			{
				startOptionsScreen();
			}
		};
    	btn.setOnClickListener(listener);
    }
	
	private void startQueueScreen()
	{
		
		Intent intent = new Intent();
		intent.setComponent(new ComponentName(this, QueueScreen.class));
		startActivity(intent);
	}
	
	private void startDownloadedScreen()
	{
		
		Intent intent = new Intent();
		intent.setComponent(new ComponentName(this, DownloadedScreen.class));
		startActivity(intent);
	}
	
	private void startSubsScreen()
	{
		
		Intent intent = new Intent();
		intent.setComponent(new ComponentName(this, SubsListScreen.class));
		startActivity(intent);
	}
	
	private void startOptionsScreen()
	{
		
		Intent intent = new Intent();
		intent.setComponent(new ComponentName(this, OptionScreen.class));
		startActivity(intent);
	}	
	
	
	
}