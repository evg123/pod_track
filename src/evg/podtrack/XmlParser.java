package evg.podtrack;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.net.Uri;
import android.util.Xml;
import android.widget.Toast;

public class XmlParser {
	
	public Context context;
	private XmlPullParser parser = null;
	int itemLimit, itemCount = 0;
	public XmlParser(Context context) {
		this.context = context;
	}
	
	public Subscription readSubscription(String urlStr, int itemLimit) 
	{
		this.itemLimit = itemLimit;
		Subscription sub = null;
		InputStream inStream;
		try {
			parser = Xml.newPullParser();
			URL url = new URL(urlStr);
			inStream = url.openConnection().getInputStream();
			parser.setInput(inStream, null);
			parser.nextTag();
			parser.require(XmlPullParser.START_TAG, null, "rss");
			while (parser.next() != XmlPullParser.END_TAG)
			{
				if (parser.getEventType() != XmlPullParser.START_TAG) {
		            continue;
		        }
				String name = parser.getName();
		        
		        if (name.equals("channel")) {
		        	sub = readChannelForSubscriptionInfo(parser);
		        } else {
		            skip(parser);
		        }
			}
			
		} catch (Exception ex) {
			Toast toast = Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT);
			toast.show();
		}
		
		return sub;
	}
	
	private Subscription readChannelForSubscriptionInfo(XmlPullParser parser) throws IOException, XmlPullParserException {
		int itemCount = 0;
	    parser.require(XmlPullParser.START_TAG, null, "channel");
	    Subscription sub = new Subscription(context);
	    while (parser.next() != XmlPullParser.END_TAG)
		{
			if (parser.getEventType() != XmlPullParser.START_TAG) {
	            continue;
	        }
			
	        String name = parser.getName();
	        
	        if (name.equals("title")) {
	        	sub.name = readTitle(parser);
	        } else if (name.equals("description")) {
	        	sub.description = readDescription(parser);
	        } else if (name.equals("image") && (sub.subImageUrl == null || sub.subImageUrl.equals(""))) {
	        	String img = readImage(parser);
	        	sub.subImageUrl = img;
	            sub.subImagePath = ""+Math.abs(img.hashCode()); // can just store as an int if this actually works
	        } else if (name.equals("item")) {
		       	FeedItem i = parseFeedItem(parser);
		       	if (i.link == null) {
		       		continue;
		       	}
		       	sub.feedItemList.add(i);
	        	// theres a good chance this shortcircuit will mess things up
	        	// try it for now
	        	itemCount++;
	        	if (itemCount > itemLimit) {
	        		return sub;
	        	}
	        } else {
	            skip(parser);
	        }
		}
	    parser.require(XmlPullParser.END_TAG, null, "channel");
	    
	    return sub;
	}
	
	private FeedItem parseFeedItem(XmlPullParser parser) throws XmlPullParserException, IOException
	{
		parser.require(XmlPullParser.START_TAG, null, "item");
		FeedItem item = new FeedItem();
		
		while (parser.next() != XmlPullParser.END_TAG) {
	        if (parser.getEventType() != XmlPullParser.START_TAG) {
	            continue;
	        }
	        
	        String name = parser.getName();
	        if (name.equals("title")) {
	            item.title = readTitle(parser);
	        } else if (name.equals("description")) {
	            item.description = readDescription(parser);
	        } else if (name.equals("link")) {
	            readLink(parser);
	        } else if (name.equals("pubDate")) {
	            item.pubDate = readPubDate(parser);
	        } else if (name.equals("enclosure")) {
	            item.link = readEnclosure(parser);
	        } else {
	            skip(parser);
	        }
	    }
		
		return item;
	}
	
	private String readTitle(XmlPullParser parser) throws IOException, XmlPullParserException {
	    parser.require(XmlPullParser.START_TAG, null, "title");
	    String title = readText(parser);
	    parser.require(XmlPullParser.END_TAG, null, "title");
	    return title;
	}
	
	private String readDescription(XmlPullParser parser) throws IOException, XmlPullParserException {
	    parser.require(XmlPullParser.START_TAG, null, "description");
	    String description = readText(parser);
	    parser.require(XmlPullParser.END_TAG, null, "description");
	    return description;
	}
	
	private Uri readLink(XmlPullParser parser) throws IOException, XmlPullParserException {
	    parser.require(XmlPullParser.START_TAG, null, "link");
	    String link = readText(parser);
	    // consider storing as a string
	    Uri uri = Uri.parse(link);
	    parser.require(XmlPullParser.END_TAG, null, "link");
	    return uri;
	}
	
	private Date readPubDate(XmlPullParser parser) throws IOException, XmlPullParserException {
	    parser.require(XmlPullParser.START_TAG, null, "pubDate");
	    Date date;
		try {
			date = DateFormat.getDateInstance().parse(readText(parser));
		} catch (ParseException e) {
			// TODO error handling
			date = new Date();
		}
	    parser.require(XmlPullParser.END_TAG, null, "pubDate");
	    return date;
	}
	
	private String readImage(XmlPullParser parser) throws IOException, XmlPullParserException {
		String image = "";
		
	    parser.require(XmlPullParser.START_TAG, null, "image");
	    while (parser.next() != XmlPullParser.END_TAG) {
	        if (parser.getEventType() != XmlPullParser.START_TAG) {
	            continue;
	        }
	        
	        String name = parser.getName();
	        if (name.equals("url")) {
	            image = readUrl(parser);
	        } else {
	            skip(parser);
	        }
	    }
	    parser.require(XmlPullParser.END_TAG, null, "image");
	    return image;
	}
	
	private String readUrl(XmlPullParser parser) throws IOException, XmlPullParserException {
	    parser.require(XmlPullParser.START_TAG, null, "url");
	    String url = readText(parser);
	    parser.require(XmlPullParser.END_TAG, null, "url");
	    return url;
	}
	
	private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
	    String result = "";
	    if (parser.next() == XmlPullParser.TEXT) {
	        result = parser.getText();
	        parser.nextTag();
	    }
	    return result;
	}
	
	private Uri readEnclosure(XmlPullParser parser) throws IOException, XmlPullParserException {
	    parser.require(XmlPullParser.START_TAG, null, "enclosure");
	    String type = parser.getAttributeValue(null, "type");
	    Uri uri = null;
	    if (type.equals("audio/mpeg")) {
	    	String url = parser.getAttributeValue(null, "url");
		    uri = Uri.parse(url);
	    }
	    parser.nextTag();
	    parser.require(XmlPullParser.END_TAG, null, "enclosure");
	    return uri;
	}
	
	private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
	    if (parser.getEventType() != XmlPullParser.START_TAG) {
	        throw new IllegalStateException();
	    }
	    int depth = 1;
	    while (depth != 0) {
	        switch (parser.next()) {
	        case XmlPullParser.END_TAG:
	            depth--;
	            break;
	        case XmlPullParser.START_TAG:
	            depth++;
	            break;
	        }
	    }
	 }
	
	
}
