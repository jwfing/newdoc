package cn.leancloud.demo.todo;

import java.io.File;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class JsoupHTMLExtractor {

	public Document getDocument(File file, String url) {
		try {
			org.jsoup.nodes.Document doc = Jsoup.parse(file, "UTF-8");
			Element contentElement = doc.select(".doc-content").first();
			Element titleElement = contentElement.select("h1").first();
			String title = titleElement.text();
			Elements hnElements = contentElement.select("h2,h3,h4,h5");
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < hnElements.size(); i++) {
				String h = hnElements.get(i).text();
				sb.append(" " + h);
			}
			String headlines = sb.toString();
			String content = contentElement.text();
			Document result = new Document(url);
			result.setContent(content);
			result.setHeadlines(headlines);
			result.setTitle(title);
			return result;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
