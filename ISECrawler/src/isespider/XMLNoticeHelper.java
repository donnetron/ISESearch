package isespider;

import java.io.*;
import java.util.*;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Iterator;

import org.w3c.dom.DocumentFragment;
import org.w3c.dom.*;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class XMLNoticeHelper {

	static Logger XMLHelperLogger = LogManager.getLogger("XMLHelperLogger");
	static String logFile  = new String("log4j2.xml");

	static boolean checkUID = false;
	static boolean merge = false;
	static boolean splitYear = false;
	static String splitFileSize = "0";

	static String input = ".";
	static String output = "mergefile.xml";


	public static void main(String[] args) {
		//initialise timer to show length of runtime at end
		final long startTime = System.currentTimeMillis();

		XMLHelperLogger.info("XML Notice Helper started");
		XMLHelperLogger.info("=========================");
		
		//check command line arguments to see if we are merging or splitting a file(s). Check for input/output files
		checkCLI(args);
		
		XMLHelperLogger.debug("input was: {}", input);

		//set input and output variables to run merge against
		String outputFile	= output;
		String[] inputFile	= setInputFile(input);

		if (merge == true) { //if we are merging files, set the input directory/wildcards and set the output.xml
			mergeXMLFiles(inputFile, outputFile);
		}
		else if (splitYear == true) {
			splitXMLFileByYear(inputFile, outputFile);
		}
		else if (!splitFileSize.equals("0")) {
			splitXMLFileBySize(inputFile, outputFile, splitFileSize);
		}
		else if (checkUID == true) {
			checkUID(inputFile);
		}

		//calculate runtime and output as seconds
		final long duration = System.currentTimeMillis() - startTime;
		XMLHelperLogger.info("Runtime: " + duration/1000 + " seconds");
	}
	

	static String[] setInputFile(String input) {
		XMLHelperLogger.debug("input: {}", input);
		
		File dir = new File(FilenameUtils.getPath(input));
		XMLHelperLogger.debug("input dir: {}", dir);
		
		FilenameFilter filenameFilter = new WildcardFileFilter(FilenameUtils.getName(input));
		File[] files = dir.listFiles(filenameFilter);

		String[] paths = new String[files.length];
		for (int i=0; i<files.length; i++) { paths[i] = files[i].getPath(); }

		Arrays.sort(paths);

		XMLHelperLogger.debug("Total files: {}", paths.length); 
		for (String item : paths) { XMLHelperLogger.debug("Filename: {}", item); }

		return paths;
	}

	static void mergeXMLFiles(String[] input, String output) {
		XMLHelperLogger.info("Creating file:" + output);
		BigXMLFileHandler outputData = new BigXMLFileHandler(output, "add");	//new empty (temp object for pushing a DocumentFragment into the file for that year

		// for each input file, loop through the <doc> elements and add each fragment to the output file
		for (int i=0; i<input.length; i++) {
			XMLHelperLogger.info("Processing: " + input[i]);

			// new input reader for this file
			BigXMLFileHandler inputData = new BigXMLFileHandler(input[i]);

			// count the number of <doc> elements in an XML file.
			int docElements = inputData.countElements("doc");

			// loop through all the <doc> fragments 
			while (docElements > 0) {
				DocumentFragment docElement = inputData.getNextElement("doc");
				ISEDOMNotice isedn = new ISEDOMNotice(docElement);
				outputData.addElement(docElement);
				docElements--;
			}
		}
		outputData.closeFile();
	}


	static void splitXMLFileByYear(String[] input, String output) {
		BigXMLFileHandler outputData;	//new empty (temp object for pushing a DocumentFragment into the file for that year

		//use a map of XML files where the key corresponds to the year
		HashMap<String, BigXMLFileHandler> yearFiles = new HashMap<String, BigXMLFileHandler>();

		// for each input file, loop through the <doc> elements and add each fragment to the appropriate year.xml file
		for (int i=0; i<input.length; i++) {
			
			// new input reader for this file
			BigXMLFileHandler inputData = new BigXMLFileHandler(input[i]);

			// count the number of <doc> elements in an XML file.
			int docElements = inputData.countElements("doc");

			// loop through all the <doc> fragments 
			while (docElements > 0) {
				DocumentFragment docElement = inputData.getNextElement("doc");
				ISEDOMNotice isedn = new ISEDOMNotice(docElement);

				//get the year of the notice
				String year = isedn.getDateTimeYear();
		
				//if the globap map of year.xml files does NOT already contain a key for this year, then create a new outputData file object and put it in the map
				if (!yearFiles.containsKey(year)) {
					XMLHelperLogger.debug("yearFiles does NOT contain: {}", year);
					yearFiles.put(year, new BigXMLFileHandler(output + "_" + year + ".xml", "add"));
				}

				outputData = yearFiles.get(year);
				outputData.addElement(docElement);

				docElements--;
			}
		}
		//close all of the year.xml files (write close root element)
		yearFiles.forEach((year, yearFile) -> yearFile.closeFile());

	}

	static void splitXMLFileBySize(String[] input, String output, String fileSize) {
		XMLHelperLogger.info("Creating file:" + output);
		BigXMLFileHandler outputData = new BigXMLFileHandler(output, "add");	//new empty (temp object for pushing a DocumentFragment into the file for that year

		// for each input file, loop through the <doc> elements and add each fragment to the output file
		for (int i=0; i<input.length; i++) {
			XMLHelperLogger.info("Processing: " + input[i]);

			// new input reader for this file
			BigXMLFileHandler inputData = new BigXMLFileHandler(input[i]);

			// count the number of <doc> elements in an XML file.
			int docElements = inputData.countElements("doc");

			// loop through all the <doc> fragments 
			while (docElements > 0) {
				
				if (docElements % 10 == 0) {
					outputData.checkIncrement(fileSize);
				}
				
				DocumentFragment docElement = inputData.getNextElement("doc");
				ISEDOMNotice isedn = new ISEDOMNotice(docElement);
				outputData.addElement(docElement);
				docElements--;
			}
		}
		outputData.closeFile();
	}

	static void checkUID(String[] input) {
		
		int noticeUID = 0;
		int prevNoticeUID = 0;

		// for each input file, loop through the <doc> elements and add each fragment to the output file
		for (int i=0; i<input.length; i++) {
			XMLHelperLogger.info("Processing: " + input[i]);

			// new input reader for this file
			BigXMLFileHandler inputData = new BigXMLFileHandler(input[i]);

			// count the number of <doc> elements in an XML file.
			int docElements = inputData.countElements("doc");
			boolean firstElement = true;
			
			// loop through all the <doc> fragments 
			while (docElements > 0) {
				
				DocumentFragment docElement = inputData.getNextElement("doc");
				ISEDOMNotice isedn = new ISEDOMNotice(docElement);

				if (firstElement == true) {	prevNoticeUID = Integer.parseInt(isedn.getUID())-1; firstElement = false;	}

				noticeUID = Integer.parseInt(isedn.getUID());

				if (isedn.getContent().equals("[CONTENT NOT COLLECTED]")) {
					XMLHelperLogger.info("UID {} is incomplete [CONTENT NOT COLLECTED]", isedn.getUID());
				}
				if (prevNoticeUID+1 != noticeUID) {
					int difference = noticeUID - prevNoticeUID;
					XMLHelperLogger.info("UID {} jumped {} spaces to UID {}", prevNoticeUID, difference, noticeUID);
				}
				
				if (noticeUID % 1000 == 0) {
					XMLHelperLogger.info("Processed 1000 files (to UID {})", noticeUID);
				}
				prevNoticeUID = noticeUID;
	
				docElements--;
			}
		}
		XMLHelperLogger.info("Done (finished at UID: {})", noticeUID);
	}

	static void checkCLI(String[] args) {
		// create the command line parser
		CommandLineParser parser = new GnuParser();

		// create the Options
		Options options = new Options();
		options.addOption("c", "merge", false, "check sequential notice UID in files");
		options.addOption("m", "merge", false, "merge files into one output file");
		options.addOption("sy", "splityear", false, "split input file by year");
		options.addOption("sf", "splitfilesize", true, "split input file by filesize in bytes, kB, MB, or GB");
		options.addOption("o", "output", true, "output filename or directory");
		options.addOption("i", "input", true, "input filename or directory (wildcards acceptable, e.g. *.xml)");
	
		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);

			if (line.hasOption("c"))  { checkUID = true; }
			if (line.hasOption("m"))  { merge = true; }
			if (line.hasOption("sy")) { splitYear = true; }
			if (line.hasOption("sf")) { splitFileSize = line.getOptionValue("sf");}
			if (line.hasOption("o"))  { output = line.getOptionValue("o"); }
			if (line.hasOption("i"))  { input = line.getOptionValue("i"); }
		}
		catch(ParseException e) {
			XMLHelperLogger.catching(e);
		}
	}

}