
package com.library.util;

import java.io.Console;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class LoginSetup {

    public static void main(String[] args) {
        Console console = System.console();

        if (console == null) {
            System.err.println(
                "No interactive console available. Run this program " +
                "directly from PowerShell, not through an IDE console."
            );
            return;
        }

        String memIdInput = console.readLine(
                "Enter member ID (301 for Rahul Sharma): ");

        int memId;

        try {
            memId = Integer.parseInt(memIdInput.trim());

            if (memId <= 0) {
                System.out.println("Invalid member ID.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Member ID must be a number.");
            return;
        }

        char[] password = console.readPassword(
                "Choose a password (at least 12 characters): ");

        if (password == null || password.length < 12) {
            System.out.println(
                    "Password must contain at least 12 characters."
            );
            if (password != null) {
                Arrays.fill(password, '\0');
            }
            return;
        }

        char[] confirmation = console.readPassword(
                "Confirm password: ");

        if (confirmation == null
                || !java.security.MessageDigest.isEqual(
                        new String(password).getBytes(
                                java.nio.charset.StandardCharsets.UTF_8),
                        new String(confirmation).getBytes(
                                java.nio.charset.StandardCharsets.UTF_8))) {

            System.out.println("Passwords do not match.");
            Arrays.fill(password, '\0');

            if (confirmation != null) {
                Arrays.fill(confirmation, '\0');
            }
            return;
        }

        Arrays.fill(confirmation, '\0');

        try {
            String email = createLogin(memId, password);

            if (email != null) {
                System.out.println("Login created successfully!");
                System.out.println("Member ID: " + memId);
                System.out.println("Registered email: " + email);
                System.out.println(
                        "You can now sign in using this email and password."
                );
            }

        } catch (SQLException | GeneralSecurityException e) {
            System.err.println("Unable to create login: " + e.getMessage());
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    private static String createLogin(int memId, char[] password)
            throws SQLException, GeneralSecurityException {

        String findMemberSql = """
                SELECT MEMNAME, EMAIL
                FROM SYSTEM.MEMBERS
                WHERE MEMID = ?
                """;

        String insertSql = """
                INSERT INTO SYSTEM.LOGIN_CREDENTIALS
                    (MEMID, EMAIL, PASSWORD_HASH)
                VALUES (?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection()) {

            String email;

            try (PreparedStatement statement =
                         connection.prepareStatement(findMemberSql)) {

                statement.setInt(1, memId);

                try (ResultSet rs = statement.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException(
                                "No member exists with ID " + memId
                        );
                    }

                    String name = rs.getString("MEMNAME");
                    email = rs.getString("EMAIL");

                    if (email == null || email.isBlank()) {
                        throw new SQLException(
                                "This member has no registered email."
                        );
                    }

                    System.out.println("Member found: " + name);
                }
            }

            String hash = PasswordUtil.hashPassword(password);

            try (PreparedStatement statement =
                         connection.prepareStatement(insertSql)) {

                statement.setInt(1, memId);
                statement.setString(2, email.trim());
                statement.setString(3, hash);

                statement.executeUpdate();
            }

            return email.trim();
        }
    }
}
