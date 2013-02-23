package evg.podtrack;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class DbContentProvider extends ContentProvider {

	private static final String AUTHORITY = "evg.podtrack.provider.db";
	
	private static final int QUEUE = 100;
	private static final int QUEUE_ITEMS = 101;
	private static final int SUBS = 200;
	//private static final int SUB_ID = 201;
	private static final int FEED_ITEMS = 300;
	//private static final int FEED_ITEM_ID = 301;

	private static final String QUEUE_PATH = "queue";
	private static final String QUEUE_ITEMS_PATH = "queue_items";
	private static final String SUBS_PATH = "subscription";
	//private static final String SUB_ID_PATH = "subscription/#";
	private static final String FIS_PATH = "feed_item";
	//private static final String FI_ID_PATH = "feed_item/#";

	public static final Uri QUEUE_URI = Uri.parse("content://" + AUTHORITY + "/" + QUEUE_PATH);
	public static final Uri QUEUE_ITEMS_URI = Uri.parse("content://" + AUTHORITY + "/" + QUEUE_ITEMS_PATH);
	public static final Uri SUBS_URI = Uri.parse("content://" + AUTHORITY + "/" + SUBS_PATH);
	//public static final Uri SUB_ID_URI = Uri.parse("content://" + AUTHORITY + "/" + SUB_ID_PATH);
	public static final Uri FIS_URI = Uri.parse("content://" + AUTHORITY + "/" + FIS_PATH);
	//public static final Uri FI_ID_URI = Uri.parse("content://" + AUTHORITY + "/" + FI_ID_PATH);
	
	private static final UriMatcher uriMatcher;
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITY, QUEUE_PATH, QUEUE);
		uriMatcher.addURI(AUTHORITY, QUEUE_ITEMS_PATH, QUEUE_ITEMS);
		uriMatcher.addURI(AUTHORITY, SUBS_PATH, SUBS);
		//uriMatcher.addURI(AUTHORITY, SUB_ID_PATH, SUB_ID);      
		uriMatcher.addURI(AUTHORITY, FIS_PATH, FEED_ITEMS);
		//uriMatcher.addURI(AUTHORITY, FI_ID_PATH, FEED_ITEM_ID);      
	}
	
	private DbHelper dbh;

	@Override
	public boolean onCreate() {
		dbh = new DbHelper(getContext());
		return false;
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,	String[] selectionArgs, String sortOrder) {
		SQLiteDatabase db = dbh.getReadableDatabase();
		Cursor cursor;

		int uriType = uriMatcher.match(uri);
		switch (uriType) {
		case QUEUE:
			cursor = db.query(DbDefinition.QueueTable.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
			cursor.setNotificationUri(getContext().getContentResolver(), QUEUE_URI);
			break;
		case QUEUE_ITEMS:
			// this only supports getting the entire queue
			String sqlStr = "SELECT * FROM "+DbDefinition.QueueTable.TABLE_NAME+" qt, "+DbDefinition.FeedItemTable.TABLE_NAME+
							" fit WHERE "+DbDefinition.QueueTable.COLUMN_NAME_FI_ID+"=fit."+DbDefinition.FeedItemTable._ID+" ORDER BY "+sortOrder;
			cursor = db.rawQuery(sqlStr, null);
			cursor.setNotificationUri(getContext().getContentResolver(), QUEUE_URI);
			break;
		case SUBS:
			cursor = db.query(DbDefinition.SubscriptionTable.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
			cursor.setNotificationUri(getContext().getContentResolver(), SUBS_URI);
			break;
		case FEED_ITEMS:
			cursor = db.query(DbDefinition.FeedItemTable.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
			cursor.setNotificationUri(getContext().getContentResolver(), FIS_URI);
			break;
		default:
			throw new IllegalArgumentException("Invalid URI: " + uri);
		}
		
		return cursor;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = dbh.getWritableDatabase();
		long id;
		Uri newUri;
		
		int uriType = uriMatcher.match(uri);
		switch (uriType) {
		case QUEUE:
			// when inserting into the queue, add the item to the end
			// this requires some extra manipulation of the insert request
			// could replace this with a trigger?
			long queuePos = DatabaseUtils.queryNumEntries(db, DbDefinition.QueueTable.TABLE_NAME) + 1;
			values.put(DbDefinition.QueueTable.COLUMN_NAME_QUEUE_POS, queuePos);
			id = db.insert(DbDefinition.QueueTable.TABLE_NAME, null, values);
			getContext().getContentResolver().notifyChange(QUEUE_URI, null);
			newUri = Uri.parse(QUEUE_URI + "/" + id);
			break;
		case SUBS:
			id = db.insert(DbDefinition.SubscriptionTable.TABLE_NAME, null, values);
			getContext().getContentResolver().notifyChange(SUBS_URI, null);
			newUri = Uri.parse(SUBS_URI + "/" + id);
			break;
		case FEED_ITEMS:
			id = db.insert(DbDefinition.FeedItemTable.TABLE_NAME, null, values);
			getContext().getContentResolver().notifyChange(FIS_URI, null);
			newUri = Uri.parse(FIS_URI + "/" + id);
			break;
		default:
			throw new IllegalArgumentException("Invalid URI: " + uri);
		}
		
		return newUri;
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = dbh.getWritableDatabase();
		int numDeleted;
		
		int uriType = uriMatcher.match(uri);
		switch (uriType) {
		case QUEUE:
			numDeleted = db.delete(DbDefinition.QueueTable.TABLE_NAME, selection, selectionArgs);
			getContext().getContentResolver().notifyChange(QUEUE_URI, null);
			break;
		case SUBS:
			numDeleted = db.delete(DbDefinition.SubscriptionTable.TABLE_NAME, selection, selectionArgs);
			getContext().getContentResolver().notifyChange(SUBS_URI, null);
			getContext().getContentResolver().notifyChange(FIS_URI, null);
			getContext().getContentResolver().notifyChange(QUEUE_URI, null);
			break;
		case FEED_ITEMS:
			numDeleted = db.delete(DbDefinition.FeedItemTable.TABLE_NAME, selection, selectionArgs);
			getContext().getContentResolver().notifyChange(FIS_URI, null);
			getContext().getContentResolver().notifyChange(QUEUE_URI, null);
			break;
		default:
			throw new IllegalArgumentException("Invalid URI: " + uri);
		}
		
		return numDeleted;
	}
	
	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SQLiteDatabase db = dbh.getWritableDatabase();
		int numUpdated;
		
		int uriType = uriMatcher.match(uri);
		switch (uriType) {
		case QUEUE:
			// delete the queue item then insert to allow db triggers to handle queue_pos updating
			// TODO look into creating update triggers that dont interfere with each other
			numUpdated = db.delete(DbDefinition.QueueTable.TABLE_NAME, selection, selectionArgs);
			db.insert(DbDefinition.QueueTable.TABLE_NAME, null, values);
			getContext().getContentResolver().notifyChange(QUEUE_URI, null);
			break;
		case SUBS:
			numUpdated = db.update(DbDefinition.SubscriptionTable.TABLE_NAME, values, selection, selectionArgs);
			getContext().getContentResolver().notifyChange(SUBS_URI, null);
			break;
		case FEED_ITEMS:
			numUpdated = db.update(DbDefinition.FeedItemTable.TABLE_NAME, values, selection, selectionArgs);
			getContext().getContentResolver().notifyChange(FIS_URI, null);
			break;
		default:
			throw new IllegalArgumentException("Invalid URI: " + uri);
		}
		
		return numUpdated;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

}
