package app.in.lafolle.musendrid;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;

/**
 * Created by lafolle on 17/11/14.
 */
public class SettingsActivtity extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final String PREF_SENSITYVITY_KEY = "pref_senstivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.general_settings);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        Log.d("Preference", "changed");

        if (key.equals(PREF_SENSITYVITY_KEY)) {
            Log.d("Preference", "sensitivity changed");
            Preference preference = findPreference(PREF_SENSITYVITY_KEY);
            preference.setSummary(sharedPreferences.getString(key, "0.8"));
        }

    }
}
