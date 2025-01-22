package com.example.project1311;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private MaterialButton btnFlashcards, btnTests, btnLogout;
    private Button leaderboards;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Check if user is signed in
        if (mAuth.getCurrentUser() == null) {
            // User is not signed in, redirect to login
            navigateToLogin();
            return;
        }

        // Initialize buttons
        btnFlashcards = findViewById(R.id.btnFlashCards);
        btnTests = findViewById(R.id.btnTests);
        btnLogout = findViewById(R.id.btnLogout);
        leaderboards = findViewById(R.id.leaderbords);

        btnFlashcards.setOnClickListener(this);
        btnTests.setOnClickListener(this);
        btnLogout.setOnClickListener(this);
        leaderboards.setOnClickListener(this);

        // Initialize AuthStateListener with user deletion handling
        mAuthStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                // Verify if the user token is still valid
                user.getIdToken(true)
                        .addOnCompleteListener(task -> {
                            if (!task.isSuccessful()) {
                                // Token refresh failed, user might be deleted
                                Toast.makeText(MainActivity.this,
                                        "User session expired",
                                        Toast.LENGTH_SHORT).show();
                                navigateToLogin();
                            }
                        });
            } else {
                // User is signed out or deleted
                Toast.makeText(MainActivity.this,
                        "Please sign in to continue",
                        Toast.LENGTH_SHORT).show();
                navigateToLogin();
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Attach the AuthStateListener when the activity starts
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Remove the listener when the activity stops to avoid memory leaks
        if (mAuthStateListener != null) {
            mAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == btnFlashcards) {
            Intent intent = new Intent(this, FCSelection.class);
            startActivity(intent);
        }
        if (v == btnTests) {
            Intent intent = new Intent(this, TestSelection.class);
            startActivity(intent);
        }
        if (v == leaderboards) {
            Intent intent = new Intent(this, Leaderboards.class);
            startActivity(intent);
        }
        if (v == btnLogout) {
            signOut();
        }
    }

    private void signOut() {
        mAuth.signOut();
        Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show();
        navigateToLogin();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, MainActivity2.class);
        // Clear the back stack so user can't go back after logout
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}