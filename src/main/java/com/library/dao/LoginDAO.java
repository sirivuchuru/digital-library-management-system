
package com.library.dao;

import com.library.util.DBConnection;
import com.library.util.PasswordUtil;

import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginDAO {

    public boolean registerMember(
            int memId, String email, char[] password)
            throws SQLException, GeneralSecurityException {

        String sql = """
                INSERT INTO SYSTEM.LOGIN_CREDENTIALS
                    (MEMID, EMAIL, PASSWORD_HASH)
                SELECT MEMID, ?, ?
                FROM SYSTEM.MEMBERS
                WHERE MEMID = ?
                """;

        String passwordHash = PasswordUtil.hashPassword(password);

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, email.trim());
            statement.setString(2, passwordHash);
            statement.setInt(3, memId);

            return statement.executeUpdate() == 1;
        }
    }

    public Integer authenticate(String email, char[] password)
            throws SQLException, GeneralSecurityException {

        String sql = """
                SELECT MEMID, PASSWORD_HASH
                FROM SYSTEM.LOGIN_CREDENTIALS
                WHERE LOWER(EMAIL) = LOWER(?)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, email.trim());

            try (ResultSet result = statement.executeQuery()) {

                if (!result.next()) {
                    return null;
                }

                int memId = result.getInt("MEMID");
                String storedHash =
                        result.getString("PASSWORD_HASH");

                if (PasswordUtil.verifyPassword(
                        password, storedHash)) {
                    return memId;
                }

                return null;
            }
        }
    }
}
