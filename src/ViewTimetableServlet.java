// ViewTimetableServlet.java - Servlet for displaying timetable on web
// Concepts: Servlets (Exp 6)

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class ViewTimetableServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Fetch timetable data from database
        try {
            List<String[]> timetable = DatabaseHelper.getTimetable();
            request.setAttribute("timetable", timetable);
            request.getRequestDispatcher("/WEB-INF/timetable.jsp").forward(request, response);
        } catch (IllegalStateException ex) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "ExamCell cannot read the timetable database. Check the database connection settings.");
            getServletContext().log("Timetable database unavailable", ex);
        }
    }
}
