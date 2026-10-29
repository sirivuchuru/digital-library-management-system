
package com.library.servlet;

import com.library.dao.BookDAO;
import com.library.dao.BookDAO.Book;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/books")
public class BooksServlet extends HttpServlet {

    private final BookDAO bookDAO = new BookDAO();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        // Only logged-in members can access the Books page.
        if (session == null || session.getAttribute("memId") == null) {
            response.sendRedirect(
                request.getContextPath() + "/login"
            );
            return;
        }

        try {
            List<Book> books = bookDAO.getAllBooks();

            request.setAttribute("books", books);

            request.getRequestDispatcher("/books.jsp")
                   .forward(request, response);

        } catch (SQLException e) {
            getServletContext().log(
                "Failed to retrieve books from Oracle Database.", e
            );

            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Unable to load books right now. Please try again later."
            );
        }
    }
}
