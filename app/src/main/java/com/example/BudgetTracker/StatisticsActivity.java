package com.example.BudgetTracker;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class StatisticsActivity extends AppCompatActivity {

    private TransactionRepository transactionRepository; // Repository für Transaktionen
    private int loggedInUserId; // Benutzer-ID des angemeldeten Benutzers

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics); // Setze das Layout für diese Activity

        Button backButton = findViewById(R.id.buttonBack);
        backButton.setOnClickListener(v -> finish());

        // Initialisierung des Repositories, das Transaktionen verwaltet
        transactionRepository = new TransactionRepository(this);
        // Hole die ID des aktuell angemeldeten Benutzers
        loggedInUserId = transactionRepository.getLoggedInUserId();

        // Hole das benutzerdefinierte Balkendiagramm (CustomBarChart) aus dem Layout
        CustomBarChart barChart = findViewById(R.id.barChart);

        // Lade die Daten aus der Datenbank und zeige sie im Balkendiagramm an
        loadDataForChart(barChart);
    }

    /**
     * Diese Methode lädt die Daten für das Balkendiagramm und füllt es mit den entsprechenden Werten.
     * @param barChart Das benutzerdefinierte Balkendiagramm, das die Daten anzeigen wird.
     */
    private void loadDataForChart(CustomBarChart barChart) {
        // Hole die Monatsstatistiken des Benutzers
        Cursor cursor = transactionRepository.getMonthlyStatistics(loggedInUserId);

        // Listen zum Speichern der Daten für Einkommens- und Ausgabewerte sowie der Monatsbezeichner
        List<Float> einnahmen = new ArrayList<>();
        List<Float> ausgaben = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        // Wenn der Cursor Daten enthält, iteriere darüber
        if (cursor != null) {
            while (cursor.moveToNext()) {
                // Hole den Monat aus der Datenbank (z. B. "01", "02", ...)
                String month = cursor.getString(cursor.getColumnIndexOrThrow("month"));

                // Hole die Gesamteinnahmen und Gesamtausgaben für den Monat
                float totalIncome = cursor.getFloat(cursor.getColumnIndexOrThrow("total_income"));
                float totalExpense = cursor.getFloat(cursor.getColumnIndexOrThrow("total_expense"));

                // Wandelt den Monat in ein lesbares Format um (z.B. "01" -> "Jan")
                String label = getMonthName(month);

                // Füge die Werte zur jeweiligen Liste hinzu
                labels.add(label); // Monatsname zur Label-Liste hinzufügen
                einnahmen.add(totalIncome); // Einkommenswerte zur Einkommens-Liste hinzufügen
                ausgaben.add(Math.abs(totalExpense)); // Ausgabewerte zur Ausgaben-Liste hinzufügen (Betrag immer positiv)
            }
            cursor.close(); // Schließe den Cursor nach der Verwendung
        }

        // Übergebe die gesammelten Daten an das Balkendiagramm, damit es angezeigt werden kann
        barChart.setData(einnahmen, ausgaben, labels);
    }

    /**
     * Hilfsmethode zum Umwandeln des Monats von einer Zahl (z.B. "01") in den Monatsnamen (z.B. "Jan").
     * @param month Der Monat als String (z. B. "01", "02", "03" ...)
     * @return Der Monatsname in lesbarem Format (z. B. "Jan", "Feb", "Mär" ...)
     */
    private String getMonthName(String month) {
        // Wandle die Monatsnummer in den entsprechenden Monatsnamen um
        switch (month) {
            case "01":
                return "Jan";
            case "02":
                return "Feb";
            case "03":
                return "Mär";
            case "04":
                return "Apr";
            case "05":
                return "Mai";
            case "06":
                return "Jun";
            case "07":
                return "Jul";
            case "08":
                return "Aug";
            case "09":
                return "Sep";
            case "10":
                return "Okt";
            case "11":
                return "Nov";
            case "12":
                return "Dez";
            default:
                return ""; // Falls der Monat ungültig ist, gebe eine leere Zeichenkette zurück
        }
    }
}
