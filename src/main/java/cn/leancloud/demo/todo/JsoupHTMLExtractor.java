package cn.leancloud.demo.todo;

import java.io.File;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class JsoupHTMLExtractor {

	public Document getDocument(File file, String url) {
		try {
			String title = "";
			String headlines = "";
			String content = "";
			org.jsoup.nodes.Document doc = Jsoup.parse(file, "UTF-8");
			Element contentElement = null;
			Elements cntElements = doc.select(".doc-content");
			if (null != cntElements && cntElements.size() > 0) {
				contentElement = cntElements.first();
				Elements titleCandidates = contentElement.select("h1");
				if (null != titleCandidates && titleCandidates.size() > 0) {
					Element titleElement = titleCandidates.first();
					title = titleElement.text();
				}
				Elements hnElements = contentElement.select("h2,h3,h4,h5");
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < hnElements.size(); i++) {
					String h = hnElements.get(i).text();
					sb.append(" " + h);
				}
			    headlines = sb.toString();
				content = contentElement.text();
			} else {
				content = doc.text();
			}
			
			
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
