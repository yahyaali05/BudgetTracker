package com.example.BudgetTracker;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    // UI-Elemente für Benutzereingaben und Buttons
    private EditText usernameEditText, passwordEditText, confirmPasswordEditText;
    private Button registerButton, loginRedirectButton;

    // Instanz der Datenbank-Hilfsklasse
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Verknüpfung der UI-Elemente mit ihren IDs aus dem Layout
        usernameEditText = findViewById(R.id.editTextUsername);
        passwordEditText = findViewById(R.id.editTextPassword);
        confirmPasswordEditText = findViewById(R.id.editTextConfirmPassword);
        registerButton = findViewById(R.id.buttonRegister);
        loginRedirectButton = findViewById(R.id.buttonRedirectLogin);

        // Initialisierung der Datenbank-Hilfsklasse
        databaseHelper = new DatabaseHelper(this);

        // Event-Handler für den Registrierungs-Button
        registerButton.setOnClickListener(view -> registerUser());

        // Event-Handler für den Button, um zur Login-Seite zu wechseln
        loginRedirectButton.setOnClickListener(view -> {
            startActivity(new Intent(this, LoginActivity.class)); // Login-Aktivität starten
            finish(); // Registrierungs-Aktivität beenden
        });
    }

    private void registerUser() {
        // Abrufen der Benutzereingaben aus den Textfeldern
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        // Validierung der Eingaben
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            // Fehlermeldung bei leeren Feldern
            Toast.makeText(this, "Benutzername, Passwort und Passwort bestätigen dürfen nicht leer sein!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Überprüfung der Mindestlänge des Benutzernamens
        if (username.length() < 3) {
            Toast.makeText(this, "Benutzername muss mindestens 3 Zeichen lang sein!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Überprüfung der Passwortkriterien
        if (!isValidPassword(password)) {
            Toast.makeText(this, "Passwort muss mindestens 8 Zeichen lang sein und ein Sonderzeichen enthalten!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Überprüfung, ob die Passwörter übereinstimmen
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwörter stimmen nicht überein!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Überprüfung, ob der Benutzername bereits existiert
        if (isUsernameTaken(username)) {
            Toast.makeText(this, "Benutzername existiert bereits!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Speichern des Benutzers in der Datenbank
        long result = saveUserToDatabase(username, password);
        if (result != -1) {
            // Erfolgreiche Registrierung
            Toast.makeText(this, "Registrierung erfolgreich!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class)); // Weiterleitung zur Login-Aktivität
            finish(); // Registrierungs-Aktivität beenden
        } else {
            // Fehler beim Speichern in der Datenbank
            Toast.makeText(this, "Registrierung fehlgeschlagen!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Prüft, ob ein Passwort die Anforderungen erfüllt.
     *
     * Anforderungen:
     * - Mindestens 8 Zeichen lang.
     * - Mindestens ein Sonderzeichen.
     *
     * @param password Das zu überprüfende Passwort.
     * @return true, wenn das Passwort gültig ist; andernfalls false.
     */
    private boolean isValidPassword(String password) {
        // Regulärer Ausdruck für ein Passwort mit mindestens 8 Zeichen und einem Sonderzeichen
        String passwordPattern = "^(?=.*[!@#$%^&*(),.?\":{}|<>])[A-Za-z\\d!@#$%^&*(),.?\":{}|<>]{8,}$";
        return password.matches(passwordPattern);
    }



    /**
     * Überprüft, ob ein Benutzername bereits in der Datenbank existiert.
     *
     * @param username Der Benutzername, der überprüft werden soll.
     * @return true, wenn der Benutzername bereits existiert, andernfalls false.
     */
    private boolean isUsernameTaken(String username) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        // Abfrage, ob der Benutzername in der Tabelle "users" existiert
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE username=?", new String[]{username});
        boolean exists = cursor.getCount() > 0; // Überprüfung, ob Ergebnisse vorliegen
        cursor.close(); // Cursor schließen
        return exists;
    }

    /**
     * Speichert einen neuen Benutzer in der Datenbank.
     *
     * @param username Der Benutzername des neuen Benutzers.
     * @param password Das Passwort des neuen Benutzers.
     * @return Die ID der eingefügten Zeile oder -1, wenn ein Fehler auftritt.
     */
    private long saveUserToDatabase(String username, String password) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username); // Benutzername speichern
        values.put("password", password); // Passwort speichern
        return db.insert("users", null, values); // Einfügen der Daten in die Tabelle "users"
    }

}
