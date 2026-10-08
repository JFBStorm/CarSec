package com.example.carsec;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    private Button enableBiometricButton, logoutButton;
    private TextView greetingTextView, vehicleStatus;
    private ImageButton lockButton, unlockButton, hornButton, lightsButton, trunkButton, trunkCloseButton;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Get the name passed from MainActivity
        String name = getIntent().getStringExtra("name");

        // Initialize UI elements
        greetingTextView = findViewById(R.id.greetingTextView);
        lockButton = findViewById(R.id.lockButton);
        unlockButton = findViewById(R.id.unlockButton);
        hornButton = findViewById(R.id.hornButton);
        lightsButton = findViewById(R.id.lightsButton);
        trunkButton = findViewById(R.id.trunkButton);
        trunkCloseButton = findViewById(R.id.trunkCloseButton);
        enableBiometricButton = findViewById(R.id.enableBiometricButton);
        logoutButton = findViewById(R.id.logoutButton);

        // Set greeting text
        if (name != null && !name.isEmpty()) {
            greetingTextView.setText("Hello, " + name);
        } else {
            greetingTextView.setText("Hello, User");
        }

        // Check current biometric status and update button text
        checkBiometricStatus();

        // Set up vehicle control button listeners
        setupVehicleControls();

        // Enable Biometric Button Logic
        enableBiometricButton.setOnClickListener(view -> toggleBiometricLogin());

        // Logout Button Logic
        logoutButton.setOnClickListener(v -> logout());
    }

    @SuppressLint("SetTextI18n")
    private void checkBiometricStatus() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        boolean isBiometricEnabled = sharedPreferences.getBoolean("isBiometricEnabled", false);

        // Update button text based on current biometric status
        if (isBiometricEnabled) {
            enableBiometricButton.setText("Disable Biometric Login");
        } else {
            enableBiometricButton.setText("Enable Biometric Login");
        }
    }

    @SuppressLint("SetTextI18n")
    private void toggleBiometricLogin() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        boolean isBiometricEnabled = sharedPreferences.getBoolean("isBiometricEnabled", false);

        if (isBiometricEnabled) {
            // Disable biometric login
            editor.putBoolean("isBiometricEnabled", false);
            enableBiometricButton.setText("Enable Biometric Login");
            showBiometricStatusDialog("Biometric Login Disabled", "You have disabled biometric login. You can enable it again anytime.");
        } else {
            // Enable biometric login
            editor.putBoolean("isBiometricEnabled", true);
            enableBiometricButton.setText("Disable Biometric Login");
            showBiometricStatusDialog("Biometric Login Enabled", "You can now use biometric login for quick access.");
        }
        editor.apply();
    }

    private void showBiometricStatusDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupVehicleControls() {
        String name = getIntent().getStringExtra("name");
        Handler handler = new Handler();
        if (name != null && !name.isEmpty()) {
            Runnable clearText = () -> greetingTextView.setText("Hello, " + name);

            // Set touch listener for lockButton
            lockButton.setOnTouchListener((view, motionEvent) -> handleVehicleControl(
                    motionEvent, lockButton, R.drawable.lockclicked, R.drawable.lock, "Vehicle Locked", handler, clearText));

            // Set touch listener for unlockButton
            unlockButton.setOnTouchListener((view, motionEvent) -> handleVehicleControl(
                    motionEvent, unlockButton, R.drawable.unlockclick, R.drawable.unlock, "Vehicle Unlocked", handler, clearText));

            // Set up touch listener for hornButton
            hornButton.setOnTouchListener((view, motionEvent) -> handleVehicleControl(
                    motionEvent, hornButton, R.drawable.hornclicked, R.drawable.horn, "Honking Horn", handler, clearText));

            // Set up touch listener for lightsButton
            lightsButton.setOnTouchListener((view, motionEvent) -> handleVehicleControl(
                    motionEvent, lightsButton, R.drawable.lightsclicked, R.drawable.lights, "Flashing Lights", handler, clearText));

            // Set up touch listener for trunkButton
            trunkButton.setOnTouchListener((view, motionEvent) -> handleVehicleControl(
                    motionEvent, trunkButton, R.drawable.trunkclicked, R.drawable.trunk, "Opening Trunk", handler, clearText));

            // Set up touch listener for trunkCloseButton
            trunkCloseButton.setOnTouchListener((view, motionEvent) -> handleVehicleControl(
                    motionEvent, trunkCloseButton, R.drawable.trunkcloseclicked, R.drawable.trunkclose, "Closing Trunk", handler, clearText));

        } else {
            Runnable clearText = () -> greetingTextView.setText("Hello, User");

            // Repeat same setup for user with no name passed
            lockButton.setOnTouchListener((view, motionEvent) -> handleVehicleControl(
                    motionEvent, lockButton, R.drawable.lockclicked, R.drawable.lock, "Vehicle Locked", handler, clearText));
            unlockButton.setOnTouchListener((view, motionEvent) -> handleVehicleControl(
                    motionEvent, unlockButton, R.drawable.unlockclick, R.drawable.unlock, "Vehicle Unlocked", handler, clearText));
            hornButton.setOnTouchListener((view, motionEvent) -> handleVehicleControl(
                    motionEvent, hornButton, R.drawable.hornclicked, R.drawable.horn, "Honking Horn", handler, clearText));
            lightsButton.setOnTouchListener((view, motionEvent) -> handleVehicleControl(
                    motionEvent, lightsButton, R.drawable.lightsclicked, R.drawable.lights, "Flashing Lights", handler, clearText));
            trunkButton.setOnTouchListener((view, motionEvent) -> handleVehicleControl(
                    motionEvent, trunkButton, R.drawable.trunkclicked, R.drawable.trunk, "Opening Trunk", handler, clearText));
            trunkCloseButton.setOnTouchListener((view, motionEvent) -> handleVehicleControl(
                    motionEvent, trunkCloseButton, R.drawable.trunkcloseclicked, R.drawable.trunkclose, "Closing Trunk", handler, clearText));
        }
    }

    private boolean handleVehicleControl(MotionEvent motionEvent, ImageButton button, int activeResId, int defaultResId,
                                         String statusMessage, Handler handler, Runnable clearText) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                button.setImageResource(activeResId);
                greetingTextView.setText(statusMessage);

                // Remove any previous callbacks and schedule to clear the text
                handler.removeCallbacks(clearText);
                handler.postDelayed(clearText, 3000); // Clear text after 3 seconds
                return true;

            case MotionEvent.ACTION_UP:
                button.setImageResource(defaultResId);
                return true;
        }
        return false;
    }

    private void logout() {
        // Clear preferences but retain biometric status
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        boolean isBiometricEnabled = sharedPreferences.getBoolean("isBiometricEnabled", false);

        editor.clear();
        editor.putBoolean("isBiometricEnabled", isBiometricEnabled); // Retain biometric preference
        editor.apply();

        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
