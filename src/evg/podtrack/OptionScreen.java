package evg.podtrack;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class OptionScreen extends PreferenceActivity
{	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.option_screen);
	}
	
}
