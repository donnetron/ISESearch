package isespider;

import java.io.*;

import javax.xml.parsers.*;
import javax.xml.stream.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BigXMLFileHandler {
	Logger logger = LogManager.getLogger(this.getClass().getName());
	
	private BasicFileHandler bfh;
	private XMLStreamWriter writer;
	private XMLStreamReader reader;
	
	private String rootElement;
	private String currentOutputFile;
	private String currentInputFile;
	private BufferedReader input;
	private BufferedWriter output;
	private Transformer transformer;
	private Document doc;

	// constructor for input file only
	public BigXMLFileHandler(String inputFile) {
		bfh = new BasicFileHandler(); 

		currentInputFile = inputFile;
		connectInputStream(currentInputFile);
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		}
		catch (ParserConfigurationException e) { logger.catching(e); }
		
		try {
			transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		}
		catch (IllegalArgumentException | TransformerConfigurationException e) { logger.catching(e); }

	}
	
	// constructor for output file only
	public BigXMLFileHandler(String outputFile, String rootElement) {
		bfh = new BasicFileHandler(); 
		this.rootElement = rootElement;
		
		if (bfh.checkFileExists(outputFile)) { currentOutputFile = bfh.createIncrementedFile(outputFile); }
		else { currentOutputFile = outputFile; }

		connectOutputStream(currentOutputFile);

		try {
			transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		}
		catch (IllegalArgumentException | TransformerConfigurationException e) { logger.catching(e); }
	}

	// constructor for input file and output file
	public BigXMLFileHandler(String inputFile, String outputFile, String rootElement) {
		this(outputFile, rootElement);
		currentInputFile = inputFile;
		connectInputStream(currentInputFile);
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		}
		catch (ParserConfigurationException e) { logger.catching(e); }
	}
	// resets the 'cursor' in the XML document so we are starting again (essentially by reconnection to the file)
	public void resetReader() { connectInputStream(currentInputFile); }

	// add element to the XML file by transforming a DocumentFragment into the stream
	public void addElement(DocumentFragment doc) {
		try {
			//USING STREAMRESULT AS StAXResult WILL IGNORE TRANSFORMER PROPERTY
			transformer.transform(new DOMSource(doc), new StreamResult(output));
		}
		catch (TransformerException e) { logger.catching(e); }
	}

	// returns a DocumentFragment from the XML which is the 'next' fragment with elementTag
	public DocumentFragment getNextElement(String elementTag) {
		try {
			DocumentFragment frag = doc.createDocumentFragment();			

			while (reader.hasNext()) {
				//logger.debug(printEventType(reader.getEventType()));
				if (reader.getEventType() == XMLStreamReader.START_ELEMENT && reader.getLocalName().equals(elementTag)) {
					//logger.debug("Found <{}>", elementTag);
					transformer.transform(new StAXSource(reader), new DOMResult(frag));
					return frag;
				}
				reader.next();
			}
			return frag;
		}
		catch (XMLStreamException | TransformerException e) { logger.catching(e); }
		return null;
	}

	// returns the number of elements in the file with elementTag
	public int countElements(String elementName) {
		logger.debug("Counting number of <{}> elements", elementName);
		int numberOfElements = -1;		
		if (reader != null) {
			numberOfElements = 0;
			try {
				while (reader.hasNext()) {
					reader.next();
					if  (reader.getEventType() == XMLStreamReader.START_ELEMENT) {
						if (reader.getLocalName().equals(elementName)) {
							numberOfElements++;
						}
					}
				}
			}
			catch (XMLStreamException e) { logger.catching(e); }
		}
		else {
			logger.warn("Unable to count <{}> elements (no input file given)", elementName);
		}
		logger.debug("{} <{}> elements counted", numberOfElements, elementName);
		// resets the cursor to the beginning of the file
		connectInputStream(currentInputFile);
		return numberOfElements;
	}

	// check if the current outputFile has reached the specified threshold (in bytes) and create an incremented file if needed
	public String checkIncrement(String maxFileSize) {
		logger.debug("Check/create increment file for {}", currentOutputFile);
		
		long fileSizeThreshold = Long.parseLong(maxFileSize);
		long fileSize = bfh.getFileSize(currentOutputFile);
		
		logger.debug("Current size of {} = {}MB ({} bytes)", currentOutputFile, fileSize/1024/1024, fileSize);
		logger.debug("Threshold = {}MB ({} bytes)", fileSizeThreshold/1024/1024, fileSizeThreshold);

		if (fileSize > fileSizeThreshold) {
			logger.debug("Threshold surpassed!");
			closeFile();
			currentOutputFile = bfh.createIncrementedFile(currentOutputFile);
			try {
				connectOutputStream(currentOutputFile);
			}
			catch (Exception e) { logger.catching(e); }
		}
		return currentOutputFile;
	}

	// close and write the file
	public void closeFile() {
		logger.debug("Wrote to file and closed");
		try {
			writer.writeEndElement();
			writer.flush();
			writer.close();
		}
		catch (XMLStreamException e) { logger.catching(e); }
	}
	
	// get current file that is being written to
	public String getCurrentOutputFile() {
		return currentOutputFile;
	}

	// open the current XML file being written to
	private void connectOutputStream(String newOutputFile) {
		logger.debug("Connecting OutputStream... {}", newOutputFile);
		try {
			XMLOutputFactory factory = XMLOutputFactory.newInstance();
			output = new BufferedWriter(new OutputStreamWriter(bfh.getOutputStream(newOutputFile)));
			writer = factory.createXMLStreamWriter(output);
			writer.writeStartDocument(); 
			writer.writeStartElement(rootElement);
			writer.writeCharacters("\n");
		}
		catch (XMLStreamException e) { logger.catching(e); }
	}

	// open the current XML file being read
	private void connectInputStream(String newInputFile) {
		logger.debug("Connecting InputStream... {}", newInputFile);
		try {
			XMLInputFactory factory = XMLInputFactory.newInstance();
			input = new BufferedReader(new InputStreamReader(bfh.getInputStream(newInputFile)));
			reader = factory.createXMLStreamReader(input);
			//checkRoot();
		}
		catch (XMLStreamException e) { logger.catching(e); }
	}

	// debug method to print the event type the XML reader is parsing
	public String printEventType(int event) {
		switch (event) {
			case 10:
				return "ATTRIBUTE";

			case 12:
				return "CDATA";
					
			case 4:
				return "CHARACTERS";
						
			case 5:
				return "COMMENT";
		
			case 11:
				return "DTD";

			case 8:
				return "END_DOCUMENT";

			case 2:
				return "END_ELEMENT";

			case 15:
				return "ENTITY_DECLARATION";

			case 9:
				return "ENTITY_REFERENCE";

			case 13:
				return "NAMESPACE";

			case 14:
				return "NOTATION_DECLARATION";
		
			case 3:
				return "PROCESSING_INSTRUCTION";

			case 6:
				return "SPACE";

			case 7:
				return "START_DOCUMENT";

			case 1:
				return "START_ELEMENT";
		}
		return "error - no code";
	}

	//uses fully qualified class names to avoid importing everything for using just once
	public String getDocumentString(DocumentFragment doc) {
		try {
			Transformer tf = TransformerFactory.newInstance().newTransformer();
			tf.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			tf.setOutputProperty(OutputKeys.INDENT, "yes");
			java.io.Writer out = new java.io.StringWriter();
			tf.transform(new DOMSource(doc), new StreamResult(out));
			return(out.toString());
		}
		catch (TransformerException e) { logger.catching(e); }
		return null;
	}

}