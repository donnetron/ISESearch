package isespider;

import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class ISECrawler {

	static Logger ISECrawlerLogger = LogManager.getLogger("ISECrawlerLogger");
	static String logFile  = new String("conf/log4j2.xml");

	static String confFile = new String("conf/config.properties");
	static String beginDate = "";
	static String endDate = "";
	static boolean noConfirm = false;
	static boolean resumeCrawl = false;
	static boolean repairCrawl = false;
	static boolean update = false;
	
	public static void main(String[] args) {
		
		ISECrawlerLogger.info("ISE Crawler started");
		ISECrawlerLogger.info("==================");
		
		checkCLI(args);
		
		ISEConfigurator isec = new ISEConfigurator(confFile, beginDate, endDate); 
		ISEProperties	isep = isec.getISEProperties();
		
		// add shutdown hook after isep is created so properties can be written on ctrl-c
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				isep.write();
			}
		});

		if (update == true)		{ isec.setUpdate(); }
		if (noConfirm == false) { 
			if (repairCrawl == true)		{ isec.confirmSearch("Proceed with repair? (Y/n): "); }
			else if (resumeCrawl == true)	{ isec.confirmSearch("Resume search with these details? (Y/n): "); }
			else if (update == true)		{ isec.confirmSearch("Proceed with update? (Y/n): "); }
			else							{ isec.confirmSearch("Begin search with these details? (Y/n): "); }
		}

		/*************************************/
		/* START TRAWLING INDEX RESULT PAGES */
		/*************************************/
		ISESpider ises = new ISESpider(isep);

		if (repairCrawl == true) { ises.repair(); }
		else if (resumeCrawl == true) {	ises.resume(); }
		else { ises.crawl(); }


	}

	// global exit, e.g. ISECrawler.exit("");
	static void exit(String exitMessage) {
		ISECrawlerLogger.fatal(exitMessage);
		ISECrawlerLogger.fatal("Exiting...");
		System.exit(0);
	}
	
	static void checkCLI(String[] args) {
		// create the command line parser
		CommandLineParser parser = new GnuParser();

		// create the Options
		Options options = new Options();
		options.addOption("c", "config", true, "set location of config file");
		options.addOption("n", "noconfirm", false, "no prompt to confirm search");
		options.addOption("p", "repair", false, "repair data from previous calls");
		options.addOption("r", "resume", false, "resume processing from previously stopped crawl");
		options.addOption("u", "update", false, "update from the last crawl");
		options.addOption("b", "begindate", true, "start date for crawl (inclusive)");
		options.addOption("e", "enddate", true, "end date for crawl (inclusive)");
	
		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);

			if (line.hasOption("c")) { confFile = line.getOptionValue("c"); }
			if (line.hasOption("n")) { noConfirm = true; }
			if (line.hasOption("p")) { repairCrawl = true; }
			if (line.hasOption("r")) { resumeCrawl = true; }
			if (line.hasOption("u")) { update = true; }
			if (line.hasOption("b")) { beginDate = line.getOptionValue("b").replaceAll("[/. ]","-"); }
			if (line.hasOption("e")) { endDate = line.getOptionValue("e").replaceAll("[/. ]","-"); }
		}
		catch(ParseException e) {
			ISECrawlerLogger.catching(e);
		}
	}

}