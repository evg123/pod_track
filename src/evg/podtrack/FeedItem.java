package evg.podtrack;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;

public class FeedItem {
	
	public long id;
	public String title;
	public Uri link;
    public String subName;
  	public long subId;
	public Date pubDate;
	public String description;
	public int downloaded;
	public int listened;
	    
    public static FeedItem feedItemFromCursor(Cursor cursor) {
		FeedItem item = new FeedItem();
		
		item.id = cursor.getLong(0);
        item.title = cursor.getString(1);
        item.link = Uri.parse(cursor.getString(2));
        item.subId = cursor.getLong(3);
        item.subName = cursor.getString(4);
        try {
			item.pubDate = DateFormat.getInstance().parse(cursor.getString(5));
		} catch (ParseException e) {
			// TODO error handling
			item.pubDate = new Date();
		}
        item.description = cursor.getString(6);
        item.downloaded = cursor.getInt(7);
        item.listened = cursor.getInt(8);
        
		return item;
	}
    

	public static void delete(Context context, long itemId) {
		// set the feed item state to not downloaded
		ContentValues values = new ContentValues();
		values.put(DbDefinition.FeedItemTable.COLUMN_NAME_DOWNLOADED, 0);
		String selection = DbDefinition.FeedItemTable._ID + "=?";
		String[] selectionArgs = {""+itemId};
		context.getContentResolver().update(DbContentProvider.FIS_URI, values, selection, selectionArgs);
		
		// delete the file on disk
		String[] projection = {
			DbDefinition.FeedItemTable.COLUMN_NAME_SUB_NAME,
			DbDefinition.FeedItemTable.COLUMN_NAME_TITLE,
		};
		Cursor cursor = context.getContentResolver().query(DbContentProvider.FIS_URI, projection, selection, selectionArgs, null);
		cursor.moveToFirst();
		String subName = cursor.getString(0);
		String title = cursor.getString(1);
		File file = FeedItem.getFile(subName, title);
		file.delete();
	}
	
	public static File getFile(String subName, String title) {
		File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS) + subName + "/" + title);
		return file;
	}
	
    public static void clearQueue(Context context) {
		context.getContentResolver().delete(DbContentProvider.QUEUE_URI, null, null);
	}
	
	public static void addToQueue(Context context, long itemId) {
		ContentValues queueValues = new ContentValues();
		queueValues.put(DbDefinition.QueueTable.COLUMN_NAME_FI_ID, itemId);
		context.getContentResolver().insert(DbContentProvider.QUEUE_URI, queueValues);
	}
	
	public static void removeFromQueue(Context context, long itemId) {
		String selection = DbDefinition.QueueTable.COLUMN_NAME_FI_ID + "=?";
        String[] selectionArgs = {""+itemId};
		context.getContentResolver().delete(DbContentProvider.QUEUE_URI, selection, selectionArgs);
	}

	public static void changeQueuePos(Context context, long itemId, long newPos) {
		ContentValues values = new ContentValues();
		values.put(DbDefinition.QueueTable.COLUMN_NAME_FI_ID, itemId);
		values.put(DbDefinition.QueueTable.COLUMN_NAME_QUEUE_POS, newPos);
		
		String selection = DbDefinition.QueueTable.COLUMN_NAME_FI_ID + "=?";
        String[] selectionArgs = {""+itemId};
		context.getContentResolver().update(DbContentProvider.QUEUE_URI, values, selection, selectionArgs);
	}
	
	
}
