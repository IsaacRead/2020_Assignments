package nz.ac.vuw.swen301.a2.server;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class StatsCSVServlet extends HttpServlet {


		@Override
		public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
			 response.setContentType("text/csv");
			 List<List<String>> logList = LogsStatistics.logsToNestedList();
			// setup the header line
			 StringBuilder sb = new StringBuilder();

			 // now append your data in a loop
			 for (int i = 0; i < logList.size(); i++) {
				 
				 for (int j = 0; j < logList.get(i).size(); j++) {
					sb.append(logList.get(i).get(j));
					sb.append("\t");
				}
			    sb.append("\n");
			 }
			 response.getWriter().write(sb.toString());
			 
}
}