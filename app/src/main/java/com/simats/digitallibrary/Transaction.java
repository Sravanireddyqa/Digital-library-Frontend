package com.simats.digitallibrary;

/**
 * Transaction model for Payments & Refunds
 */
public class Transaction {
    private int id;
    private String transactionId;
    private int userId;
    private String userName;
    private String userEmail;
    private Integer reservationId;
    private Integer bookId;
    private String bookTitle;
    private String type; // deposit, refund, fine, pending_fine
    private double amount;
    private String status; // completed, pending, cancelled
    private String reason; // safe, late, damaged, lost, booking, rejected
    private int daysLate;
    private double fineRate;
    private String notes;
    private String createdAt;
    private String processedAt;

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Integer getReservationId() {
        return reservationId;
    }

    public void setReservationId(Integer reservationId) {
        this.reservationId = reservationId;
    }

    public Integer getBookId() {
        return bookId;
    }

    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public int getDaysLate() {
        return daysLate;
    }

    public void setDaysLate(int daysLate) {
        this.daysLate = daysLate;
    }

    public double getFineRate() {
        return fineRate;
    }

    public void setFineRate(double fineRate) {
        this.fineRate = fineRate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(String processedAt) {
        this.processedAt = processedAt;
    }

    // Helper methods
    public String getTypeDisplay() {
        switch (type) {
            case "deposit":
                return "💰 Deposit";
            case "refund":
                return "💸 Refund";
            case "fine":
                return "⚠️ Fine";
            case "pending_fine":
                return "⏳ Pending Fine";
            default:
                return type;
        }
    }

    public String getReasonDisplay() {
        if (reason == null)
            return "";
        switch (reason) {
            case "safe":
                return "Safe Return";
            case "late":
                return "Late Return";
            case "damaged":
                return "Book Damaged";
            case "lost":
                return "Book Lost";
            case "booking":
                return "Booking Deposit";
            case "rejected":
                return "Booking Rejected";
            default:
                return reason;
        }
    }

    public int getTypeColor() {
        switch (type) {
            case "deposit":
                return 0xFF10B981; // Green
            case "refund":
                return 0xFF3B82F6; // Blue
            case "fine":
                return 0xFFF59E0B; // Orange
            case "pending_fine":
                return 0xFFEF4444; // Red
            default:
                return 0xFF6B7280; // Gray
        }
    }
}
