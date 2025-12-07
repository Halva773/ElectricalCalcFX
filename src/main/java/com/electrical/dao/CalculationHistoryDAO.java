package com.electrical.dao;

import com.electrical.model.CalculationHistory;
import com.electrical.model.CalculationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO для работы с историей расчётов
 */
public class CalculationHistoryDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(CalculationHistoryDAO.class);
    private final DatabaseManager dbManager;
    
    public CalculationHistoryDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }
    
    public CalculationHistory save(CalculationHistory history) {
        String sql = "INSERT INTO calculation_history (user_id, calculation_type, input_parameters, result) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, history.getUserId());
            stmt.setString(2, history.getCalculationType().name());
            stmt.setString(3, history.getInputParameters());
            stmt.setString(4, history.getResult());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    history.setId(generatedKeys.getLong(1));
                    history.setCreatedAt(LocalDateTime.now());
                    logger.info("Запись истории создана, ID: " + history.getId());
                }
            }
        } catch (SQLException e) {
            logger.error("Ошибка сохранения записи истории", e);
        }
        
        return history;
    }
    
    public List<CalculationHistory> findByUserId(Long userId) {
        List<CalculationHistory> history = new ArrayList<>();
        String sql = """
            SELECT h.*, u.username 
            FROM calculation_history h 
            JOIN users u ON h.user_id = u.id 
            WHERE h.user_id = ? 
            ORDER BY h.created_at DESC
        """;
        
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                history.add(mapResultSetToHistory(rs));
            }
        } catch (SQLException e) {
            logger.error("Ошибка получения истории пользователя, ID: " + userId, e);
        }
        
        return history;
    }
    
    public List<CalculationHistory> findAll() {
        List<CalculationHistory> history = new ArrayList<>();
        String sql = """
            SELECT h.*, u.username 
            FROM calculation_history h 
            JOIN users u ON h.user_id = u.id 
            ORDER BY h.created_at DESC
        """;
        
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                history.add(mapResultSetToHistory(rs));
            }
        } catch (SQLException e) {
            logger.error("Ошибка получения всей истории", e);
        }
        
        return history;
    }
    
    public List<CalculationHistory> findByUserIdAndType(Long userId, CalculationType type) {
        List<CalculationHistory> history = new ArrayList<>();
        String sql = """
            SELECT h.*, u.username 
            FROM calculation_history h 
            JOIN users u ON h.user_id = u.id 
            WHERE h.user_id = ? AND h.calculation_type = ?
            ORDER BY h.created_at DESC
        """;
        
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setString(2, type.name());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                history.add(mapResultSetToHistory(rs));
            }
        } catch (SQLException e) {
            logger.error("Ошибка получения истории по типу", e);
        }
        
        return history;
    }
    
    public List<CalculationHistory> findByType(CalculationType type) {
        List<CalculationHistory> history = new ArrayList<>();
        String sql = """
            SELECT h.*, u.username 
            FROM calculation_history h 
            JOIN users u ON h.user_id = u.id 
            WHERE h.calculation_type = ?
            ORDER BY h.created_at DESC
        """;
        
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, type.name());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                history.add(mapResultSetToHistory(rs));
            }
        } catch (SQLException e) {
            logger.error("Ошибка получения истории по типу", e);
        }
        
        return history;
    }
    
    public void delete(Long id) {
        String sql = "DELETE FROM calculation_history WHERE id = ?";
        
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
            logger.info("Запись истории удалена, ID: " + id);
        } catch (SQLException e) {
            logger.error("Ошибка удаления записи истории, ID: " + id, e);
        }
    }
    
    public void deleteByUserId(Long userId) {
        String sql = "DELETE FROM calculation_history WHERE user_id = ?";
        
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.executeUpdate();
            logger.info("История пользователя удалена, user_id: " + userId);
        } catch (SQLException e) {
            logger.error("Ошибка удаления истории пользователя, user_id: " + userId, e);
        }
    }
    
    private CalculationHistory mapResultSetToHistory(ResultSet rs) throws SQLException {
        CalculationHistory history = new CalculationHistory();
        history.setId(rs.getLong("id"));
        history.setUserId(rs.getLong("user_id"));
        history.setUsername(rs.getString("username"));
        history.setCalculationType(CalculationType.valueOf(rs.getString("calculation_type")));
        history.setInputParameters(rs.getString("input_parameters"));
        history.setResult(rs.getString("result"));
        
        Timestamp timestamp = rs.getTimestamp("created_at");
        if (timestamp != null) {
            history.setCreatedAt(timestamp.toLocalDateTime());
        }
        
        return history;
    }
}


