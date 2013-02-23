package evg.podtrack;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;

public class Subscription
{
	public Context context;

	long id; // db primary key
	public String url;
	public String name;
	public String description;
    public String subImageUrl;
    public String subImagePath;
    
    public ArrayList<FeedItem> feedItemList; // not guaranteed to be populated
    
	public Subscription(Context context) {
		this.context = context;
		feedItemList = new ArrayList<FeedItem>();
	}
	
	@Override
	public String toString() {
		String subStr = url;
		
		return subStr;
	}
    
	public static Subscription subscriptionFromCursor(Cursor cursor, Context context) {
		Subscription sub = new Subscription(context);
		
		sub.id = cursor.getLong(0);
		sub.name = cursor.getString(1);
		sub.url = cursor.getString(2);
		sub.description = cursor.getString(3);
  		sub.subImageUrl = cursor.getString(3);
  		sub.subImagePath = cursor.getString(3);
		
		return sub;
	}
	
	public static void unsubscribe(Context context, long subId) {
		String selection = DbDefinition.SubscriptionTable._ID + "=?";
		String[] selectionArgs = {""+subId};
		context.getContentResolver().delete(DbContentProvider.SUBS_URI, selection, selectionArgs);
	}
}











