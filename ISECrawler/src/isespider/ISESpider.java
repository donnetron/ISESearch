package isespider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ISESpider {
	Logger logger = LogManager.getLogger(this.getClass().getName());

	private ISEProperties prop;
	private ISEIndexSpider iseis;
	private ISENoticeSpider isens;

	// constructor
	ISESpider(ISEProperties prop) {
		this.prop = prop;
	}
	
	// standard crawl using config.properties (or CLI input)
	public void crawl() {
		
		// *INDEX* spider (crawls the page search results)
		iseis = new ISEIndexSpider(prop);
		iseis.crawl();

		// *NOTICE* spider (crawls the actual notices)
		isens = new ISENoticeSpider(prop);
		isens.crawl();
		
		prop.write();	
	}
	
	public void resume() {
		//resume index spider
		if (prop.getProperty("indexSpiderActive").equals("true")) {
			ISECrawler.exit("Error! 'Resume' option selected while Index spider running ('Resume' only supported for Notice spider)");
		}
		//resume notice spider
		else if (prop.getProperty("noticeSpiderActive").equals("true")) {
			isens = new ISENoticeSpider(prop);
			isens.resume();
		} 
		else { logger.info("No resume state"); }
	}
	
	public void repair() {
		isens = new ISENoticeSpider(prop);
		isens.repair();
	}

}