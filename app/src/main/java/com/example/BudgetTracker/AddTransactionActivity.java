package com.example.BudgetTracker;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddTransactionActivity extends AppCompatActivity {

    // UI-Komponenten für die Benutzereingabe
    private EditText amountEditText, categoryEditText, dateEditText, descriptionEditText;
    private RadioGroup typeRadioGroup;
    private RadioButton incomeRadioButton, expenseRadioButton;
    private Button saveButton;

    // Variablen für Transaktionsdetails
    private int transactionId;
    private double amount;
    private String category;
    private String description;
    private String date;
    private int type;

    // Repository für die Verwaltung der Transaktionen
    private TransactionRepository transactionRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        Button backButton = findViewById(R.id.buttonBack);
        backButton.setOnClickListener(v -> finish());

        // Initialisierung der UI-Elemente
        amountEditText = findViewById(R.id.editTextAmount);
        categoryEditText = findViewById(R.id.editTextCategory);
        descriptionEditText = findViewById(R.id.editTextDescription);
        dateEditText = findViewById(R.id.editTextDate);
        typeRadioGroup = findViewById(R.id.radioGroupType);
        incomeRadioButton = findViewById(R.id.radioButtonIncome);
        expenseRadioButton = findViewById(R.id.radioButtonExpense);
        saveButton = findViewById(R.id.buttonSaveTransaction);

        // Abrufen der übergebenen Daten aus dem Intent
        Intent intent = getIntent();
        transactionId = intent.getIntExtra("transactionId", -1); // ID der Transaktion
        amount = intent.getDoubleExtra("amount", 0.0);           // Betrag der Transaktion
        category = intent.getStringExtra("category");           // Kategorie der Transaktion
        description = intent.getStringExtra("description");     // Beschreibung der Transaktion
        date = intent.getStringExtra("date");                   // Datum der Transaktion
        type = intent.getIntExtra("type", 1);                   // Typ (Einnahme oder Ausgabe)

        // Befüllen der Eingabefelder mit den vorhandenen Daten
        amountEditText.setText(String.valueOf(amount));
        categoryEditText.setText(category);
        descriptionEditText.setText(description);
        dateEditText.setText(date);

        // Initialisierung des TransactionRepository
        transactionRepository = new TransactionRepository(this);

        // Klick-Listener für das Datumseingabefeld
        dateEditText.setOnClickListener(view -> showDatePickerDialog());

        // Klick-Listener für den Speichern-Button
        saveButton.setOnClickListener(view -> saveTransaction());
    }

    // Öffnet einen DatePickerDialog für die Datumsauswahl
    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();

        // Verwendet ein bereits eingegebenes Datum, falls vorhanden
        String existingDate = dateEditText.getText().toString();
        if (existingDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            String[] parts = existingDate.split("-");
            calendar.set(Calendar.YEAR, Integer.parseInt(parts[0]));
            calendar.set(Calendar.MONTH, Integer.parseInt(parts[1]) - 1); // Monate sind 0-basiert
            calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(parts[2]));
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Zeigt den DatePickerDialog an
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            // Speichert das ausgewählte Datum im Format YYYY-MM-DD
            String selectedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
            dateEditText.setText(selectedDate);
            Log.d("AddTransactionActivity", "Datum ausgewählt: " + selectedDate);
        }, year, month, day);

        datePickerDialog.show();
    }

    // Validiert die Benutzereingabe
    private boolean validateInput(String category, String date, String amountText) {
        if (category.isEmpty() || date.isEmpty() || amountText.isEmpty()) {
            Toast.makeText(this, "Bitte füllen Sie alle Felder aus!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            Toast.makeText(this, "Ungültiges Datum! Bitte verwenden Sie das Format YYYY-MM-DD.", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Ungültiger Betrag!", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    // Speichert die Transaktion
    private void saveTransaction() {
        // Holt die Benutzer-ID (muss angemeldet sein)
        int userId = transactionRepository.getLoggedInUserId();
        if (userId == -1) {
            Log.e("AddTransactionActivity", "Benutzer ist nicht angemeldet! userId = -1");
            Toast.makeText(this, "Benutzer nicht angemeldet!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Werte aus den Eingabefeldern lesen
        String category = categoryEditText.getText().toString().trim();
        String date = dateEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String amountText = amountEditText.getText().toString().trim();

        // Validiert die Eingaben
        if (!validateInput(category, date, amountText)) {
            return;
        }

        // Konvertiert den Betrag in eine Zahl
        double amount = Double.parseDouble(amountText);
        // Ermittelt den Transaktionstyp (Einnahme oder Ausgabe)
        int type = incomeRadioButton.isChecked() ? DatabaseHelper.EINNAHME : DatabaseHelper.AUSGABE;

        // Fügt die Transaktion hinzu
        transactionRepository.addTransaction(userId, type, amount, category, date, description);
        Log.d("AddTransactionActivity", "Transaktion gespeichert: Betrag=" + amount +
                ", Typ=" + (type == DatabaseHelper.EINNAHME ? "Einnahme" : "Ausgabe") +
                ", Kategorie=" + category + ", Datum=" + date +
                ", Beschreibung=" + description);

        // Erfolgsnachricht anzeigen
        Toast.makeText(this, "Transaktion erfolgreich gespeichert", Toast.LENGTH_SHORT).show();
        finish(); // Schließt die Aktivität
    }
}
