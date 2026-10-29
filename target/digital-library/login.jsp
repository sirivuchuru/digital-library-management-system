 <%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Digital Library | Login</title>

    <style>
        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            min-height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
            padding: 20px;
            font-family: Arial, sans-serif;
            background: linear-gradient(135deg, #e0eaff, #f5e9ff);
        }

        .login-card {
            width: 100%;
            max-width: 420px;
            padding: 36px;
            background: white;
            border-radius: 18px;
            box-shadow: 0 12px 35px rgba(30, 40, 80, 0.12);
        }

        .icon {
            font-size: 42px;
            text-align: center;
        }

        h1 {
            margin-bottom: 8px;
            text-align: center;
            color: #242b49;
        }

        .subtitle {
            margin-bottom: 28px;
            text-align: center;
            color: #6b7280;
        }

        label {
            display: block;
            margin: 16px 0 7px;
            font-weight: bold;
            color: #374151;
        }

        input {
            width: 100%;
            padding: 12px;
            border: 1px solid #d1d5db;
            border-radius: 8px;
            font-size: 15px;
        }

        input:focus {
            outline: 2px solid #7c83ff;
            border-color: transparent;
        }

        button {
            width: 100%;
            margin-top: 24px;
            padding: 13px;
            border: none;
            border-radius: 8px;
            background: #5757db;
            color: white;
            font-size: 16px;
            font-weight: bold;
            cursor: pointer;
        }

        button:hover {
            background: #4444bb;
        }

        .error {
            padding: 10px;
            border-radius: 7px;
            background: #fff0f0;
            color: #b42318;
            text-align: center;
        }

        .footer {
            margin-top: 22px;
            text-align: center;
            font-size: 13px;
            color: #6b7280;
        }
    </style>
</head>

<body>
    <main class="login-card">
        <div class="icon" aria-hidden="true">📚</div>

        <h1>Digital Library</h1>
        <p class="subtitle">Sign in to explore your library</p>

        <% if (request.getAttribute("error") != null) { %>
            <p class="error" role="alert">
                Invalid email or password.
            </p>
        <% } %>

        <form action="${pageContext.request.contextPath}/login"
              method="post">

            <label for="email">Email address</label>
            <input
                type="email"
                id="email"
                name="email"
                autocomplete="username"
                maxlength="100"
                required>

            <label for="password">Password</label>
            <input
                type="password"
                id="password"
                name="password"
                autocomplete="current-password"
                required>

            <button type="submit">Sign In</button>
        </form>

        <div class="footer">
            Digital Library Management System
        </div>
    </main>
</body>
</html>