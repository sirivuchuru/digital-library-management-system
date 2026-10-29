
package com.library.dao;

import com.library.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {

    public static class Book {
        private final int bookId;
        private final String bookName;
        private final String genre;
        private final int year;
        private final String authorName;
        private final int copiesAvailable;

        // Physical shelf location
        private final String blockName;
        private final Integer floorNumber;
        private final Integer rackNumber;
        private final Integer shelfNumber;

        public Book(
                int bookId,
                String bookName,
                String genre,
                int year,
                String authorName,
                int copiesAvailable,
                String blockName,
                Integer floorNumber,
                Integer rackNumber,
                Integer shelfNumber) {

            this.bookId = bookId;
            this.bookName = bookName;
            this.genre = genre;
            this.year = year;
            this.authorName = authorName;
            this.copiesAvailable = copiesAvailable;
            this.blockName = blockName;
            this.floorNumber = floorNumber;
            this.rackNumber = rackNumber;
            this.shelfNumber = shelfNumber;
        }

        public int getBookId() {
            return bookId;
        }

        public String getBookName() {
            return bookName;
        }

        public String getGenre() {
            return genre;
        }

        public int getYear() {
            return year;
        }

        public String getAuthorName() {
            return authorName;
        }

        public int getCopiesAvailable() {
            return copiesAvailable;
        }

        public String getBlockName() {
            return blockName;
        }

        public Integer getFloorNumber() {
            return floorNumber;
        }

        public Integer getRackNumber() {
            return rackNumber;
        }

        public Integer getShelfNumber() {
            return shelfNumber;
        }

        public String getPhysicalLocation() {
            if (blockName == null || blockName.trim().isEmpty()
                    || floorNumber == null
                    || rackNumber == null
                    || shelfNumber == null) {
                return "Location not assigned";
            }

            return "Block " + blockName
                    + " \u2192 Floor " + floorNumber
                    + " \u2192 Rack " + rackNumber
                    + " \u2192 Shelf " + shelfNumber;
        }
    }

    // Retrieve all books and their physical locations.
    public List<Book> getAllBooks() throws SQLException {
        List<Book> books = new ArrayList<>();

        String sql =
            "SELECT b.BOOKID, b.BOOKNAME, b.GENRE, b.YEAR, " +
            "a.AUTHORNAME, b.COPIESAVAILABLE, " +
            "b.BLOCK_NAME, b.FLOOR_NUMBER, " +
            "b.RACK_NUMBER, b.SHELF_NUMBER " +
            "FROM SYSTEM.BOOKS b " +
            "LEFT JOIN SYSTEM.AUTHORS a " +
            "ON b.AUTHORID = a.AUTHORID " +
            "ORDER BY b.BOOKNAME";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int yearValue = resultSet.getInt("YEAR");
                Integer year = resultSet.wasNull()
                        ? null : yearValue;

                int copiesValue =
                        resultSet.getInt("COPIESAVAILABLE");
                Integer copies = resultSet.wasNull()
                        ? null : copiesValue;

                int floorValue =
                        resultSet.getInt("FLOOR_NUMBER");
                Integer floor = resultSet.wasNull()
                        ? null : floorValue;

                int rackValue =
                        resultSet.getInt("RACK_NUMBER");
                Integer rack = resultSet.wasNull()
                        ? null : rackValue;

                int shelfValue =
                        resultSet.getInt("SHELF_NUMBER");
                Integer shelf = resultSet.wasNull()
                        ? null : shelfValue;

                String authorName =
                        resultSet.getString("AUTHORNAME");

                books.add(new Book(
                    resultSet.getInt("BOOKID"),
                    resultSet.getString("BOOKNAME"),
                    resultSet.getString("GENRE"),
                    year == null ? 0 : year,
                    authorName == null
                            ? "Unknown author" : authorName,
                    copies == null ? 0 : copies,
                    resultSet.getString("BLOCK_NAME"),
                    floor,
                    rack,
                    shelf
                ));
            }
        }

        return books;
    }

    /**
     * Borrows a book for a member.
     *
     * @return a message describing the outcome
     */
    public String borrowBook(int memId, int bookId)
            throws SQLException {

        if (memId <= 0 || bookId <= 0) {
            return "Invalid member or book ID.";
        }

        try (Connection connection = DBConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {
                // 1. Lock the selected book row.
                int copiesAvailable;

                String lockSql =
                    "SELECT COPIESAVAILABLE " +
                    "FROM SYSTEM.BOOKS " +
                    "WHERE BOOKID = ? " +
                    "FOR UPDATE";

                try (PreparedStatement lockStatement =
                         connection.prepareStatement(lockSql)) {

                    lockStatement.setInt(1, bookId);

                    try (ResultSet resultSet =
                             lockStatement.executeQuery()) {

                        if (!resultSet.next()) {
                            connection.rollback();
                            return "Book not found.";
                        }

                        copiesAvailable =
                                resultSet.getInt("COPIESAVAILABLE");

                        if (resultSet.wasNull()
                                || copiesAvailable <= 0) {
                            connection.rollback();
                            return "This book is currently unavailable.";
                        }
                    }
                }

                // 2. Confirm that the member exists.
                String memberSql =
                    "SELECT MEMID FROM SYSTEM.MEMBERS " +
                    "WHERE MEMID = ?";

                try (PreparedStatement memberStatement =
                         connection.prepareStatement(memberSql)) {

                    memberStatement.setInt(1, memId);

                    try (ResultSet resultSet =
                             memberStatement.executeQuery()) {

                        if (!resultSet.next()) {
                            connection.rollback();
                            return "Member account not found.";
                        }
                    }
                }

                // 3. Prevent a duplicate active loan.
                String duplicateSql =
                    "SELECT COUNT(*) FROM SYSTEM.BORROW " +
                    "WHERE MEMID = ? AND BOOKID = ? " +
                    "AND RETURNDATE IS NULL";

                try (PreparedStatement duplicateStatement =
                         connection.prepareStatement(duplicateSql)) {

                    duplicateStatement.setInt(1, memId);
                    duplicateStatement.setInt(2, bookId);

                    try (ResultSet resultSet =
                             duplicateStatement.executeQuery()) {

                        if (resultSet.next()
                                && resultSet.getInt(1) > 0) {
                            connection.rollback();
                            return "You have already borrowed this book and not returned it.";
                        }
                    }
                }

                // 4. Decrease available copies.
                String updateSql =
                    "UPDATE SYSTEM.BOOKS " +
                    "SET COPIESAVAILABLE = COPIESAVAILABLE - 1 " +
                    "WHERE BOOKID = ? AND COPIESAVAILABLE > 0";

                try (PreparedStatement updateStatement =
                         connection.prepareStatement(updateSql)) {

                    updateStatement.setInt(1, bookId);

                    if (updateStatement.executeUpdate() != 1) {
                        connection.rollback();
                        return "This book is currently unavailable.";
                    }
                }

                // 5. Insert the borrowing record.
                String insertSql =
                    "INSERT INTO SYSTEM.BORROW " +
                    "(BORROWID, MEMID, BOOKID, ISSUEDATE, RETURNDATE) " +
                    "VALUES (SYSTEM.BORROW_SEQ.NEXTVAL, ?, ?, SYSDATE, NULL)";

                try (PreparedStatement insertStatement =
                         connection.prepareStatement(insertSql)) {

                    insertStatement.setInt(1, memId);
                    insertStatement.setInt(2, bookId);

                    if (insertStatement.executeUpdate() != 1) {
                        throw new SQLException(
                            "The borrowing record could not be created."
                        );
                    }
                }

                // 6. Commit the transaction.
                connection.commit();

                return "Book borrowed successfully!";

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
