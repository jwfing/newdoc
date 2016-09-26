package cn.leancloud.demo.todo;

public class Document {
	private String title;
	private String headlines;
	private String content;
	private String url;
	private String highlighter;
	public Document(String url) {
		this.url = url;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getHeadlines() {
		return headlines;
	}
	public void setHeadlines(String headlines) {
		this.headlines = headlines;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String toString() {
		return "{title:" + this.title + ", headliens:" + this.headlines + ", content:" + this.content + "}";
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getHighlighter() {
		return highlighter;
	}
	public void setHighlighter(String highlighter) {
		this.highlighter = highlighter;
	}
}
