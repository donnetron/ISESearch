package isespider;

import java.io.*;
import java.net.*;
import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.DocumentFragment;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class ISEIndexSpider {
	Logger logger = LogManager.getLogger(this.getClass().getName());

	private BasicFileHandler bfh;
	private BigXMLFileHandler indexCrawlData;
	private ISEProperties prop;
	private int lastUID;
	private LocalDate searchBeginDate;
	private LocalDate searchEndDate;

	//Constructor
	ISEIndexSpider(ISEProperties prop) {
		logger.info("Initialising ISE Index Spider");
		
		this.prop = prop;
		bfh = new BasicFileHandler();
		
		//init search dates
		searchBeginDate = LocalDate.parse(prop.getProperty("searchBeginDate"), DateTimeFormatter.ofPattern("dd-MM-yyyy"));
		searchEndDate = LocalDate.parse(prop.getProperty("searchEndDate"), DateTimeFormatter.ofPattern("dd-MM-yyyy"));

		lastUID = Integer.parseInt(prop.getProperty("lastUID"));
	}

	public void crawl() {
		
		prop.setProperty("indexSpiderActive", "true");
		
		ISEIndexDeque ised = new ISEIndexDeque(searchBeginDate, searchEndDate); // creates a deque (double ended queue) of all the index pages to scrape by the IndexSpider (uses jSoup to get initial search URL)
		Deque<String> indexDeque = ised.getIndexDeque(); // deque is ordered from [1]oldest date -> [N]newest date
		
		String indexDataFile = prop.getProperty("lastIndexDataFile");
		indexCrawlData = new BigXMLFileHandler(indexDataFile, "add");	//new xml file with root element 'add'
		
		while (!indexDeque.isEmpty()) {
			String indexPageURL = indexDeque.peek();

			Document indexPage = jsoupGetURL(indexPageURL);
			try {
				DocumentFragment noticeData = scrapeIndexPage(indexPage);
				indexCrawlData.addElement(noticeData);
			}
			catch (NullPointerException e) { logger.error("No result data found on page ({})", indexPageURL); }
			catch (Exception e) { logger.catching(e); }

			prop.setProperty("lastIndexURLCollected", indexPageURL);
			prop.setProperty("lastIndexDataFile", indexCrawlData.checkIncrement(prop.getProperty("maxDataFileSize")));
			indexDeque.pop();
		}
		
		indexCrawlData.closeFile();
		prop.setProperty("indexSpiderActive", "false");
		prop.write();
		
		// zip up the meta data we have, just in case...
		// uncomment the below if doing massive crawls (not worth zipping up a folder one file (or none) every day)
		// bfh.zipDirectory(prop.getProperty("dataProcessingDir"), prop.getProperty("backupDir") + "metadata");

		logger.info("-- Index crawl done --");
	
	}
	
	private DocumentFragment scrapeIndexPage(Document resultPage) throws Exception {
		//logger.info("Parsing...");
		/*
		SAMPLE <doc> ELEMENT CHUNK
			<doc>
				<field name="UID">0000000</field>
				<field name="company">Titan Europe 2007-2</field>
				<field name="datetime">2012-10-22T14:14:00Z</field>
				<field name="title">Notice</field>
				<field name="url">http://www.ise.ie/app/announcementDetails.aspx?ID=11370839</field>
				<field name="notice">Company name Irish Stock Exchange Headline MSM...............</field>
				<field name="cachefile">00000000_2012-10-22_11370839</field>
			</doc>
		*/

		// tries to parse the page for the data we need, but will return a null object if there was nothing to parse.
		Element resultTable = resultPage.select("table[class=announcementList]").first();
		Elements rows = resultTable.select("tr");
		logger.debug("Total page rows: {}", rows.size());

		// remove the first row element which is the heading row
		rows.remove(0);
		logger.debug("Rows to be parsed: {}", rows.size());
		
		// reverse the list so that we start from oldest first
		Collections.reverse(rows);
		
		// create a new DOM Document that will be a temporary store for all the <doc> elements on this index page
		// becuase it must be well formed including a single root element, the root will be <indexPage>. Remember this is a temporary DOM
		org.w3c.dom.Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		org.w3c.dom.Element rootElement = doc.createElement("indexPage");
		doc.appendChild(rootElement);

		//iterate through the array of rows <tr>
		for (Element tr : rows) {
			lastUID++;
			Elements tds = tr.select("td");
			logger.debug("<td> elements: {}", tds.size());
			
			// (THIS IS A SINGLE LINE DEBUG LOOP) iterate through the array of cells <td>
			for (Element td : tds) { logger.debug("     {}", td.text()); }
			ISEDOMNotice isedn = new ISEDOMNotice();
			
			// <field name="UID">UID</field>
			String sUID = String.format("%07d", lastUID);
			isedn.setUID(String.format("%07d", lastUID));
			prop.setProperty("lastUID", sUID);	//set config file
			logger.debug("set UID: {}", sUID);	
			
			// <field name="company">Titan Europe 2007-2</field>
			String sCompany = tds.get(1).text();
			logger.debug("setting Company: {}", sCompany);
			isedn.setCompany(tds.get(1).text());

			// <field name="datetime">2012-10-22T14:14:00Z</field>
			String[] date = tds.get(0).text().split("/");
			String[] time = tds.get(3).text().split(":");
			LocalDateTime ldt = LocalDateTime.of(Integer.parseInt(date[2]),
												 Integer.parseInt(date[1]),
												 Integer.parseInt(date[0]),
												 Integer.parseInt(time[0]),
												 Integer.parseInt(time[1]));
			String sDateTime = ldt.toString() + ":00Z";
			isedn.setDateTime(sDateTime);
			prop.setProperty("lastNoticeDate", sDateTime); //set config file
			logger.debug("set DateTime: {}", sDateTime);
			
			// <field name="title">Notice</field>
			String sTitle = tds.get(2).text();
			isedn.setTitle(sTitle);
			logger.debug("set Title: {}", sTitle);
			
			// <field name="url">http://www.ise.ie/app/announcementDetails.aspx?ID=11370839</field>
			Element link = tds.get(2).getElementsByTag("a").first();
			String sURL = "http://www.ise.ie" + link.attr("href");
			isedn.setURL(sURL);
			logger.debug("set URL: {}", sURL);

			// <field name="content">
			isedn.setContent("[CONTENT NOT COLLECTED]");
			logger.debug("set content: {}", "[CONTENT NOT COLLECTED]");

			
			// <field name="cachefile">
			String sCacheFile = sUID + "_" + sDateTime.substring(0, sDateTime.indexOf("T")) + "_" + sURL.substring(sURL.indexOf("=")+1);
			isedn.setCacheFile(sCacheFile);
			logger.debug("set cacheFile: {}", sCacheFile);
			
			logger.info("Scraped metadata for: {} ({} / {})", isedn.getCompany(), isedn.getDateTime(), sCacheFile);
			
			// convert the ISEDOMNode into a node that can be appended to our temp <indexPage> DOM
			org.w3c.dom.Node documentFragmentNode = doc.importNode(isedn.getNodeSet(), true);
			rootElement.appendChild(documentFragmentNode);
		}
		//create a new documentFragment which will be an XML chunk of the ENTIRE INDEX PAGE (e.g. multiple <doc> elements) to insert into the big XML file but no root elements
		DocumentFragment indexPageData = doc.createDocumentFragment();

		//iterate over the temp <indexPage> DOM and build up a document fragment with each of the <doc> childNodes
		org.w3c.dom.NodeList nodeList = rootElement.getChildNodes();
		// for a we need to use this while loop as a for loop iterating over the node list doesn't work (it reduces in size so you can't test the size each iteration, and for the same reason a fixed limit at the start would go passed the size limit as it reduced)
		while(nodeList.getLength() > 0) {
			org.w3c.dom.Node childNode = nodeList.item(0);
			indexPageData.appendChild(childNode);
		}
		logger.debug("Final: indexPageData nodelength = {}", indexPageData.getChildNodes().getLength());
		return indexPageData;
	}
	
	// uses jSoup to get an URL but retries 3 times if there is a timeout
	private Document jsoupGetURL(String fetchURL) {
		Document page = null;
		for (int i=0;i<3;i++) {
			try {
				logger.info("Fetching: {} ", fetchURL);
				page = Jsoup.connect(fetchURL).timeout(15*1000).get(); //worry here about read timeouts
				break;
			}
			catch (SocketTimeoutException e) { logger.warn("jsoup Timeout occurred " + i + " time(s)"); }
			catch (IOException e) { logger.catching(e); }
		}
		if (page == null) {
			//push to error url stack
		}
		else {
			return page;
		}
		return page;
	}
	
}