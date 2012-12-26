package com.github.androidserialport;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class SerialPreferenceActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
    public static final String KEY_COMM_TYPE_PREFERENCE = "COMM_TYPE";

    private ListPreference commTypes;

    /**
     * Interface
     */
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        preference.setSummary((String) newValue);
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.communication_preferences);
        commTypes = (ListPreference) findPreference("COMM_TYPE");
    }

    @Override
    public void onResume() {
        super.onResume();
        // Setup the initial values
        commTypes.setSummary(commTypes.getValue());
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Set summary, when preference value changes
        if (key.equals(KEY_COMM_TYPE_PREFERENCE)) {
            commTypes.setSummary(commTypes.getValue());
        }
    }
}
