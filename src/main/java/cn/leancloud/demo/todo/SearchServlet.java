package cn.leancloud.demo.todo;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.queryparser.classic.ParseException;

@WebServlet(name = "SearchServlet", urlPatterns = { "/search" })
public class SearchServlet extends HttpServlet {
	private static final Logger logger = LogManager.getLogger(SearchServlet.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 5100584188811425593L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String queryStr = req.getParameter("q");
		LuceneWrapper wrapper = LuceneWrapper.getInstance();
		try {
			List<Document> docs = wrapper.search(queryStr);
			req.setAttribute("results", docs);
			req.getRequestDispatcher("/search.jsp").forward(req, resp);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
