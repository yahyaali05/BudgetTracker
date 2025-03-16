package com.example.BudgetTracker;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private final Context context; // Kontext zum Zugriff auf Ressourcen und UI
    private Cursor transactionsCursor; // Cursor, der die Transaktionsdaten enthält
    private final TransactionRepository transactionRepository; // Repository für Datenbankoperationen

    // Konstanten für Spaltennamen in der Datenbank
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_AMOUNT = "amount";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_DESCRIPTION = "description";

    // Konstruktor, der den Kontext, den Cursor und das Repository initialisiert
    public TransactionAdapter(Context context, Cursor cursor, TransactionRepository transactionRepository) {
        this.context = context;
        this.transactionsCursor = cursor;
        this.transactionRepository = transactionRepository;
    }

    // ViewHolder-Klasse zum Halten von Referenzen auf die Views
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView typeTextView; // TextView für den Transaktionstyp
        final TextView amountTextView; // TextView für den Transaktionsbetrag
        final TextView categoryTextView; // TextView für die Transaktionskategorie
        final TextView dateTextView; // TextView für das Transaktionsdatum
        final TextView descriptionTextView; // TextView für die Transaktionsbeschreibung

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialisierung der Views mit itemView.findViewById
            typeTextView = itemView.findViewById(R.id.textViewType);
            amountTextView = itemView.findViewById(R.id.textViewAmount);
            categoryTextView = itemView.findViewById(R.id.textViewCategory);
            dateTextView = itemView.findViewById(R.id.textViewDate);
            descriptionTextView = itemView.findViewById(R.id.textViewDescription);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Layout für das Listenelement aufblähen und den ViewHolder erstellen
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Überprüfen, ob der Cursor gültig ist und auf die richtige Position bewegt wird
        if (transactionsCursor == null || transactionsCursor.isClosed() || !transactionsCursor.moveToPosition(position)) {
            bindEmptyData(holder); // Leere Daten binden, wenn der Cursor ungültig ist
            return;
        }

        // Abrufen der Daten aus dem Cursor anhand der Spaltenindizes
        int typeIndex = transactionsCursor.getColumnIndex(COLUMN_TYPE);
        int amountIndex = transactionsCursor.getColumnIndex(COLUMN_AMOUNT);
        int categoryIndex = transactionsCursor.getColumnIndex(COLUMN_CATEGORY);
        int dateIndex = transactionsCursor.getColumnIndex(COLUMN_DATE);
        int descriptionIndex = transactionsCursor.getColumnIndex(COLUMN_DESCRIPTION);

        // Überprüfen, ob die Indizes gültig sind, andernfalls leere Daten binden
        if (typeIndex == -1 || amountIndex == -1 || categoryIndex == -1 || dateIndex == -1 || descriptionIndex == -1) {
            bindEmptyData(holder);
            return;
        }

        // Werte aus dem Cursor extrahieren und ein Transaction-Objekt erstellen
        int type = transactionsCursor.getInt(typeIndex);
        double amount = transactionsCursor.getDouble(amountIndex);
        String category = transactionsCursor.getString(categoryIndex);
        String date = transactionsCursor.getString(dateIndex);
        String description = transactionsCursor.getString(descriptionIndex);
        int id = transactionsCursor.getInt(transactionsCursor.getColumnIndex("id"));

        Transaction transaction = new Transaction(id, type, amount, category, date, description);

        // ClickListener für das Bearbeiten der Transaktion setzen
        holder.itemView.setOnClickListener(v -> openEditTransactionDialog(transaction));

        // Falls Kategorie oder Beschreibung null ist, Standardwerte setzen
        category = (category != null) ? category : context.getString(R.string.default_category);
        description = (description != null) ? description : context.getString(R.string.default_description);

        // Text für jede TextView setzen
        String typeText = (type == DatabaseHelper.EINNAHME)
                ? context.getString(R.string.type_income)
                : context.getString(R.string.type_expense);

        holder.typeTextView.setText(typeText);
        holder.amountTextView.setText(String.format(Locale.getDefault(), context.getString(R.string.amount_format), amount));
        holder.categoryTextView.setText(context.getString(R.string.category_label, category));
        holder.dateTextView.setText(context.getString(R.string.date_label, formatDate(date)));
        holder.descriptionTextView.setText(context.getString(R.string.description_label, description));

        // Textfarbe basierend auf dem Transaktionstyp (Einnahme oder Ausgabe) setzen
        int textColor = (type == DatabaseHelper.EINNAHME)
                ? ContextCompat.getColor(context, R.color.income_color)
                : ContextCompat.getColor(context, R.color.expense_color);
        holder.amountTextView.setTextColor(textColor);

        // Löschen-Button ClickListener
        holder.itemView.findViewById(R.id.buttonDeleteTransaction).setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle(R.string.delete_confirmation_title)
                    .setMessage(R.string.delete_confirmation_message)
                    .setPositiveButton(R.string.yes, (dialog, which) -> {
                        // Transaktion löschen und Liste aktualisieren
                        int transactionId = transactionsCursor.getInt(transactionsCursor.getColumnIndex("id"));
                        boolean success = transactionRepository.deleteTransaction(transactionId);
                        if (!success) {
                            Toast.makeText(context, R.string.delete_error, Toast.LENGTH_SHORT).show();
                        }
                        updateCursor(transactionRepository.getTransactionsForUser(transactionRepository.getLoggedInUserId(), true));
                    })
                    .setNegativeButton(R.string.no, null)
                    .show();
        });

        // Bearbeiten-Button ClickListener
        holder.itemView.findViewById(R.id.buttonEditTransaction).setOnClickListener(v -> {
            openEditTransactionDialog(transaction); // Öffnet den Bearbeitungsdialog
        });
    }

    // Methode, um den Bearbeitungsdialog für eine Transaktion anzuzeigen
    private void openEditTransactionDialog(Transaction transaction) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.edit_transaction_title);

        // Layout für den Dialog aufblähen
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_transaction, null);
        builder.setView(dialogView);

        // Widgets aus dem Dialoglayout
        EditText amountEditText = dialogView.findViewById(R.id.amountEditText);
        EditText categoryEditText = dialogView.findViewById(R.id.categoryEditText);
        EditText dateEditText = dialogView.findViewById(R.id.dateEditText);
        EditText descriptionEditText = dialogView.findViewById(R.id.descriptionEditText);

        // Aktuelle Werte für die Felder setzen
        amountEditText.setText(String.valueOf(transaction.getAmount()));
        categoryEditText.setText(transaction.getCategory());
        dateEditText.setText(transaction.getDate());
        descriptionEditText.setText(transaction.getDescription());

        // Speichern-Button ClickListener
        builder.setPositiveButton(R.string.save, (dialog, which) -> {
            double newAmount = Double.parseDouble(amountEditText.getText().toString());
            String newCategory = categoryEditText.getText().toString();
            String newDate = dateEditText.getText().toString();
            String newDescription = descriptionEditText.getText().toString();

            // Transaktion in der Datenbank aktualisieren
            boolean updated = transactionRepository.updateTransaction(transaction.getId(), newAmount, newCategory, newDate, newDescription);
            if (updated) {
                Toast.makeText(context, R.string.transaction_updated, Toast.LENGTH_SHORT).show();
                updateCursor(transactionRepository.getTransactionsForUser(transactionRepository.getLoggedInUserId(), true));

                // Kontostand im DashboardActivity aktualisieren
                if (context instanceof DashboardActivity) {
                    ((DashboardActivity) context).updateBalance();
                }
            } else {
                Toast.makeText(context, R.string.transaction_update_error, Toast.LENGTH_SHORT).show();
            }
        });

        // Abbrechen-Button ClickListener
        builder.setNegativeButton(R.string.cancel, null);

        // Den Dialog anzeigen
        builder.create().show();
    }

    @Override
    public int getItemCount() {
        // Gibt die Anzahl der Elemente im Cursor zurück
        return (transactionsCursor != null && !transactionsCursor.isClosed()) ? transactionsCursor.getCount() : 0;
    }

    // Methode, um den Cursor mit neuen Daten zu aktualisieren
    public void updateCursor(Cursor newCursor) {
        if (transactionsCursor != null && !transactionsCursor.isClosed()) {
            transactionsCursor.close();
        }
        transactionsCursor = newCursor;
        notifyDataSetChanged();
    }

    // Methode, um den Cursor zu schließen, wenn er nicht mehr benötigt wird
    public void closeCursor() {
        if (transactionsCursor != null && !transactionsCursor.isClosed()) {
            transactionsCursor.close();
            transactionsCursor = null;
        }
    }

    // Methode zur Formatierung des Datums von "yyyy-MM-dd" nach "dd.MM.yyyy"
    private String formatDate(String date) {
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat userFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        try {
            return userFormat.format(dbFormat.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
            return date;
        }
    }

    // Methode, um leere Daten zu binden, falls der Cursor ungültig ist
    private void bindEmptyData(ViewHolder holder) {
        holder.typeTextView.setText("");
        holder.amountTextView.setText("");
        holder.categoryTextView.setText("");
        holder.dateTextView.setText("");
        holder.descriptionTextView.setText("");
    }
}
