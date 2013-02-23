package evg.podtrack;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

public class PlayerService extends Service implements OnPreparedListener, OnErrorListener {
	
	private PlayerServiceBinder playerBinder = new PlayerServiceBinder();
	private MediaPlayer player;
	
	@Override
	public void onCreate() {
		/*
	 	see localservice example on http://developer.android.com/reference/android/app/Service.html for more
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        // Display a notification about the service starting
        showNotification();
        */
		
		player = new MediaPlayer();
		player.setOnPreparedListener(this);
		player.setOnErrorListener(this);
		
	}
	
	public void setSource(String uriStr) {
		try {
			Uri uri = Uri.parse(uriStr);
			player.setDataSource(getApplicationContext(), uri);
		} catch (Exception ex) {
			Toast.makeText(this, "error playing file: " + uriStr, Toast.LENGTH_SHORT).show();
		}
		player.prepareAsync();
	}
	
	public void play() {
		player.start();
		
	}
	
	public void pause() {		
		player.pause();
	}

	public void stop() {
		player.stop();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// eventually use this instead of binding, to allow it to run in the background
		return START_STICKY;
	}
	
	@Override
    public void onDestroy() {
		Toast.makeText(this, "PlayerService stopped", Toast.LENGTH_SHORT).show();
    }
	
	@Override
	public IBinder onBind(Intent intent) {
		return playerBinder;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		// TODO: activate the start button
		Toast.makeText(this, "player is ready", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		return false;
	}
	
	public class PlayerServiceBinder extends Binder {
		PlayerService getService() {
            return PlayerService.this;
        }
    }
	
}
