package isespider;

import java.io.*;
import java.nio.file.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BasicFileHandler {
	Logger logger = LogManager.getLogger(this.getClass().getName());

	// empty constructor as methods all require path string
	public BasicFileHandler() {	}

	// check if a file or directory exists
	public boolean checkFileExists(String filePath) {
		logger.debug("Checking file exists: {}", filePath);
		
		Path path = Paths.get(filePath);
		if (Files.exists(path)) { 
			logger.debug("Found: {}", filePath);
			return true;
		}
		else {
			logger.debug("Could not find: {}", filePath);
			return false;
		}
	}
	
	// check if a file or directory is writable
	public boolean checkFileWritable(String filePath) {
		logger.debug("Checking file is writable: {}", filePath);
		
		Path path = Paths.get(filePath);
		if (Files.isWritable(path)) { 
			logger.debug("Writable: {}", filePath);
			return true;
		}
			
		else {
			logger.debug("Not writable: {}", filePath);
			return false;
		}
	}
	
	// returns the size of a file (in bytes)
	public long getFileSize(String filePath) {
		Path path = Paths.get(filePath);
		try {
			return Files.size(path);
		}
		catch (IOException e) { logger.catching(e); return 0; }
	}

	// returns the size of a directory (in bytes)
	public long getDirectorySize(String filePath) {
		Path path = Paths.get(filePath);
		return FileUtils.sizeOfDirectory(path.toFile());
	}
	
	// get a new OutputStream based on filePath
	public OutputStream getOutputStream(String filePath) {
		
		Path path = Paths.get(filePath);
		try {
			return Files.newOutputStream(path);
		}
		catch (IOException e ) { logger.catching(e); return null; }
	}

	// get a new InputStream based on filePath
	public InputStream getInputStream(String filePath) {
		
		Path path = Paths.get(filePath);
		try {
			return Files.newInputStream(path);
		}
		catch (IOException e ) { logger.catching(e); return null; }
	}
	
	// create a new file (SEPARATE METHOD FOR DIRECTORY)
	public void createFile(String filePath) {
		
		Path path = Paths.get(filePath);
		if (!checkFileExists(filePath)) {
			try { 
				if (filePath.contains(File.separator)) {
					logger.debug("Creating directories for: {}", path.toString());
					Files.createDirectories(path.getParent()); 
				}
				Files.createFile(path);
				logger.info("Created file: {}", filePath);
			}
			catch (IOException e) { logger.catching(e); }
		}
	}

	// create a new directory (SEPARATE METHOD FOR FILE)
	public void createDirectory(String directoryPath) {
		
		Path path = Paths.get(directoryPath);
		if (!checkFileExists(directoryPath)) {
			try {
				Files.createDirectories(path);
				logger.info("Created directory: {}", directoryPath);
			} 
			//catch (NullPointerException npe) { }
			catch (IOException e) { logger.catching(e); }
		}
	}

	public void saveURL(String url, String filePath) {
		int maxRetries = 5;

		while (maxRetries >= 0) {
			try {
				//read connection timeout and read timeout 5 seconds
				FileUtils.copyURLToFile(new URL(url), new File(filePath),5000,5000);
				break;
			}
			catch (IOException e) {
				if (--maxRetries == 0) { 
					logger.catching(e);
				}
				else { 
					logger.info("{} failed to download. Retrying...", url);
					continue; 
				}
			}
		}
	}

	// create an incremented file based on the supplied filePath
	// so  /directory/file.xml 		-> /directory/file_0001.xml
	// and /directory/file_0001.xml -> /directory/file_0002.xml
	public String createIncrementedFile(String filePath) {
		
		String incrementFile;

		if (filePath.toLowerCase().contains("repair")) {
			incrementFile = getIncrementedRepairFileName(filePath);
		}
		else {
			incrementFile = getIncrementedFileName(filePath);
		}	
		logger.debug("Incremented filename is: {}", incrementFile); 
		
		createFile(incrementFile);
		return incrementFile;
	}

	// create an incremented file based on the supplied filePath
	public String getRepairFileName(String filePath) {
		
		Path path = Paths.get(filePath);
		String	fileName;
		String	fileDirectory;

		fileName = path.getFileName().toString();
		if (path.getParent() == null) { fileDirectory = ""; }
		else { fileDirectory = path.getParent().toString() + "/"; }
		
		String newFileName = fileName.substring(0, fileName.lastIndexOf("_")) + "-repair" + fileName.substring(fileName.lastIndexOf("_"));
		String repairFile = fileDirectory + newFileName;

		return repairFile;
	}
	
	
	public String getIncrementedFileName(String filePath) {
		
		Path path = Paths.get(filePath);
		
		String	fileName;
		String	fileDirectory;
		String	fileExtension; 
		int 	fileNumber;
		String	incrementedFilePath;
		
		// set up the fileName
		fileName = path.getFileName().toString();
		logger.debug("fileName is: {}", fileName); 

		// set up the fileDirectory
		if (path.getParent() == null) { fileDirectory = ""; }
		else { fileDirectory = path.getParent().toString() + "/"; }
		logger.debug("fileDirectory is: {}", fileDirectory); 
		
		logger.debug("Determining incremented filename...");

		fileExtension = "." + FilenameUtils.getExtension(fileName);
		logger.debug("fileExtension is: {}", fileExtension);

		int fileNameUnderscoreIndex = fileName.lastIndexOf("_");
		logger.debug("fileNameUnderscoreIndex is: {}", fileNameUnderscoreIndex);
		
		if (fileNameUnderscoreIndex != -1)  {
			String tempFileNumber = fileName.substring(fileNameUnderscoreIndex);
			logger.debug("tempFileNumber is: {}", tempFileNumber);
			tempFileNumber = tempFileNumber.replaceAll("\\D", "");
			logger.debug("tempFileNumber is now: {}", tempFileNumber);
			fileNumber = Integer.parseInt(tempFileNumber);
			// chop down the fileName so it is just the name without the number and without the extension
			fileName = fileName.substring(0, fileName.lastIndexOf("_"));
			logger.debug("fileName is: {}", fileName);
		}
		else { fileNumber = 0; }
		logger.debug("fileNumber is: {}", fileNumber);
		logger.debug("fileName is: {}", fileName);
		
		// do/while loop so we advance the number at least 1 time.
		do {
			fileNumber++;
			incrementedFilePath = fileDirectory + fileName + "_" + String.format("%04d", fileNumber) + fileExtension;
			logger.debug("incrementedFilePath is: {}", incrementedFilePath);
		}
		while (checkFileExists(incrementedFilePath));
				
		logger.debug("Final incrementedFilePath is: {}", incrementedFilePath);
		logger.info("Created incremented file: {} -> {}", filePath, incrementedFilePath);
		return incrementedFilePath;
	}

	public String getIncrementedRepairFileName(String filePath) {
	Path path = Paths.get(filePath);
		
		String	fileName;
		String	fileDirectory;
		String	fileExtension; 
		int 	fileNumber;
		String	incrementedFilePath;
		
		// set up the fileName
		fileName = FilenameUtils.getBaseName(filePath);
		logger.debug("fileName is: {}", fileName); 

		// set up the fileDirectory
		if (path.getParent() == null) { fileDirectory = ""; }
		else { fileDirectory = path.getParent().toString() + "/"; }
		logger.debug("fileDirectory is: {}", fileDirectory); 
		
		logger.debug("Determining incremented filename...");

		fileExtension = "." + FilenameUtils.getExtension(filePath);
		logger.debug("fileExtension is: {}", fileExtension);

		int fileNameUnderscoreIndex = fileName.lastIndexOf("_");
		logger.debug("fileNameUnderscoreIndex is: {}", fileNameUnderscoreIndex);
		
		if (fileNameUnderscoreIndex > 18)  {
			String tempFileNumber = fileName.substring(fileNameUnderscoreIndex);
			logger.debug("tempFileNumber is: {}", tempFileNumber);
			tempFileNumber = tempFileNumber.replaceAll("\\D", "");
			logger.debug("tempFileNumber is now: {}", tempFileNumber);
			fileNumber = Integer.parseInt(tempFileNumber);
			// chop down the fileName so it is just the name without the number and without the extension
			fileName = fileName.substring(0, fileName.lastIndexOf("_"));
			logger.debug("fileName is: {}", fileName);
		}
		else { fileNumber = 0; }
		logger.debug("fileNumber is: {}", fileNumber);
		logger.debug("fileName is: {}", fileName);
		
		// do/while loop so we advance the number at least 1 time.
		do {
			fileNumber++;
			incrementedFilePath = fileDirectory + fileName + "_" + String.format("%02d", fileNumber) + fileExtension;
			logger.debug("incrementedFilePath is: {}", incrementedFilePath);
		}
		while (checkFileExists(incrementedFilePath));
				
		logger.debug("Final incrementedFilePath is: {}", incrementedFilePath);
		logger.info("Created incremented file: {} -> {}", filePath, incrementedFilePath);
		return incrementedFilePath;
	}

	public void zipDirectory(String sourceDirectory, String destPath) {
		byte[] buffer = new byte[1024];

		List<String> fileNames = getFileList(sourceDirectory);

		if (!FilenameUtils.getExtension(destPath).equals("zip")) {
			logger.debug("Setting file {} to {}", destPath, destPath + ".zip");
			destPath = destPath + ".zip";
		}

		if (checkFileExists(destPath)) {
			destPath = createIncrementedFile(destPath);
		}

		try {
			FileOutputStream fos = new FileOutputStream(destPath);
			ZipOutputStream zos = new ZipOutputStream(fos);
			zos.setMethod(ZipOutputStream.DEFLATED);
			zos.setLevel(9);
		
			logger.info("Zipping directory: {}", sourceDirectory);
			
			for(String file : fileNames) {
				logger.debug("Adding to zip: {}", file);
				ZipEntry ze = new ZipEntry(file);
				zos.putNextEntry(ze);

				FileInputStream in = new FileInputStream(file);

				int len;
				while ((len = in.read(buffer)) > 0) {
					zos.write(buffer, 0, len);
				}
				in.close();
			}
			zos.closeEntry();
			zos.close();
			
			logger.info("Saving file: {}", destPath);
		}
		catch (IOException e) { logger.catching(e); }

	}

	public List<String> getFileList(String directory) {
		List<String> fileNames = new ArrayList<>();
		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directory))) {
			for (Path path : directoryStream) {
				fileNames.add(path.toString());
			}
		} catch (IOException e) {}
		logger.debug("File list: {}", Arrays.toString(fileNames.toArray()));
		Collections.sort(fileNames);
		logger.debug("Sorted file list: {}", Arrays.toString(fileNames.toArray()));
		return fileNames;
    }

	public void deleteDirectoryContents(String directory) {
		List<String> fileNames = getFileList(directory);
		for(String file : fileNames) {
			deleteFile(file);
		}
	}

	public void deleteFile(String filePath) {
		Path path = Paths.get(filePath);
		try {
			Files.delete(path);
		}
		catch (IOException e) { logger.catching(e); }
    }

	public void renameFile(String oldFilePath, String newFilePath) {
		Path oldPath = Paths.get(oldFilePath);
		Path newPath = Paths.get(newFilePath);
		
		try {
			Files.move(oldPath, newPath);
			logger.debug("Renamed: " + oldFilePath + " -> " + newFilePath);
		}
		catch (IOException e) { logger.catching(e); }
    }
	
	public boolean contains(String filePath, String searchString) {
		int match = -1;
		Path path = Paths.get(filePath);
		if (Files.isRegularFile(path)) { 
			try {
				String content = new String(Files.readAllBytes(path));
				match = content.indexOf(searchString);
				//logger.debug("Result = {}", match);
			}
			catch (IOException e) { logger.catching(e); }
		}
		if (match != -1) { return true; }
		else { return false; }
    }
	
}