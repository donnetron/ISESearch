//isesearch-download.js
//contains function to POST the selectedDocIDs array to the download.php page for delivering the downloads
//contains functions dealing with the settings modal (how you would like your downloads), and the cookie associated with that settings

//ajax request to get downloaded files
$(function() {
	$('#download-button').click(function() {
		//selectedDocIDsDebug();
		var selectedDocIDsData = JSON.stringify(selectedDocIDs);
		$('#downloadProgress').modal({
									backdrop: 'static',
									keyboard: false,
									show: true});
		$.ajax({
			type: "POST",
			url: 'download.php',
			data: {'selectedDocIDs': selectedDocIDsData, 'downloadOption': downloadOption},
			context: document.body,
			//these two 'success' parameters are for debugging:
			//the first changes the download.php output to the main window, so you can see php errors
			//the second outputs a 'success' alert, and prints the datareceived back into the console window
			
			//success: function() {
			//	window.location = 'download.php';
			//},

			//success: function(data){
				//alert("success");
				//window.location = data;
				//console.log(data);
			//}, 
			
			success: function(data){
				if (data) {
					console.log(data);
					if (data.path) {
						//loads an invisible iframe with the download header
						//the browser is smart enough to know that once the php page has processed the array, and generated a zip/pdf of files that it presents a download dialogue
						var dlif = $('<iframe/>',{'src':data.path}).hide();
						$('#download-button').append(dlif);
						$('#downloadProgress').modal('hide');
					}
				}
				else {
					console.log('Something went wrong');
				}
			},
			error: function (xhr, ajaxOptions, thrownError) { console.log("ERROR: " + xhr.responseText+" - " + thrownError); }
		});
	});
});


///////////////////////
// DOWNLOAD SETTINGS //
///////////////////////

//this handles the modal and settings on how a user wants to download notices (e.g. zip HTML, zip PDFs, or one big PDFs
function initDownloadOption() {
	downloadOption = getCookie("downloadOption");
	
	//validate the cookie (if its not one of the three options, its not validly set, or (more likely) doesn't exist)
	if ((downloadOption != "zipHTML") && (downloadOption != "zipPDF") && (downloadOption != "onePDF")) {
		//sets the cookie to a default option of zipHTML
		downloadOption = "zipHTML";
		setCookie("downloadOption", downloadOption, 365);
		//console.log("cookie was not found or invalid value, setting new cookie to: " + downloadOption);
	}
	//at this poiint should have either found the existing cookie or set a new one
	//console.log("Cookie found/set. Setting form to: " + downloadOption);
	
	//set the form in the modal to reflect the cookie
	$('#downloadOptionForm input:radio[name="optradio"]').filter('[value="' + downloadOption + '"]').prop('checked','checked');
}
	
//function to (re)set cookie when you click the modal form "save" button
$(function() {
	$('#saveDownloadOption').click(function() {
		//console.log("setting cookie to form value: " + $('#downloadOptionForm input:radio[name="optradio"]:checked').val());
		//gets to the value of the radio button that is checked
		downloadOption = $('#downloadOptionForm input:radio[name="optradio"]:checked').val();
		//saves a cookie with this value
		setCookie("downloadOption", downloadOption, 365);
		$('#downloadOptions').modal('hide');
	});
});

//function to reset the correct form value if not saved (e.g. if you click close or out of the modal, it won't save the form). When you re-open the modal the form will be set to match the current cookie value
$(function() {	
	$('#downloadOptionMenu').click(function() {
		console.log("reset setting form to: " + downloadOption);
		$('#downloadOptionForm input:radio[name="optradio"]').removeAttr('checked');
		$('#downloadOptionForm input:radio[name="optradio"]').filter('[value="' + downloadOption + '"]').prop('checked','checked');	
	});
});
	 
//function to check if a cookie with a particular name exists
function checkCookie(name) {
	if (document.cookie.indexOf(name) >= 0) {
		return true;
	}
	else {
		return false;
	}
}

//function to set a cookie for this domain
function setCookie(name, value, duration) {
	var d = new Date();
	d.setTime(d.getTime() + (duration*24*60*60*1000));
	var expires = "expires=" + d.toUTCString();
	document.cookie = name + "=" + value + "; " + expires;
}

//function to get a cookie for this domain
function getCookie(name) {
	var name = name + "=";
	var cookieArray = document.cookie.split(';');		//gets an array of cookies for this domain
		for(var i = 0; i < cookieArray.length; i++) {	//loop through the array looking for a cookie with the name of the argument
			var cookie = cookieArray[i];
			while (cookie.charAt(0)==' ') {
				cookie = cookie.substring(1);			//removes whitespace at beginning of cookie
			}
			if (cookie.indexOf(name) != -1) {			//check if the name of the cookie matches the one we have from the array
				return cookie.substring(name.length,cookie.length);
			}
		}
	return null;
}

//function to clear all cookies for this domain
function clearCookies() {
	var c = document.cookie.split("; ");
	for (i in c) 
	document.cookie =/^[^=]+/.exec(c[i])[0]+"=;expires=Thu, 01 Jan 1970 00:00:00 GMT"; //sets the expiry date to the past which has the effect of automatically deleting it
}