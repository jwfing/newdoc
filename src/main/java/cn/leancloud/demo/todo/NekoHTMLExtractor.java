package cn.leancloud.demo.todo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.html.dom.HTMLDocumentImpl;
import org.cyberneko.html.parsers.DOMFragmentParser;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class NekoHTMLExtractor {
	private DOMFragmentParser parser = new DOMFragmentParser();
	private static final String []HEADTAGS = {"h2", "h3", "h4", "h5"};
	
	public Document getDocument(File source, String url) {
		InputStream is;
		try {
			is = new FileInputStream(source);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
		DocumentFragment node = new HTMLDocumentImpl().createDocumentFragment();
		try {
			parser.parse(new InputSource(is), node);
			cleanDocumentFragment(node);
			
			StringBuffer sb = new StringBuffer();
			getText(sb, node, "h1");
			String title = sb.toString();
			
			sb.setLength(0);
			getText(sb, node);
			String content = sb.toString();
			
			List<String> headlines = new ArrayList<>();
			sb.setLength(0);
			for (String tag : HEADTAGS) {
				if (getText(sb, node, tag)) {
					headlines.add(" " + sb.toString());
				}
			}
			Document doc = new Document(url);
			doc.setTitle(title);
			doc.setContent(content);
			doc.setHeadlines(sb.toString());
			is.close();
			return doc;
		} catch (IOException ex) {
			;
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private void cleanDocumentFragment(Node node) {
		if (null == node) {
			return;
		}
		short nodeType = node.getNodeType();
		if (nodeType == Node.ELEMENT_NODE) {
			String nodeName = node.getNodeName();
			if ("script".equalsIgnoreCase(nodeName)
					|| "header".equalsIgnoreCase(nodeName)
					|| "footer".equalsIgnoreCase(nodeName)) {
				node.getParentNode().removeChild(node);
			}
		} else if (nodeType == Node.CDATA_SECTION_NODE || nodeType == Node.COMMENT_NODE) {
			node.getParentNode().removeChild(node);
		} else if (nodeType == Node.TEXT_NODE || nodeType == Node.ATTRIBUTE_NODE) {
			return;
		}
		NodeList children = node.getChildNodes();
		if (null != children) {
			int len = children.getLength();
			for (int i = 0; i < len; i++) {
				cleanDocumentFragment(children.item(i));
			}
		}
	}
	private void getText(StringBuffer sb, Node node) {
		if (null == sb || null == node) {
			return;
		}
		if (node.getNodeType() == Node.TEXT_NODE) {
			sb.append(node.getNodeValue());
		}
		NodeList children = node.getChildNodes();
		if (null != children) {
			int len = children.getLength();
			for (int i = 0; i < len; i++) {
				getText(sb, children.item(i));
			}
		}
	}
	
	private boolean getText(StringBuffer sb, Node node, String element) {
		if (null == sb || null == node || null == element) {
			return false;
		}
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			if (element.equalsIgnoreCase(node.getNodeName())) {
				getText(sb, node);
				return true;
			}
		}
		NodeList children = node.getChildNodes();
		if (null != children) {
			int len = children.getLength();
			for (int i = 0; i < len; i++) {
				if (getText(sb, children.item(i), element)) {
					return true;
				}
			}
		}
		return false;
	}
}
