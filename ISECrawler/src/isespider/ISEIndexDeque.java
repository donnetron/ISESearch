package isespider;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import java.time.*;
import static java.time.temporal.TemporalAdjusters.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* 
creates a deque (double ended queue) of all the index pages to scrape by the IndexSpider
supply the start and end date and it gives the whole stack (so a full crawl, 2003->2015 would give you a deque of over 9000 items)
E.g. 
1. http://www.ise.ie/Market-Data-Announcements/Announcements/?ACTIVEGROUP=40&START_DATE_DAY=1&START_DATE_MONTH=1&START_DATE_YEAR=2003&END_DATE_DAY=31&END_DATE_MONTH=1&END_DATE_YEAR=2003 <- oldest (1 Jan 2003) 
2. http://www.ise.ie/Market-Data-Announcements/Announcements/?ACTIVEGROUP=39&START_DATE_DAY=1&START_DATE_MONTH=1&START_DATE_YEAR=2003&END_DATE_DAY=31&END_DATE_MONTH=1&END_DATE_YEAR=2003 <- older (lower actv grp)
3. http://www.ise.ie/Market-Data-Announcements/Announcements/?ACTIVEGROUP=38&START_DATE_DAY=1&START_DATE_MONTH=1&START_DATE_YEAR=2003&END_DATE_DAY=31&END_DATE_MONTH=1&END_DATE_YEAR=2003 <- old (lower actv grp)
4. http://www.ise.ie/Market-Data-Announcements/Announcements/?ACTIVEGROUP=37&START_DATE_DAY=1&START_DATE_MONTH=1&START_DATE_YEAR=2003&END_DATE_DAY=31&END_DATE_MONTH=1&END_DATE_YEAR=2003 <- newer (lower actv grp)
.
.
.
93.  http://www.ise.ie/Market-Data-Announcements/Announcements/?ACTIVEGROUP=1&START_DATE_DAY=1&START_DATE_MONTH=2&START_DATE_YEAR=2003&END_DATE_DAY=28&END_DATE_MONTH=2&END_DATE_YEAR=2003 <- newest (next month)
*/
public class ISEIndexDeque {
	Logger logger = LogManager.getLogger(this.getClass().getName());
	
	private Deque<String> indexURLDeque;  
	
	//Constructor
	ISEIndexDeque(LocalDate searchBeginDate, LocalDate searchEndDate) {

		indexURLDeque = new ArrayDeque<String>();
	
		//if the period between the start and end date is LESS THAN OR EQUAL to 1 month, create a deque for the full search and push in the active group hierarchy 
		if (Period.between(searchBeginDate, searchEndDate).toTotalMonths() <= 1) {
			logger.debug("less than 1 month search period");

			String searchURL = getSearchURL(searchBeginDate, searchEndDate, 1);		//construct an URL for the date, with the first active group
			int maxActiveGroup = getMaxActiveGroup(searchURL);						//use that URL to get the highest active group for that search URL
			
			for (int i=1; i<=maxActiveGroup; i++) {	//loop through the active group numbers from 1 to highest number (note we start at 1, not 0, and INCLUDE the maxActiveGroup number)
				searchURL = getSearchURL(searchBeginDate, searchEndDate, i); //create a search URL using the same dates each time just the next activeGroup
				indexURLDeque.addFirst(searchURL); //push the search URL to the front of the deque. Once the deque is full it starts with the oldest at the front of the queue (e.g. highest active group)
			}
		}
		//else if the period is greater than one month, we make a deque full of searches that are spaced by 1 month
		else {
			logger.debug("greater than 1 month search period");
			LocalDate finalEndDate = searchEndDate;	//make a new FINAL end date so we don't go passed this, e.g. 1 Jan 2015
			searchEndDate = searchBeginDate.with(lastDayOfMonth()); //searchEndDate will be adjusted to be the end of the month, e.g. 31 Jan 2003 (so an interim end date)
			
			do {
				String searchURL = getSearchURL(searchBeginDate, searchEndDate, 1);		//construct an URL for the date, with the first active group
				logger.debug("searchURL is: {}", searchURL);
				int maxActiveGroup = getMaxActiveGroup(searchURL);						//use that URL to get the highest active group for that search URL
				
				for (int i=maxActiveGroup; i>0; i--) {	//loop through the active group numbers from highest number down to 1 (note we INCLUDE the maxActiveGroup number and finish at 1, not 0)
					logger.debug("maxActiveGroup = {}, i = {}", maxActiveGroup, i);

					searchURL = getSearchURL(searchBeginDate, searchEndDate, i); //create a search URL using the same dates each time just the next activeGroup
					indexURLDeque.addLast(searchURL); //push the search URL to the END of the deque. Once the deque is full it starts with the oldest notices at the front of the queue (e.g. highest active group)
				}
				
				searchBeginDate = searchBeginDate.with(firstDayOfNextMonth()); //e.g. 1 Feb 2003
				searchEndDate = earlierOf(searchBeginDate.with(lastDayOfMonth()), finalEndDate);	//e.g. 28 Feb 2003
				
			} while (!searchBeginDate.isAfter(searchEndDate)); //check is if beginDate is NOT AFTER endDate. So loop isn't broken if two dates are equal
		}
	}
		
	public Deque<String> getIndexDeque() {
		return indexURLDeque;
	}
	
	private LocalDate earlierOf(LocalDate firstDate, LocalDate secondDate) {
		if (firstDate.isBefore(secondDate)) { return firstDate; }
		else { return secondDate; }
	}
	
	private String getSearchURL(LocalDate searchBeginDate, LocalDate searchEndDate, int activeGroup) {

		int startDay 	= searchBeginDate.getDayOfMonth();
		int startMonth 	= searchBeginDate.getMonthValue();
		int startYear 	= searchBeginDate.getYear();

		int endDay 		= searchEndDate.getDayOfMonth();
		int endMonth 	= searchEndDate.getMonthValue();
		int endYear 	= searchEndDate.getYear();

		// set up our search url that only gets a month at a time
		String searchURL =	"http://www.ise.ie/Market-Data-Announcements/Announcements/?ACTIVEGROUP=" + activeGroup +
							"&START_DATE_DAY=" + startDay +	"&START_DATE_MONTH=" + startMonth +	"&START_DATE_YEAR=" + startYear + 
							"&END_DATE_DAY=" + endDay + "&END_DATE_MONTH=" + endMonth + "&END_DATE_YEAR=" + endYear;
		
		logger.debug("getSearchURL() returned: {}", searchURL);
		return searchURL;
	}
	
	// parses the URL string to return the highest ACTIVEGROUP param for that search as an int
	private int getMaxActiveGroup(String searchURL) {
		
		String indexURL = "";
		int activeGroup = -1;
		
		Document indexFirstPage = jsoupGetURL(searchURL);
		
		try { indexURL = indexFirstPage.select("td.navigationPages a").last().attr("abs:href");	} //get the LAST URL in the navigationPages class (assuming this will be the URL to the last ACTIVEGROUP)
		catch (NullPointerException e) { logger.error("No elements found on this page! (index url: {} / search url: {})", indexURL, searchURL); }
		
		logger.debug("indexURL: {} ", indexURL);
		
		Pattern p = Pattern.compile("(.*ACTIVEGROUP=)(\\p{Digit}*)(.*)");
		Matcher m = p.matcher(indexURL);
		
		if (m.find()) {
			logger.debug("groupCount: {}", m.groupCount());
			activeGroup = Integer.parseInt(m.group(2));
			return activeGroup;
		}
		//else { ISECrawler.exit("Pattern Matcher failed"); }
		
		return 0;
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