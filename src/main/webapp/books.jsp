<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.library.dao.BookDAO.Book" %>
<%@ page import="java.util.Locale" %>

<%!
    private String escapeHtml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }
%>

<%
    if (!(session.getAttribute("memId") instanceof Integer)) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }

    List<Book> books = (List<Book>) request.getAttribute("books");
    String contextPath = request.getContextPath();
    String status = request.getParameter("status");

    String statusMessage = null;
    String statusClass = "info";

    if ("success".equals(status)) {
        statusMessage = "Book borrowed successfully! You can see it on your dashboard.";
        statusClass = "success";
    } else if ("already".equals(status)) {
        statusMessage = "You have already borrowed this book and have not returned it yet.";
        statusClass = "warning";
    } else if ("unavailable".equals(status)) {
        statusMessage = "Sorry, this book is currently unavailable.";
        statusClass = "warning";
    } else if ("notfound".equals(status)) {
        statusMessage = "The selected book could not be found.";
        statusClass = "warning";
    } else if ("member".equals(status)) {
        statusMessage = "Your member account could not be found. Please sign in again.";
        statusClass = "warning";
    } else if ("invalid".equals(status)) {
        statusMessage = "Invalid book selection. Please try again.";
        statusClass = "warning";
    } else if ("error".equals(status)) {
        statusMessage = "We couldn't complete the borrowing request. Please try again.";
        statusClass = "warning";
    }
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Browse Books | Digital Library</title>

    <style>
        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            font-family: Arial, Helvetica, sans-serif;
            background: #f4f7fb;
            color: #243047;
        }

        header {
            background: #172554;
            color: white;
            padding: 20px 7%;
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 15px;
        }

        header h1 {
            margin: 0;
            font-size: 24px;
        }

        nav {
            display: flex;
            gap: 18px;
            flex-wrap: wrap;
        }

        nav a {
            color: white;
            text-decoration: none;
            font-weight: bold;
        }

        nav a:hover {
            text-decoration: underline;
        }

        main {
            max-width: 1200px;
            margin: 35px auto;
            padding: 0 20px;
        }

        .intro {
            margin-bottom: 25px;
        }

        .intro h2 {
            font-size: 30px;
            margin-bottom: 8px;
        }

        .intro p {
            color: #64748b;
            line-height: 1.6;
        }

        .toolbar {
            display: flex;
            gap: 12px;
            margin: 22px 0;
            flex-wrap: wrap;
        }

        .toolbar input,
        .toolbar select {
            padding: 12px;
            border: 1px solid #cbd5e1;
            border-radius: 8px;
            background: white;
            font-size: 15px;
        }

        .toolbar input {
            flex: 1;
            min-width: 220px;
        }

        .summary {
            margin-bottom: 18px;
            color: #475569;
        }

        .status-message {
            padding: 14px 16px;
            margin: 20px 0;
            border-radius: 9px;
            line-height: 1.5;
            border: 1px solid transparent;
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

        .book-grid {
            display: grid;
            grid-template-columns: repeat(
                auto-fit,
                minmax(240px, 1fr)
            );
            gap: 20px;
        }

        .book-card {
            background: white;
            border: 1px solid #e2e8f0;
            border-radius: 14px;
            padding: 22px;
            box-shadow: 0 4px 12px rgba(15, 23, 42, 0.04);
            display: flex;
            flex-direction: column;
            gap: 12px;
        }

        .book-icon {
            width: 48px;
            height: 48px;
            display: grid;
            place-items: center;
            background: #dbeafe;
            border-radius: 12px;
            font-size: 25px;
        }

        .book-card h3 {
            margin: 0;
            font-size: 19px;
            line-height: 1.4;
        }

        .author {
            margin: 0;
            color: #64748b;
            font-size: 14px;
        }

        .genre {
            display: inline-block;
            align-self: flex-start;
            padding: 6px 10px;
            background: #eef2ff;
            color: #3730a3;
            border-radius: 20px;
            font-size: 12px;
            font-weight: bold;
        }

        /* Physical shelf location */
        .location-info {
            background: #f8fafc;
            border: 1px solid #dbeafe;
            border-radius: 10px;
            padding: 12px;
        }

        .location-heading {
            display: flex;
            align-items: center;
            gap: 7px;
            color: #1e40af;
            font-size: 14px;
            margin-bottom: 7px;
        }

        .location-details {
            margin: 0;
            color: #475569;
            font-size: 13px;
            line-height: 1.7;
            overflow-wrap: anywhere;
        }

        .book-meta {
            border-top: 1px solid #e2e8f0;
            padding-top: 12px;
            margin-top: auto;
            display: flex;
            justify-content: space-between;
            gap: 10px;
            font-size: 13px;
            color: #475569;
        }

        .available {
            color: #15803d;
            font-weight: bold;
        }

        .unavailable {
            color: #b91c1c;
            font-weight: bold;
        }

        .borrow-form {
            margin-top: 4px;
        }

        .borrow-button {
            width: 100%;
            padding: 12px 16px;
            border: none;
            border-radius: 8px;
            background: #2563eb;
            color: white;
            font-size: 15px;
            font-weight: bold;
            cursor: pointer;
            transition: background 0.2s;
        }

        .borrow-button:hover {
            background: #1d4ed8;
        }

        .borrow-button:focus-visible {
            outline: 3px solid #93c5fd;
            outline-offset: 3px;
        }

        .borrow-button:disabled {
            background: #94a3b8;
            cursor: not-allowed;
        }

        .empty {
            display: none;
            background: white;
            border-radius: 12px;
            padding: 30px;
            text-align: center;
            color: #64748b;
        }

        footer {
            text-align: center;
            color: #64748b;
            padding: 25px 15px;
            font-size: 13px;
        }

        @media (max-width: 600px) {
            header {
                padding: 18px 5%;
            }

            .intro h2 {
                font-size: 25px;
            }

            main {
                margin-top: 25px;
            }
        }
    </style>
</head>

<body>

<header>
    <h1>📚 Digital Library</h1>

    <nav>
        <a href="<%= contextPath %>/dashboard">Dashboard</a>
        <a href="<%= contextPath %>/books">Borrow Books</a>
        <a href="<%= contextPath %>/logout">Log out</a>
    </nav>
</header>

<main>
    <section class="intro">
        <h2>Discover Your Next Read</h2>
        <p>
            Explore our collection of books, find your favourite genre,
            and borrow an available book from the library.
        </p>
    </section>

    <% if (statusMessage != null) { %>
        <div class="status-message <%= statusClass %>"
             role="status"
             aria-live="polite">
            <%= escapeHtml(statusMessage) %>
        </div>
    <% } %>

    <div class="toolbar">
        <input
            type="search"
            id="searchInput"
            placeholder="Search by book title or author..."
            aria-label="Search books by title or author">

        <select id="genreFilter" aria-label="Filter books by genre">
            <option value="">All genres</option>
        </select>
    </div>

    <p class="summary" id="bookCount" aria-live="polite">
        Loading books...
    </p>

    <section class="book-grid" id="bookGrid">

        <% if (books != null) {
            for (Book book : books) {

                String genre = book.getGenre() == null
                        ? "Uncategorized" : book.getGenre();

                String author = book.getAuthorName() == null
                        ? "Unknown author" : book.getAuthorName();

                String title = book.getBookName() == null
                        ? "Untitled" : book.getBookName();

                String safeTitle = escapeHtml(title);
                String safeAuthor = escapeHtml(author);
                String safeGenre = escapeHtml(genre);

                String physicalLocation =
                        escapeHtml(book.getPhysicalLocation());

                int copies = book.getCopiesAvailable();
        %>

        <article class="book-card"
                 data-title="<%= escapeHtml(title.toLowerCase(Locale.ROOT)) %>"
                 data-author="<%= escapeHtml(author.toLowerCase(Locale.ROOT)) %>"
                 data-genre="<%= safeGenre %>">

            <div class="book-icon" aria-hidden="true">📖</div>

            <h3><%= safeTitle %></h3>

            <p class="author">By <%= safeAuthor %></p>

            <span class="genre"><%= safeGenre %></span>

            <!-- Physical location shown beneath the genre -->
            <div class="location-info">
                <div class="location-heading">
                    <span aria-hidden="true">📍</span>
                    <strong>Physical Location</strong>
                </div>

                <p class="location-details">
                    <%= physicalLocation %>
                </p>
            </div>

            <div class="book-meta">
                <span>
                    Published:
                    <%= book.getYear() == 0
                            ? "Unknown" : book.getYear() %>
                </span>

                <span class="<%= copies > 0 ? "available" : "unavailable" %>">
                    <%= copies > 0
                            ? copies + " available"
                            : "Unavailable" %>
                </span>
            </div>

            <% if (copies > 0) { %>
                <form class="borrow-form"
                      method="post"
                      action="<%= contextPath %>/borrow"
                      onsubmit="return confirmBorrow(this);">

                    <input type="hidden"
                           name="bookId"
                           value="<%= book.getBookId() %>">

                    <button type="submit" class="borrow-button">
                        Borrow Book
                    </button>
                </form>
            <% } else { %>
                <button type="button" class="borrow-button" disabled>
                    Currently Unavailable
                </button>
            <% } %>

        </article>

        <%  }
           }
        %>

    </section>

    <div class="empty" id="emptyMessage">
        <h3>No books found</h3>
        <p>Try another title, author, or genre.</p>
    </div>
</main>

<footer>
    Digital Library Management System
</footer>

<script>
    const searchInput = document.getElementById("searchInput");
    const genreFilter = document.getElementById("genreFilter");
    const bookGrid = document.getElementById("bookGrid");
    const bookCount = document.getElementById("bookCount");
    const emptyMessage = document.getElementById("emptyMessage");

    const cards = Array.from(
        bookGrid.querySelectorAll(".book-card")
    );

    const genres = [...new Set(
        cards.map(card => card.dataset.genre)
    )].sort();

    genres.forEach(genre => {
        const option = document.createElement("option");
        option.value = genre.toLowerCase();
        option.textContent = genre;
        genreFilter.appendChild(option);
    });

    function filterBooks() {
        const search = searchInput.value.trim().toLowerCase();
        const selectedGenre = genreFilter.value;

        let visibleCount = 0;

        cards.forEach(card => {
            const matchesSearch =
                card.dataset.title.includes(search) ||
                card.dataset.author.includes(search);

            const matchesGenre =
                selectedGenre === "" ||
                card.dataset.genre.toLowerCase() === selectedGenre;

            const visible = matchesSearch && matchesGenre;

            card.style.display = visible ? "flex" : "none";

            if (visible) {
                visibleCount++;
            }
        });

        bookCount.textContent =
            visibleCount + (visibleCount === 1
                ? " book found"
                : " books found");

        emptyMessage.style.display =
            visibleCount === 0 ? "block" : "none";
    }

    function confirmBorrow(form) {
        const button = form.querySelector("button[type='submit']");

        if (!confirm("Would you like to borrow this book?")) {
            return false;
        }

        button.disabled = true;
        button.textContent = "Processing...";

        return true;
    }

    searchInput.addEventListener("input", filterBooks);
    genreFilter.addEventListener("change", filterBooks);

    filterBooks();
</script>

</body>
</html>
