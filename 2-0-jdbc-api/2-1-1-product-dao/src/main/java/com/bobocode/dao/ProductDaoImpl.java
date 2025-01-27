package com.bobocode.dao;

import com.bobocode.exception.DaoOperationException;
import com.bobocode.model.Product;
import com.bobocode.util.ExerciseNotCompletedException;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProductDaoImpl implements ProductDao {
    private static final String INSERT_SQL = "INSERT INTO products(name, producer, price, expiration_date) VALUES (?, ?, ?, ?);";
    private static final String SELECT_ALL_SQL = "SELECT * FROM products;";
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM products WHERE id = ?;";
    private static final String UPDATE_BY_ID_SLQ = "UPDATE products SET name = ?, producer = ?, price = ?, expiration_date = ? WHERE id = ?;";
    private static final String REMOVE_BY_ID_SQL = "DELETE FROM products WHERE id = ?;";

    private DataSource dataSource;

    public ProductDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(Product product) {
        Objects.requireNonNull(product);
        try (var connection = dataSource.getConnection()) {
            saveProduct(product, connection);
        } catch (SQLException e) {
            throw new DaoOperationException(String.format("Error saving product: %s", product), e);
        }
    }

    private void saveProduct(Product product, Connection connection) throws SQLException {
        PreparedStatement insertStatement = prepareInsertStatement(product, connection);
        insertStatement.executeUpdate();
        Long id = fetchGeneratedId(insertStatement);
        product.setId(id);
    }


    private PreparedStatement prepareInsertStatement(Product product, Connection connection) {
        try {
            PreparedStatement insertStatement = connection.prepareStatement(INSERT_SQL, PreparedStatement.RETURN_GENERATED_KEYS);
            fillProductStatement(product, insertStatement);
            return insertStatement;
        } catch (SQLException e) {
            throw new DaoOperationException(String.format("Cannot prepare statement for product: %s", product), e);
        }
    }

    private void fillProductStatement(Product product, PreparedStatement updateStatement) throws SQLException {
        updateStatement.setString(1, product.getName());
        updateStatement.setString(2, product.getProducer());
        updateStatement.setBigDecimal(3, product.getPrice());
        updateStatement.setDate(4, Date.valueOf(product.getExpirationDate()));
    }

    private Long fetchGeneratedId(PreparedStatement insertStatement) throws SQLException {
        ResultSet resultSet = insertStatement.getGeneratedKeys();
        if (resultSet.next()) {
            return resultSet.getLong(1);
        } else {
            throw new DaoOperationException("Can not obtain product ID");
        }
    }


    @Override
    public List<Product> findAll() {
        try (Connection connection = dataSource.getConnection()) {
            return findAllProducts(connection);
        } catch (SQLException e) {
            throw new DaoOperationException("Error finding all products", e);
        }
    }

    private List<Product> findAllProducts(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(SELECT_ALL_SQL);
        return collectToList(resultSet);
    }

    private List<Product> collectToList(ResultSet resultSet) throws SQLException {
        List<Product> products = new ArrayList<>();
        while (resultSet.next()) {
            Product product = parseRow(resultSet);
            products.add(product);
        }
        return products;
    }

    private Product parseRow(ResultSet resultSet) {
        try {
            return createFromResultSet(resultSet);
        } catch (SQLException e) {
            throw new DaoOperationException("Cannot parse row to create product instance", e);
        }
    }

    private Product createFromResultSet(ResultSet resultSet) throws SQLException {
        Product product = new Product();
        product.setId(resultSet.getLong("id"));
        product.setName(resultSet.getString("name"));
        product.setProducer(resultSet.getString("producer"));
        product.setPrice(resultSet.getBigDecimal("price"));
        product.setExpirationDate(resultSet.getDate("expiration_date").toLocalDate());
        product.setCreationTime(resultSet.getTimestamp("creation_time").toLocalDateTime());
        return product;
    }

    @Override
    public Product findOne(Long id) {
        Objects.requireNonNull(id);
        try (Connection connection = dataSource.getConnection()) {
            return findProductById(id, connection);
        } catch (SQLException e) {
            throw new DaoOperationException(String.format("Error finding product by id = %d", id), e);
        }
    }

    private Product findProductById(Long id, Connection connection) throws SQLException {
        PreparedStatement selectByIdStatement = prepareSelectByIdStatement(id, connection);
        ResultSet resultSet = selectByIdStatement.executeQuery();
        if (resultSet.next()) {
            return parseRow(resultSet);
        } else {
            throw new DaoOperationException(String.format("Product with id = %d does not exist", id));
        }
    }

    private PreparedStatement prepareSelectByIdStatement(Long id, Connection connection) {
        try {
            PreparedStatement selectByIdStatement = connection.prepareStatement(SELECT_BY_ID_SQL);
            selectByIdStatement.setLong(1, id);
            return selectByIdStatement;
        } catch (SQLException e) {
            throw new DaoOperationException(String.format("Cannot prepare select by id statement for id = %d", id), e);
        }
    }

    @Override
    public void update(Product product) {
        Objects.requireNonNull(product);
        try (Connection connection = dataSource.getConnection()) {
            updateProduct(product, connection);
        } catch (SQLException e) {
            throw new DaoOperationException(String.format("Error updating product: %s", product), e);
        }
    }

    private void updateProduct(Product product, Connection connection) throws SQLException {
        checkIdIsNotNull(product);
        PreparedStatement updateStatement = prepareUpdateStatement(product, connection);
        executeUpdateById(updateStatement, product.getId());
    }

    private void executeUpdateById(PreparedStatement insertStatement, Long productId) throws SQLException {
        int rowsAffected = insertStatement.executeUpdate(); // returns number of rows that were changed
        if (rowsAffected == 0) {
            throw new DaoOperationException(String.format("Product with id = %d does not exist", productId));
        }
    }

    private PreparedStatement prepareUpdateStatement(Product product, Connection connection) {
        try {
            PreparedStatement updateStatement = connection.prepareStatement(UPDATE_BY_ID_SLQ);
            fillProductStatement(product, updateStatement);
            updateStatement.setLong(5, product.getId());
            return updateStatement;
        } catch (Exception e) {
            throw new DaoOperationException(String.format("Cannot prepare update statement for product: %s", product), e);
        }
    }

    @Override
    public void remove(Product product) {
        Objects.requireNonNull(product);
        try (Connection connection = dataSource.getConnection()) {
            removeProduct(product, connection);
        } catch (SQLException e) {
            throw new DaoOperationException(String.format("Error removing product by id = %d", product.getId()), e);
        }
    }

    private void removeProduct(Product product, Connection connection) throws SQLException {
        checkIdIsNotNull(product);
        PreparedStatement removeStatement = prepareRemoveStatement(product, connection);
        executeUpdateById(removeStatement, product.getId());
    }

    private PreparedStatement prepareRemoveStatement(Product product, Connection connection) {
        try {
            PreparedStatement removeStatement = connection.prepareStatement(REMOVE_BY_ID_SQL);
            removeStatement.setLong(1, product.getId());
            return removeStatement;
        } catch (SQLException e) {
            throw new DaoOperationException(String.format("Cannot prepare statement for product: %s", product), e);
        }
    }

    private void checkIdIsNotNull(Product product) {
        if (product.getId() == null) {
            throw new DaoOperationException("Product id cannot be null");
        }
    }

}
