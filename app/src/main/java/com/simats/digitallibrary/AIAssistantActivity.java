package com.simats.digitallibrary;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * AI Assistant Activity
 * Smart NLP-powered chatbot for semantic book search
 */
public class AIAssistantActivity extends AppCompatActivity {

    private static final int VOICE_REQUEST_CODE = 100;

    private RecyclerView recyclerChat;
    private EditText etMessage;
    private ImageButton btnSend, btnBack, btnClearChat, btnVoice;
    private TextView tvStatus;
    private LinearLayout layoutExamples;
    private View statusDot;

    private ChatAdapter adapter;
    private List<ChatMessage> messages = new ArrayList<>();
    private Handler handler = new Handler(Looper.getMainLooper());

    // Chat persistence
    private static final String PREF_CHAT_HISTORY = "AIAssistantChatHistory";
    private static final String KEY_MESSAGES = "chat_messages";
    private static final String KEY_TIMESTAMP = "chat_timestamp";
    private static final long ONE_DAY_MILLIS = 24L * 60L * 60L * 1000L; // 1 day in milliseconds
    private SharedPreferences chatPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_assistant);

        chatPrefs = getSharedPreferences(PREF_CHAT_HISTORY, MODE_PRIVATE);

        initViews();
        setupRecyclerView();
        setupListeners();

        // Load chat history or show welcome
        if (!loadChatHistory()) {
            showWelcomeMessage();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveChatHistory();
    }

    private void initViews() {
        recyclerChat = findViewById(R.id.recyclerChat);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnBack = findViewById(R.id.btnBack);
        btnClearChat = findViewById(R.id.btnClearChat);
        btnVoice = findViewById(R.id.btnVoice);
        tvStatus = findViewById(R.id.tvStatus);
        layoutExamples = findViewById(R.id.layoutExamples);
        statusDot = findViewById(R.id.statusDot);
    }

    private void setupRecyclerView() {
        adapter = new ChatAdapter(messages);
        recyclerChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerChat.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnSend.setOnClickListener(v -> sendMessage());

        btnVoice.setOnClickListener(v -> startVoiceInput());

        btnClearChat.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Clear Chat")
                    .setMessage("Are you sure you want to clear the conversation?")
                    .setPositiveButton("Clear", (d, w) -> {
                        messages.clear();
                        adapter.notifyDataSetChanged();
                        clearChatHistory(); // Also clear saved history
                        showWelcomeMessage();
                        layoutExamples.setVisibility(View.VISIBLE);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        etMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });

        // Example chips
        findViewById(R.id.chipExample1).setOnClickListener(v -> processQuery("Easy C programming books"));
        findViewById(R.id.chipExample2).setOnClickListener(v -> processQuery("Python beginner books"));
        findViewById(R.id.chipExample3).setOnClickListener(v -> processQuery("Data science books"));
        findViewById(R.id.chipExample4).setOnClickListener(v -> processQuery("Fiction novels"));
        findViewById(R.id.chipExample5).setOnClickListener(v -> processQuery("My reservations"));
        findViewById(R.id.chipExample6).setOnClickListener(v -> processQuery("Library hours"));
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Ask about books...");

        try {
            startActivityForResult(intent, VOICE_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Voice input not supported", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VOICE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                String voiceText = results.get(0);
                etMessage.setText(voiceText);
                sendMessage();
            }
        }
    }

    private void showWelcomeMessage() {
        addMessage("👋 Hi! I'm your **AI Library Assistant**.\n\n" +
                "I use **semantic search** to understand what you're looking for.\n\n" +
                "Try asking me:\n" +
                "• \"Easy C programming books\"\n" +
                "• \"Python for beginners\"\n" +
                "• \"My reservations\" - Check your bookings\n" +
                "• \"Library hours\" - Opening times\n\n" +
                "What would you like to find?", false);
        layoutExamples.setVisibility(View.VISIBLE);
    }

    private void sendMessage() {
        String message = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(message))
            return;

        etMessage.setText("");
        layoutExamples.setVisibility(View.GONE);
        processQuery(message);
    }

    private void processQuery(String query) {
        addMessage(query, true);

        // Check if this is a conversational message (not a book search)
        if (isConversationalQuery(query)) {
            handleLocalQuery(query);
            return;
        }

        // Check for reservation/booking queries
        if (isReservationQuery(query)) {
            showMyReservations();
            return;
        }

        // Check for library info queries
        if (isLibraryInfoQuery(query)) {
            showLibraryInfo(query);
            return;
        }

        // Check for help/doubt queries
        if (isHelpQuery(query)) {
            showHelpResponse();
            return;
        }

        showTypingIndicator();

        // Call NLP search API for book searches
        String url = ApiConfig.URL_NLP_SEARCH + "?query=" + query.replace(" ", "%20");

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    hideTypingIndicator();
                    handleSearchResponse(query, response);
                },
                error -> {
                    hideTypingIndicator();
                    handleLocalQuery(query);
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    /**
     * Check if query is conversational (not a book search)
     */
    private boolean isConversationalQuery(String query) {
        String lower = query.toLowerCase().trim();

        // Greetings - only pure greetings
        if (lower.matches("^(hi+|hey|hello|hii+|hola|namaste)$") ||
                lower.matches("^(hi+|hey|hello|hii+|hola|namaste)\\s*[!.]*$"))
            return true;

        // Questions about the bot/help
        if (lower.contains("who are you") || lower.contains("what are you"))
            return true;
        if (lower.contains("what can you do") || lower.contains("features"))
            return true;

        // Thanks/bye
        if (lower.contains("thank") || lower.contains("bye") || lower.contains("goodbye"))
            return true;

        // How-to questions (not book searches)
        if (lower.contains("how to reserve") || lower.contains("how to return"))
            return true;
        if (lower.contains("how do i") && !lower.contains("book"))
            return true;

        // Very short messages (likely not book searches)
        if (lower.length() <= 2)
            return true;

        return false;
    }

    /**
     * Check if query is asking for help/doubt
     */
    private boolean isHelpQuery(String query) {
        String lower = query.toLowerCase().trim();
        return lower.contains("doubt") || lower.contains("help me") ||
                lower.contains("problem") || lower.contains("issue") ||
                lower.contains("not working") || lower.contains("confused");
    }

    /**
     * Show help response
     */
    private void showHelpResponse() {
        addMessage("🤔 I'm here to help! Here's what I can do:\n\n" +
                "📚 **Find Books**\n" +
                "Try: \"Python books\", \"Fiction novels\"\n\n" +
                "📋 **Check Reservations**\n" +
                "Say: \"My reservations\" or \"My bookings\"\n\n" +
                "🕐 **Library Info**\n" +
                "Ask: \"Library hours\", \"Library location\"\n\n" +
                "What would you like help with?", false);
    }

    /**
     * Check if query is about reservations
     */
    private boolean isReservationQuery(String query) {
        String lower = query.toLowerCase().trim();
        return lower.contains("my reservation") || lower.contains("my booking") ||
                lower.contains("my books") || lower.contains("reserved books") ||
                lower.contains("show reservation") || lower.contains("check booking");
    }

    /**
     * Check if query is about library info
     */
    private boolean isLibraryInfoQuery(String query) {
        String lower = query.toLowerCase().trim();
        return lower.contains("library hour") || lower.contains("opening time") ||
                lower.contains("library timing") || lower.contains("when open") ||
                lower.contains("close time") || lower.contains("library policy") ||
                lower.contains("library address") || lower.contains("library location");
    }

    /**
     * Show user's reservations
     */
    private void showMyReservations() {
        addMessage("📋 Opening your reservations...", false);

        // Navigate to My Bookings
        addActionMessage("📚 View My Bookings", () -> {
            Intent intent = new Intent(this, MyBookingsActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Show library information
     */
    private void showLibraryInfo(String query) {
        String lower = query.toLowerCase();

        if (lower.contains("hour") || lower.contains("time") || lower.contains("open") || lower.contains("close")) {
            addMessage("🕐 **Library Hours**\n\n" +
                    "📅 **Monday - Friday:** 9:00 AM - 7:00 PM\n" +
                    "📅 **Saturday:** 9:00 AM - 7:00 PM\n" +
                    "📅 **Sunday:** Closed\n\n" +
                    "📞 Contact: +91 9876543210", false);
        } else if (lower.contains("address") || lower.contains("location")) {
            addMessage("📍 **Library Location**\n\n" +
                    "SIMATS Digital Library\n" +
                    "Saveetha Medical College Campus\n" +
                    "Thandalam, Chennai - 602105\n\n" +
                    "📞 Contact: +91 9876543210", false);
        } else if (lower.contains("policy")) {
            addMessage("📜 **Library Policies**\n\n" +
                    "• Books can be reserved for up to **14 days**\n" +
                    "• Maximum **3 books** at a time\n" +
                    "• Late return: **₹5/day** fine\n" +
                    "• Damaged books: **Full replacement cost**\n" +
                    "• Deposit is refundable on book return", false);
        } else {
            addMessage("🏛️ **SIMATS Digital Library**\n\n" +
                    "🕐 Hours: Mon-Sat 9AM-7PM, Sun Closed\n" +
                    "📍 Saveetha Medical College Campus\n" +
                    "📞 Contact: +91 9876543210\n\n" +
                    "How can I help you today?", false);
        }
    }

    private void handleSearchResponse(String query, JSONObject response) {
        try {
            if (response.getBoolean("success")) {
                JSONArray books = response.getJSONArray("books");
                int count = books.length();

                JSONObject nlp = response.optJSONObject("nlp");
                String understood = nlp != null ? nlp.optString("understood_as", query) : query;

                if (count > 0) {
                    // Add text response
                    addMessage("🔍 **Semantic Search:** " + understood + "\n\n" +
                            "Found **" + count + " books** matching your query!", false);

                    // Add book cards (show up to 5)
                    int showCount = Math.min(5, count);
                    for (int i = 0; i < showCount; i++) {
                        JSONObject book = books.getJSONObject(i);
                        addBookCard(book);
                    }

                    if (count > 5) {
                        addActionMessage("📚 View all " + count + " results", () -> {
                            Intent intent = new Intent(this, SearchBooksActivity.class);
                            intent.putExtra("category", query);
                            startActivity(intent);
                        });
                    }
                } else {
                    addMessage("🤔 No books found for \"" + query + "\".\n\n" +
                            "Try:\n" +
                            "• Different keywords\n" +
                            "• Broader terms like \"programming\" or \"fiction\"\n" +
                            "• Author names", false);
                }
            } else {
                handleLocalQuery(query);
            }
        } catch (JSONException e) {
            handleLocalQuery(query);
        }
    }

    private void addBookCard(JSONObject book) {
        try {
            ChatMessage msg = new ChatMessage("", false, System.currentTimeMillis());
            msg.isBookCard = true;
            msg.bookId = book.optInt("id", 0);
            msg.bookTitle = book.optString("title", "Unknown");
            msg.bookAuthor = book.optString("author", "Unknown");
            msg.bookCategory = book.optString("category", "General");
            msg.bookCover = book.optString("cover_url", "");
            msg.bookAvailable = book.optInt("available_copies", 1) > 0;

            messages.add(msg);
            adapter.notifyItemInserted(messages.size() - 1);
            recyclerChat.scrollToPosition(messages.size() - 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleLocalQuery(String query) {
        // Use Gemini AI for intelligent responses
        askGemini(query);
    }

    /**
     * Call Gemini AI API for intelligent chatbot responses
     */
    private void askGemini(String query) {
        showTypingIndicator();

        try {
            // Build the request body
            JSONObject requestBody = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject content = new JSONObject();
            JSONArray parts = new JSONArray();
            JSONObject part = new JSONObject();

            // System prompt to make it library-focused
            String systemPrompt = "You are a helpful AI Library Assistant for a digital library app. " +
                    "Your main job is to help users with library-related questions like: " +
                    "finding books, reservations, returns, library policies, and general help. " +
                    "Keep responses friendly, concise, and helpful. Use emojis occasionally. " +
                    "If someone asks about books, suggest they search for specific topics. " +
                    "User query: ";

            part.put("text", systemPrompt + query);
            parts.put(part);
            content.put("parts", parts);
            contents.put(content);
            requestBody.put("contents", contents);

            // Add generation config for better responses
            JSONObject genConfig = new JSONObject();
            genConfig.put("maxOutputTokens", 500);
            genConfig.put("temperature", 0.7);
            requestBody.put("generationConfig", genConfig);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    ApiConfig.GEMINI_API_URL,
                    requestBody,
                    response -> {
                        hideTypingIndicator();
                        try {
                            // Parse Gemini response
                            JSONArray candidates = response.getJSONArray("candidates");
                            if (candidates.length() > 0) {
                                JSONObject candidate = candidates.getJSONObject(0);
                                JSONObject contentObj = candidate.getJSONObject("content");
                                JSONArray partsArr = contentObj.getJSONArray("parts");
                                if (partsArr.length() > 0) {
                                    String aiResponse = partsArr.getJSONObject(0).getString("text");
                                    addMessage(aiResponse, false);
                                    return;
                                }
                            }
                            // Fallback if parsing fails
                            addMessage("I'm having trouble understanding. Could you rephrase that?", false);
                        } catch (JSONException e) {
                            addMessage("Sorry, I couldn't process that. Try asking differently! 😊", false);
                        }
                    },
                    error -> {
                        hideTypingIndicator();
                        // Fallback to simple responses if Gemini fails
                        handleFallbackResponse(query);
                    });

            VolleySingleton.getInstance(this).addToRequestQueue(request);

        } catch (JSONException e) {
            hideTypingIndicator();
            handleFallbackResponse(query);
        }
    }

    /**
     * Fallback responses when Gemini API is unavailable
     */
    private void handleFallbackResponse(String query) {
        String lowerQuery = query.toLowerCase();
        String response;

        if (lowerQuery.contains("hello") || lowerQuery.contains("hi") || lowerQuery.contains("hey")) {
            response = "👋 Hello! How can I help you today?";
        } else if (lowerQuery.contains("thank")) {
            response = "You're welcome! 😊";
        } else if (lowerQuery.contains("bye")) {
            response = "👋 Goodbye! Happy reading!";
        } else {
            response = "🤖 I'm having trouble connecting to my brain right now. " +
                    "Please try searching for books directly or ask me again later!";
        }

        addMessage(response, false);
    }

    private void addMessage(String text, boolean isUser) {
        messages.add(new ChatMessage(text, isUser, System.currentTimeMillis()));
        adapter.notifyItemInserted(messages.size() - 1);
        recyclerChat.scrollToPosition(messages.size() - 1);
    }

    private void addActionMessage(String buttonText, Runnable action) {
        ChatMessage msg = new ChatMessage(buttonText, false, System.currentTimeMillis());
        msg.isActionButton = true;
        msg.action = action;
        messages.add(msg);
        adapter.notifyItemInserted(messages.size() - 1);
        recyclerChat.scrollToPosition(messages.size() - 1);
    }

    private void showTypingIndicator() {
        tvStatus.setText("Searching...");
        statusDot.setBackgroundResource(R.drawable.bg_status_dot_yellow);
    }

    private void hideTypingIndicator() {
        tvStatus.setText("Online • Semantic Search Ready");
        statusDot.setBackgroundResource(R.drawable.bg_status_dot);
    }

    /**
     * Save chat history to SharedPreferences
     * Saves text messages and book cards (not action buttons)
     */
    private void saveChatHistory() {
        try {
            JSONArray jsonArray = new JSONArray();
            for (ChatMessage msg : messages) {
                // Skip action buttons only
                if (msg.isActionButton)
                    continue;

                JSONObject jsonMsg = new JSONObject();
                jsonMsg.put("text", msg.text);
                jsonMsg.put("isUser", msg.isUser);
                jsonMsg.put("timestamp", msg.timestamp);
                jsonMsg.put("isBookCard", msg.isBookCard);

                // Save book card data if it's a book card
                if (msg.isBookCard) {
                    jsonMsg.put("bookId", msg.bookId);
                    jsonMsg.put("bookTitle", msg.bookTitle);
                    jsonMsg.put("bookAuthor", msg.bookAuthor);
                    jsonMsg.put("bookCategory", msg.bookCategory);
                    jsonMsg.put("bookCover", msg.bookCover);
                    jsonMsg.put("bookAvailable", msg.bookAvailable);
                }

                jsonArray.put(jsonMsg);
            }

            chatPrefs.edit()
                    .putString(KEY_MESSAGES, jsonArray.toString())
                    .putLong(KEY_TIMESTAMP, System.currentTimeMillis())
                    .apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load chat history from SharedPreferences
     * Returns true if history was loaded, false if empty or expired
     */
    private boolean loadChatHistory() {
        try {
            // Check if chat history has expired (1 day)
            long savedTimestamp = chatPrefs.getLong(KEY_TIMESTAMP, 0);
            if (System.currentTimeMillis() - savedTimestamp > ONE_DAY_MILLIS) {
                // Clear expired history
                clearChatHistory();
                return false;
            }

            String jsonString = chatPrefs.getString(KEY_MESSAGES, "[]");
            JSONArray jsonArray = new JSONArray(jsonString);

            if (jsonArray.length() == 0)
                return false;

            messages.clear();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonMsg = jsonArray.getJSONObject(i);
                ChatMessage msg = new ChatMessage(
                        jsonMsg.getString("text"),
                        jsonMsg.getBoolean("isUser"),
                        jsonMsg.getLong("timestamp"));

                // Load book card data if present
                msg.isBookCard = jsonMsg.optBoolean("isBookCard", false);
                if (msg.isBookCard) {
                    msg.bookId = jsonMsg.optInt("bookId", 0);
                    msg.bookTitle = jsonMsg.optString("bookTitle", "");
                    msg.bookAuthor = jsonMsg.optString("bookAuthor", "");
                    msg.bookCategory = jsonMsg.optString("bookCategory", "");
                    msg.bookCover = jsonMsg.optString("bookCover", "");
                    msg.bookAvailable = jsonMsg.optBoolean("bookAvailable", false);
                }

                messages.add(msg);
            }

            adapter.notifyDataSetChanged();
            recyclerChat.scrollToPosition(messages.size() - 1);
            layoutExamples.setVisibility(View.GONE);
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Clear saved chat history
     */
    private void clearChatHistory() {
        chatPrefs.edit().clear().apply();
    }

    // Chat Message Model
    static class ChatMessage {
        String text;
        boolean isUser;
        long timestamp;
        boolean isActionButton = false;
        boolean isBookCard = false;
        Runnable action;

        // Book card data
        int bookId;
        String bookTitle, bookAuthor, bookCategory, bookCover;
        boolean bookAvailable;

        ChatMessage(String text, boolean isUser, long timestamp) {
            this.text = text;
            this.isUser = isUser;
            this.timestamp = timestamp;
        }

        String getFormattedTime() {
            return new SimpleDateFormat("h:mm a", Locale.getDefault())
                    .format(new Date(timestamp));
        }
    }

    // Chat Adapter
    class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_USER = 0;
        private static final int TYPE_AI = 1;
        private static final int TYPE_ACTION = 2;
        private static final int TYPE_BOOK = 3;

        private List<ChatMessage> messages;

        ChatAdapter(List<ChatMessage> messages) {
            this.messages = messages;
        }

        @Override
        public int getItemViewType(int position) {
            ChatMessage msg = messages.get(position);
            if (msg.isBookCard)
                return TYPE_BOOK;
            if (msg.isActionButton)
                return TYPE_ACTION;
            return msg.isUser ? TYPE_USER : TYPE_AI;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            switch (viewType) {
                case TYPE_USER:
                    return new UserViewHolder(inflater.inflate(R.layout.item_chat_user, parent, false));
                case TYPE_ACTION:
                    return new ActionViewHolder(inflater.inflate(R.layout.item_chat_action, parent, false));
                case TYPE_BOOK:
                    return new BookViewHolder(inflater.inflate(R.layout.item_chat_book_card, parent, false));
                default:
                    return new AIViewHolder(inflater.inflate(R.layout.item_chat_ai, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ChatMessage msg = messages.get(position);
            if (holder instanceof UserViewHolder) {
                ((UserViewHolder) holder).bind(msg);
            } else if (holder instanceof AIViewHolder) {
                ((AIViewHolder) holder).bind(msg);
            } else if (holder instanceof ActionViewHolder) {
                ((ActionViewHolder) holder).bind(msg);
            } else if (holder instanceof BookViewHolder) {
                ((BookViewHolder) holder).bind(msg);
            }
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        class UserViewHolder extends RecyclerView.ViewHolder {
            TextView tvMessage, tvTime;

            UserViewHolder(View itemView) {
                super(itemView);
                tvMessage = itemView.findViewById(R.id.tvMessage);
                tvTime = itemView.findViewById(R.id.tvTime);
            }

            void bind(ChatMessage msg) {
                tvMessage.setText(msg.text);
                tvTime.setText(msg.getFormattedTime());
            }
        }

        class AIViewHolder extends RecyclerView.ViewHolder {
            TextView tvMessage, tvTime;

            AIViewHolder(View itemView) {
                super(itemView);
                tvMessage = itemView.findViewById(R.id.tvMessage);
                tvTime = itemView.findViewById(R.id.tvTime);
            }

            void bind(ChatMessage msg) {
                String text = msg.text.replace("**", "");
                tvMessage.setText(text);
                tvTime.setText(msg.getFormattedTime());
            }
        }

        class ActionViewHolder extends RecyclerView.ViewHolder {
            TextView tvButton;

            ActionViewHolder(View itemView) {
                super(itemView);
                tvButton = itemView.findViewById(R.id.tvActionButton);
            }

            void bind(ChatMessage msg) {
                tvButton.setText(msg.text);
                tvButton.setOnClickListener(v -> {
                    if (msg.action != null)
                        msg.action.run();
                });
            }
        }

        class BookViewHolder extends RecyclerView.ViewHolder {
            ImageView ivCover;
            TextView tvTitle, tvAuthor, tvCategory, tvAvailability, btnReserve;

            BookViewHolder(View itemView) {
                super(itemView);
                ivCover = itemView.findViewById(R.id.ivBookCover);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvAuthor = itemView.findViewById(R.id.tvAuthor);
                tvCategory = itemView.findViewById(R.id.tvCategory);
                tvAvailability = itemView.findViewById(R.id.tvAvailability);
                btnReserve = itemView.findViewById(R.id.btnReserve);
            }

            void bind(ChatMessage msg) {
                tvTitle.setText(msg.bookTitle);
                tvAuthor.setText(msg.bookAuthor);
                tvCategory.setText(msg.bookCategory);

                if (msg.bookAvailable) {
                    tvAvailability.setText("Available");
                    tvAvailability.setBackgroundResource(R.drawable.bg_available_badge);
                    tvAvailability.setTextColor(0xFF059669);
                } else {
                    tvAvailability.setText("Unavailable");
                    tvAvailability.setBackgroundResource(R.drawable.bg_unavailable_badge);
                    tvAvailability.setTextColor(0xFFDC2626);
                }

                // Load cover
                if (msg.bookCover != null && msg.bookCover.startsWith("http")) {
                    Glide.with(itemView.getContext())
                            .load(msg.bookCover)
                            .placeholder(R.drawable.ic_library_book)
                            .into(ivCover);
                }

                btnReserve.setOnClickListener(v -> {
                    Intent intent = new Intent(AIAssistantActivity.this, BookDetailsActivity.class);
                    intent.putExtra("book_id", msg.bookId);
                    intent.putExtra("book_title", msg.bookTitle);
                    startActivity(intent);
                });

                itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(AIAssistantActivity.this, BookDetailsActivity.class);
                    intent.putExtra("book_id", msg.bookId);
                    intent.putExtra("book_title", msg.bookTitle);
                    startActivity(intent);
                });
            }
        }
    }
}
