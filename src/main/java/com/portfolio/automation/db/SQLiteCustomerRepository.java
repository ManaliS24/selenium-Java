package com.portfolio.automation.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

/** Owns one SQLite connection and the customer/order operations used by database tests. */
public final class SQLiteCustomerRepository implements AutoCloseable {
    private static final String CREATE_CUSTOMERS = """
            CREATE TABLE customers (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                email TEXT NOT NULL UNIQUE
            )
            """;
    private static final String CREATE_ORDERS = """
            CREATE TABLE orders (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                customer_id INTEGER NOT NULL,
                total_cents INTEGER NOT NULL CHECK (total_cents >= 0),
                FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
            )
            """;

    private final Connection connection;

    private SQLiteCustomerRepository(Connection connection) {
        this.connection = connection;
    }

    public static SQLiteCustomerRepository inMemory() throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        try {
            enableForeignKeys(connection);
            initializeSchema(connection);
            return new SQLiteCustomerRepository(connection);
        } catch (SQLException | RuntimeException failure) {
            try {
                connection.close();
            } catch (SQLException closeFailure) {
                failure.addSuppressed(closeFailure);
            }
            throw failure;
        }
    }

    public long createCustomer(String name, String email) throws SQLException {
        String sql = "INSERT INTO customers (name, email) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, email);
            statement.executeUpdate();
        }

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT last_insert_rowid()")) {
            if (!resultSet.next()) {
                throw new SQLException("SQLite did not return a generated customer identifier");
            }
            return resultSet.getLong(1);
        }
    }

    public Optional<Customer> findCustomer(long customerId) throws SQLException {
        String sql = "SELECT id, name, email FROM customers WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, customerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(new Customer(
                        resultSet.getLong("id"),
                        resultSet.getString("name"),
                        resultSet.getString("email")));
            }
        }
    }

    public long createOrder(long customerId, int totalCents) throws SQLException {
        String sql = "INSERT INTO orders (customer_id, total_cents) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, customerId);
            statement.setInt(2, totalCents);
            statement.executeUpdate();
        }

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT last_insert_rowid()")) {
            if (!resultSet.next()) {
                throw new SQLException("SQLite did not return a generated order identifier");
            }
            return resultSet.getLong(1);
        }
    }

    public void deleteCustomer(long customerId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM customers WHERE id = ?")) {
            statement.setLong(1, customerId);
            statement.executeUpdate();
        }
    }

    public int customerCount() throws SQLException {
        return count("SELECT COUNT(*) FROM customers");
    }

    public int orderCount() throws SQLException {
        return count("SELECT COUNT(*) FROM orders");
    }

    @Override
    public void close() throws SQLException {
        connection.close();
    }

    private int count(String sql) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            if (!resultSet.next()) {
                throw new SQLException("Count query returned no result: " + sql);
            }
            return resultSet.getInt(1);
        }
    }

    private static void enableForeignKeys(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("PRAGMA foreign_keys")) {
            if (!resultSet.next() || resultSet.getInt(1) != 1) {
                throw new SQLException("SQLite foreign-key enforcement could not be enabled");
            }
        }
    }

    private static void initializeSchema(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(CREATE_CUSTOMERS);
            statement.execute(CREATE_ORDERS);
        }
    }

    public record Customer(long id, String name, String email) {
    }
}
