<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.*" %>
<%!
    private String html(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                    .replace("\"", "&quot;").replace("'", "&#39;");
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>ExamCell | Examination Timetable</title>
    <style>
        body { font: 16px/1.5 Arial, sans-serif; margin: 0; padding: 32px 5%; color: #183044; background: #f3f6f9; }
        main { max-width: 1100px; margin: auto; }
        h1 { margin-bottom: 4px; }
        .sub { color: #526777; margin-top: 0; }
        .table-wrap { overflow-x: auto; background: white; border-radius: 8px; box-shadow: 0 2px 12px #18304418; }
        table { width: 100%; border-collapse: collapse; }
        th, td { text-align: left; padding: 12px 14px; border-bottom: 1px solid #e5ebf0; }
        th { background: #176b87; color: white; }
        tr:last-child td { border-bottom: 0; }
        .empty { background: white; padding: 24px; border-radius: 8px; color: #526777; }
    </style>
</head>
<body><main>
    <h1>Examination Timetable</h1>
    <p class="sub">Published schedule from the ExamCell resource scheduler</p>
    <%
        List<String[]> timetable = (List<String[]>) request.getAttribute("timetable");
        if (timetable != null && !timetable.isEmpty()) {
    %>
    <div class="table-wrap"><table>
        <thead><tr><th>Subject</th><th>Batch</th><th>Room</th><th>Supervisor</th><th>Date</th><th>Time</th></tr></thead>
        <tbody>
        <% for (String[] row : timetable) { %>
        <tr><td><%= html(row[0]) %></td><td><%= html(row[1]) %></td><td><%= html(row[2]) %></td><td><%= html(row[3]) %></td><td><%= html(row[4]) %></td><td><%= html(row[5]) %></td></tr>
        <% } %>
        </tbody>
    </table></div>
    <% } else { %>
    <p class="empty">No timetable has been generated yet. The examination office must add the resources and generate a schedule in the desktop app.</p>
    <% } %>
</main></body>
</html>
