package org.thehellnet.shab.mobile.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import org.thehellnet.shab.mobile.R;
import org.thehellnet.shab.mobile.config.Prefs;

/**
 * Created by sardylan on 18/07/16.
 */
public class SettingsActivity extends ShabActivity {

    private SharedPreferences preferences;

    private Button saveButton;

    private EditText serverAddressText;
    private EditText serverPortText;
    private EditText nameText;

    private ImageButton serverAddressImageButton;
    private ImageButton serverPortImageButton;
    private ImageButton nameImageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    @Override
    protected void onResume() {
        super.onResume();
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        initElements();
        loadSettings();
        setListeners();
    }

    private void initElements() {
        saveButton = (Button) findViewById(R.id.settings_button_save);

        serverAddressText = (EditText) findViewById(R.id.settings_server_address_value);
        serverPortText = (EditText) findViewById(R.id.settings_server_port_value);
        nameText = (EditText) findViewById(R.id.settings_name_value);

        serverAddressImageButton = (ImageButton) findViewById(R.id.settings_server_address_reset);
        serverPortImageButton = (ImageButton) findViewById(R.id.settings_server_port_reset);
        nameImageButton = (ImageButton) findViewById(R.id.settings_name_reset);
    }

    private void setListeners() {
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSettings();
                setResult(RESULT_OK);
                finish();
            }
        });

        serverAddressImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serverAddressText.setText(Prefs.Default.SERVER_ADDRESS);
            }
        });
        serverPortImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serverPortText.setText(String.valueOf(Prefs.Default.SOCKET_PORT));
            }
        });
        nameImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nameText.setText(Prefs.Default.NAME);
            }
        });
    }

    private void loadSettings() {
        serverAddressText.setText(preferences.getString(Prefs.SERVER_ADDRESS, Prefs.Default.SERVER_ADDRESS));
        serverPortText.setText(String.valueOf(preferences.getInt(Prefs.SOCKET_PORT, Prefs.Default.SOCKET_PORT)));
        nameText.setText(preferences.getString(Prefs.NAME, Prefs.Default.NAME));
    }

    private void saveSettings() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Prefs.SERVER_ADDRESS, serverAddressText.getText().toString());
        editor.putInt(Prefs.SOCKET_PORT, Integer.parseInt(serverPortText.getText().toString()));
        editor.putString(Prefs.NAME, nameText.getText().toString());
        editor.apply();
    }
}
