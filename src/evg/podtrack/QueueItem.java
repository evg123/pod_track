package evg.podtrack;

import org.json.JSONException;
import org.json.JSONObject;

public class QueueItem {

	public int itemId;
	public String subId;
	
	public JSONObject toJson() throws JSONException
	{
		JSONObject json = new JSONObject();
		
		json.put("itemId", itemId);
		json.put("subId", subId);
		
		return json;
	}
	
	public static QueueItem fromJson(JSONObject json) throws JSONException
	{
		QueueItem item = new QueueItem();
		
		item.itemId = json.getInt("itemId");
		item.subId = json.getString("subId");
		
		return item;
	} 
}
