package se.trollektivet.fragments;

import se.trollektivet.cap_android.R;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
	SharedPreferences sp;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		sp = getPreferenceManager().getSharedPreferences();
	}

	@Override
	public void onResume() {
		super.onResume();
		// Initialize summaries!
		for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
            initSummaries(getPreferenceScreen().getPreference(i));
        }
		sp.registerOnSharedPreferenceChangeListener(this);
	}
	private void initSummaries(Preference preference) {
		if (preference instanceof PreferenceCategory) {
			PreferenceCategory category = (PreferenceCategory) preference;
			for (int i = 0; i < category.getPreferenceCount(); ++i) {
				category.getPreference(i).setSummary(sp.getString(category.getPreference(i).getKey(), ""));
			}
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Preference preference = findPreference(key);
		if (preference instanceof EditTextPreference) {
			preference.setSummary(sharedPreferences.getString(key, ""));
		}
	}

}
