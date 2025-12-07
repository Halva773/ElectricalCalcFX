package com.electrical.util;

import com.electrical.model.User;

/**
 * Менеджер сессии текущего пользователя
 */
public class SessionManager {
    
    private static User currentUser;
    
    private SessionManager() {
    }
    
    public static void setCurrentUser(User user) {
        currentUser = user;
    }
    
    public static User getCurrentUser() {
        return currentUser;
    }
    
    public static boolean isLoggedIn() {
        return currentUser != null;
    }
    
    public static boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }
    
    public static void logout() {
        currentUser = null;
    }
}


