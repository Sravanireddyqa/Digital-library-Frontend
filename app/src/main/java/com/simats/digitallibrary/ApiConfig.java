package com.simats.digitallibrary;

/**
 * Centralized API Configuration
 * ================================
 * CHANGE THE IP ADDRESS BELOW TO YOUR SERVER'S IP
 * All API calls will automatically use this IP
 */
public class ApiConfig {

    // =====================================================
    // SERVER CONFIGURATION - CHANGE THESE VALUES
    // =====================================================

    // 10.0.2.2 = Android Emulator's alias for host computer's localhost
    public static final String SERVER_IP = "10.36.207.135";

    // Server port (XAMPP default is 80)
    public static final String SERVER_PORT = "80";

    // Folder name in htdocs
    public static final String PROJECT_FOLDER = "digitallibrary_API";

    // =====================================================
    // BASE URL - Auto-constructed from above
    // =====================================================
    public static final String BASE_URL = "http://" + SERVER_IP + ":" + SERVER_PORT + "/" + PROJECT_FOLDER + "/";

    // =====================================================
    // API ENDPOINTS
    // =====================================================
    public static final String URL_LOGIN = BASE_URL + "login.php";
    public static final String URL_REGISTER = BASE_URL + "register.php";
    public static final String URL_BOOK_LOOKUP = BASE_URL + "book_lookup.php";
    public static final String URL_GET_PROFILE = BASE_URL + "get_profile.php";
    public static final String URL_ADD_BOOK = BASE_URL + "add_book.php";
    public static final String URL_GET_BOOKS = BASE_URL + "get_books.php";
    public static final String URL_DELETE_BOOK = BASE_URL + "delete_book.php";
    public static final String URL_UPDATE_BOOK = BASE_URL + "update_book.php";
    public static final String URL_GET_RESERVATIONS = BASE_URL + "get_reservations.php";
    public static final String URL_UPDATE_RESERVATION = BASE_URL + "update_reservation.php";
    public static final String URL_GET_USERS = BASE_URL + "get_users.php";
    public static final String URL_UPDATE_USER = BASE_URL + "update_user.php";
    public static final String URL_GET_INVOICES = BASE_URL + "get_invoices.php";
    public static final String URL_UPDATE_INVOICE = BASE_URL + "update_invoice.php";
    public static final String URL_GET_RECENT_ACTIVITY = BASE_URL + "get_recent_activity.php";

    // Reader APIs
    public static final String URL_GET_READER_STATS = BASE_URL + "get_reader_stats.php";
    public static final String URL_GET_CATEGORIES = BASE_URL + "get_categories.php";
    public static final String URL_GET_MY_BOOKINGS = BASE_URL + "get_my_bookings.php";
    public static final String URL_CREATE_RESERVATION = BASE_URL + "create_reservation.php";
    public static final String URL_GET_WISHLIST = BASE_URL + "get_wishlist.php";
    public static final String URL_TOGGLE_WISHLIST = BASE_URL + "toggle_wishlist.php";
    public static final String URL_NLP_SEARCH = BASE_URL + "nlp_search.php";
    public static final String URL_GET_LIBRARIES = BASE_URL + "get_libraries.php";
    public static final String URL_GET_BOOK_DETAILS = BASE_URL + "get_book_details.php";
    public static final String URL_UPDATE_PROFILE = BASE_URL + "update_profile.php";
    public static final String URL_CHANGE_PASSWORD = BASE_URL + "change_password.php";

    // Image Upload APIs
    public static final String URL_UPLOAD_IMAGE = BASE_URL + "upload_image.php";
    public static final String URL_DELETE_IMAGE = BASE_URL + "delete_image.php";
    public static final String IMAGES_BASE_URL = BASE_URL + "uploads/";

    // Forgot Password APIs
    public static final String URL_FORGOT_PASSWORD = BASE_URL + "forgot_password.php";
    public static final String URL_VERIFY_OTP = BASE_URL + "verify_otp.php";
    public static final String URL_RESET_PASSWORD = BASE_URL + "reset_password.php";

    // Notification APIs
    public static final String URL_REGISTER_FCM_TOKEN = BASE_URL + "register_fcm_token.php";
    public static final String URL_GET_NOTIFICATIONS = BASE_URL + "get_notifications.php";
    public static final String URL_MARK_NOTIFICATION_READ = BASE_URL + "mark_notification_read.php";
    public static final String URL_DELETE_NOTIFICATION = BASE_URL + "delete_notification.php";

    // Rating APIs
    public static final String URL_SUBMIT_RATING = BASE_URL + "submit_rating.php";
    public static final String URL_GET_RATINGS = BASE_URL + "get_ratings.php";

    // Transaction/Payment APIs
    public static final String URL_GET_TRANSACTIONS = BASE_URL + "get_transactions.php";
    public static final String URL_PROCESS_RETURN = BASE_URL + "process_return.php";
    public static final String URL_SEND_REMINDER = BASE_URL + "send_reminder.php";
    public static final String URL_CANCEL_RESERVATION = BASE_URL + "cancel_reservation.php";

    // =====================================================
    // TIMEOUT SETTINGS
    // =====================================================
    public static final int TIMEOUT_MS = 30000; // 30 seconds
    public static final int MAX_RETRIES = 1;

    // =====================================================
    // GEMINI AI API (for chatbot)
    // =====================================================
    public static final String GEMINI_API_KEY = "AIzaSyDbk3ECfin-Uvpy-N9NpgiqrRmUrzwtsSc";
    public static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key="
            + GEMINI_API_KEY;
}
