package com.portfolio.automation.db;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.sql.SQLException;

public final class SQLiteDatabaseTest {
    private SQLiteCustomerRepository repository;

    @BeforeMethod(alwaysRun = true)
    public void createFreshDatabase() throws SQLException {
        repository = SQLiteCustomerRepository.inMemory();
    }

    @AfterMethod(alwaysRun = true)
    public void closeDatabase() throws SQLException {
        if (repository != null) {
            repository.close();
        }
    }

    @Test(groups = {"db", "smoke"})
    public void customerCanBeCreatedAndRetrieved() throws SQLException {
        long customerId = repository.createCustomer("Ada Lovelace", "ada@example.com");

        SQLiteCustomerRepository.Customer customer = repository.findCustomer(customerId).orElseThrow();

        Assert.assertEquals(customer,
                new SQLiteCustomerRepository.Customer(customerId, "Ada Lovelace", "ada@example.com"));
    }

    @Test(groups = {"db", "regression"})
    public void customerEmailMustBeUnique() throws SQLException {
        repository.createCustomer("First Customer", "shared@example.com");

        SQLException exception = Assert.expectThrows(SQLException.class,
                () -> repository.createCustomer("Second Customer", "shared@example.com"));

        Assert.assertTrue(exception.getMessage().contains("UNIQUE constraint failed"));
    }

    @Test(groups = {"db", "regression"})
    public void orderRequiresExistingCustomer() {
        SQLException exception = Assert.expectThrows(SQLException.class,
                () -> repository.createOrder(999, 2_500));

        Assert.assertTrue(exception.getMessage().contains("FOREIGN KEY constraint failed"));
    }

    @Test(groups = {"db", "regression"})
    public void deletingCustomerCascadesToOrders() throws SQLException {
        long customerId = repository.createCustomer("Grace Hopper", "grace@example.com");
        repository.createOrder(customerId, 4_200);
        Assert.assertEquals(repository.orderCount(), 1);

        repository.deleteCustomer(customerId);

        Assert.assertEquals(repository.customerCount(), 0);
        Assert.assertEquals(repository.orderCount(), 0);
    }

    @Test(groups = {"db", "regression"})
    public void databaseInstancesAreIsolated() throws SQLException {
        repository.createCustomer("Existing Customer", "existing@example.com");
        Assert.assertEquals(repository.customerCount(), 1);

        try (SQLiteCustomerRepository anotherDatabase = SQLiteCustomerRepository.inMemory()) {
            Assert.assertEquals(anotherDatabase.customerCount(), 0);
            Assert.assertTrue(anotherDatabase.findCustomer(1).isEmpty());
        }
    }
}
