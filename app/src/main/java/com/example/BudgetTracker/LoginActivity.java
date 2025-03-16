package com.example.BudgetTracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    // UI-Elemente für Benutzername, Passwort und Buttons
    EditText usernameEditText, passwordEditText;
    Button loginButton, registerButton;

    // Datenbank-Hilfsklasse
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // UI-Elemente mit den Layout-IDs verbinden
        usernameEditText = findViewById(R.id.editTextUsername);
        passwordEditText = findViewById(R.id.editTextPassword);
        loginButton = findViewById(R.id.buttonLogin);
        registerButton = findViewById(R.id.buttonRegister);

        // Instanz der Datenbank-Hilfsklasse erstellen
        dbHelper = new DatabaseHelper(this);

        // Zugriff auf SharedPreferences, um Benutzerdaten zu speichern
        SharedPreferences preferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);

        // Prüfen, ob der Benutzer bereits angemeldet ist
        int storedUserId = preferences.getInt("user_id", -1); // Abrufen der gespeicherten Benutzer-ID
        if (storedUserId != -1) {
            // Wenn eine Benutzer-ID vorhanden ist, direkt zum Dashboard weiterleiten
            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
            finish(); // Login-Aktivität beenden
        }

        // Listener für den Login-Button
        loginButton.setOnClickListener(view -> {
            // Benutzereingaben aus den EditText-Feldern abrufen
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            // Überprüfen, ob Felder leer sind
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Bitte alle Felder ausfüllen!", Toast.LENGTH_SHORT).show();
            } else {
                // Validieren der Anmeldedaten in der Datenbank
                boolean isValid = dbHelper.checkUser(username, password);
                if (isValid) {
                    // Benutzer-ID abrufen
                    int userId = dbHelper.getUserId(username, password);
                    if (userId != -1) {
                        // Benutzer-ID und -Name in SharedPreferences speichern
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putInt("user_id", userId);
                        editor.putString("username", username); // Optional: Benutzername speichern
                        editor.apply();

                        Toast.makeText(LoginActivity.this, "Login erfolgreich!", Toast.LENGTH_SHORT).show();

                        // Zur Dashboard-Aktivität weiterleiten
                        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                        startActivity(intent);
                        finish(); // Login-Aktivität beenden
                    } else {
                        Toast.makeText(LoginActivity.this, "Benutzer-ID nicht gefunden!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Fehlermeldung bei ungültigen Anmeldedaten
                    Toast.makeText(LoginActivity.this, "Ungültige Anmeldedaten!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Listener für den Registrieren-Button
        registerButton.setOnClickListener(view ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class))); // Weiter zur Registrierungsaktivität
    }


}
