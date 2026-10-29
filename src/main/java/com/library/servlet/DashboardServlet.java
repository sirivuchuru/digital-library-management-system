
package com.library.servlet;

import com.library.dao.MemberDAO;
import com.library.dao.MemberDAO.Member;
import com.library.dao.MemberDAO.BorrowedBook;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    private final MemberDAO memberDAO = new MemberDAO();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("memId") == null) {
            response.sendRedirect(
                    request.getContextPath() + "/login");
            return;
        }

        int memId = (Integer) session.getAttribute("memId");

        try {
            Member member = memberDAO.getMember(memId);

            if (member == null) {
                session.invalidate();
                response.sendRedirect(
                        request.getContextPath() + "/login");
                return;
            }

            List<BorrowedBook> borrowedBooks =
                    memberDAO.getBorrowedBooks(memId);

            request.setAttribute("member", member);
            request.setAttribute("borrowedBooks", borrowedBooks);

            request.getRequestDispatcher("/dashboard.jsp")
                   .forward(request, response);

        } catch (SQLException e) {
            throw new ServletException(
                    "Unable to load member dashboard.", e);
        }
    }
}
