
package com.library.servlet;

import com.library.dao.BookDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/borrow")
public class BorrowServlet extends HttpServlet {

    private final BookDAO bookDAO = new BookDAO();

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String contextPath = request.getContextPath();

        // 1. Verify that the member is logged in.
        HttpSession session = request.getSession(false);

        if (session == null || !(session.getAttribute("memId") instanceof Integer)) {
            response.sendRedirect(contextPath + "/login");
            return;
        }

        int memId = (Integer) session.getAttribute("memId");

        // 2. Read and validate the selected book ID.
        int bookId;

        try {
            bookId = Integer.parseInt(request.getParameter("bookId"));

            if (bookId <= 0) {
                response.sendRedirect(contextPath + "/books?status=invalid");
                return;
            }
        } catch (NumberFormatException | NullPointerException e) {
            response.sendRedirect(contextPath + "/books?status=invalid");
            return;
        }

        // 3. Perform the borrowing operation.
        try {
            String result = bookDAO.borrowBook(memId, bookId);
            String status;

            if ("Book borrowed successfully!".equals(result)) {
                status = "success";
            } else if ("Book not found.".equals(result)) {
                status = "notfound";
            } else if ("Member account not found.".equals(result)) {
                status = "member";
            } else if (result.startsWith("You have already borrowed")) {
                status = "already";
            } else if (result.startsWith("This book is currently unavailable")) {
                status = "unavailable";
            } else {
                status = "error";
            }

            response.sendRedirect(contextPath + "/books?status=" + status);

        } catch (SQLException e) {
            // Keep database details out of the browser response.
            getServletContext().log("Failed to borrow book.", e);

            response.sendRedirect(contextPath + "/books?status=error");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.sendError(
            HttpServletResponse.SC_METHOD_NOT_ALLOWED,
            "Please use the Borrow Book button."
        );
    }
}
