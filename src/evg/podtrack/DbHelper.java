package evg.podtrack;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {
	
	// key definitions for metadata table
	public static final int META_QUEUE_KEY = 1; // a comma seperated list of quoted strings: 'one', 'two', 'three'
	
	private static final String SQL_CREATE_SUBSCRIPTION_TABLE =
	    "CREATE TABLE " + DbDefinition.SubscriptionTable.TABLE_NAME + " (" +
	    DbDefinition.SubscriptionTable._ID + " INTEGER PRIMARY KEY, " +
	    DbDefinition.SubscriptionTable.COLUMN_NAME_NAME + " TEXT, " + 
	    DbDefinition.SubscriptionTable.COLUMN_NAME_SUB_URL + " TEXT, " +
	    DbDefinition.SubscriptionTable.COLUMN_NAME_DESCRIPTION + " TEXT, " +
	    DbDefinition.SubscriptionTable.COLUMN_NAME_SUB_IMAGE_URL + " TEXT, " +
	    DbDefinition.SubscriptionTable.COLUMN_NAME_SUB_IMAGE_PATH + " TEXT " +
	    " );";
	
	private static final String SQL_CREATE_FEED_ITEM_TABLE =
	    "CREATE TABLE " + DbDefinition.FeedItemTable.TABLE_NAME + " (" +
	    DbDefinition.FeedItemTable._ID + " INTEGER PRIMARY KEY, " +
	    DbDefinition.FeedItemTable.COLUMN_NAME_TITLE + " TEXT, " +
	    DbDefinition.FeedItemTable.COLUMN_NAME_FILE_LINK + " TEXT, " +
	    DbDefinition.FeedItemTable.COLUMN_NAME_SUB_ID + " INTEGER REFERENCES " + 
				DbDefinition.SubscriptionTable.TABLE_NAME + "("+ DbDefinition.SubscriptionTable._ID + ") ON DELETE CASCADE, " +
	    DbDefinition.FeedItemTable.COLUMN_NAME_SUB_NAME + " TEXT, " +
	    DbDefinition.FeedItemTable.COLUMN_NAME_PUB_DATE + " TEXT, " +
	    DbDefinition.FeedItemTable.COLUMN_NAME_DESCRIPTION + " TEXT, " +
	    DbDefinition.FeedItemTable.COLUMN_NAME_DOWNLOADED + " INTEGER, " +
	    DbDefinition.FeedItemTable.COLUMN_NAME_LISTENED + " INTEGER " +
	    " );";
	
	private static final String SQL_CREATE_QUEUE_TABLE = 
		"CREATE TABLE " + DbDefinition.QueueTable.TABLE_NAME + " (" +
	    DbDefinition.QueueTable._ID + " INTEGER PRIMARY KEY, " +
	    DbDefinition.QueueTable.COLUMN_NAME_QUEUE_POS + " INTEGER, " +
	    DbDefinition.QueueTable.COLUMN_NAME_FI_ID + " INTEGER REFERENCES " + 
	    		DbDefinition.FeedItemTable.TABLE_NAME + "("+ DbDefinition.FeedItemTable._ID + ") ON DELETE CASCADE " +
	    " );";
	/*
	private static final String SQL_CREATE_METADATA_TABLE = 
		"CREATE TABLE " + DbDefinition.MetadataTable.TABLE_NAME + " (" +
	    DbDefinition.MetadataTable._ID + " INTEGER PRIMARY KEY, " +
	    DbDefinition.MetadataTable.COLUMN_NAME_KEY + " INTEGER, " +
	    DbDefinition.MetadataTable.COLUMN_NAME_VALUE + " TEXT " +
	    " );";
	*/
	
	private static final String QUEUE_DELETE_TRIGGER_NAME = "queue_delete_trigger";
	private static final String SQL_QUEUE_DELETE_TRIGGER = 
		"CREATE TRIGGER " + QUEUE_DELETE_TRIGGER_NAME +
		" AFTER DELETE ON " + DbDefinition.QueueTable.TABLE_NAME + 
		" BEGIN " +
			"UPDATE " + DbDefinition.QueueTable.TABLE_NAME + 
			" SET " + DbDefinition.QueueTable.COLUMN_NAME_QUEUE_POS + "=" + DbDefinition.QueueTable.COLUMN_NAME_QUEUE_POS + "-1 " +
			"WHERE " + DbDefinition.QueueTable.COLUMN_NAME_QUEUE_POS + ">old." + DbDefinition.QueueTable.COLUMN_NAME_QUEUE_POS + ";" +
		" END;";
	
	private static final String QUEUE_INSERT_TRIGGER_NAME = "queue_insert_trigger";
	private static final String SQL_QUEUE_INSERT_TRIGGER = 
		"CREATE TRIGGER " + QUEUE_INSERT_TRIGGER_NAME +
		" BEFORE INSERT ON " + DbDefinition.QueueTable.TABLE_NAME + 
		" BEGIN " +
			"UPDATE " + DbDefinition.QueueTable.TABLE_NAME + 
			" SET " + DbDefinition.QueueTable.COLUMN_NAME_QUEUE_POS + "=" + DbDefinition.QueueTable.COLUMN_NAME_QUEUE_POS + "+1 " + 
			"WHERE " + DbDefinition.QueueTable.COLUMN_NAME_QUEUE_POS + ">=new." + DbDefinition.QueueTable.COLUMN_NAME_QUEUE_POS + ";" +
		" END;";
	
	/*
	private static final String QUEUE_UPDATE_UP_TRIGGER_NAME = "queue_update_up_trigger";
	private static final String SQL_QUEUE_UPDATE_UP_TRIGGER = 
		"CREATE TRIGGER " + QUEUE_UPDATE_UP_TRIGGER_NAME +
		" BEFORE UPDATE ON " + DbDefinition.QueueTable.TABLE_NAME + 
		" WHEN old." + DbDefinition.QueueTable.COLUMN_NAME_QUEUE_POS + ">new." + DbDefinition.QueueTable.COLUMN_NAME_QUEUE_POS + 
		" BEGIN " +
			"UPDATE " + DbDefinition.QueueTable.TABLE_NAME + 
			" SET " + DbDefinition.QueueTable.COLUMN_NAME_QUEUE_POS + "=" + DbDefinition.QueueTable.COLUMN_NAME_QUEUE_POS + "+1 " + 
			"WHERE " + DbDefinition.QueueTable.COLUMN_NAME_QUEUE_POS + ">=new." + DbDefinition.QueueTable.COLUMN_NAME_QUEUE_POS + 
			" AND " + DbDefinition.QueueTable.COLUMN_NAME_QUEUE_POS + "<=old." + DbDefinition.QueueTable.COLUMN_NAME_QUEUE_POS + ";" +
		" END;";
	
	private static final String QUEUE_UPDATE_DOWN_TRIGGER_NAME = "queue_update_down_trigger";
	private static final String SQL_QUEUE_UPDATE_DOWN_TRIGGER = 
		"CREATE TRIGGER " + QUEUE_UPDATE_DOWN_TRIGGER_NAME + 
		" BEFORE UPDATE ON " + DbDefinition.QueueTable.TABLE_NAME + 
		" WHEN old." + DbDefinition.QueueTable.COLUMN_NAME_QUEUE_POS + "<new." + DbDefinition.QueueTable.COLUMN_NAME_QUEUE_POS + 
		" BEGIN " +
			"UPDATE " + DbDefinition.QueueTable.TABLE_NAME + 
			" SET " + DbDefinition.QueueTable.COLUMN_NAME_QUEUE_POS + "=" + DbDefinition.QueueTable.COLUMN_NAME_QUEUE_POS + "-1 " + 
			"WHERE " + DbDefinition.QueueTable.COLUMN_NAME_QUEUE_POS + ">=old." + DbDefinition.QueueTable.COLUMN_NAME_QUEUE_POS + 
			" AND " + DbDefinition.QueueTable.COLUMN_NAME_QUEUE_POS + "<=new." + DbDefinition.QueueTable.COLUMN_NAME_QUEUE_POS + ";" + 
		" END;";
	*/
	
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 32;
    public static final String DATABASE_NAME = "PodTrack.db";
    
    private static SQLiteDatabase db = null;
    
    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase mdb) {
    	mdb.execSQL(SQL_CREATE_SUBSCRIPTION_TABLE);
        mdb.execSQL(SQL_CREATE_FEED_ITEM_TABLE);
        mdb.execSQL(SQL_CREATE_QUEUE_TABLE);
        //mdb.execSQL(SQL_CREATE_METADATA_TABLE);
        
        // triggers
        mdb.execSQL(SQL_QUEUE_DELETE_TRIGGER);
        mdb.execSQL(SQL_QUEUE_INSERT_TRIGGER);
        //mdb.execSQL(SQL_QUEUE_UPDATE_UP_TRIGGER);
        //mdb.execSQL(SQL_QUEUE_UPDATE_DOWN_TRIGGER);
        
        /*
        // do the initial insert into metadata table
        ContentValues cv = new ContentValues();
        cv.put(DbDefinition.MetadataTable.COLUMN_NAME_KEY, META_QUEUE_KEY);
        cv.put(DbDefinition.MetadataTable.COLUMN_NAME_VALUE, "");
        mdb.insert(DbDefinition.MetadataTable.TABLE_NAME, null, cv);
        */
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase mdb, int oldVersion, int newVersion) {
        // discard the data and start over
        mdb.execSQL("DROP TABLE IF EXISTS " + DbDefinition.SubscriptionTable.TABLE_NAME);
        mdb.execSQL("DROP TABLE IF EXISTS " + DbDefinition.FeedItemTable.TABLE_NAME);
        mdb.execSQL("DROP TABLE IF EXISTS " + DbDefinition.QueueTable.TABLE_NAME);
        //mdb.execSQL("DROP TABLE IF EXISTS " + DbDefinition.MetadataTable.TABLE_NAME);
        
        mdb.execSQL("DROP TRIGGER IF EXISTS " + QUEUE_DELETE_TRIGGER_NAME);
        mdb.execSQL("DROP TRIGGER IF EXISTS " + QUEUE_INSERT_TRIGGER_NAME);
        //mdb.execSQL("DROP TRIGGER IF EXISTS " + QUEUE_UPDATE_UP_TRIGGER_NAME);
        //mdb.execSQL("DROP TRIGGER IF EXISTS " + QUEUE_UPDATE_DOWN_TRIGGER_NAME);
        
        onCreate(mdb);
    }
    
    @Override
    public void onConfigure(SQLiteDatabase mdb) {
    	mdb.execSQL("PRAGMA foreign_keys=ON;");
    }
    
    @Override
    public void onDowngrade(SQLiteDatabase mdb, int oldVersion, int newVersion) {
        onUpgrade(mdb, oldVersion, newVersion);
    }
    
    @Override
    public SQLiteDatabase getReadableDatabase() {
    	initDb();
    	return db;
    }
    @Override
    public SQLiteDatabase getWritableDatabase() {
    	initDb();
    	return db;
    }
    
    private void initDb() {
    	if (db == null) {
    		db = super.getWritableDatabase();
    	}
    }
    
}







