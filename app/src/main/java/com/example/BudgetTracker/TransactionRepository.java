package com.example.BudgetTracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class TransactionRepository {
    private final DatabaseHelper dbHelper; // Instanz des DatabaseHelper, um auf die Datenbank zuzugreifen

    // Konstruktor, der den DatabaseHelper initialisiert
    public TransactionRepository(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    // Methode, um die monatlichen Statistiken (Einnahmen und Ausgaben) des Benutzers abzurufen
    public Cursor getMonthlyStatistics(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase(); // Lesezugriff auf die Datenbank

        // SQL-Abfrage, um die monatlichen Einnahmen und Ausgaben zu berechnen
        String query = "SELECT strftime('%m', " + DatabaseHelper.getColumnDate() + ") AS month, " +
                "SUM(CASE WHEN " + DatabaseHelper.getColumnAmount() + " > 0 THEN " + DatabaseHelper.getColumnAmount() + " ELSE 0 END) AS total_income, " +
                "SUM(CASE WHEN " + DatabaseHelper.getColumnAmount() + " < 0 THEN " + DatabaseHelper.getColumnAmount() + " ELSE 0 END) AS total_expense " +
                "FROM " + DatabaseHelper.getTableTransactions() +
                " WHERE " + DatabaseHelper.getColumnUserId() + " = ? " + // Benutzer-ID als Parameter
                "GROUP BY month " +
                "ORDER BY month";

        // Ausführen der SQL-Abfrage und Rückgabe des Cursors
        return db.rawQuery(query, new String[]{String.valueOf(userId)});
    }

    // Methode, um alle Transaktionen eines Benutzers abzurufen
    public Cursor getTransactionsForUser(int userId, boolean sortByDate) {
        SQLiteDatabase db = dbHelper.getReadableDatabase(); // Lesezugriff auf die Datenbank
        String orderBy = sortByDate ? DatabaseHelper.getColumnDate() + " DESC" : DatabaseHelper.getColumnAmount() + " ASC"; // Sortieren nach Datum oder Betrag

        // SQL-Abfrage, um die Transaktionen des Benutzers zu erhalten
        return db.rawQuery(
                "SELECT * FROM " + DatabaseHelper.getTableTransactions() +
                        " WHERE " + DatabaseHelper.getColumnUserId() + " = ?" + // Benutzer-ID als Parameter
                        " ORDER BY " + orderBy, // Sortierreihenfolge
                new String[]{String.valueOf(userId)} // Parameter für Benutzer-ID
        );
    }

    // Methode, um eine Transaktion zu löschen
    public boolean deleteTransaction(int transactionId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase(); // Schreibzugriff auf die Datenbank
        // Löschen der Transaktion mit der gegebenen ID
        int rowsDeleted = db.delete(
                DatabaseHelper.getTableTransactions(),
                "id = ?", // Bedingung für das Löschen (ID der Transaktion)
                new String[]{String.valueOf(transactionId)} // Transaktions-ID als Parameter
        );
        return rowsDeleted > 0; // Gibt true zurück, wenn die Transaktion erfolgreich gelöscht wurde
    }

    // Methode, um den aktuellen Kontostand des Benutzers abzurufen
    public double getBalance(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase(); // Lesezugriff auf die Datenbank

        // SQL-Abfrage, um die Summe der Transaktionsbeträge des Benutzers zu berechnen
        String query = "SELECT SUM(" + DatabaseHelper.getColumnAmount() + ") AS balance FROM " + DatabaseHelper.getTableTransactions() +
                " WHERE " + DatabaseHelper.getColumnUserId() + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)}); // Ausführen der Abfrage

        double balance = 0.0; // Standardwert für das Guthaben
        if (cursor != null && cursor.moveToFirst()) { // Wenn der Cursor nicht leer ist
            balance = cursor.getDouble(cursor.getColumnIndexOrThrow("balance")); // Guthaben aus dem Cursor lesen
            cursor.close(); // Cursor schließen, wenn er nicht mehr benötigt wird
        }

        return balance; // Rückgabe des berechneten Guthabens
    }

    // Methode, um eine neue Transaktion hinzuzufügen
    public boolean addTransaction(int userId, int type, double amount, String category, String date, String description) {
        dbHelper.addTransaction(userId, type, amount, category, date, description); // Transaktion in die Datenbank hinzufügen
        return true; // Gibt true zurück, wenn die Transaktion erfolgreich hinzugefügt wurde
    }

    // Methode, um die ID des aktuell angemeldeten Benutzers abzurufen
    public int getLoggedInUserId() {
        return dbHelper.getLoggedInUserId(); // Gibt die Benutzer-ID des aktuell angemeldeten Benutzers zurück
    }

    // Methode, um den Benutzer abzumelden
    public void logout() {
        dbHelper.logout(); // Abmelden des Benutzers
    }

    // Methode, um eine Transaktion zu aktualisieren
    public boolean updateTransaction(int id, double amount, String category, String date, String description) {
        SQLiteDatabase db = dbHelper.getWritableDatabase(); // Schreibzugriff auf die Datenbank
        ContentValues contentValues = new ContentValues(); // ContentValues für die neuen Werte
        contentValues.put("amount", amount); // Betrag setzen
        contentValues.put("category", category); // Kategorie setzen
        contentValues.put("date", date); // Datum setzen
        contentValues.put("description", description); // Beschreibung setzen

        // SQL-Abfrage, um die Transaktion mit der gegebenen ID zu aktualisieren
        int result = db.update(DatabaseHelper.getTableTransactions(), contentValues, "id = ?", new String[]{String.valueOf(id)});
        return result > 0; // Gibt true zurück, wenn die Transaktion erfolgreich aktualisiert wurde
    }
}
