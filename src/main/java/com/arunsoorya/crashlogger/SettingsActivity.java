package com.arunsoorya.crashlogger;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.arunsoorya.crashlogger.R;

public class SettingsActivity extends AppCompatActivity {

    private MyPreferenceFragment myPreferenceFragment;
    private Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        myPreferenceFragment = new MyPreferenceFragment();
        getFragmentManager().beginTransaction().replace(R.id.container, myPreferenceFragment).commit();

        submit = (Button) findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CrashLogger.getInstance(SettingsActivity.this).sentBulkLogToServer();
                Toast.makeText(SettingsActivity.this,"enabled", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public static class MyPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        private Context context;

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            this.context = context;
        }

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.crashpreferences);

        }

        @Override
        public void onResume() {
            super.onResume();
            // Set up a listener whenever a key changes
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            // Set up a listener whenever a key changes
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }


        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equalsIgnoreCase(LoggerConstants.LOG_INSTANT)) {
                CheckBoxPreference checkBoxPreference = (CheckBoxPreference) findPreference(key);
                View view = ((SettingsActivity)getActivity()).submit;
                if(view== null)
                    return;
                if (checkBoxPreference.isChecked())
                    view.setEnabled(false);
                else
                    view.setEnabled(true);

            }
        }
    }
}
