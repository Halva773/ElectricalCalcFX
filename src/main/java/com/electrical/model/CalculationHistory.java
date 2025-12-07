package com.electrical.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Модель записи истории расчётов
 */
public class CalculationHistory {
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    
    private Long id;
    private Long userId;
    private String username;
    private CalculationType calculationType;
    private String inputParameters;
    private String result;
    private LocalDateTime createdAt;
    
    public CalculationHistory() {
    }
    
    public CalculationHistory(Long userId, CalculationType calculationType, String inputParameters, String result) {
        this.userId = userId;
        this.calculationType = calculationType;
        this.inputParameters = inputParameters;
        this.result = result;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public CalculationType getCalculationType() {
        return calculationType;
    }
    
    public void setCalculationType(CalculationType calculationType) {
        this.calculationType = calculationType;
    }
    
    public String getInputParameters() {
        return inputParameters;
    }
    
    public void setInputParameters(String inputParameters) {
        this.inputParameters = inputParameters;
    }
    
    public String getResult() {
        return result;
    }
    
    public void setResult(String result) {
        this.result = result;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getFormattedDate() {
        return createdAt != null ? createdAt.format(FORMATTER) : "";
    }
    
    public String getCalculationTypeDisplay() {
        return calculationType != null ? calculationType.getDisplayName() : "";
    }
    
    @Override
    public String toString() {
        return "CalculationHistory{" +
                "id=" + id +
                ", userId=" + userId +
                ", calculationType=" + calculationType +
                ", inputParameters='" + inputParameters + '\'' +
                ", result='" + result + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}


