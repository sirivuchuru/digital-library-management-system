package com.library.servlet;

import com.library.dao.ReturnDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/return")
public class ReturnServlet extends HttpServlet {

    private final ReturnDAO returnDAO = new ReturnDAO();

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String contextPath = request.getContextPath();

        // Only logged-in members can return books.
        HttpSession session = request.getSession(false);

        if (session == null ||
            !(session.getAttribute("memId") instanceof Integer)) {
            response.sendRedirect(contextPath + "/login");
            return;
        }

        int memId = (Integer) session.getAttribute("memId");

        // Validate the submitted book ID.
        int bookId;

        try {
            bookId = Integer.parseInt(request.getParameter("bookId"));

            if (bookId <= 0) {
                response.sendRedirect(
                    contextPath + "/dashboard?returnStatus=invalid"
                );
                return;
            }

        } catch (NumberFormatException | NullPointerException e) {
            response.sendRedirect(
                contextPath + "/dashboard?returnStatus=invalid"
            );
            return;
        }

        // Process the return.
        try {
            String result = returnDAO.returnBook(memId, bookId);

            String status;

            switch (result) {
                case "SUCCESS":
                    status = "success";
                    break;

                case "BOOK_NOT_FOUND":
                    status = "notfound";
                    break;

                case "NOT_BORROWED":
                    status = "notborrowed";
                    break;

                default:
                    status = "error";
                    break;
            }

            response.sendRedirect(
                contextPath + "/dashboard?returnStatus=" + status
            );

        } catch (SQLException e) {
            getServletContext().log("Failed to return book.", e);

            response.sendRedirect(
                contextPath + "/dashboard?returnStatus=error"
            );
        }
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.sendError(
            HttpServletResponse.SC_METHOD_NOT_ALLOWED,
            "Please use the Return Book button."
        );
    }
}
