package com.simats.digitallibrary;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

/**
 * Helper class to manage app locale/language
 */
public class LocaleHelper {

    private static final String PREF_NAME = "AppSettings";
    private static final String KEY_LANGUAGE = "app_language";

    // Language codes - MUST match spinner order exactly
    // Position 0 = English, 1 = Telugu, 2 = Hindi, 3 = Tamil
    private static final String[] LANGUAGE_CODES = { "en", "te", "hi", "ta" };

    /**
     * Get saved language code
     */
    public static String getLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANGUAGE, "en"); // Default English
    }

    /**
     * Save language preference
     */
    public static void saveLanguage(Context context, String languageCode) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply();
    }

    /**
     * Apply saved locale on app start - call this in attachBaseContext
     */
    public static Context onAttach(Context context) {
        String language = getLanguage(context);
        return updateResources(context, language);
    }

    /**
     * Update context with new locale
     */
    public static Context updateResources(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = new Configuration(resources.getConfiguration());
        config.setLocale(locale);

        return context.createConfigurationContext(config);
    }

    /**
     * Get language code from spinner position
     * Position: 0=English, 1=Telugu, 2=Hindi, 3=Tamil
     */
    public static String getLanguageCode(int position) {
        if (position >= 0 && position < LANGUAGE_CODES.length) {
            return LANGUAGE_CODES[position];
        }
        return "en"; // Default English
    }

    /**
     * Get spinner position from language code
     */
    public static int getLanguagePosition(String code) {
        if (code == null)
            return 0;
        for (int i = 0; i < LANGUAGE_CODES.length; i++) {
            if (LANGUAGE_CODES[i].equals(code)) {
                return i;
            }
        }
        return 0; // Default to English position
    }

    /**
     * Apply language and recreate activity
     */
    public static void applyLanguage(Activity activity, String languageCode) {
        saveLanguage(activity, languageCode);
        activity.recreate();
    }
}
