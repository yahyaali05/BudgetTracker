package com.example.BudgetTracker;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class ReportActivity extends AppCompatActivity {

    private TextView emptyReportView; // TextView, die angezeigt wird, wenn keine Berichte vorliegen
    private RecyclerView reportRecyclerView; // RecyclerView für die Anzeige der Berichte
    private TransactionAdapter reportAdapter; // Adapter, um die Transaktionen im RecyclerView darzustellen
    private TransactionRepository transactionRepository; // Repository für Transaktionen
    private int userId; // ID des aktuell eingeloggten Benutzers

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report); // Layout für die ReportActivity setzen

        Button backButton = findViewById(R.id.buttonBack);
        backButton.setOnClickListener(v -> finish());

        // UI-Komponenten initialisieren
        emptyReportView = findViewById(R.id.textViewEmptyReport); // TextView für leeren Bericht
        reportRecyclerView = findViewById(R.id.recyclerViewReports); // RecyclerView für Berichte
        MaterialButton buttonShowStatistics = findViewById(R.id.buttonShowStatistics); // Button für Statistik-Ansicht

        // Click-Listener für Statistik-Button
        buttonShowStatistics.setOnClickListener(v -> showStatistics()); // Aufruf der Statistik-Ansicht bei Klick

        // Initialisiere TransactionRepository
        transactionRepository = new TransactionRepository(this);
        userId = transactionRepository.getLoggedInUserId(); // Hole die ID des eingeloggt Benutzers

        // Überprüfe, ob Benutzer eingeloggt ist
        if (userId == -1) {
            // Wenn der Benutzer nicht eingeloggt ist, zeige eine Fehlermeldung und beende die Activity
            Toast.makeText(this, R.string.error_not_logged_in, Toast.LENGTH_SHORT).show();
            finish(); // Beendet die Aktivität, wenn der Benutzer nicht eingeloggt ist
            return;
        }

        // Setze LayoutManager für RecyclerView
        reportRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Berichte laden
        loadReport();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Lade die Berichte erneut, wenn die Aktivität wieder sichtbar wird
        loadReport();
    }

    private void loadReport() {
        // Hole die Transaktionen des Benutzers (aktive Transaktionen)
        Cursor cursor = transactionRepository.getTransactionsForUser(userId, true);

        // Überprüfe, ob der Cursor null ist (z.B. bei einem Datenbankfehler)
        if (cursor == null) {
        //    Toast.makeText(this, R.string.error_loading_transactions, Toast.LENGTH_SHORT).show();
            return;
        }

        // Wenn es Transaktionen gibt, setze RecyclerView sichtbar und zeige Berichte an
        if (cursor.getCount() > 0) {
            emptyReportView.setVisibility(TextView.GONE); // Verstecke die leere Bericht-Nachricht
            reportRecyclerView.setVisibility(RecyclerView.VISIBLE); // Zeige RecyclerView an

            // Initialisiere den Adapter, wenn er noch nicht gesetzt wurde
            if (reportAdapter == null) {
                reportAdapter = new TransactionAdapter(this, cursor, transactionRepository);
                reportRecyclerView.setAdapter(reportAdapter); // Setze den Adapter für RecyclerView
            } else {
                // Wenn der Adapter bereits existiert, aktualisiere den Cursor mit neuen Daten
                reportAdapter.updateCursor(cursor);
            }
        } else {
            // Wenn keine Transaktionen vorhanden sind, zeige die leere Nachricht an
            emptyReportView.setVisibility(TextView.VISIBLE);
            reportRecyclerView.setVisibility(RecyclerView.GONE);
        }
    }

    private void showStatistics() {
        // Hole die Monatsstatistiken des Benutzers
        Cursor cursor = transactionRepository.getMonthlyStatistics(userId); // Methode, die Monatsstatistiken abruft
        if (cursor == null || cursor.getCount() <= 0) {
            Toast.makeText(this, "Keine Daten für die Statistik verfügbar.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Float> einnahmen = new ArrayList<>(); // Liste für Einkommenswerte
        List<Float> ausgaben = new ArrayList<>();  // Liste für Ausgabenwerte
        List<String> labels = new ArrayList<>();   // Liste für Monatsbezeichner

        // Iteriere über den Cursor und hole die Werte
        while (cursor.moveToNext()) {
            String month = cursor.getString(cursor.getColumnIndex("month")); // Monatsname
            float income = cursor.getFloat(cursor.getColumnIndex("total_income")); // Gesamteinnahmen des Monats
            float expense = cursor.getFloat(cursor.getColumnIndex("total_expense")); // Gesamtausgaben des Monats

            labels.add(month);  // Monat zur Label-Liste hinzufügen
            einnahmen.add(income); // Einkommen zur Einkommens-Liste hinzufügen
            ausgaben.add(expense); // Ausgaben zur Ausgaben-Liste hinzufügen
        }

        cursor.close(); // Schließe den Cursor nach der Nutzung, um Ressourcen freizugeben

        // Übergebe die gesammelten Daten an die StatisticsActivity
        Intent intent = new Intent(this, StatisticsActivity.class);
        intent.putExtra("einnahmen", convertToFloatArray(einnahmen)); // Einkommensdaten als Array
        intent.putExtra("ausgaben", convertToFloatArray(ausgaben)); // Ausgabedaten als Array
        intent.putExtra("labels", labels.toArray(new String[0])); // Labels als String-Array
        startActivity(intent); // Starte die StatisticsActivity
    }

    // Hilfsmethode zum Konvertieren von Listen in Arrays
    private float[] convertToFloatArray(List<Float> list) {
        float[] array = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i); // Liste in Array konvertieren
        }
        return array;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Wenn der Adapter existiert, schließe den Cursor beim Zerstören der Activity
        if (reportAdapter != null) {
            reportAdapter.closeCursor();
        }
    }

    // Methode zum Löschen einer Transaktion
    public void deleteTransaction(int transactionId) {
        boolean deleted = transactionRepository.deleteTransaction(transactionId); // Lösche die Transaktion
        if (deleted) {
            Toast.makeText(this, "Transaktion gelöscht", Toast.LENGTH_SHORT).show();
            loadReport();  // Lade die Berichte nach dem Löschen neu
        } else {
            Toast.makeText(this, "Fehler beim Löschen der Transaktion", Toast.LENGTH_SHORT).show();
        }
    }
}
