package cn.leancloud.demo.todo;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.lionsoul.jcseg.analyzer.v5x.JcsegAnalyzer5X;
import org.lionsoul.jcseg.tokenizer.core.JcsegTaskConfig;

import java.nio.file.FileSystems;

import com.chenlb.mmseg4j.analysis.ComplexAnalyzer;
import com.chenlb.mmseg4j.analysis.SimpleAnalyzer;

public class LuceneWrapper {
	private static final Logger logger = LogManager.getLogger(LuceneWrapper.class);
	private static final LuceneWrapper wrapper = new LuceneWrapper();
	private static final String parserFields[] = {"content", "title", "headlines"};

	private RAMDirectory dir = null;
	private Analyzer analyzer = null;
	private IndexWriterConfig config = null;
	private IndexWriter index = null;
	private DirectoryReader reader = null;
	private IndexSearcher searcher = null;

	private LuceneWrapper() {
		dir = new RAMDirectory();
		
		JcsegAnalyzer5X  jcseg = new JcsegAnalyzer5X(JcsegTaskConfig.COMPLEX_MODE);
		JcsegTaskConfig segConfig = jcseg.getTaskConfig();
		segConfig.setAppendCJKSyn(false);
		segConfig.setAppendCJKPinyin(false);
		analyzer = jcseg;  //new StandardAnalyzer();
		config = new IndexWriterConfig(analyzer);
	}
	
	public static LuceneWrapper getInstance() {
		return wrapper;
	}
	public void destroyAll() {
		logger.info("destroyAll...");
		if (null != reader) {
			try {
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (null != dir) {
			dir.close();
		}
	}
	public void beginIndexing() {
	    try {
			index = new IndexWriter(dir, config);
			logger.info("create index writer...");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	public void addDocument(Document doc) {
		org.apache.lucene.document.Document luceneDoc = new org.apache.lucene.document.Document();
		Field urlField = new Field("url", doc.getUrl(), TextField.TYPE_STORED);
		luceneDoc.add(urlField);
		Field titleField = new Field("title", doc.getTitle(), TextField.TYPE_STORED);
		titleField.setBoost(3.0f);
		luceneDoc.add(titleField);
		Field hlField = new Field("headlines", doc.getHeadlines(), TextField.TYPE_STORED);
		hlField.setBoost(2.0f);
		luceneDoc.add(hlField);
		luceneDoc.add(new Field("content", doc.getContent(), TextField.TYPE_STORED));
		try {
			index.addDocument(luceneDoc);
			logger.info("add document: " + doc.getUrl() + "...");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void endIndexing() {
		try {
			index.close();
			logger.info("close index writer...");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void startSearching() throws IOException {
		reader = DirectoryReader.open(dir);
		searcher = new IndexSearcher(reader);
		logger.info("create index searcher...");
	}
	
	public List<Document> search(String queryStr) throws ParseException, IOException {
		MultiFieldQueryParser parser = new MultiFieldQueryParser(parserFields, analyzer);
		Query query = parser.parse(queryStr);
		ScoreDoc docs[] = searcher.search(query, 10).scoreDocs;
		QueryScorer scorer = new QueryScorer(query, "content");
		Highlighter highlighter = new Highlighter(scorer);
		highlighter.setTextFragmenter(
	               new SimpleSpanFragmenter(scorer, 128));
		List<Document> result = new ArrayList<>();
		for (ScoreDoc doc:docs) {
			org.apache.lucene.document.Document tmp = searcher.doc(doc.doc);
			String title = tmp.get("title");
			String url = tmp.get("url");
			Document resultDoc = new Document(url);
			resultDoc.setTitle(title);

			String contentString = tmp.get("content");
			TokenStream stream = analyzer.tokenStream("content", new StringReader(tmp.get("content")));
			try {
				String fragment = highlighter.getBestFragment(stream, contentString);
				resultDoc.setHighlighter(fragment);
			} catch (InvalidTokenOffsetsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				resultDoc.setHighlighter("");
			}
			result.add(resultDoc);
		}
		logger.info("query:" + queryStr + ", hits:" + result.size());
		return result;
	}
}
