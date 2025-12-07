package com.electrical.model;

/**
 * Роли пользователей в системе
 */
public enum Role {
    USER("Пользователь"),
    ADMIN("Администратор");
    
    private final String displayName;
    
    Role(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}


