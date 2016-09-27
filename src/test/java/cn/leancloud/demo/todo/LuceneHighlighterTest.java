package cn.leancloud.demo.todo;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;
import org.lionsoul.jcseg.analyzer.v5x.JcsegAnalyzer5X;
import org.lionsoul.jcseg.tokenizer.core.JcsegTaskConfig;

public class LuceneHighlighterTest {

	public void test() throws IOException, ParseException {
		String title = "管理控制台使用指南";
		String headlines = "创建应用 管理应用数据 开发者信息 账单和付款";
		String content = "在使用 LeanCloud 数据存储的时候，我们应用每天的调用量如何，"
				+ "不同平台过来的请求量有多少，里面哪些请求比较耗时，主要是什么操作导致的，如何才能得到更好的性能提升用户体验，"
				+ "等等数据都离不开 API 统计结果。 进入 存储 > API 统计 菜单，你可以看到：API 汇总，这里汇总了应用每天访问"
				+ "量的变化趋势，支持线性图、饼状图展示，并且也可以按照 iOS／Android／Javascript／云引擎等不同平台区别展示"
				+ "所有调用。按照操作类型分类展示 API 调用量，这里可以看到 Create 请求、Find 请求、Get 请求、Update 请求、"
				+ "Delete 请求等不同操作类型下每天的请求量变化趋势，方便我们对特殊的操作来做 profiling 和优化。文件存储空间和"
				+ "流量方面的统计。其它更多的统计项目。";
		RAMDirectory ramDir = new RAMDirectory();

		JcsegAnalyzer5X  jcseg = new JcsegAnalyzer5X(JcsegTaskConfig.COMPLEX_MODE);
		JcsegTaskConfig segConfig = jcseg.getTaskConfig();
		segConfig.setAppendCJKSyn(false);
		segConfig.setAppendCJKPinyin(false);

		Analyzer analyzer = jcseg;//new StandardAnalyzer();
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		IndexWriter index = new IndexWriter(ramDir, config);

		FieldType type = new FieldType();
		type.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
		type.setStored(true);
		type.setStoreTermVectorOffsets(true);
		type.setTokenized(true);
		type.setStoreTermVectorOffsets(true);
		
		org.apache.lucene.document.Document luceneDoc = new org.apache.lucene.document.Document();
		Field urlField = new Field("url", "http://localhost:3000/index.html", TextField.TYPE_STORED);
		luceneDoc.add(urlField);
		Field titleField = new Field("title", title, TextField.TYPE_STORED);
		titleField.setBoost(3.0f);
		luceneDoc.add(titleField);
		Field hlField = new Field("headlines", headlines, TextField.TYPE_STORED);
		hlField.setBoost(2.0f);
		luceneDoc.add(hlField);
		luceneDoc.add(new Field("content", content, TextField.TYPE_STORED));
    	index.addDocument(luceneDoc);
    	index.close();
    	
		DirectoryReader reader = DirectoryReader.open(ramDir);
		IndexSearcher searcher = new IndexSearcher(reader);
    	
		String queryStr = "数据存储";
		String parserFields[] = {"content", "title", "headlines"};
		MultiFieldQueryParser parser = new MultiFieldQueryParser(parserFields, analyzer);
		Query query = parser.parse(queryStr);
		ScoreDoc docs[] = searcher.search(query, 10).scoreDocs;
		QueryScorer scorer = new QueryScorer(query, "content");
		Highlighter highlighter = new Highlighter(scorer);
		highlighter.setTextFragmenter(
	               new SimpleSpanFragmenter(scorer, 128));
		for (ScoreDoc doc:docs) {
			org.apache.lucene.document.Document tmp = searcher.doc(doc.doc);
			String contentString = tmp.get("content");
			TokenStream stream = analyzer.tokenStream("content", new StringReader(tmp.get("content")));
			try {
				String fragment = highlighter.getBestFragment(stream, contentString);
				System.out.println(fragment);
			} catch (InvalidTokenOffsetsException e) {
				e.printStackTrace();
			}
		}
		reader.close();
		ramDir.close();
	}

}
