package com.example.school.auth;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "school_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_ROLE = "role";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context ctx) {
        prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    /**
     * Convenience: create session with id + role only
     */
    public void createSession(int userId, String role) {
        if (role == null) role = "";
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_ROLE, role.toLowerCase().trim());
        editor.apply();
    }

    /**
     * Preferred: create session and store username as well
     */
    public void createLoginSession(int userId, String username, String role) {
        if (username == null) username = "";
        if (role == null) role = "";
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_ROLE, role.toLowerCase().trim());
        editor.apply();
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }

    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }

    public String getUserRole() {
        String r = prefs.getString(KEY_ROLE, "");
        return r == null ? "" : r.toLowerCase().trim();
    }

    public boolean isLoggedIn() {
        return getUserId() != -1;
    }

    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(getUserRole());
    }

    public boolean isTeacher() {
        String r = getUserRole();
        return "giaovien".equalsIgnoreCase(r) || "giaovien".equals(r);
    }

    public boolean isParent() {
        String r = getUserRole();
        return "phuhuynh".equalsIgnoreCase(r) || "phuhuynh".equals(r);
    }
}
