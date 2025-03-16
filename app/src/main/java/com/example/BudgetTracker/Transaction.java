package com.example.BudgetTracker;

public class Transaction {

    private int id; // Transaction ID
    private int type; // Transaction type (e.g., income or expense)
    private double amount; // Amount of the transaction
    private String category; // Category of the transaction (e.g., Food, Rent)
    private String date; // Date of the transaction
    private String description; // Description or additional information about the transaction

    // Constructor to initialize a new Transaction object with given parameters
    public Transaction(int id, int type, double amount, String category, String date, String description) {
        this.id = id; // Initialize transaction ID
        this.type = type; // Initialize transaction type (e.g., income or expense)
        this.amount = amount; // Initialize amount
        this.category = category; // Initialize category (e.g., Food, Rent)
        this.date = date; // Initialize transaction date
        this.description = description; // Initialize description
    }

    // Getter and Setter methods for each property

    // Getter for transaction ID
    public int getId() {
        return id;
    }

    // Setter for transaction ID
    public void setId(int transactionId) {
        this.id = transactionId;
    }

    // Getter for transaction amount
    public double getAmount() {
        return amount;
    }

    // Setter for transaction amount
    public void setAmount(double amount) {
        this.amount = amount;
    }

    // Getter for transaction category
    public String getCategory() {
        return category;
    }

    // Setter for transaction category
    public void setCategory(String category) {
        this.category = category;
    }

    // Getter for transaction description
    public String getDescription() {
        return description;
    }

    // Setter for transaction description
    public void setDescription(String description) {
        this.description = description;
    }

    // Getter for transaction type (e.g., income or expense)
    public int getType() {
        return type;
    }

    // Setter for transaction type
    public void setType(int type) {
        this.type = type;
    }

    // Getter for transaction date
    public String getDate() {
        return date;
    }

    // Setter for transaction date
    public void setDate(String date) {
        this.date = date;
    }

}
