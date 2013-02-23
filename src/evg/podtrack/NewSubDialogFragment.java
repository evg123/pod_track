package evg.podtrack;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

public class NewSubDialogFragment extends DialogFragment {
	
	public static final int DEFAULT_ITEM_PARSE_LIMIT = 20; 
	
	private NewSubDialogListener listeningActivity;
	private View dialogView;
	
	public String url = null;
	
	public interface NewSubDialogListener {
        public void onPositiveClick(NewSubDialogFragment dialog);
        public void onNegativeClick(NewSubDialogFragment dialog);
    }
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();

		dialogView = inflater.inflate(R.layout.new_sub_dialog, null);
		builder.setView(dialogView)
				.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						
					}
				})
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						listeningActivity.onNegativeClick(NewSubDialogFragment.this);
						getDialog().cancel();
					}
				});
		
		// override the "Done" onlclick listener so that it doesn't automatically close the dialog
		AlertDialog ad = builder.create();
		ad.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface dialog) {
				AlertDialog alertDialog = (AlertDialog)dialog;
				alertDialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						AlertDialog alertDiag = (AlertDialog)getDialog();
						EditText et = (EditText)alertDiag.findViewById(R.id.newSubRssUrl);
						url = et.getText().toString();
						
						// TODO remove debug code
						if (url.equals("1")) {
							url = getResources().getString(R.string.test_rss_url);
						} else if (url.equals("2")) {
							url = getResources().getString(R.string.test_rss_url2);
						} else if (url.equals("3")) {
							url = getResources().getString(R.string.test_rss_url3);
						} else if (url.equals("4")) {
							url = getResources().getString(R.string.test_rss_url4);
						} else if (url.equals("5")) {
							url = getResources().getString(R.string.test_rss_url5);
						}
						
						listeningActivity.onPositiveClick(NewSubDialogFragment.this);

						ProgressBar progBar = (ProgressBar)dialogView.findViewById(R.id.progBar);
						progBar.setVisibility(View.VISIBLE);
						addSubscription(url);
					}
				});
			}
		});
		
		return ad; 
	}
	
	@Override
	public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listeningActivity = (NewSubDialogListener)activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString() + " must implement NoticeDialogListener");
        }
        
    }
	
	private void addSubscription(String subUrl) {
		// parses xml and adds new sub/items to database
		new SubscriptionParser(this).execute(subUrl);
	}
	
	private class SubscriptionParser extends AsyncTask<String, Void, Subscription> {
		
		NewSubDialogFragment parentFragment;
		Context context;
		
		public SubscriptionParser(NewSubDialogFragment parentFragment) {
			this.parentFragment = parentFragment;
			context = parentFragment.getActivity().getApplicationContext();
		}
		
		@Override
		protected Subscription doInBackground(String... params) {
			
			String subUrl = params[0];
			XmlParser xp = new XmlParser(context);
			Subscription sub = xp.readSubscription(subUrl, DEFAULT_ITEM_PARSE_LIMIT);
			
			if (sub.subImageUrl != null && !sub.subImageUrl.equals("")) {
				StorageHelper sh = new StorageHelper(context);
				sh.downloadImage(sub.subImageUrl);
			}
			
			ContentValues subValues = new ContentValues();
			subValues.put(DbDefinition.SubscriptionTable.COLUMN_NAME_NAME, sub.name);
			subValues.put(DbDefinition.SubscriptionTable.COLUMN_NAME_SUB_URL, subUrl);
			subValues.put(DbDefinition.SubscriptionTable.COLUMN_NAME_DESCRIPTION, sub.description);
			subValues.put(DbDefinition.SubscriptionTable.COLUMN_NAME_SUB_IMAGE_URL, sub.subImageUrl);
			subValues.put(DbDefinition.SubscriptionTable.COLUMN_NAME_SUB_IMAGE_PATH, sub.subImagePath);

			// Insert the new row, returning the URI of the new row
			Uri subUri = context.getContentResolver().insert(DbContentProvider.SUBS_URI, subValues);
			long subId = Long.parseLong(subUri.getLastPathSegment());
			for (FeedItem item : sub.feedItemList) {

				// set some default values for new items
				item.downloaded = 0;
				item.listened = 0;
				item.subId = subId;
				item.subName = sub.name;
				
				ContentValues itemValues = new ContentValues();
				itemValues.put(DbDefinition.FeedItemTable.COLUMN_NAME_TITLE, item.title);
				itemValues.put(DbDefinition.FeedItemTable.COLUMN_NAME_FILE_LINK, item.link.toString());
				itemValues.put(DbDefinition.FeedItemTable.COLUMN_NAME_SUB_ID, item.subId);
				itemValues.put(DbDefinition.FeedItemTable.COLUMN_NAME_SUB_NAME, item.subName);
				itemValues.put(DbDefinition.FeedItemTable.COLUMN_NAME_PUB_DATE, item.pubDate.toString());
				itemValues.put(DbDefinition.FeedItemTable.COLUMN_NAME_DESCRIPTION, item.description);
				itemValues.put(DbDefinition.FeedItemTable.COLUMN_NAME_DOWNLOADED, item.downloaded);
				itemValues.put(DbDefinition.FeedItemTable.COLUMN_NAME_LISTENED, item.listened);
				
				context.getContentResolver().insert(DbContentProvider.FIS_URI, itemValues);
			}
			
			// dont really need this return because the sub isnt used
			return sub;
		}

		@Override
		protected void onPostExecute(Subscription result) {
			parentFragment.dismiss();
	    }
	}
}