package com.portfolio.automation.db;

import com.portfolio.automation.config.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Read-only JDBC access to the public ClassicModels MariaDB dataset. */
public final class ClassicModelsRepository implements AutoCloseable {
    private final Connection connection;
    private final int queryTimeoutSeconds;

    private ClassicModelsRepository(Connection connection, int queryTimeoutSeconds) {
        this.connection = connection;
        this.queryTimeoutSeconds = queryTimeoutSeconds;
    }

    public static ClassicModelsRepository connect() throws SQLException {
        String host = Configuration.get("DB_HOST");
        int port = positive("DB_PORT");
        String database = Configuration.get("DB_NAME");
        int timeoutSeconds = positive("SELENIUM_TIMEOUT");
        int timeoutMillis = Math.multiplyExact(timeoutSeconds, 1_000);
        String url = "jdbc:mariadb://%s:%d/%s?connectTimeout=%d&socketTimeout=%d".formatted(
                host, port, database, timeoutMillis, timeoutMillis);

        Connection connection;
        try {
            connection = DriverManager.getConnection(
                    url, Configuration.get("DB_USER"), Configuration.get("DB_PASSWORD"));
        } catch (SQLException exception) {
            throw new SQLException(
                    "Unable to connect to external ClassicModels database at %s:%d/%s"
                            .formatted(host, port, database),
                    exception);
        }

        try {
            connection.setReadOnly(true);
            connection.setAutoCommit(false);
            return new ClassicModelsRepository(connection, timeoutSeconds);
        } catch (SQLException | RuntimeException failure) {
            try {
                connection.close();
            } catch (SQLException closeFailure) {
                failure.addSuppressed(closeFailure);
            }
            throw failure;
        }
    }

    public Optional<Customer> findCustomer(int customerNumber) throws SQLException {
        String sql = """
                SELECT customerNumber, customerName, country
                FROM customers
                WHERE customerNumber = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setQueryTimeout(queryTimeoutSeconds);
            statement.setInt(1, customerNumber);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(toCustomer(resultSet)) : Optional.empty();
            }
        }
    }

    public List<Customer> firstCustomers(int limit) throws SQLException {
        if (limit <= 0) {
            throw new IllegalArgumentException("Customer query limit must be greater than zero");
        }
        String sql = """
                SELECT customerNumber, customerName, country
                FROM customers
                ORDER BY customerNumber
                LIMIT ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setQueryTimeout(queryTimeoutSeconds);
            statement.setInt(1, limit);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Customer> customers = new ArrayList<>();
                while (resultSet.next()) {
                    customers.add(toCustomer(resultSet));
                }
                return List.copyOf(customers);
            }
        }
    }

    @Override
    public void close() throws SQLException {
        try {
            if (!connection.isClosed()) {
                connection.rollback();
            }
        } finally {
            connection.close();
        }
    }

    private static Customer toCustomer(ResultSet resultSet) throws SQLException {
        return new Customer(
                resultSet.getInt("customerNumber"),
                resultSet.getString("customerName"),
                resultSet.getString("country"));
    }

    private static int positive(String key) {
        int value = Configuration.getInt(key);
        if (value <= 0) {
            throw new IllegalArgumentException(key + " must be greater than zero, but was: " + value);
        }
        return value;
    }

    public record Customer(int customerNumber, String customerName, String country) {
    }
}
