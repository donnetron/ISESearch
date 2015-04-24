package isespider;

import java.io.*;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//class extends Properties so we can use a quick read/write to file method already set up when configured.
public class ISEProperties extends Properties {
	Logger logger = LogManager.getLogger(this.getClass().getName());
	private Path configFileLocation;

	// write to file taking file path as an argument
	public void write(Path confFile) {
		logger.debug("Writing config file: {}", confFile);
		try {
			OutputStream out = new FileOutputStream(confFile.toString());
			store(out, null);
			out.close();
			logger.debug("Wrote: {} ", confFile.toString());
		}
		catch (IOException e) { logger.catching(e); }
	}

	//write to file (uses configFileLocation as automatic file path)
	public void write() {
		logger.debug("Writing config file: {}", configFileLocation);
		try {
			OutputStream out = new FileOutputStream(configFileLocation.toString());
			store(out, null);
			out.close();
			logger.debug("Wrote: {} ", configFileLocation.toString());
		}
		catch (IOException e) { logger.catching(e); }
	}

	//read from file taking file path as an argument
	public void read(Path confFile) {
		logger.debug("Reading config file: {}", confFile);
		try {
			InputStream in = new FileInputStream(confFile.toString());
			load(in);
			in.close();
			logger.debug("Read: {} ", confFile.toString());
		}
		catch (IOException e) { logger.catching(e); }
	}

	// read from file (uses configFileLocation as automatic file path)
	public void read() {
		logger.debug("Reading internally set config file: {}", configFileLocation);
		try {
			InputStream in = new FileInputStream(configFileLocation.toString());
			load(in);
			in.close();
			logger.debug("Read: {} ", configFileLocation.toString());
		}
		catch (IOException e) { logger.catching(e); }
	}

	//print the key values 
	public void printKeyValues() {
		logger.info("Key (Value) pairs");
		logger.info("-----------------");
		Enumeration<?> e = keys();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			String value = this.getProperty(key);

			if (value.equals("")) { value = "null"; }
			logger.info("Key: {} ({})", key, value);
		}
		logger.info("-----------------");
	}

	//override to sort properties file
    public synchronized Enumeration<Object> keys() {
        return Collections.enumeration(new TreeSet<Object>(super.keySet()));
    }
	
	//set/access methods for a config file
	public void setConfigFileLocation(String confFile) {
		logger.debug("Setting internal config file: {}", confFile);
		configFileLocation = Paths.get(confFile);
	}
	
	//return string path to config file
	public String getConfigFileLocation() {
		return configFileLocation.toString();
	}

}