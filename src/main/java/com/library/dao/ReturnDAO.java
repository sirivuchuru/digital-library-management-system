package com.library.dao;

import com.library.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ReturnDAO {

    /**
     * Returns a book currently borrowed by a member.
     *
     * Possible results:
     * SUCCESS
     * BOOK_NOT_FOUND
     * NOT_BORROWED
     */
    public String returnBook(int memId, int bookId)
            throws SQLException {

        if (memId <= 0 || bookId <= 0) {
            return "NOT_BORROWED";
        }

        try (Connection connection = DBConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {
                // 1. Lock the book row first.
                // This uses the same lock order as BookDAO.borrowBook(),
                // helping prevent deadlocks between borrow and return.
                String lockBookSql =
                    "SELECT BOOKID " +
                    "FROM SYSTEM.BOOKS " +
                    "WHERE BOOKID = ? " +
                    "FOR UPDATE";

                try (PreparedStatement statement =
                         connection.prepareStatement(lockBookSql)) {

                    statement.setInt(1, bookId);

                    try (ResultSet rs = statement.executeQuery()) {
                        if (!rs.next()) {
                            connection.rollback();
                            return "BOOK_NOT_FOUND";
                        }
                    }
                }

                // 2. Find and lock this member's active borrowing record.
                String findBorrowSql =
                    "SELECT BORROWID " +
                    "FROM SYSTEM.BORROW " +
                    "WHERE MEMID = ? " +
                    "AND BOOKID = ? " +
                    "AND RETURNDATE IS NULL " +
                    "FOR UPDATE";

                int borrowId;

                try (PreparedStatement statement =
                         connection.prepareStatement(findBorrowSql)) {

                    statement.setInt(1, memId);
                    statement.setInt(2, bookId);

                    try (ResultSet rs = statement.executeQuery()) {
                        if (!rs.next()) {
                            connection.rollback();
                            return "NOT_BORROWED";
                        }

                        borrowId = rs.getInt("BORROWID");
                    }
                }

                // 3. Mark the borrowing record as returned.
                String updateBorrowSql =
                    "UPDATE SYSTEM.BORROW " +
                    "SET RETURNDATE = SYSDATE " +
                    "WHERE BORROWID = ? " +
                    "AND MEMID = ? " +
                    "AND BOOKID = ? " +
                    "AND RETURNDATE IS NULL";

                try (PreparedStatement statement =
                         connection.prepareStatement(updateBorrowSql)) {

                    statement.setInt(1, borrowId);
                    statement.setInt(2, memId);
                    statement.setInt(3, bookId);

                    if (statement.executeUpdate() != 1) {
                        connection.rollback();
                        return "NOT_BORROWED";
                    }
                }

                // 4. Increase the available-copy count.
                String updateBookSql =
                    "UPDATE SYSTEM.BOOKS " +
                    "SET COPIESAVAILABLE = NVL(COPIESAVAILABLE, 0) + 1 " +
                    "WHERE BOOKID = ?";

                try (PreparedStatement statement =
                         connection.prepareStatement(updateBookSql)) {

                    statement.setInt(1, bookId);

                    if (statement.executeUpdate() != 1) {
                        throw new SQLException(
                            "Could not update the available-copy count."
                        );
                    }
                }

                // 5. Commit both changes together.
                connection.commit();

                return "SUCCESS";

            } catch (SQLException | RuntimeException e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    e.addSuppressed(rollbackException);
                }

                throw e;

            } finally {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException ignored) {
                    // Closing the connection releases its resources.
                }
            }
        }
    }
}
