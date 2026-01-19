package com.simats.digitallibrary;

public enum NotificationType {
    NEW_BOOK("new_book", "📚", "New Book Added", 1),
    RESERVATION_CONFIRMED("reservation_confirmed", "✅", "Reservation Confirmed", 2),
    RESERVATION_REJECTED("reservation_rejected", "❌", "Reservation Rejected", 2),
    BOOK_READY("book_ready", "📦", "Book Ready for Pickup", 3),
    RETURN_REMINDER("return_reminder", "⏰", "Return Reminder", 2),
    OVERDUE_FINE("overdue_fine", "⚠️", "Overdue Fine", 3),
    ACCOUNT_BLOCKED("account_blocked", "🚫", "Account Alert", 3),
    GENERAL_ANNOUNCEMENT("general_announcement", "📢", "Announcement", 1),
    PROFILE_UPDATED("profile_updated", "🔐", "Profile Updated", 1);

    private final String key;
    private final String icon;
    private final String title;
    private final int priority; // 1=Low, 2=Normal, 3=High

    NotificationType(String key, String icon, String title, int priority) {
        this.key = key;
        this.icon = icon;
        this.title = title;
        this.priority = priority;
    }

    public String getKey() {
        return key;
    }

    public String getIcon() {
        return icon;
    }

    public String getTitle() {
        return title;
    }

    public int getPriority() {
        return priority;
    }

    public static NotificationType fromKey(String key) {
        for (NotificationType type : values()) {
            if (type.key.equals(key)) {
                return type;
            }
        }
        return GENERAL_ANNOUNCEMENT; // Default
    }
}
