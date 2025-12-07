package com.electrical.model;

/**
 * Типы расчётов в системе
 */
public enum CalculationType {
    OHM_LAW("Закон Ома"),
    VOLTAGE_DIVIDER("Делитель напряжения");
    
    private final String displayName;
    
    CalculationType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}


