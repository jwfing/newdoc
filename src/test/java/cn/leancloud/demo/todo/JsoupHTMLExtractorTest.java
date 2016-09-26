package cn.leancloud.demo.todo;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

public class JsoupHTMLExtractorTest {

	@Test
	public void test() {
		File source = new File("./src/main/webapp/leanstorage_guide-android.html");
		JsoupHTMLExtractor extractor = new JsoupHTMLExtractor();
		Document doc = extractor.getDocument(source, "");
		System.out.println(doc.toString());
		assert(null != doc);
	}

}
