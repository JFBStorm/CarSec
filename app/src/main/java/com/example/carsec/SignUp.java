package com.example.carsec;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignUp extends AppCompatActivity {

    private EditText emailTxt, passwordTxt, nameTxt;
    private Button registerBtn;
    private TextView loginRedirectText;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // UI elements
        emailTxt = findViewById(R.id.emailTxt);
        passwordTxt = findViewById(R.id.passwordTxt);
        nameTxt = findViewById(R.id.nameTxt);
        registerBtn = findViewById(R.id.registerBtn);
        loginRedirectText = findViewById(R.id.loginRedirectText);

        // Register button click listener
        registerBtn.setOnClickListener(view -> {
            if (validateInputs()) {
                String email = emailTxt.getText().toString().trim();
                String password = passwordTxt.getText().toString().trim();
                String name = nameTxt.getText().toString().trim();

                // Creating user with Firebase Authentication
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignUp.this, task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    // Send verification email
                                    user.sendEmailVerification()
                                            .addOnCompleteListener(emailTask -> {
                                                if (emailTask.isSuccessful()) {
                                                    // Save user data to Firestore
                                                    db.collection("users").document(user.getUid())
                                                            .set(createUserData(name))
                                                            .addOnSuccessListener(aVoid -> {
                                                                Toast.makeText(SignUp.this, "Registration successful! Please verify your email.", Toast.LENGTH_LONG).show();
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Toast.makeText(SignUp.this, "Failed to save user data.", Toast.LENGTH_SHORT).show();
                                                            });

                                                    // Redirect to login page
                                                    Intent intent = new Intent(SignUp.this, MainActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                } else {
                                                    Toast.makeText(SignUp.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            } else {
                                // Log detailed error message
                                String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                                Log.e("SignUp", "Registration failed: " + errorMessage);

                                // Handle specific error cases
                                if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                    emailTxt.setError("Email is already in use");
                                } else {
                                    Toast.makeText(SignUp.this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        // Redirect to login page
        loginRedirectText.setOnClickListener(view -> {
            Intent intent = new Intent(SignUp.this, MainActivity.class);
            startActivity(intent);
        });
    }

    private boolean validateInputs() {
        boolean valid = true;

        // Check if the name is empty
        if (nameTxt.getText().toString().trim().isEmpty()) {
            nameTxt.setError("Name cannot be empty");
            valid = false;
        }

        // Check if the email is valid
        String email = emailTxt.getText().toString().trim();
        if (email.isEmpty()) {
            emailTxt.setError("Email cannot be empty");
            valid = false;
        } else if (!isValidEmail(email)) {
            emailTxt.setError("Enter an email address");
            valid = false;
        }

        // Check if the password is valid
        String password = passwordTxt.getText().toString().trim();
        if (password.isEmpty()) {
            passwordTxt.setError("Password cannot be empty");
            valid = false;
        } else if (password.length() < 6) {  // Assuming minimum password length is 6 characters
            passwordTxt.setError("Password must be at least 6 characters long");
            valid = false;
        }

        return valid;
    }

    private boolean isValidEmail(String email) {
        // Simple regex for email validation
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }

    private Map<String, Object> createUserData(String name) {
        // Create a map to store only the name (no password)
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);  // Store only the name
        return userData;
    }
}