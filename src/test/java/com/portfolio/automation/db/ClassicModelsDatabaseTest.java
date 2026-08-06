package com.portfolio.automation.db;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;

public final class ClassicModelsDatabaseTest {
    private static final String EXPECTED_CUSTOMERS = "data/expected_customers.csv";
    private ClassicModelsRepository repository;

    @BeforeClass(alwaysRun = true)
    public void connectToClassicModels() throws SQLException {
        repository = ClassicModelsRepository.connect();
    }

    @AfterClass(alwaysRun = true)
    public void closeClassicModels() throws SQLException {
        if (repository != null) {
            repository.close();
        }
    }

    @Test(groups = {"external", "smoke"})
    public void customer103IsAtelierGraphique() throws SQLException {
        ClassicModelsRepository.Customer customer = repository.findCustomer(103).orElseThrow();

        Assert.assertEquals(customer.customerNumber(), 103);
        Assert.assertEquals(customer.customerName(), "Atelier graphique");
    }

    @Test(groups = {"external", "regression"})
    public void firstTenCustomersMatchCsvBaseline() throws SQLException, IOException {
        List<ClassicModelsRepository.Customer> expected = loadExpectedCustomers();

        List<ClassicModelsRepository.Customer> actual = repository.firstCustomers(10);

        Assert.assertEquals(expected.size(), 10, "CSV baseline must contain exactly ten customers");
        Assert.assertEquals(actual, expected);
    }

    private List<ClassicModelsRepository.Customer> loadExpectedCustomers() throws IOException {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(EXPECTED_CUSTOMERS)) {
            if (stream == null) {
                throw new IllegalStateException("CSV resource not found: " + EXPECTED_CUSTOMERS);
            }
            CsvMapper mapper = new CsvMapper();
            CsvSchema schema = CsvSchema.emptySchema().withHeader();
            return mapper.readerFor(ClassicModelsRepository.Customer.class)
                    .with(schema)
                    .<ClassicModelsRepository.Customer>readValues(stream)
                    .readAll();
        }
    }
}
