
package com.library.dao;

import com.library.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MemberDAO {

    public static class Member {
        private final int memId;
        private final String name;
        private final String email;

        public Member(int memId, String name, String email) {
            this.memId = memId;
            this.name = name;
            this.email = email;
        }

        public int getMemId() {
            return memId;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }
    }

    public static class BorrowedBook {
        private final int bookId;
        private final String bookName;
        private final String issueDate;
        private final String authorName;

        public BorrowedBook(
                int bookId,
                String bookName,
                String issueDate,
                String authorName) {
            this.bookId = bookId;
            this.bookName = bookName;
            this.issueDate = issueDate;
            this.authorName = authorName;
        }

        public int getBookId() {
            return bookId;
        }

        public String getBookName() {
            return bookName;
        }

        public String getIssueDate() {
            return issueDate;
        }

        public String getAuthorName() {
            return authorName;
        }
    }

    public Member getMember(int memId) throws SQLException {
        String sql = """
                SELECT MEMID, MEMNAME, EMAIL
                FROM SYSTEM.MEMBERS
                WHERE MEMID = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, memId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return new Member(
                            rs.getInt("MEMID"),
                            rs.getString("MEMNAME"),
                            rs.getString("EMAIL")
                    );
                }
            }
        }

        return null;
    }

    public List<BorrowedBook> getBorrowedBooks(int memId)
            throws SQLException {

        List<BorrowedBook> books = new ArrayList<>();

        String sql = """
                SELECT B.BOOKID,
                       B.BOOKNAME,
                       A.AUTHORNAME,
                       TO_CHAR(BR.ISSUEDATE, 'DD Mon YYYY') AS ISSUE_DATE
                FROM SYSTEM.BORROW BR
                JOIN SYSTEM.BOOKS B
                    ON BR.BOOKID = B.BOOKID
                LEFT JOIN SYSTEM.AUTHORS A
                    ON B.AUTHORID = A.AUTHORID
                WHERE BR.MEMID = ?
                  AND BR.RETURNDATE IS NULL
                ORDER BY BR.ISSUEDATE DESC
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, memId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    books.add(new BorrowedBook(
                            rs.getInt("BOOKID"),
                            rs.getString("BOOKNAME"),
                            rs.getString("ISSUE_DATE"),
                            rs.getString("AUTHORNAME")
                    ));
                }
            }
        }

        return books;
    }
}
