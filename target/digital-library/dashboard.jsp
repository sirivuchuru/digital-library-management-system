
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.library.dao.MemberDAO.Member" %>
<%@ page import="com.library.dao.MemberDAO.BorrowedBook" %>

<%!
    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#x27;");
    }
%>

<%
    if (!(session.getAttribute("memId") instanceof Integer)) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }

    Member member = (Member) request.getAttribute("member");

    List<BorrowedBook> borrowedBooks =
            (List<BorrowedBook>) request.getAttribute("borrowedBooks");

    if (member == null || borrowedBooks == null) {
        response.sendRedirect(request.getContextPath() + "/dashboard");
        return;
    }

    String contextPath = request.getContextPath();
    String returnStatus = request.getParameter("returnStatus");

    String statusMessage = null;
    String statusClass = "info";

    if ("success".equals(returnStatus)) {
        statusMessage = "Book returned successfully! The available-copy count has been updated.";
        statusClass = "success";
    } else if ("notfound".equals(returnStatus)) {
        statusMessage = "The selected book could not be found.";
        statusClass = "warning";
    } else if ("notborrowed".equals(returnStatus)) {
        statusMessage = "You do not have an active loan for that book, or it has already been returned.";
        statusClass = "warning";
    } else if ("invalid".equals(returnStatus)) {
        statusMessage = "Invalid book selection. Please try again.";
        statusClass = "warning";
    } else if ("error".equals(returnStatus)) {
        statusMessage = "We couldn't complete the return. Please try again.";
        statusClass = "warning";
    }
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>My Dashboard | Digital Library</title>

    <style>
        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            font-family: Arial, sans-serif;
            background: #f4f6fb;
            color: #202744;
        }

        header {
            background: #292b64;
            color: white;
            padding: 20px 6%;
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 15px;
            flex-wrap: wrap;
        }

        header h1 {
            margin: 0;
            font-size: 23px;
        }

        .header-actions {
            display: flex;
            align-items: center;
            gap: 12px;
            flex-wrap: wrap;
        }

        header a {
            color: white;
            text-decoration: none;
            padding: 10px 16px;
            border: 1px solid rgba(255,255,255,.5);
            border-radius: 8px;
            transition: background .2s ease;
        }

        header a:hover {
            background: rgba(255,255,255,.12);
        }

        .container {
            max-width: 1100px;
            margin: 35px auto;
            padding: 0 20px;
        }

        .welcome {
            margin-bottom: 25px;
        }

        .welcome h2 {
            margin-bottom: 8px;
            font-size: 28px;
        }

        .welcome p {
            color: #68708a;
            line-height: 1.6;
        }

        .dashboard-actions {
            margin-top: 20px;
            margin-bottom: 30px;
        }

        .browse-books-btn {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            gap: 9px;
            padding: 13px 22px;
            background: #292b64;
            color: #ffffff;
            text-decoration: none;
            border: 1px solid #292b64;
            border-radius: 9px;
            font-size: 15px;
            font-weight: bold;
            transition: background .2s ease, transform .2s ease;
        }

        .browse-books-btn:hover {
            background: #41449a;
            transform: translateY(-1px);
        }

        .browse-books-btn:focus-visible,
        .return-btn:focus-visible {
            outline: 3px solid #818cf8;
            outline-offset: 3px;
        }

        .profile {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(210px, 1fr));
            gap: 18px;
            margin-bottom: 30px;
        }

        .card {
            padding: 24px;
            background: white;
            border-radius: 14px;
            box-shadow: 0 5px 20px rgba(30,40,80,.06);
        }

        .card-label {
            color: #6b7280;
            font-size: 14px;
            margin-bottom: 10px;
        }

        .card-value {
            font-size: 19px;
            font-weight: bold;
            overflow-wrap: anywhere;
        }

        .section-title {
            margin: 30px 0 16px;
            font-size: 22px;
        }

        .status-message {
            padding: 14px 16px;
            margin: 20px 0;
            border: 1px solid transparent;
            border-radius: 9px;
            line-height: 1.6;
        }

        .status-message.success {
            color: #166534;
            background: #dcfce7;
            border-color: #86efac;
        }

        .status-message.warning {
            color: #92400e;
            background: #fef3c7;
            border-color: #fcd34d;
        }

        .table-wrapper {
            background: white;
            border-radius: 14px;
            overflow-x: auto;
            box-shadow: 0 5px 20px rgba(30,40,80,.06);
        }

        table {
            width: 100%;
            border-collapse: collapse;
            min-width: 760px;
        }

        th, td {
            text-align: left;
            padding: 16px;
            border-bottom: 1px solid #edf0f6;
        }

        th {
            background: #f8f9fd;
            color: #4b5270;
            font-size: 13px;
            text-transform: uppercase;
        }

        tbody tr:last-child td {
            border-bottom: none;
        }

        .return-btn {
            padding: 10px 14px;
            border: none;
            border-radius: 7px;
            background: #b91c1c;
            color: white;
            font-size: 13px;
            font-weight: bold;
            cursor: pointer;
            white-space: nowrap;
        }

        .return-btn:hover {
            background: #991b1b;
        }

        .return-btn:disabled {
            background: #94a3b8;
            cursor: wait;
        }

        .empty {
            padding: 35px;
            text-align: center;
            color: #68708a;
        }

        .empty a {
            color: #292b64;
            font-weight: bold;
        }

        footer {
            text-align: center;
            padding: 25px;
            color: #777f95;
            font-size: 13px;
        }

        @media (max-width: 600px) {
            header {
                padding: 18px 5%;
            }

            .container {
                margin-top: 24px;
            }

            .welcome h2 {
                font-size: 23px;
            }

            .browse-books-btn {
                width: 100%;
            }
        }
    </style>
</head>

<body>

<header>
    <h1>📚 Digital Library</h1>

    <div class="header-actions">
        <a href="<%= contextPath %>/books">Borrow Books</a>
        <a href="<%= contextPath %>/logout">Log Out</a>
    </div>
</header>

<main class="container">

    <section class="welcome">
        <h2>Welcome, <%= escapeHtml(member.getName()) %>!</h2>
        <p>Here is your personal library overview.</p>

        <div class="dashboard-actions">
            <a href="<%= contextPath %>/books" class="browse-books-btn">
                📚 Browse Books
                <span aria-hidden="true">→</span>
            </a>
        </div>
    </section>

    <% if (statusMessage != null) { %>
        <div class="status-message <%= statusClass %>"
             role="status"
             aria-live="polite">
            <%= escapeHtml(statusMessage) %>
        </div>
    <% } %>

    <section class="profile">
        <div class="card">
            <div class="card-label">Member ID</div>
            <div class="card-value"><%= member.getMemId() %></div>
        </div>

        <div class="card">
            <div class="card-label">Member Name</div>
            <div class="card-value"><%= escapeHtml(member.getName()) %></div>
        </div>

        <div class="card">
            <div class="card-label">Email Address</div>
            <div class="card-value"><%= escapeHtml(member.getEmail()) %></div>
        </div>

        <div class="card">
            <div class="card-label">Currently Borrowed</div>
            <div class="card-value"><%= borrowedBooks.size() %> book(s)</div>
        </div>
    </section>

    <h2 class="section-title">My Borrowed Books</h2>

    <div class="table-wrapper">

        <% if (borrowedBooks.isEmpty()) { %>

            <div class="empty">
                <h3>No books currently borrowed</h3>
                <p>When you borrow a book, it will appear here.</p>
                <a href="<%= contextPath %>/books">Explore available books</a>
            </div>

        <% } else { %>

            <table>
                <thead>
                    <tr>
                        <th>Book ID</th>
                        <th>Book Title</th>
                        <th>Author</th>
                        <th>Issue Date</th>
                        <th>Action</th>
                    </tr>
                </thead>

                <tbody>
                    <% for (BorrowedBook book : borrowedBooks) { %>
                        <tr>
                            <td><%= book.getBookId() %></td>
                            <td><%= escapeHtml(book.getBookName()) %></td>
                            <td><%= escapeHtml(
                                    book.getAuthorName() == null
                                    ? "Unknown" : book.getAuthorName()) %></td>
                            <td><%= escapeHtml(book.getIssueDate()) %></td>
                            <td>
                                <form method="post"
                                      action="<%= contextPath %>/return"
                                      onsubmit="return confirmReturn(this);">

                                    <input type="hidden"
                                           name="bookId"
                                           value="<%= book.getBookId() %>">

                                    <button type="submit" class="return-btn">
                                        Return Book
                                    </button>
                                </form>
                            </td>
                        </tr>
                    <% } %>
                </tbody>
            </table>

        <% } %>

    </div>

</main>

<footer>
    Digital Library Management System
</footer>

<script>
    function confirmReturn(form) {
        if (!confirm("Are you sure you want to return this book?")) {
            return false;
        }

        const button = form.querySelector("button[type='submit']");
        button.disabled = true;
        button.textContent = "Returning...";

        return true;
    }
</script>

</body>
</html>
