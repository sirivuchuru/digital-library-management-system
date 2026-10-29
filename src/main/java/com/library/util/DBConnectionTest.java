
package com.library.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DBConnectionTest {

    public static void main(String[] args) {

        try (Connection connection = DBConnection.getConnection()) {

            System.out.println("JDBC CONNECTION SUCCESSFUL!");

            System.out.println(
                    "Database: " +
                    connection.getMetaData().getDatabaseProductName()
            );

            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(
                         "SELECT COUNT(*) FROM SYSTEM.BOOKS")) {

                if (resultSet.next()) {
                    System.out.println(
                            "Total books in database: " +
                            resultSet.getInt(1)
                    );
                }
            }

            System.out.println("Database test completed successfully.");

        } catch (Exception e) {
            System.out.println("JDBC CONNECTION FAILED!");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
