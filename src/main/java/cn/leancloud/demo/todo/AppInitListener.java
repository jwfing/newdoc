package cn.leancloud.demo.todo;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.avos.avoscloud.internal.impl.JavaRequestSignImplementation;

import cn.leancloud.LeanEngine;

@WebListener
public class AppInitListener implements ServletContextListener {

  private static final Logger logger = LogManager.getLogger(AppInitListener.class);

  private String appId = System.getenv("LEANCLOUD_APP_ID");
  private String appKey = System.getenv("LEANCLOUD_APP_KEY");
  private String appMasterKey = System.getenv("LEANCLOUD_APP_MASTER_KEY");

  @Override
  public void contextDestroyed(ServletContextEvent arg0) {
	  LuceneWrapper wrapper = LuceneWrapper.getInstance();
	  wrapper.destroyAll();
  }

  @Override
  public void contextInitialized(ServletContextEvent arg0) {
    logger.info("LeanEngine app init.");
    // 注册子类化
//    AVObject.registerSubclass(Todo.class);
    // 初始化AVOSCloud，请保证在整个项目中间只初始化一次
    LeanEngine.initialize(appId, appKey, appMasterKey);
    // 在请求签名中使用masterKey以激活云代码的最高权限
    JavaRequestSignImplementation.instance().setUseMasterKey(true);
    // 打开 debug 日志
    // AVOSCloud.setDebugLogEnabled(true);
    // 向云引擎注册云函数
    LeanEngine.register(Cloud.class);
    if (System.getenv("LEANCLOUD_APP_ENV").equals("development")) {
      // 如果是开发环境，则设置 AVCloud.callFunction 和 AVCloud.rpcFunction 调用本地云函数实现
      // 如果需要本地开发时调用云端云函数实现，则注释掉下面语句。
      LeanEngine.setLocalEngineCallEnabled(true);
    }

    Thread indexingThread = new Thread(){
    	@Override
    	public void run(){
    	    LuceneWrapper wrapper = LuceneWrapper.getInstance();
    	    wrapper.beginIndexing();
    	    JsoupHTMLExtractor extractor = new JsoupHTMLExtractor();

    	    String htmlDir = "./src/main/webapp";
    	    String docRootPath = "http://newdoc.leanapp.cn";
    	    if (System.getenv("LEANCLOUD_APP_ENV").equals("development")) {
    	    	docRootPath = "http://localhost:3000";
    	    }

    	    try {
    	        File dir = new File(htmlDir);
    	        List<String> htmlFiles = listDir(dir);
    	        for (String htmlFile: htmlFiles) {
    	        	String url = htmlFile.replace(htmlDir, docRootPath);
    	        	File source = new File(htmlFile);
    	    		Document doc = extractor.getDocument(source, url);
    	    		wrapper.addDocument(doc);
    	        }
    	        wrapper.endIndexing();
    			wrapper.startSearching();
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    	}
    };
    indexingThread.start();
  }
  
  private List<String> listDir(File root) {
	  List<String> results = new ArrayList<>();
	  String files[] = root.list(new FilenameFilter() {
		  @Override
		  public boolean accept(File dir, String name) {
			  return name.endsWith(".html");
		  }
	  });
	  for (String file: files) {
		  results.add(root.getPath() + "/" + file);
	  }
	  File[] subDirs = root.listFiles(new FileFilter() {
	    	@Override
	    	public boolean accept(File filepath) {
	    		if (!filepath.isDirectory()) {
	    			return false;
	    		}
	    		if (filepath.getPath().endsWith("custom")) {
	    			return false;
	    		}
	    		if (filepath.getPath().endsWith("images")) {
	    			return false;
	    		}
	    		return true;
	    	}
	    });
	  for (File subDir: subDirs) {
		  List<String> tmp = listDir(subDir);
		  if (null != tmp && tmp.size() > 0) {
			  results.addAll(tmp);
		  }
	  }
	  return results;
  }
}
