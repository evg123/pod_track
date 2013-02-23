package evg.podtrack;

import android.provider.BaseColumns;

/*
 * /data/data/evg.podtrack/databases/PodTrack.db
 */
public class DbDefinition {
	
	public static abstract class SubscriptionTable implements BaseColumns {
	    public static final String TABLE_NAME = "subscription";
	    public static final String COLUMN_NAME_NAME = "name";
	    public static final String COLUMN_NAME_SUB_URL = "sub_url";
	    public static final String COLUMN_NAME_DESCRIPTION = "description";
        public static final String COLUMN_NAME_SUB_IMAGE_URL = "sub_image_url";
        public static final String COLUMN_NAME_SUB_IMAGE_PATH = "sub_image_path";
        public static final String COLUMN_NAME_LAST_UPDATED = "last_updated";
	}
	
	public static abstract class FeedItemTable implements BaseColumns {
	    public static final String TABLE_NAME = "feed_item";
	    public static final String COLUMN_NAME_TITLE = "title";
	    public static final String COLUMN_NAME_FILE_LINK = "file_link";
	    public static final String COLUMN_NAME_SUB_ID = "sub_id";
	    public static final String COLUMN_NAME_SUB_NAME = "sub_name";
        public static final String COLUMN_NAME_PUB_DATE = "pub_date";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
        public static final String COLUMN_NAME_LISTENED = "listened";
        public static final String COLUMN_NAME_DOWNLOADED = "downloaded";
        public static final String COLUMN_NAME_PLAYBACK_POS = "playback_pos";
	}
	
	public static abstract class QueueTable implements BaseColumns {
		public static final String TABLE_NAME = "queue";
		public static final String COLUMN_NAME_QUEUE_POS = "queue_pos";
		public static final String COLUMN_NAME_FI_ID = "fi_id";
	}
	/*
	public static abstract class MetadataTable implements BaseColumns {
	    public static final String TABLE_NAME = "metadata";
	    public static final String COLUMN_NAME_KEY = "key";
	    public static final String COLUMN_NAME_VALUE = "value";
	}
	*/
	// possibly use a seperate table for options
	// would be an int key to an int value?
	// might need some string options though
	// other option is to add int column to metadata table and use that for options as well
	// or just parse string representations of ints...
	
}
