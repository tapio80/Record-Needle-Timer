package com.recordneedletimer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import java.util.HashSet;

public class SettingsActivity extends AppCompatActivity {
    private SharedPreferences preferences;
    private static final String KEY_DARK_MODE = "DarkMode";
    private static final String PREFS_NAME = "NeedlePrefs";
    private static final String KEY_HISTORY = "TimeHistory";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        /* Shows back button in top */
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        /* Dark mode switch */
        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch darkModeSwitch = findViewById(R.id.dark_mode_switch);

        boolean isDarkMode = preferences.getBoolean(KEY_DARK_MODE, false);
        darkModeSwitch.setChecked(isDarkMode);

        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(KEY_DARK_MODE, isChecked);
            editor.apply();
            AppCompatDelegate.setDefaultNightMode(isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        /* Cartridge settings button */
        View cartridgeSettingsButton = findViewById(R.id.cartridge_settings_button);
        cartridgeSettingsButton.setOnClickListener(v -> openCartridgeSettings());

        /* Clear history button */
        Button clearHistoryButton = findViewById(R.id.clear_history_button);
        clearHistoryButton.setOnClickListener(view -> clearHistory());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Closes settings and returns to main
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openCartridgeSettings() {
        Intent intent = new Intent(this, CartridgeSettingsActivity.class);
        startActivity(intent);
    }

    private void clearHistory() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putStringSet(KEY_HISTORY , new HashSet<>()); // Resets history
        editor.apply();

        Toast.makeText(this, getString(R.string.history_cleared), Toast.LENGTH_SHORT).show();
    }
}