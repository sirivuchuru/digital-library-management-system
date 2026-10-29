
package com.library.servlet;

import com.library.dao.LoginDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final LoginDAO loginDAO = new LoginDAO();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.getRequestDispatcher("/login.jsp")
               .forward(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String email = request.getParameter("email");
        String submittedPassword = request.getParameter("password");

        if (email == null || submittedPassword == null
                || email.isBlank() || submittedPassword.isEmpty()) {

            request.setAttribute(
                    "error", "Please enter your email and password.");

            request.getRequestDispatcher("/login.jsp")
                   .forward(request, response);
            return;
        }

        char[] password = submittedPassword.toCharArray();
        Arrays.fill(submittedPassword.toCharArray(), '\0');

        try {
            Integer memId = loginDAO.authenticate(email, password);

            if (memId == null) {
                request.setAttribute(
                        "error", "Invalid email or password.");

                request.getRequestDispatcher("/login.jsp")
                       .forward(request, response);
                return;
            }

            HttpSession oldSession = request.getSession(false);

            if (oldSession != null) {
                oldSession.invalidate();
            }

            HttpSession session = request.getSession(true);
            session.setAttribute("memId", memId);
            session.setMaxInactiveInterval(20 * 60);

            response.sendRedirect(
                    request.getContextPath() + "/dashboard");

        } catch (GeneralSecurityException | java.sql.SQLException e) {
            throw new ServletException(
                    "Unable to authenticate at this time.", e);
        } finally {
            Arrays.fill(password, '\0');
        }
    }
}
