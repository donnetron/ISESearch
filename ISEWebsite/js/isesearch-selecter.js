//isesearch-selector.js
//contains functions that manipulate a global array called selectedDocIDs[] to match the notices that are selected by a user.
//the selectedDocIDs[] global array is used to keep track of what has been selected for download
//these functions are used by the select widget to update the checkAllIcon so need to be pre-loaded before the widgets in index.html


//////////////
// BINDINGS //
//////////////

//if you click on the 'CheckAll' icon, set the array appropriately and then check what the 'CheckAll' icon should be (separate function to do this)
$(function() {
	$('#checkAll').on('click', (function() {
		//console.log("#checkAll clicked");
		//if 'CheckAll' icon is currently showing full-range-check then uncheck all items in the array
		if ($('#checkAll span').hasClass('full-range-check')) {
			$.each(selectedDocIDs, function(key, value) { 		//this line iterates over the selectedDocIDs object properties (like an associative array)
				selectedDocIDs[key] = 'unchecked';				//this line sets the value of that property to 'unchecked' 
			});
		//else if 'CheckAll' icon is currently set to checked, then full-range-check all items in the array
		} else if ($('#checkAll span').hasClass('glyphicon-check')) {
			$.each(selectedDocIDs, function(key, value) {		//iterates
				selectedDocIDs[key] = 'checked';				//sets to checked
			});
		//else if 'CheckAll' icon is currently set to unchecked, then check just page of icons, this involves a bit of work...
		} else if ($('#checkAll span').hasClass('glyphicon-unchecked')) {
			//first maps the current page of IDs into an associative array
			var pageIDs = $('.ise-checkbox').map(function() {	//this line sets an array variable which is a map all of the element ids if they have class '.ise-checkbox' into an object key=[0] value=[id], key=[1] value=[id]... where id would be the id of the element
				return this.id;
			}).get();

			//loops over the associative array, and uses the value to set the selectedDocIDs[key] to checked
			//so we are effectively setting the selectedDocIDs with keys that are on this page to checked
			//note we are using the VALUE in the pageIDs object to set the KEY in the selectedDocIDs object
			$.each(pageIDs, function(key, value) {				//iterates over object we just created, so key will be 0, 1, 2, 3 etc, and the value will be the specific element ID
				selectedDocIDs[value] = 'checked';				//for the current iteration, set the matching selectedDocIDs KEY (matching the page ID) to 'checked'
			});
		} //end of 'this page' ID checker
		
		//finally, call the function to check what the CheckAll icon should be 
		checkCheckAllIcon();
	}));
});

//if you click on a checkbox, set it the array appropriately and then check what the 'CheckAll' icon should be (separate function to do this)
$(function() {
	//slightly odd change from using the '.classname'.on('change', fucntion()... jQuery becuase the checkboxes are dynamically displayed.
	// see this url http://stackoverflow.com/questions/15090942/jquery-on-method-not-working-on-dynamic-content for explanation
	$(document.body).on('change', '.ise-checkbox', (function(e) {
		//console.log(".ise-checkbox changed");
		if (($(e.target).is(':checked')) && (selectedDocIDs[e.target.id] != 'checked')) { 			//double check to make sure the checkbox has been checked AND it isn't already set as checked in the global associative array (this is to deal with if the 'change' is as a result of clicking the CheckAll icon rather than from clicking on the checkbox itself
			selectedDocIDs[e.target.id] = 'checked';												//the ID is taken from the element clicked, to set the applicable key in the selectedDocIDs object
			$('#' + e.target.id).closest('.row').addClass('selected');
		} else if (!($(e.target).is(':checked')) && (selectedDocIDs[e.target.id] != 'unchecked')) {	//as above but for unchecked
			selectedDocIDs[e.target.id] = 'unchecked';												//as above but for unchecked
			$('#' + e.target.id).closest('.row').removeClass('selected');
		}
		
		//finally check if the CheckAll icon should have change as a result of clicking on this one checkbox (it might have been the last of a full page, or full-range etc)
		checkCheckAllIcon();
	}));
});

//below function sets up the checkboxes to match the selectedDocIDs array if you change page or sort the results
//if you click on a button or link (which might have the result or reordering or moving to another page), check the selectedDocIDs and check/uncheck the relevant checkboxes
$(function() {
	$('a, button').click(function(e) {
		checkISECheckboxes();	//separate function below
	});
});


///////////////
// FUNCTIONS //
///////////////

//this function is called where if a page of results is loaded, it iterates over the selectedDocIDs and checks or unchecks each checkbox depending on the array value
function checkISECheckboxes() {
	//first maps the current page of IDs into an associative array
	var pageIDs = $('.ise-checkbox').map(function() {	//this line maps all of the elements ids if they have class '.ise-checkbox' into an object key=[0] value=[id], key=[1] value=[id]... where id would be the id of the element
		return this.id;
	}).get();

	//then iterates over the associative array
	$.each(pageIDs, function(key, value) {
		//checks uses the pageIDs VALUE (as key is just index) to see if the corresponding selectedDocIDs KEY has a value that is checked
		if (selectedDocIDs[value] == 'checked') {
			$('#' + value).prop('checked', true); //if it does then is shows the checkbox on the page as checked (i.e. to match the selectedDocIDs array)
			$('#' + value).closest('.row').addClass('selected');
		}
		//ELSE IF checks uses the pageIDs VALUE (as key is just index) to see if the corresponding selectedDocIDs KEY has a value that is unchecked
		else if (selectedDocIDs[value] == 'unchecked') {
			$('#' + value).prop('checked', false); //if it does then is shows the checkbox on the page as unchecked (i.e. to match the selectedDocIDs array)
			$('#' + value).closest('.row').removeClass('selected');
		}
	}); //end iteration
}

//this function is to look at the selectedDocIDs array and decide if the CheckAll icon should be set to 'unchecked', 'checked', or 'full-range-check'	
function checkCheckAllIcon() {

	//variable set to equal the return of another function (see further below) that checks if if ALL, SOME, or NONE of the KEYS in the selectedDocIDs are set to checked
	var selected = docsSelected(); 

	//if 'none' items are checked in the selectedDocIDs array
	if (selected == 'none') {
		$('#download-button').prop('disabled', true); 		//disable the download button
	}
	else { $('#download-button').prop('disabled', false); }	//else, there must be at least 'some' or 'all' checked items, so enable the button
	
	//this is if only 'some' of the items in the array are selected, we need to know if this page of notice results should make the current CheckAll icon as checked
	if (selected == 'some') {
		//first maps the current page of IDs into an associative array
		var pageIDs = $('.ise-checkbox').map(function() {	//this line maps all of the elements ids if they have class '.ise-checkbox' into an object key=[0] value=[id], key=[1] value=[id]... where id would be the id of the element
			return this.id;
		}).get();
		
		//we will start off with the assumption that this page is fully selected, so we can set the variable to 'page'
		selected = 'page';
		
		//now iterate over the IDs of checkboxes on this page
		$.each(pageIDs, function(key, value) {
			//checks uses the pageIDs VALUE (as key is just index) to see if the corresponding selectedDocIDs KEY has a value that is unchecked
			if (selectedDocIDs[value] == 'unchecked') { 
				//if there is a value in the selectedDocIDs that is unchecked, we know that not all of the checkboxes on this page is checked.
				//so we revert the selected var to be 'none' instead of 'page' so that the CheckAll icon is unticked on this page (allowing us to check this page if we want, with one click)
				selected = 'none';
			}
		});
	}
	
	//so finally, we can set the CheckAll icon to a state it will expect
	setCheckAllIcon(selected);
	//console.log('Setting #CheckAll icon to: ' + selected);
}

//this function changes the class of the CheckAll icon to match either 'none' (unchecked), 'some' (blue tick), or 'full-range' (red tick)
function setCheckAllIcon(state) {
	switch(state) {
		case 'none':
			$('#checkAll span').removeClass().addClass('glyphicon glyphicon-unchecked');
			break;
		case 'page':
			$('#checkAll span').removeClass().addClass('glyphicon glyphicon-check');
			break;
		case 'full-range':
			$('#checkAll span').removeClass().addClass('glyphicon glyphicon-check full-range-check');
			break;
	}
}

//this function will check if are 'all', 'none', or 'some' of the items in the selectedDocIDs array are set as 'checked'
function docsSelected() {
	//set up variables to count the number of checked and unchecked values
	var checked = 0;
	var unchecked = 0;
	var result;

	//iterate over the selectedDocIDs array and increase the count depending on the value of the iteration
	$.each(selectedDocIDs, function(key, value) {
		//console.log('ID: ' + key + ' = ' + value);
		if (value == 'checked') { checked++; }
		if (value == 'unchecked') { unchecked++; }
	});
	
	//console.log('Total checked = ' + checked);
	//console.log('Total unchecked = ' + unchecked);
	selectedMessageNav(checked);
	
	if (unchecked == 0) { result = 'full-range'; }	//i.e. we know that nothing was unchecked
	else if (checked == 0) { result = 'none'; }		//i.e. we know that nothing was checked
	else { result = 'some'; }						//i.e. if everything wasn't checked or unchecked, it must be some
	
	//console.log("docsSelected() = " + result);
	return result;
}

function selectedDocIDsDebug() {
	$.each(selectedDocIDs, function(key, value) {
		console.log('ID: ' + key + ' = ' + value);
	});
}

//this is a separate function that sets the nav bar with a message of the number of notices selected if you have selected more than 10
function selectedMessageNav(count) {
	if (count > 200) {
		if ($('#message-count').length == 0) {
			var warningMessage = 'Only 200 messages can be downloaded at a time (<span id="message-count">' + count + '</span> messages selected).';			
			setMessageNav('alert-danger', 'Warning!', warningMessage);
		}
		else {
			$('#message-count').html(count);
		}
	}
	else if (count > 10) {
		if ($('#message-count').length == 0) {
			var warningMessage = '<span id="message-count">' + count + '</span> messages selected.';			
			setMessageNav('alert-warning', '', warningMessage);
		}
		else {
			$('#message-count').html(count);
		}
	}
	else {
		var $this = $('#message-count').closest('.message-nav');
		$this.fadeOut(200, function() { $this.remove() });
	}	
}