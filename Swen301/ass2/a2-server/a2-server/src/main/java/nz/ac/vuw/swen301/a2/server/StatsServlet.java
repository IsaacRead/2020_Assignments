package nz.ac.vuw.swen301.a2.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class StatsServlet extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();

		// Write the response message, in an HTML page
		try {
			out.println("<!DOCTYPE html>");
			out.println("<html><head>");
			out.println("<meta http-equiv='Content-Type' content='text/html; charset=UTF-8'>");
			out.println("<title>Log Statistics</title></head>");
			out.println("<body>");

			out.println("<table>");
			boolean isHeader = true;
			List<List<String>> logsList = LogsStatistics.logsToNestedList();
			for (List<String> row : logsList) {
				out.println("<tr>");
				for (String cell : row) {
					if (isHeader) {
						out.println("<th>" + cell + "</th>");
						isHeader = false;
					} else {
						out.println("<td>" + cell + "</td>");
					}
				}
				out.println("</tr>");
			}
			out.println("</table>");
			out.println("</body>");
			out.println("</html>");

		} finally {
			out.close(); // Always close the output writer
		}
	}
}
