package com.example.BudgetTracker;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class DashboardActivity extends AppCompatActivity {

    // UI-Komponenten
    private TextView balanceTextView, emptyView; // Anzeige des Kontostands und leere Ansicht
    private Button addTransactionButton, viewReportButton, logoutButton; // Buttons für verschiedene Aktionen
    private RecyclerView transactionsRecyclerView; // Liste für Transaktionen
    private SwipeRefreshLayout swipeRefreshLayout; // Swipe-Refresh-Layout zur Aktualisierung

    // Adapter und Repository für die Verwaltung von Transaktionen
    private TransactionAdapter transactionAdapter;
    private TransactionRepository transactionRepository;

    private int userId; // ID des angemeldeten Benutzers

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);


        // Initialisieren der UI-Komponenten
        balanceTextView = findViewById(R.id.textViewBalance);
        emptyView = findViewById(R.id.textViewEmpty);
        addTransactionButton = findViewById(R.id.buttonAddTransaction);
        viewReportButton = findViewById(R.id.buttonViewReport);
        logoutButton = findViewById(R.id.buttonLogout);
        transactionsRecyclerView = findViewById(R.id.recyclerViewTransactions);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        // Repository zur Verwaltung der Datenbankzugriffe initialisieren
        transactionRepository = new TransactionRepository(this);

        // ID des aktuell angemeldeten Benutzers abrufen
        userId = transactionRepository.getLoggedInUserId();

        // Wenn kein Benutzer angemeldet ist, zur Login-Aktivität weiterleiten
        if (userId == -1) {
            Toast.makeText(this, R.string.error_not_logged_in, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish(); // Beendet die aktuelle Aktivität
            return;
        }

        // RecyclerView für die Anzeige von Transaktionen einrichten
        transactionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Swipe-to-Refresh-Aktion einrichten
        swipeRefreshLayout.setOnRefreshListener(this::refreshDashboard);

        // Klick-Listener für Buttons einrichten
        addTransactionButton.setOnClickListener(view ->
                startActivity(new Intent(this, AddTransactionActivity.class)));

        viewReportButton.setOnClickListener(view ->
                startActivity(new Intent(this, ReportActivity.class)));

        logoutButton.setOnClickListener(view -> showLogoutConfirmation());

        // Dashboard initial laden
        refreshDashboard();
    }

    //test
//test2
    /**
     * Aktualisiert die Anzeige des Dashboards.
     */
    private void refreshDashboard() {
        loadTransactions(true); // Transaktionen nach Datum sortieren
        updateBalance(); // Kontostand aktualisieren
        swipeRefreshLayout.setRefreshing(false); // Swipe-Refresh-Indikator deaktivieren
    }

    /**
     * Lädt die Transaktionen des Benutzers und zeigt sie im RecyclerView an.
     * @param sortByDate Bestimmt, ob die Transaktionen nach Datum sortiert werden sollen.
     */
    private void loadTransactions(boolean sortByDate) {
        Cursor cursor = transactionRepository.getTransactionsForUser(userId, sortByDate);

        if (cursor == null || cursor.getCount() == 0) {
            // Keine Transaktionen gefunden: leere Ansicht anzeigen
         //   Toast.makeText(this, R.string.error_loading_transactions, Toast.LENGTH_SHORT).show();
            emptyView.setVisibility(TextView.VISIBLE);
            transactionsRecyclerView.setVisibility(RecyclerView.GONE);
            return;
        }

        // Transaktionen anzeigen
        emptyView.setVisibility(TextView.GONE);
        transactionsRecyclerView.setVisibility(RecyclerView.VISIBLE);

        // Adapter initialisieren oder aktualisieren
        if (transactionAdapter == null) {
            transactionAdapter = new TransactionAdapter(this, cursor, transactionRepository);
            transactionsRecyclerView.setAdapter(transactionAdapter);
        } else {
            transactionAdapter.updateCursor(cursor); // Daten im Adapter aktualisieren
        }
    }

    /**
     * Aktualisiert den Kontostand und zeigt ihn an.
     */
    public void updateBalance() {
        double balance = transactionRepository.getBalance(userId);
        balanceTextView.setText(String.format(getString(R.string.amount_format), balance));
    }

    /**
     * Zeigt einen Bestätigungsdialog für das Abmelden an.
     */
    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.logout_title)
                .setMessage(R.string.logout_confirmation)
                .setPositiveButton(R.string.logout_yes, (dialogInterface, i) -> logout())
                .setNegativeButton(R.string.logout_no, null)
                .show();
    }

    /**
     * Meldet den Benutzer ab und leitet ihn zur Login-Aktivität weiter.
     */
    private void logout() {
        transactionRepository.logout();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshDashboard(); // Dashboard aktualisieren, wenn die Aktivität wieder sichtbar wird
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Ressourcen freigeben, wenn die Aktivität pausiert wird
        if (transactionAdapter != null) {
            transactionAdapter.closeCursor(); // Cursor im Adapter schließen
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Ressourcen freigeben, wenn die Aktivität gestoppt wird
        if (transactionAdapter != null) {
            transactionAdapter.closeCursor();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Letzter Ressourcen-Cleanup beim Zerstören der Aktivität
        if (transactionAdapter != null) {
            transactionAdapter.closeCursor();
        }
    }
}
