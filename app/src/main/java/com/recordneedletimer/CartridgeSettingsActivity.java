package com.recordneedletimer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class CartridgeSettingsActivity extends AppCompatActivity {
    private EditText nameInput, profileInput, vtfInput, maxHoursInput;
    private SharedPreferences preferences;
    private static final String PREFS_NAME = "NeedlePrefs";
    private static final String KEY_CARTRIDGE_NAME = "CartridgeName";
    private static final String KEY_CARTRIDGE_PROFILE = "CartridgeProfile";
    private static final String KEY_CARTRIDGE_VTF = "CartridgeVTF";
    private static final String KEY_CARTRIDGE_MAX_HOURS = "CartridgeMaxHours";
    private static final String KEY_TIME = "TimeUsed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cartridge_settings);

        /* Shows back button in top */
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        nameInput = findViewById(R.id.name_input);
        profileInput = findViewById(R.id.profile_input);
        vtfInput = findViewById(R.id.vtf_input);
        maxHoursInput = findViewById(R.id.max_hours_input);
        Button saveButton = findViewById(R.id.save_button);

        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        loadCartridgeInfo();

        saveButton.setOnClickListener(v -> saveCartridgeInfo());

        Button clearCartridgeButton = findViewById(R.id.clear_cartridge_button);
        clearCartridgeButton.setOnClickListener(view -> clearCartridgeData());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Closes cartridge settings and returns to main settings
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadCartridgeInfo() {
        nameInput.setText(preferences.getString("CartridgeName", ""));
        profileInput.setText(preferences.getString("CartridgeProfile", ""));
        vtfInput.setText(preferences.getString("CartridgeVTF", ""));
        int maxHours = (preferences.getInt("CartridgeMaxHours", -1)); // Default value which tells that value is not set
        if (maxHours != -1) {
            maxHoursInput.setText(String.valueOf(maxHours));
        } else {
            maxHoursInput.setText(""); // Empties the field if value is not set
        }
    }

    private void saveCartridgeInfo() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("CartridgeName", nameInput.getText().toString());
        editor.putString("CartridgeProfile", profileInput.getText().toString());
        editor.putString("CartridgeVTF", vtfInput.getText().toString());

        String maxHoursString = maxHoursInput.getText().toString();

        if (!maxHoursString.isEmpty()) { // Saves maxHours only if user has input value
            try {
                int maxHours = Integer.parseInt(maxHoursString); // Parse the value
                editor.putInt(KEY_CARTRIDGE_MAX_HOURS, maxHours); // Saves it
            } catch (NumberFormatException e) {
                /* If parse fails (ex. not relevant value), shows error message */
                Toast.makeText(this, getString(R.string.compatible_max_value), Toast.LENGTH_SHORT).show();
            }
        } else {
            editor.remove(KEY_CARTRIDGE_MAX_HOURS); // Removes early saved value if input is empty
        }
        editor.apply();
        finish();
        Toast.makeText(this, getString(R.string.cartridge_info_save), Toast.LENGTH_SHORT).show();
    }

    private void clearCartridgeData() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(KEY_CARTRIDGE_NAME);
        editor.remove(KEY_CARTRIDGE_PROFILE);
        editor.remove(KEY_CARTRIDGE_VTF);
        editor.remove(KEY_CARTRIDGE_MAX_HOURS);
        editor.remove(KEY_TIME);
        editor.apply();

        Toast.makeText(this, getString(R.string.settings_reset), Toast.LENGTH_SHORT).show();

        updateUI();  // Updates UI
    }

    private void updateUI() {
        EditText cartridgeNameText = findViewById(R.id.name_input);
        EditText cartridgeProfileText = findViewById(R.id.profile_input);
        EditText cartridgeVtfText = findViewById(R.id.vtf_input);
        EditText cartridgeMaxHoursText = findViewById(R.id.max_hours_input);

        cartridgeNameText.setText("");
        cartridgeProfileText.setText("");
        cartridgeVtfText.setText("");
        cartridgeMaxHoursText.setText("");

        Toast.makeText(this, getString(R.string.settings_reset), Toast.LENGTH_SHORT).show();
    }
}