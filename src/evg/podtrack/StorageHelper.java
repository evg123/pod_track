package evg.podtrack;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.util.ByteArrayBuffer;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;

public class StorageHelper {
	
	public static final String SUB_IMAGE_PATH = "/podtrack/subimage/";
	//public static final String AUDIO_PATH = "/podtrack/audio/";
	
	private File imgDir;
	//private File audioDir;
	private DownloadManager dlMgr;
	private Context context;
	
	public StorageHelper(Context appContext) {
		context = appContext;
		
		// images directory
		imgDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + SUB_IMAGE_PATH);
		imgDir.mkdirs();
	
		// audio files directory
		//audioDir = context.getExternalFilesDir(Environment.DIRECTORY_PODCASTS);
		//audioDir.mkdirs();
		
		dlMgr = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
	}

	public void downloadFeedItem(long itemId, Uri link, String subName, BroadcastReceiver onDlComplete) {
		context.registerReceiver(onDlComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)); // TODO move this to a seperate call, only need to do once per calling activity
		
		String filename = link.getLastPathSegment();
		
		DownloadManager.Request req = new DownloadManager.Request(link);
		req.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
		req.setAllowedOverRoaming(false);
		req.setTitle(subName + " - " + filename);
		req.setDescription("a podcast");
		File subDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS), subName + "/"); // TODO: come back to this - do we need the mkdirs?
		subDir.mkdirs();
		req.setDestinationInExternalPublicDir(Environment.DIRECTORY_PODCASTS + "/" + subName + "/", filename);
		dlMgr.enqueue(req);
		
		// debug code
		/**/
		long dlId = dlMgr.enqueue(req);
		Cursor dlCursor = dlMgr.query(new Query());
		dlCursor.moveToFirst();
		while (!dlCursor.isAfterLast()) {
			int status = dlCursor.getInt(dlCursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
			int reason = dlCursor.getInt(dlCursor.getColumnIndex(DownloadManager.COLUMN_REASON));
			
			dlCursor.moveToNext();
		}
		/**/
		
		// update the feed item in the db
		ContentValues values = new ContentValues();
		values.put(DbDefinition.FeedItemTable.COLUMN_NAME_DOWNLOADED, 1);
		String selection = DbDefinition.FeedItemTable._ID + "=?";
		String[] selectionArgs = {""+itemId};
		context.getContentResolver().update(DbContentProvider.FIS_URI, values, selection, selectionArgs);
	}
	
	// note: i didn't write this
	public void downloadImage(String imageUrl) 
	{
		try {
			URL url = new URL(imageUrl);
			File file = new File(imgDir, ""+Math.abs(imageUrl.hashCode()));

			URLConnection ucon = url.openConnection();
			InputStream is = ucon.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);

			ByteArrayBuffer baf = new ByteArrayBuffer(50);
			int current = 0;
			while ((current = bis.read()) != -1) {
				baf.append((byte) current);
			}

			FileOutputStream fos = new FileOutputStream(file);
			fos.write(baf.toByteArray());
			fos.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
