package com.electrical.dao;

import com.electrical.model.Role;
import com.electrical.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO для работы с пользователями
 */
public class UserDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);
    private final DatabaseManager dbManager;
    
    public UserDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }
    
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            logger.error("Ошибка поиска пользователя по имени: " + username, e);
        }
        
        return Optional.empty();
    }
    
    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            logger.error("Ошибка поиска пользователя по ID: " + id, e);
        }
        
        return Optional.empty();
    }
    
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            logger.error("Ошибка получения списка пользователей", e);
        }
        
        return users;
    }
    
    public User save(User user) {
        String sql = "INSERT INTO users (username, password_hash, role) VALUES (?, ?, ?)";
        
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getRole().name());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getLong(1));
                    user.setCreatedAt(LocalDateTime.now());
                    logger.info("Пользователь создан: " + user.getUsername());
                }
            }
        } catch (SQLException e) {
            logger.error("Ошибка создания пользователя: " + user.getUsername(), e);
        }
        
        return user;
    }
    
    public void update(User user) {
        String sql = "UPDATE users SET username = ?, password_hash = ?, role = ? WHERE id = ?";
        
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getRole().name());
            stmt.setLong(4, user.getId());
            
            stmt.executeUpdate();
            logger.info("Пользователь обновлён: " + user.getUsername());
        } catch (SQLException e) {
            logger.error("Ошибка обновления пользователя: " + user.getUsername(), e);
        }
    }
    
    public void delete(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
            logger.info("Пользователь удалён, ID: " + id);
        } catch (SQLException e) {
            logger.error("Ошибка удаления пользователя, ID: " + id, e);
        }
    }
    
    public boolean existsByUsername(String username) {
        return findByUsername(username).isPresent();
    }
    
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(Role.valueOf(rs.getString("role")));
        
        Timestamp timestamp = rs.getTimestamp("created_at");
        if (timestamp != null) {
            user.setCreatedAt(timestamp.toLocalDateTime());
        }
        
        return user;
    }
}


