package com.example.BudgetTracker;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "users.db";
    private static final int DATABASE_VERSION = 3;

    // Transaktionstypen
    public static final int EINNAHME = 1;
    public static final int AUSGABE = 2;

    // Tabellennamen und Spalten
    public static final String TABLE_TRANSACTIONS = "transactions";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_DESCRIPTION = "description";

    private static final String TABLE_USERS = "users";

    private final Context context;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        // Erstellen der Users-Tabelle
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USERNAME + " TEXT, "
                + COLUMN_PASSWORD + " TEXT);";

        // Erstellen der Transactions-Tabelle
        String CREATE_TRANSACTIONS_TABLE = "CREATE TABLE " + TABLE_TRANSACTIONS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USER_ID + " INTEGER, "
                + COLUMN_TYPE + " INTEGER, "
                + COLUMN_AMOUNT + " REAL, "
                + COLUMN_CATEGORY + " TEXT, "
                + COLUMN_DATE + " TEXT, "
                + COLUMN_DESCRIPTION + " TEXT, "
                + "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + "));";

        // Index zur Optimierung hinzufügen
        String CREATE_INDEX_USER_ID = "CREATE INDEX idx_user_id ON " + TABLE_TRANSACTIONS + "(" + COLUMN_USER_ID + ");";

        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_TRANSACTIONS_TABLE);
        db.execSQL(CREATE_INDEX_USER_ID);

        Log.d(TAG, "Tabellen erstellt: Users und Transactions");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
        Log.d(TAG, "Tabellen aktualisiert (Upgrade von Version " + oldVersion + " auf " + newVersion + ")");
    }

    public boolean deleteTransaction(int transactionId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_TRANSACTIONS, "id = ?", new String[]{String.valueOf(transactionId)});
        return result > 0; // Gibt true zurück, wenn das Löschen erfolgreich war
    }

    // Getter für Tabellennamen und Spalten
    public static String getTableTransactions() {
        return TABLE_TRANSACTIONS;
    }

    public static String getColumnUserId() {
        return COLUMN_USER_ID;
    }

    public static String getColumnAmount() {
        return COLUMN_AMOUNT;
    }

    public static String getColumnDate() {
        return COLUMN_DATE;
    }

    // Benutzeroperationen
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username, password});

        boolean userExists = cursor.getCount() > 0;
        cursor.close();
        return userExists;
    }

    public boolean isUsernameTaken(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_USERNAME + " = ?", new String[]{username});

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public void addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);

        db.insert(TABLE_USERS, null, values);
        db.close();
        Log.d(TAG, "Neuer Benutzer hinzugefügt: " + username);
    }

    public int getUserId(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_ID + " FROM " + TABLE_USERS + " WHERE " + COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username, password});

        int userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
        }
        cursor.close();
        return userId;
    }

    public int getLoggedInUserId() {
        SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        return prefs.getInt("user_id", -1);
    }

    public void logout() {
        SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("user_id");
        editor.apply();
        Log.d(TAG, "Benutzer wurde ausgeloggt.");
    }

    // Transaktionen
    public void addTransaction(int userId, int type, double amount, String category, String date, String description) {
        if (type == EINNAHME && amount < 0) {
            amount = Math.abs(amount); // Betrag für Einnahmen immer positiv
        } else if (type == AUSGABE && amount > 0) {
            amount = -amount; // Betrag für Ausgaben immer negativ
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_TYPE, type);
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_DESCRIPTION, description);

        db.insert(TABLE_TRANSACTIONS, null, values);
        db.close();
        Log.d(TAG, "Transaktion hinzugefügt: UserID=" + userId + ", Typ=" + type + ", Betrag=" + amount + ", Datum=" + date);
    }

    // Berechnung des Kontostands
    public double getBalance(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Summe der Einnahmen berechnen
        String incomeQuery = "SELECT SUM(" + COLUMN_AMOUNT + ") FROM " + TABLE_TRANSACTIONS +
                " WHERE " + COLUMN_USER_ID + " = ? AND " + COLUMN_TYPE + " = ?";
        Cursor incomeCursor = db.rawQuery(incomeQuery, new String[]{String.valueOf(userId), String.valueOf(EINNAHME)});
        double totalIncome = 0;
        if (incomeCursor.moveToFirst()) {
            totalIncome = incomeCursor.getDouble(0);
        }
        incomeCursor.close();

        // Summe der Ausgaben berechnen
        String expenseQuery = "SELECT SUM(" + COLUMN_AMOUNT + ") FROM " + TABLE_TRANSACTIONS +
                " WHERE " + COLUMN_USER_ID + " = ? AND " + COLUMN_TYPE + " = ?";
        Cursor expenseCursor = db.rawQuery(expenseQuery, new String[]{String.valueOf(userId), String.valueOf(AUSGABE)});
        double totalExpense = 0;
        if (expenseCursor.moveToFirst()) {
            totalExpense = expenseCursor.getDouble(0);
        }
        expenseCursor.close();

        // Kontostand berechnen
        return totalIncome - totalExpense;
    }

    // Abrufen aller Transaktionen für einen Benutzer
    public Cursor getTransactionsByUserId(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_TRANSACTIONS, null, COLUMN_USER_ID + " = ?", new String[]{String.valueOf(userId)}, null, null, COLUMN_DATE + " DESC");
    }


    // Hilfsmethode: Validierung des Datumsformats
    private boolean isValidDate(String date) {
        String datePattern = "\\d{4}-\\d{2}-\\d{2}"; // YYYY-MM-DD
        return date != null && date.matches(datePattern);
    }

    public boolean updateTransaction(int transactionId, double amount, String category, String date, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_DESCRIPTION, description);

        // Aktualisiere die Transaktion in der Datenbank
        int rowsAffected = db.update(TABLE_TRANSACTIONS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(transactionId)});
        db.close();

        return rowsAffected > 0; // Gibt true zurück, wenn die Transaktion erfolgreich aktualisiert wurde
    }
}