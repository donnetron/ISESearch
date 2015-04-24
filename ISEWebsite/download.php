<?php
//two part php file - handles both the POST and GET element of downloading a file
//firstly we POST the javascript array to this php file. It iterates through the array and creates a zip file, and saves it on disk
//it then returns a response which is simply an url to this page, but where the request is a GET file
//the GET part of this page takes a parapmeter which is the path to the file and returns a header which downloads it, then it deletes the file

//echo "Request method: " . $_SERVER["REQUEST_METHOD"];
//foreach ($_GET as $key => $value) { //echo "$_GET: [" . $key . "] = " . $value; //echo "\r\n"; }
//foreach ($_POST as $key => $value) { //echo "$_POST: [" . $key . "] = " . $value; //echo "\r\n"; }



///////////////////
// PART 1 (POST) //
///////////////////
if ($_SERVER['REQUEST_METHOD'] === 'POST') { // if we got to this page by a POST request
	ini_set("display_errors", "On");
	error_reporting(E_ALL | E_STRICT);
	clearstatcache(); // this function caches information about specific filenames, so you only need to call clearstatcache() if you are performing multiple operations on the same filename and require the information about that particular file to not be cached.

	$downloadOption = $_POST["downloadOption"];  				//expects a variable (not json formatted) called $_POST["downloadOption"]
	$selectedDocIDs = json_decode($_POST["selectedDocIDs"]);  	//expects a json variable called $_POST["selectedDocIDs"]
	$files = array();	//new array for all the files

	foreach ($selectedDocIDs as $fileName => $value) {
		//echo $fileName . " = " . $value . "\r\n";
		if ($value == "checked") {
			$filepath = "cache/" . substr($fileName, 8, 4) . "/" . $fileName . ".html";
			//echo "we are getting: " . $filepath . "\r\n";
			$files[] = $filepath;
		}
	}

	//foreach ($files as $fileName) { echo "We will download: " . $fileName . "/n"; echo "\r\n"; }
	$valid_files = array();
	$valid_files_count = 0;	//LIMITS NUMBER OF VALID FILES THAT MAY BE DOWNLOADED TO 200
	if (is_array($files)) {
		foreach ($files as $file) {
			if ((file_exists($file)) && ($valid_files_count < 200)) {
				//echo "Valid file: " . $file;
				$valid_files[] = $file;
				$valid_files_count++;
			} else {
				//echo "CANNOT FIND " . $file;
			}
		}
	}

	$saveDir = tmpdir();				//create temp directory which will hold the output PDFs from wkhtmltopdf
	$argFile = tempnam('/download', '');//create a temporary file which will hold the argument for wkhtmltopdf if converting multiple files into multiple PDFs
	chmod($argFile, 0644);
	$inputFiles = "";					//command line string of the input files to use as argument for wkhtmltopdf (starts empty)
	$pad = strlen(count($valid_files)); //number of zeros to use depending on number of files (e.g. up to 10 is one zero, more than 10 is two zero's, 100 is three etc.)
	$type = 'zip';						//set the type to zip, if we are in 'onePDF' mode it will change to pdf
	
	if (count($valid_files) == 0) {
		echo "Error: no valid files to download";
	}
	//if we are making a single PDF we just need to make an output PDF and pass all the files we are appending to a giant pdf.
	if (($downloadOption == "onePDF") && count($valid_files) > 0) {
		$type = 'pdf';
		$outputPDF = $saveDir . '/' . 'file.pdf'; 	//can use the tmpdir to make sure we don't have multiple "file.pdf" that could overwrite each other for different users.
	
		foreach ($valid_files as $key => $file) {	
			$inputFiles = $inputFiles . $file . ' ';				//for each file, we can go through and build up the input filelist to send to wkhtmltopdf
		}
		
		$switches = " --no-images --disable-javascript ";
		
		$argument = $inputFiles . $outputPDF;		//wkhtmltopdf argument will be "all the input files" to -> "the output PDF file in the tempdir"
		$cmd = "/usr/local/bin/wkhtmltopdf" . $switches . $argument; 			//the command we will exec plus the argument file.
		//echo "RUNNING: " . $cmd;
		exec($cmd);
		$outputFile = 'download/' . basename($saveDir);
		rename($outputPDF, $outputFile);
	}
	
		
	//if we are making a zip file that contains PDFs of all the files as individual files
	if (($downloadOption == "zipPDF") && count($valid_files) > 0) {
	
		$outputFile = tempnam("download", "download");	//make a new zip file e.g. isesearch/download/83FG6T (note there is no extension)
		$outputCmd =  tempnam("download", "cmd");		//make a new zip file e.g. isesearch/download/93ZWG1 (note there is no extension)
		$PDFarray = array();							//new array of the final filenames we are adding to the zip (e.g. /tempdir/file1.pdf, /tempdir/file2.pdf and so on)
		
		$argFileResource = fopen($argFile, 'w');
		foreach ($valid_files as $key => $file) {				//loop through the HTML file list
			$PDFname = $saveDir . '/' . str_pad($key+1, $pad, '0', STR_PAD_LEFT) . ' - ' . basename($file, '.html') . '.pdf'; //make a sensible filename in the tmp dir, e.g. /tmp/58TDM/01 - 0000698_2003-01-23_556496.pdf
			$PDFarray[] = $PDFname;								//add filename to the the PDFarray
			fwrite($argFileResource, $file . ' ' . '"' . $PDFname . '"' . PHP_EOL);	//use the argFile to write the "HTML file" plus "PDF output" plus "new line"
		}
		fclose($argFileResource);	//closes the tempfile (doesn't unlink it unlike the function tmpfile()
		
		$switches = " --no-images --disable-javascript --read-args-from-stdin";
		
		$cmd = "/usr/local/bin/wkhtmltopdf" . $switches . " < " . $argFile; //use the exec plus the argFile with all the commands, this is quicker than calling exec multiple times for each file to convert
		
		exec($cmd);
		
		zipFiles($PDFarray, $outputFile);	//zip up the PDFarray into the zip file, when we are downloading this we can rename the zip file
	} 
		
	//if we are making a zip file with all the HTML files, so no need to do fancy conversion, but we just copy the HTML files into a tmpDir and zip them up
	else if (($downloadOption == "zipHTML") && (count($valid_files) > 0)) {
		$outputFile = tempnam("download", "download");	//make a new zip file e.g. isesearch/download/83FG6T (note there is no extension)
		$HTMLarray = array();
		
		foreach ($valid_files as $key => $file) {		//loop through the HTML file list
			$tmpDirFile = $saveDir .  '/' . str_pad($key+1, $pad, '0', STR_PAD_LEFT) . ' - ' . substr($file,strrpos($file,'/') + 1); //make a sensible filename in the tmp dir, e.g. /tmp/58TDM/01 - 0000698_2003-01-23_556496.html
			$HTMLarray[] = $tmpDirFile;					//add filename to the the HTMLarray
			copy($file, $tmpDirFile);					//copy the file to the tmpdir with the new name, this just makes it easier to zip the files using the zipFiles function
		}
		zipFiles($HTMLarray, $outputFile);
	}
		
		removedir($saveDir);
	

	$download_link = 'http://' . $_SERVER["SERVER_ADDR"] . '/isesearch/download.php?type=' . $type . '&file=' . basename($outputFile);

	 $result = '{"success":1,"path":"'.$download_link.'","error":null}';
	 header('Content-type: application/json;');
	 echo $result;
}



//////////////////
// PART 2 (GET) //
//////////////////
else if ($_SERVER['REQUEST_METHOD'] === 'GET') {
 
	 $fn = $_GET['file'];
	 $ft = $_GET['type'];
	 
	 if ($fn) {
			$result = $_SERVER['DOCUMENT_ROOT'].'/isesearch/download/'.$fn;
			if (file_exists($result)) {
				header('Content-Description: File Transfer');
				header('Content-Type: application/force-download');
				header('Content-Disposition: attachment; filename="ise_notices_' . date('d-m-Y-His'). '.' . $ft . '"');
				header('Content-Transfer-Encoding: binary');
				header('Expires: 0');
				header('Cache-Control: must-revalidate, post-check=0, pre-check=0');
				header('Pragma: public');
				header('Content-Length: ' . filesize($result));
				ob_clean();
				flush();
				readfile($result);
				@unlink($result);
			}
		}
}

function tmpdir() {
	$tempfile = tempnam(sys_get_temp_dir(), '');
	if (file_exists($tempfile)) { unlink($tempfile); }
	mkdir($tempfile);
	if (is_dir($tempfile)) { return $tempfile; }
}

function removedir($dir) {
	foreach (scandir($dir) as $item) {
		if ($item == '.' || $item == '..') continue;
			unlink($dir.DIRECTORY_SEPARATOR.$item);
		}
	rmdir($dir);
}

//IT IS IMPORTANT THAT THE $OUTPUT FILE DIRECTORY HAS 777 PERMISSIONS (E.G. WRITEABLE BY EVERYONE)
function zipFiles($fileArray, $outputFile) {
	$zip      = new ZipArchive();
	
	if ($zip->open($outputFile, ZIPARCHIVE::CREATE) !== TRUE) {
		echo "* Sorry ZIP creation failed at this time";
	} else {
		//echo "Creation of zip file ok \r\n";
	}
	
	foreach ($fileArray as $key => $file) {
		if (file_exists($file)) { $zip->addFile($file, basename($file)); }
		else { echo $file . " does not exist \n"; }
		
	}
	$zip->close();
}

?>