package edu.unisabana.proyecto.infrastructure.persistence;

import edu.unisabana.proyecto.application.port.out.RegistryRepositoryPort;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador de persistencia basado en JDBC. En las pruebas de integracion se
 * configura con una base de datos H2 en memoria.
 */
public class RegistryRepository implements RegistryRepositoryPort {

    private final String jdbcUrl;
    private final String username;
    private final String password;

    public RegistryRepository(String jdbcUrl) {
        this(jdbcUrl, "", "");
    }

    public RegistryRepository(String jdbcUrl, String username, String password) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, username, password);
    }

    @Override
    public void initSchema() throws Exception {
        final String ddl = "CREATE TABLE IF NOT EXISTS registry(" +
                " id INT PRIMARY KEY," +
                " name VARCHAR(100) NOT NULL," +
                " age INT NOT NULL," +
                " is_alive BOOLEAN NOT NULL" +
                ");";
        try (Connection con = getConnection(); Statement st = con.createStatement()) {
            st.execute(ddl);
        }
    }

    @Override
    public boolean existsById(int id) throws Exception {
        final String sql = "SELECT 1 FROM registry WHERE id = ?";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public void save(int id, String name, int age, boolean isAlive) throws Exception {
        final String sql = "INSERT INTO registry(id, name, age, is_alive) VALUES(?, ?, ?, ?)";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, name);
            ps.setInt(3, age);
            ps.setBoolean(4, isAlive);
            ps.executeUpdate();
        }
    }

    @Override
    public Optional<RegistryRecord> findById(int id) throws Exception {
        final String sql = "SELECT id, name, age, is_alive FROM registry WHERE id = ?";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new RegistryRecord(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        rs.getBoolean("is_alive")));
            }
        }
    }

    @Override
    public List<RegistryRecord> findAll() throws Exception {
        final String sql = "SELECT id, name, age, is_alive FROM registry ORDER BY id";
        List<RegistryRecord> out = new ArrayList<>();
        try (Connection con = getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                out.add(new RegistryRecord(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        rs.getBoolean("is_alive")));
            }
        }
        return out;
    }

    @Override
    public boolean deleteById(int id) throws Exception {
        final String sql = "DELETE FROM registry WHERE id = ?";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public void deleteAll() throws Exception {
        try (Connection con = getConnection(); Statement st = con.createStatement()) {
            st.executeUpdate("DELETE FROM registry");
        }
    }
}
