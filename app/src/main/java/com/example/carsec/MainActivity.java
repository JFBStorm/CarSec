package com.example.carsec;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText emailEditText, passwordEditText;
    private TextView loginStatusTextView, signupRedirectText, biometricStatusTextView;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailEditText = findViewById(R.id.textInputEditText1);
        passwordEditText = findViewById(R.id.textInputEditText2);
        loginStatusTextView = findViewById(R.id.loginStatusTextView);
        signupRedirectText = findViewById(R.id.signupRedirectText);
        biometricStatusTextView = findViewById(R.id.biometricStatusTextView);

        Button loginButton = findViewById(R.id.button2);
        Button biometricLoginButton = findViewById(R.id.buttonBiometricLogin);

        loginButton.setOnClickListener(view -> {
            if (!validateEmail() || !validatePassword()) {
                return;
            }
            checkUser();
        });

        signupRedirectText.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, SignUp.class);
            startActivity(intent);
        });

        setupBiometricLogin(biometricLoginButton);
    }

    private boolean validateEmail() {
        String email = emailEditText.getText().toString().trim();
        if (email.isEmpty()) {
            emailEditText.setError("Email cannot be empty");
            return false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Enter a valid email address");
            return false;
        }
        return true;
    }

    private boolean validatePassword() {
        String password = passwordEditText.getText().toString().trim();
        if (password.isEmpty()) {
            passwordEditText.setError("Password cannot be empty");
            return false;
        }
        return true;
    }

    private void checkUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            loginStatusTextView.setText("Please enter email and password");
            loginStatusTextView.setTextColor(Color.RED);
            return;
        }

        // Attempt to log in using email and password
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Check if the email is verified
                            if (user.isEmailVerified()) {
                                fetchUserData(user.getUid());
                            } else {
                                // If email is not verified, log out and notify the user
                                mAuth.signOut();
                                loginStatusTextView.setText("Email not verified. Please check your inbox.");
                                loginStatusTextView.setTextColor(Color.RED);
                            }
                        }
                    } else {
                        loginStatusTextView.setText("Email, password or both are incorrect.\nPlease try again.");
                        loginStatusTextView.setTextColor(Color.RED);
                    }
                });
    }

    private void fetchUserData(String userId) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        redirectToHome(name);
                    } else {
                        loginStatusTextView.setText("User data not found");
                        loginStatusTextView.setTextColor(Color.RED);
                    }
                })
                .addOnFailureListener(e -> {
                    loginStatusTextView.setText("Failed to fetch user data");
                    loginStatusTextView.setTextColor(Color.RED);
                });
    }

    private void setupBiometricLogin(Button biometricLoginButton) {
        biometricLoginButton.setOnClickListener(view -> {
            SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
            boolean isBiometricEnabled = sharedPreferences.getBoolean("isBiometricEnabled", false);

            if (!isBiometricEnabled) {
                loginStatusTextView.setText("Biometric login not enabled. Please enable it from settings.");
                loginStatusTextView.setTextColor(Color.RED);
            } else {
                setupBiometricPrompt();
                biometricPrompt.authenticate(promptInfo);
            }
        });
    }

    private void setupBiometricPrompt() {
        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                loginStatusTextView.setText("Biometric authentication successful!");
                loginStatusTextView.setTextColor(Color.GREEN);
                fetchAndRedirectToHome();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                loginStatusTextView.setText("Biometric authentication failed");
                loginStatusTextView.setTextColor(Color.RED);
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Login")
                .setSubtitle("Log in using Face ID or Fingerprint")
                .setNegativeButtonText("Cancel")
                .build();
    }

    private void fetchAndRedirectToHome() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            fetchUserData(user.getUid());
        }
    }

    private void redirectToHome(String name) {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        intent.putExtra("name", name);
        startActivity(intent);
        finish();
    }
}