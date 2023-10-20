package net.mineshock.sellgui;

import org.h2.jdbcx.JdbcDataSource;

import java.sql.*;

public class SQLManager {

    public static Connection Connect() throws SQLException {

        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:./plugins/sellgui-fix/sellchests;DB_CLOSE_ON_EXIT=FALSE");

        return dataSource.getConnection();
    }

    public static void createTable(String tableName, String[] columns) {
        StringBuilder createTableSQL = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName + " (");

        for (String column : columns) {
            createTableSQL.append(column).append(",");
        }
        createTableSQL.deleteCharAt(createTableSQL.length() - 1);
        createTableSQL.append(")");

        try (Statement statement = SellGUIMain.dbConnection.createStatement()) {
            statement.executeUpdate(createTableSQL.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int SQLEdit(String editSQL, Object... parameters) {
        Connection connection = SellGUIMain.dbConnection;
        int lastInsertId = 0;
        try {
            PreparedStatement statement = connection.prepareStatement(editSQL);

            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    lastInsertId = generatedKeys.getInt(1);
                }
            }

            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lastInsertId;
    }

}
