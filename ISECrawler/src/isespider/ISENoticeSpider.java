package isespider;

import java.io.*;
import java.util.*;


import org.w3c.dom.DocumentFragment;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class ISENoticeSpider {
	Logger logger = LogManager.getLogger(this.getClass().getName());

	private BasicFileHandler bfh;
	private BigXMLFileHandler indexCrawlData;
	private ISEProperties prop;
	private List<String> indexFiles;
	private int noticesCollected;

	//Constructor
	ISENoticeSpider(ISEProperties prop) {
		logger.info("Initialising ISE Notice Spider");
		this.prop = prop;
		bfh = new BasicFileHandler();
		noticesCollected = 0;
	}
	
	public void crawl() {
		logger.info("-- Notice crawl --");
		logger.debug("Getting file list of 'incomplete' files");
		
		prop.setProperty("noticeSpiderActive", "true");
		prop.write();
		
		indexFiles = bfh.getFileList(prop.getProperty("dataProcessingDir"));

		// prints the index files found in the processing directory
		for (String fn : indexFiles) { logger.debug("Index datafile: {}", fn); }

		// iterate over the files in the processing directory to extract and populate notices
		for (String indexDataFilename : indexFiles) {
			String noticeDataFilename = bfh.getIncrementedFileName(prop.getProperty("lastNoticeDataFile"));
			
			processIndexFile(indexDataFilename, noticeDataFilename, 0);
		}
		
		logger.info("-- Notice crawl done --");
		logger.info("=======================");
		logger.info("Total notices cached (alltime): {}", prop.getProperty("lastUID"));
		logger.info("=======================");
		prop.write();

		repair();

		logger.info("");
		logger.info("=======================");
		logger.info("------ Finished -------");
		logger.info("=======================");		
		prop.setProperty("noticeSpiderActive", "false");


	}
	
	public void resume() {
		logger.info("-- Resuming notice crawl --");
		logger.debug("Getting last file parsed");

		BigXMLFileHandler lastNoticeDataFile = new BigXMLFileHandler(prop.getProperty("lastNoticeDataFile"));
		DocumentFragment firstDocElement = lastNoticeDataFile.getNextElement("doc");
		ISEDOMNotice firstISEdn = new ISEDOMNotice(firstDocElement);

		int lastSavedNoticeUID = Integer.parseInt(firstISEdn.getUID()) - 1;
		logger.debug("Last saved notice UID was {}", lastSavedNoticeUID);
		
		logger.debug("Deleting: {}", prop.getProperty("lastNoticeDataFile"));
		bfh.deleteFile(prop.getProperty("lastNoticeDataFile"));
		
		indexFiles = bfh.getFileList(prop.getProperty("dataProcessingDir"));
		// since we are in resume mode, do one pass through the list of "processing" data files and do the remainder of the first file...
		
		String resumeNoticeDataFileName = prop.getProperty("lastNoticeDataFile");
		String resumeIndexDataFileName = indexFiles.get(0);
		
		processIndexFile(resumeIndexDataFileName, resumeNoticeDataFileName, lastSavedNoticeUID);

		crawl();
	}
	

	private void processIndexFile(String indexFileName, String noticeFileName, int startUID) {
		BigXMLFileHandler noticeCrawlData = new BigXMLFileHandler(indexFileName, noticeFileName, "add");
		
		prop.setProperty("lastNoticeDataFile", noticeCrawlData.getCurrentOutputFile());

		// count the number of <doc> elements in an XML file. This will be the number of notice contents that we need to get
		// method will reset the reader to the beginning of the file so we don't need to worry about doing this
		int docElements = noticeCrawlData.countElements("doc");

		while (docElements > 0) {
			DocumentFragment docElement = noticeCrawlData.getNextElement("doc");
			ISEDOMNotice isedn = new ISEDOMNotice(docElement);

			//get the UID of the notice
			int noticeUID = Integer.parseInt(isedn.getUID());
			
			//below test is for a resume (if there is an integer startUID, don't process lower UIDs. For a plain crawl, startUID is 0 
			if (noticeUID > startUID) {

				//pull the notice URL out from the DOM fragment
				String noticeURL = isedn.getURL();
				
				//construct a filename based on the noticeURL
				String cacheFileName = prop.getProperty("cacheDir") + isedn.getDateTimeYear() + "/" + isedn.getCacheFile() + ".html";
				//logger.debug(cacheFileName);

				try {
					//download the file to disk
					bfh.saveURL(noticeURL, cacheFileName);
	
					prop.setProperty("lastNoticeURLCollected", noticeURL);
					prop.setProperty("lastCacheFile", cacheFileName);
					//open the file and parse it, then scrape just the contents and update the content element in the DOM fragment
					Document noticePage = Jsoup.parse(new File(cacheFileName), "UTF-8", "");
					String noticeText = scrapeNoticePage(noticePage);
					isedn.setContent(noticeText);
				}
				catch (IOException e) { logger.catching(e); }
				logger.info("Scraped notice: {}, saved: {}", isedn.getCompany(), cacheFileName);
				noticeCrawlData.addElement(isedn.getDocumentFragment());
				prop.setProperty("lastNoticeDataFile", noticeCrawlData.checkIncrement(prop.getProperty("maxDataFileSize")));
			}
			docElements--;
		}
		noticeCrawlData.closeFile();
		//delete the processed [incomplete] indexDataFile
		logger.debug("Deleting: {}", indexFileName);
		bfh.deleteFile(indexFileName);
	}

	public void repair() { 
		// if we are resuming a crawl, then find the very last notice data file that was written (assuming that this is an incomplete file)
		// go through it and get the first doc element's UID. We then know the last 'full data file' ended with a notice having the UID - 1, so later on we can do a check to see we are past that before we start writing again)
		
		logger.info("-- Repairing notice crawl --");
		logger.info("Analysing data files...");
		
		List<String> dataFiles = bfh.getFileList(prop.getProperty("dataDir"));
		List<String> brokenDataFiles = new ArrayList<>();
		
		//create a list of files with [CONTENT NOT COLLECTED] in them, which means they failed to find the notice for that entry
		for (String fileName : dataFiles) {
			if (bfh.contains(fileName, "[CONTENT NOT COLLECTED]") == true) {
				brokenDataFiles.add(fileName);
				logger.debug("{} contains missing notice(s)", fileName);
			}
		}
		
		int totalNumberOfBrokenNotices = 0;
		int totalNumberOfBrokenFiles = brokenDataFiles.size();
		
		if (totalNumberOfBrokenFiles == 0) {
			logger.info("No files to repair");
		}
		else {
			logger.info("Counted {} files to repair", totalNumberOfBrokenFiles);

			for (String brokenFileName : brokenDataFiles) {
				
				logger.info("Repairing: {} ...", brokenFileName);
			
				String repairFileName = bfh.getRepairFileName(brokenFileName);
				BigXMLFileHandler repairFile = new BigXMLFileHandler(brokenFileName, repairFileName, "add");
				int docElements = repairFile.countElements("doc");
				
				while (docElements > 0) {
					DocumentFragment docElement = repairFile.getNextElement("doc");
					ISEDOMNotice isedn = new ISEDOMNotice(docElement);
					if (isedn.getContent().equals("[CONTENT NOT COLLECTED]")) {
						totalNumberOfBrokenNotices++;
						//pull the notice URL out from the DOM fragment
						String noticeURL = isedn.getURL();
						
						//construct a filename based on the noticeURL
						String cacheFileName = prop.getProperty("cacheDir") + isedn.getDateTimeYear() + "/" + isedn.getCacheFile() + ".html";
						
						try {
							//download the file to disk (will overwrite previoulsy saved)
							bfh.saveURL(noticeURL, cacheFileName);
							//open the file and parse it, then scrape just the contents and update the content element in the DOM fragment
							Document noticePage = Jsoup.parse(new File(cacheFileName), "UTF-8", "");
							String noticeText = scrapeNoticePage(noticePage);
							isedn.setContent(noticeText);

							}
							catch (IOException e) { logger.catching(e); }
							logger.info("Scraped notice: {}, saved: {}", isedn.getCompany(), cacheFileName);
					}
					repairFile.addElement(isedn.getDocumentFragment());
					repairFile.checkIncrement(prop.getProperty("maxDataFileSize"));
					docElements--;
				}
			logger.info("Repaired: {} -> {} (fixed {} notices)", brokenFileName, repairFileName, totalNumberOfBrokenNotices);
			repairFile.closeFile();
			
			bfh.renameFile(brokenFileName, brokenFileName + "_broken");
			}
			
			
			//bit of a hack to rename the files without messing up the increment code...
			dataFiles = bfh.getFileList(prop.getProperty("dataDir"));
			List<String> repairDataFiles = new ArrayList<>();

			for (String fileName : dataFiles) {
				if (fileName.contains("repair")) { repairDataFiles.add(fileName); }
			}
			for (String oldFileName : repairDataFiles)	{
				String newFileName = oldFileName.replace("-repair", "");
					   newFileName = newFileName.substring(0, newFileName.lastIndexOf(".")) + "-repair" + ".xml";
				
				bfh.renameFile(oldFileName, newFileName);
			}
		}
		
		logger.info("-- Repair done --");
		logger.info("=======================");
		logger.info("Total number of data files repaired: {}", totalNumberOfBrokenFiles);
		logger.info("Total number of notices repaired: {}", totalNumberOfBrokenNotices);
		logger.info("=======================");
		
	}
	
	//takes all text out of a notice.hmtl file and strips all the whitespace to put into the XML file
	private String scrapeNoticePage(Document resultPage) {
		Element html = resultPage.select("html").first();
		
		String text = html.text();
		text = text.trim();
		text = text.replaceAll("\n", " ");
		text = text.replaceAll("\\\\n", " ");
		text = text.replaceAll("\\s", " "); //special regex to get all whitespace
		text = text.replaceAll(" +", " "); //special regex to get all whitespace
		
		return text;
	}
	
}