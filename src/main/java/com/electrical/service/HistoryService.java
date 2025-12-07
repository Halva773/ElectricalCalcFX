package com.electrical.service;

import com.electrical.dao.CalculationHistoryDAO;
import com.electrical.model.CalculationHistory;
import com.electrical.model.CalculationType;
import com.electrical.util.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Сервис для работы с историей расчётов
 */
public class HistoryService {
    
    private static final Logger logger = LoggerFactory.getLogger(HistoryService.class);
    private final CalculationHistoryDAO historyDAO;
    
    public HistoryService() {
        this.historyDAO = new CalculationHistoryDAO();
    }
    
    /**
     * Получить историю текущего пользователя
     */
    public List<CalculationHistory> getCurrentUserHistory() {
        if (!SessionManager.isLoggedIn()) {
            return List.of();
        }
        return historyDAO.findByUserId(SessionManager.getCurrentUser().getId());
    }
    
    /**
     * Получить историю всех пользователей (только для администратора)
     */
    public List<CalculationHistory> getAllHistory() {
        if (!SessionManager.isAdmin()) {
            logger.warn("Попытка получить всю историю от не-администратора");
            return getCurrentUserHistory();
        }
        return historyDAO.findAll();
    }
    
    /**
     * Получить историю по типу расчёта
     */
    public List<CalculationHistory> getHistoryByType(CalculationType type, boolean allUsers) {
        if (allUsers && SessionManager.isAdmin()) {
            return historyDAO.findByType(type);
        } else if (SessionManager.isLoggedIn()) {
            return historyDAO.findByUserIdAndType(SessionManager.getCurrentUser().getId(), type);
        }
        return List.of();
    }
    
    /**
     * Удалить запись из истории
     */
    public void deleteHistoryEntry(Long id) {
        historyDAO.delete(id);
        logger.info("Запись истории удалена, ID: " + id);
    }
    
    /**
     * Очистить историю текущего пользователя
     */
    public void clearCurrentUserHistory() {
        if (SessionManager.isLoggedIn()) {
            historyDAO.deleteByUserId(SessionManager.getCurrentUser().getId());
            logger.info("История пользователя очищена: " + SessionManager.getCurrentUser().getUsername());
        }
    }
}


