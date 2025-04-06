package com.recordneedletimer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.android.material.snackbar.Snackbar;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private TextView timerText, historyText, cartridgeInfoText;
    private final Handler handler = new Handler();
    private boolean isRunning = false;
    private int seconds = 0;
    private int sessionStartSeconds = 0;  // Saves timer start up time
    private SharedPreferences preferences;
    private static final String PREFS_NAME = "NeedlePrefs";
    private static final String KEY_TIME = "TimeUsed";
    private static final String KEY_HISTORY = "TimeHistory";
    private static final String KEY_CARTRIDGE_NAME = "CartridgeName";
    private static final String KEY_CARTRIDGE_PROFILE = "CartridgeProfile";
    private static final String KEY_CARTRIDGE_VTF = "CartridgeVTF";
    private static final String KEY_CARTRIDGE_MAX_HOURS = "CartridgeMaxHours";
    private static final String KEY_DARK_MODE = "DarkMode";
    private final ArrayList<String> historyList = new ArrayList<>();
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRunning) {
                seconds++;  // Adds second to timer
                updateTimerText(); // Update UI
                handler.postDelayed(this, 1000); // Repeat in every 1 seconds
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isDarkMode = preferences.getBoolean(KEY_DARK_MODE, false);
        AppCompatDelegate.setDefaultNightMode(isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        timerText = findViewById(R.id.timer_text);
        historyText = findViewById(R.id.history_text);
        cartridgeInfoText = findViewById(R.id.cartridge_info_text);
        ImageButton settingsButton = findViewById(R.id.settings_button);
        Button startButton = findViewById(R.id.start_button);
        Button stopButton = findViewById(R.id.stop_button);
        Button resetButton = findViewById(R.id.reset_button);

        seconds = 0;
        historyList.addAll(preferences.getStringSet(KEY_HISTORY, new HashSet<>()));

        loadCartridgeInfo();
        updateTimerText();
        updateHistoryText();

        settingsButton.setOnClickListener(view -> openSettings());
        startButton.setOnClickListener(view -> startTimer());
        stopButton.setOnClickListener(view -> stopTimer());
        resetButton.setOnClickListener(view -> resetTimer());
    }

    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void loadCartridgeInfo() {
        SharedPreferences preferences = getSharedPreferences("NeedlePrefs", MODE_PRIVATE);

        String name = preferences.getString(KEY_CARTRIDGE_NAME, getString(R.string.not_set));
        String profile = preferences.getString(KEY_CARTRIDGE_PROFILE, getString(R.string.not_set));

        /* Converts string to float if possible */
        String vtfString = preferences.getString(KEY_CARTRIDGE_VTF, "");
        float vtfValue = 0f;
        if (!vtfString.isEmpty()) {
            try {
                vtfValue = Float.parseFloat(vtfString);
            } catch (NumberFormatException e) {
                vtfValue = 0f; // Possible error can be logged
            }
        }

        int maxHours = preferences.getInt(KEY_CARTRIDGE_MAX_HOURS, -1);
        int playedSeconds = preferences.getInt(KEY_TIME, 0); // Gets saved playtime, default is 0 so it wont make null error
        /* Converts seconds to hours and minutes */
        int playedHours = playedSeconds / 3600;
        int playedMinutes = (playedSeconds % 3600) / 60;

        /* Builds info text */
        String maxHoursText = (maxHours >= 0) ? " / " + maxHours + " h" : ""; // Does not show if maxHours is not set
        String cartridgeInfo = getString(R.string.cartridge, name) + "\n"
                + getString(R.string.info_profile, profile) + "\n"
                + getString(R.string.info_vtf, vtfValue) + "\n"
                + getString(R.string.played_time, playedHours, playedMinutes, maxHoursText) + "\n";

        /* If playtime gets over max, adds warning */
        if (maxHours > 0 && playedSeconds >= maxHours * 3600) {
            cartridgeInfo += "\n" + getString(R.string.warning);
            cartridgeInfoText.setTextColor(getResources().getColor(android.R.color.holo_red_dark)); // Text colour to red
            // Shows warning with Snackbar
            Snackbar snackbar = Snackbar
                .make(findViewById(android.R.id.content), getString(R.string.warning), Snackbar.LENGTH_INDEFINITE)
                .setAction("OK", view -> {
                });
                snackbar.setActionTextColor(Color.BLUE);
                snackbar.show();
        } else {
            cartridgeInfoText.setTextAppearance(this, R.style.CartridgeInfoText); // Returns default colour
        }

        cartridgeInfoText.setText(cartridgeInfo);
    }

    private void updateTimerText() {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;

        @SuppressLint("DefaultLocale") String timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, secs);
        timerText.setText(timeFormatted);
    }

    private void updateHistoryText() {
        StringBuilder historyDisplay = new StringBuilder();
        for (String entry : historyList) {
            historyDisplay.append(entry).append("\n");
        }
        historyText.setText(historyDisplay.toString());
    }

    private void startTimer() {
        if (!isRunning) {
            isRunning = true;
            sessionStartSeconds = seconds;  // Saves timer current time
            handler.postDelayed(timerRunnable, 1000);
        }
    }

    private void stopTimer() {
        if (isRunning) {
            isRunning = false;
            handler.removeCallbacks(timerRunnable);

            int sessionDuration = seconds - sessionStartSeconds; // Count session duration
            sessionStartSeconds = seconds; // Updates so new start begins right

            updateCartridgePlayTime(sessionDuration); // Update cartridge play time
            loadCartridgeInfo();
            addToHistory(sessionDuration); // Add session duration to history
        }
    }

    @SuppressLint("DefaultLocale")
    private String formatTime(int totalSeconds) {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void updateCartridgePlayTime(int additionalSeconds) {
        int previousPlayTime = preferences.getInt(KEY_TIME, 0); // Gets previously saved play time
        int totalPlayTime = previousPlayTime + additionalSeconds; // Adds new seconds to previous play time
        int maxHours = preferences.getInt(KEY_CARTRIDGE_MAX_HOURS, 0);

        SharedPreferences.Editor editor = preferences.edit(); // Saves updated play time to SharedPreferences
        editor.putInt(KEY_TIME, totalPlayTime);
        editor.apply();

        /* Converts seconds to hours and minutes */
        int playedHours = totalPlayTime / 3600;
        int playedMinutes = (totalPlayTime % 3600) / 60;

        /* Removes previous play time and adds new */
        String[] lines = cartridgeInfoText.getText().toString().split("\n");
        StringBuilder newInfo = new StringBuilder();

        for (String line : lines) {
            if (!line.startsWith(getString(R.string.played_time))) {
                newInfo.append(line).append("\n");
            }
        }
        String maxHoursText = (maxHours > 0) ? " / " + maxHours + " h" : "";
        String playedTime = getString(R.string.played_time, playedHours, playedMinutes, maxHoursText);
        newInfo.append(playedTime); // Adds updated time stamp

        /* If playtime gets over max, adds warning */
        if (maxHours > 0 && totalPlayTime >= maxHours * 3600) {
            newInfo.append("\n").append(getString(R.string.warning));
            cartridgeInfoText.setTextColor(getResources().getColor(android.R.color.holo_red_dark)); // Text colour to red
        } else {
            cartridgeInfoText.setTextAppearance(this, R.style.CartridgeInfoText); // Returns default colour
        }

        cartridgeInfoText.setText(newInfo.toString()); // Updates play time
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCartridgeInfo();
        loadHistory(); // Reloads history
        updateHistoryText(); // Updates history if it's been reset from settings
        if (isRunning) {
            runTimer(); // Start timer only if it was already running
        }
    }

    private void loadHistory() {
        historyList.clear(); // Empties list first
        historyList.addAll(preferences.getStringSet(KEY_HISTORY, new HashSet<>())); // Loads new data
    }

    private void addToHistory(int sessionDuration) {
        if (sessionDuration > 0) {
            String timeStamp = new SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault()).format(new Date());
            String entry = timeStamp + " - " + formatTime(sessionDuration);

            historyList.add(entry);
            saveHistory();
            updateHistoryText();
        }
    }

    private void saveHistory() {
        SharedPreferences.Editor editor = preferences.edit();
        Set<String> historySet = new HashSet<>(historyList);
        editor.putStringSet(KEY_HISTORY, historySet);
        editor.apply();
    }

    private void resetTimer() {
        isRunning = false;
        handler.removeCallbacksAndMessages(null);

        seconds = 0;  // Resets seconds
        updateTimerText(); // Updates UI
    }

    private void runTimer() {
        handler.removeCallbacksAndMessages(null); // Removes all possible doubles
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    updateTimerText(); // Updates UI first
                    seconds++; // Add 1 second to play time
                    handler.postDelayed(this, 1000); // Runs again after 1 second
                }
            }
        });
    }
}