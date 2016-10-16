package isespider;

import java.util.Scanner;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ISEConfigurator {
	int maxIndexPagesToCrawl;
	Logger logger = LogManager.getLogger(this.getClass().getName());
	
	private BasicFileHandler bfh;
	private ISEProperties prop;
	private String beginDate;
	private String endDate;
	private String[][] defaultKeySet = new String[][]{	{"backupDir", "backup/"},						// zip files in this directory
														{"dataDir", "data/"}, 							// all complete files in this directory
														{"dataProcessingDir", "data/incomplete/" },		// all incomplete files in this directory (e.g. index crawl files with unpopulated content)
														{"cacheDir", "cache/"},							// all downloaded notices here
														{"maxDataFileSize", "1048576"},					// 1MB in bytes
														{"lastCacheFile", ""},
														{"lastNoticeURLCollected", ""},
														{"lastIndexURLCollected", ""},
														{"lastIndexDataFile", "data/incomplete/indexdata_0000.xml"},
														{"lastNoticeDataFile", "data/noticedata_0000.xml"},
														{"lastUID", "0"},
														//{"indexPagesCrawled", ""},
														{"indexSpiderActive", "false" },
														{"noticeSpiderActive", "false" },
														{"searchBeginDate", "01-01-2003"},
														{"searchEndDate", "01-01-2003"},
													};

	// constructor for no path file, will create a new config file
	ISEConfigurator() {
		this("conf/config.properties","","");
	}

	// constructor where config file path is supplied
	ISEConfigurator(String confFile, String bd, String ed) {
		logger.info("Initialising (specified config settings: {})...", confFile);

		bfh = new BasicFileHandler();
		prop = new ISEProperties();

		beginDate = bd;
		endDate = ed;

		if (bfh.checkFileExists(confFile) == false) {
			logger.warn("Could not find config file: {}", confFile);
			confFile = initialiseConfigFile(confFile);
		}

		prop.setConfigFileLocation(confFile);
		prop.read(Paths.get(confFile));
		
		if (beginDate.equals("now")) { prop.setProperty("searchBeginDate", LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))); }
		else if (!beginDate.equals("")) { prop.setProperty("searchBeginDate", beginDate); }

		if (endDate.equals("now")) { prop.setProperty("searchEndDate", LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))); }
		else if (!endDate.equals("")) { prop.setProperty("searchEndDate", endDate); }
		
		checkProperties(confFile);
	}

	// method to create a new config file, and initialise new properties on system default
	// does not contemplate overwrite or bespoke cacheDir/dataDir settings
	private String initialiseConfigFile(String confFile) {
		
		// Ask user if we should create a new config file (system message, as requires user input)
		System.out.print("Create new config file using default settings at <" + confFile + ">? (Y/n):");
		Scanner scanner = new Scanner(System.in); 
		String input = scanner.nextLine();
		if (input.toUpperCase().equals("Y") || input.toUpperCase().equals("")) {
			logger.debug("USER INPUT: SELECTED \"YES\" TO CREATING NEW CONFIG FILE");
			
			initialiseProperties();
			initialiseDates(beginDate, endDate); 
			bfh.createFile(confFile);
			bfh.createDirectory(prop.getProperty("backupDir"));
			bfh.createDirectory(prop.getProperty("dataDir"));
			bfh.createDirectory(prop.getProperty("dataProcessingDir"));
			bfh.createDirectory(prop.getProperty("cacheDir"));
			prop.write(Paths.get(confFile));
			//prop.printKeyValues();
		}
		else {
			ISECrawler.exit("No config file created");
		}
		return confFile;
	}
	
	// initialise ISEProperties object with default values
	private void initialiseProperties() {
		logger.info("Setting default property key/values");
		// check that the properties file has a key for each item in the defaultKeySet
		for (String key[] : defaultKeySet) {
			logger.debug("Populating key: {} ({})", key[0], key[1]);
			prop.setProperty(key[0], key[1]);
		}
	}

	// initialise ISEProperties object with default values
	private void initialiseDates(String beginDate, String endDate) {
		// Ask user if we should create a new config file (system message, as requires user input)
		Scanner scanner = new Scanner(System.in); 
		String input = "";

		if (beginDate.equals("")) {
			System.out.print("Search start date (DD-MM-YYYY):");
			input = scanner.nextLine();
			input = input.replaceAll("/|\\.|\\\\", "-");
			if (!input.equals("")) { prop.setProperty("searchBeginDate", input); }
		}
		
		if (endDate.equals("")) {
			System.out.print("Search end date (DD-MM-YYYY):");
			input = scanner.nextLine();
			input = input.replaceAll("/|\\.|\\\\", "-");
			if (!input.equals("")) { prop.setProperty("searchEndDate", input); }
		}
	}

	// 1. checks properties file to make sure all the require key values are present
	// 2. checks the dataDir and cacheDir exist and are writeable
	// 3. santity check of search dates
	// X. does not check for additional unused keys
	// X. does not check the key values are sensible (e.g. a number where required)
	private void checkProperties(String confFile) {
		logger.info("Performing config file check...");
		
		boolean checkPass = true;
		
		// check that the properties file has a key for each item in the defaultKeySet
		for (String key[] : defaultKeySet) {
			logger.debug("Checking: {}", key[0]);
			if (prop.containsKey(key[0]) == false) {
				logger.fatal("Key not found: {}", key[0]);
				checkPass = false;
			}
		}
		logger.debug("All required property keys found");
		
		// check if all the necessary files exist and can be written
		logger.debug("Checking {} exists/is writeable", confFile);
		if (bfh.checkFileExists(confFile) == false) {
			logger.fatal("Could not find confFile: {}", confFile);
			checkPass = false;
		}
		else if (bfh.checkFileWritable(confFile) == false) {
			logger.fatal("Unwritable confFile: {}", confFile);
			checkPass = false;
		}

		String[] directories = new String[] { 	prop.getProperty("backupDir"),
												prop.getProperty("dataDir"),
												prop.getProperty("dataProcessingDir"),
												prop.getProperty("cacheDir")
											};

		for (String dir : directories) {
			if (bfh.checkFileExists(dir) == false) {
				logger.fatal("Could not find directory: {}", dir);
				checkPass = false;
			}
			else if (bfh.checkFileWritable(dir) == false) {
				logger.fatal("Unwritable directory: {}", dir);
				checkPass = false;
			}
		}

		// check the supplied search dates
		try {
			LocalDate ldBeginDate = LocalDate.parse(prop.getProperty("searchBeginDate"), DateTimeFormatter.ofPattern("dd-MM-yyyy"));
			LocalDate ldEndDate = LocalDate.parse(prop.getProperty("searchEndDate"), DateTimeFormatter.ofPattern("dd-MM-yyyy"));
			
			// fail if start date is before 1 Jan 2003 (the start of ISE record), or if end date is after "today's" date
			if ((ldBeginDate.isBefore(LocalDate.of(2003, 01, 01))) || (ldEndDate.isAfter(LocalDate.now()))) {
				logger.fatal("Search dates out of range: 01-01-2003 ({}) / today ({})", prop.getProperty("searchBeginDate"), prop.getProperty("searchEndDate"));
				checkPass = false;
			}

			// fail if start date is after end date
			if (ldBeginDate.isAfter(ldEndDate)) {
				logger.fatal("Search dates in wrong order: {} to {}", prop.getProperty("searchBeginDate"), prop.getProperty("searchEndDate"));
				checkPass = false;
			}
		}
		catch (DateTimeParseException e) {
			// this catch block is in case the dates do not match the DateTimeFormatter.ofPattern("dd-MM-yyyy")
			logger.fatal("Could not parse search dates (DD-MM-YYYY): {} / {}", prop.getProperty("searchBeginDate"), prop.getProperty("searchEndDate"));
			checkPass = false;
		}

		//if any of the above tests failed exit the program
		if (checkPass == false) { ISECrawler.exit("Config file check failed"); }
		else { logger.info("Config file check passed"); }
	}

	// return the properties object
	public ISEProperties getISEProperties() {
		return prop;
	}

	public void setUpdate() {
		LocalDate searchBeginDate = LocalDate.parse(prop.getProperty("searchEndDate"), DateTimeFormatter.ofPattern("dd-MM-yyyy")).plusDays(1);
		LocalDate searchEndDate = LocalDate.now();

		//check for a 1 day update that we don't have a search from <tomorrow> to <today> (note the plus 1 day at the end above)
		if (searchBeginDate.isAfter(searchEndDate)) {
			searchBeginDate = searchEndDate;
		}
		
		prop.setProperty("searchBeginDate", searchBeginDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
		prop.setProperty("searchEndDate", searchEndDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
		prop.write();
	}

	// print properties and prompt to begin crawl
	public void confirmSearch(String message) {
		Scanner scanner = new Scanner(System.in); 
	
		prop.printKeyValues();
		System.out.print(message);
		String input = scanner.nextLine();
		if ((input.toUpperCase().equals("Y")) || input.equals("")) { return; }
		else { ISECrawler.exit("Crawl halted"); }
	}

}