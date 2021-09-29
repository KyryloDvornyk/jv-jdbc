package mate.jdbc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import mate.jdbc.exceptions.DataProcessingException;
import mate.jdbc.models.Manufacturer;
import mate.jdbc.util.ConnectionUtil;
import mate.jdbc.util.Dao;

@Dao
public class ManufacturerDao implements TaxiDao<Manufacturer> {
    private static final ConnectionUtil connectionUtil = new ConnectionUtil();

    @Override
    public Manufacturer create(Manufacturer manufacturer) {
        String insertManufacturerRequest = "INSERT INTO manufacturers (name, country) "
                + "VALUES (?, ?);";
        try (Connection connection = connectionUtil.getConnection();
                PreparedStatement createManufacturerStatement =
                        connection.prepareStatement(insertManufacturerRequest,
                             Statement.RETURN_GENERATED_KEYS)) {
            createManufacturerStatement.setString(1, manufacturer.getName());
            createManufacturerStatement.setString(2, manufacturer.getCountry());
            createManufacturerStatement.executeUpdate();
            ResultSet generatedKeys = createManufacturerStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                Long id = generatedKeys.getObject(1, Long.class);
                manufacturer.setId(id);
            }
        } catch (SQLException e) {
            throw new DataProcessingException("Can't insert manufacturer " + manufacturer, e);
        }
        return manufacturer;
    }

    @Override
    public Optional<Manufacturer> get(Long id) {
        String getManufacturerRequest = "SELECT * "
                + "FROM manufacturers "
                + "WHERE id = ? AND is_deleted = FALSE";
        try (Connection connection = connectionUtil.getConnection();
                PreparedStatement getByIdStatement =
                        connection.prepareStatement(getManufacturerRequest)) {
            getByIdStatement.setLong(1, id);
            ResultSet generatedManufacturer = getByIdStatement.executeQuery();
            if (generatedManufacturer.next()) {
                Manufacturer manufacturer = createManufacturer(generatedManufacturer);
                return Optional.of(manufacturer);
            }
        } catch (SQLException e) {
            throw new DataProcessingException("Can't get manufacturer by id " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Manufacturer> getAll() {
        String getManufacturersRequest = "SELECT * "
                + "FROM manufacturers "
                + "WHERE is_deleted = FALSE";
        List<Manufacturer> manufacturers = new ArrayList<>();
        try (Connection connection = connectionUtil.getConnection();
                 PreparedStatement getStatement =
                        connection.prepareStatement(getManufacturersRequest)) {
            ResultSet generatedManufacturers = getStatement.executeQuery();
            while (generatedManufacturers.next()) {
                Manufacturer manufacturer = createManufacturer(generatedManufacturers);
                manufacturers.add(manufacturer);
            }
        } catch (SQLException e) {
            throw new DataProcessingException("Can't get all manufacturers", e);
        }
        return manufacturers;
    }

    @Override
    public Manufacturer update(Manufacturer manufacturer) {
        String setManufacturerRequest = "UPDATE manufacturers "
                + "SET name = ?, country = ? "
                + "WHERE id = ? AND is_deleted = FALSE ";
        try (Connection connection = connectionUtil.getConnection();
                 PreparedStatement setByIdStatement =
                        connection.prepareStatement(setManufacturerRequest)) {
            setByIdStatement.setString(1, manufacturer.getName());
            setByIdStatement.setString(2, manufacturer.getCountry());
            setByIdStatement.setLong(3, manufacturer.getId());
            setByIdStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataProcessingException("Can't update manufacturer" + manufacturer, e);
        }
        return manufacturer;
    }

    @Override
    public boolean delete(Long id) {
        String deleteByIdRequest = "UPDATE manufacturers "
                + "SET is_deleted = TRUE "
                + "WHERE id = ?";
        try (Connection connection = connectionUtil.getConnection();
                PreparedStatement deleteByIdStatement =
                        connection.prepareStatement(deleteByIdRequest)) {
            deleteByIdStatement.setLong(1, id);
            int updatedRows = deleteByIdStatement.executeUpdate();
            return updatedRows > 0;
        } catch (SQLException e) {
            throw new DataProcessingException("Can't delete manufacturer by id " + id, e);
        }
    }

    private Manufacturer createManufacturer(ResultSet resultSet) {
        Manufacturer manufacturer = new Manufacturer();
        try {
            manufacturer.setId(resultSet.getObject("id", Long.class));
            manufacturer.setName(resultSet.getString("name"));
            manufacturer.setCountry(resultSet.getString("country"));
        } catch (SQLException e) {
            throw new DataProcessingException("Can't get data by result set", e);
        }
        return manufacturer;
    }
}