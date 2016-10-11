//isesearch-init.js
//contains a number of setup items to adjust the display when the page is loaded for the first time
$(function() {				//this line is syntactic sugar for $(document).ready(function() {

	$('#updateDatetime').load('cache/updated.txt');

	$('#query').focus();	//put cursor in the query box

	$('#search-from-date').datepicker({ //sets a datepicker on the search-from-date
		autoclose: true,
		endDate: new Date(),
		startDate: new Date(2003, 1, 1),
		format: 'dd/mm/yyyy',
		startView: 0,
		todayBtn: false,
		todayHighlight: true
	});
	$('#search-to-date').datepicker({ //sets a datepicker on the search-to-date
		autoclose: true,
		endDate: new Date(),
		startDate: new Date(2003, 1, 1),
		format: 'dd/mm/yyyy',
		startView: 0,
		todayBtn: false,
		todayHighlight: true
	});
	
	//hide controls to start with
	$('#advanced-nav').hide();					//hides the advanced search options
	$('#results-nav').hide();					//hides blue 'results' bar
	$('#search-within-company-label').hide();	//hides company badge (which appears in the results bar)
	$('#search-date-label').hide();				//hides date-range badge (which appears in the results bar)
	$('#notices-nav').hide();					//hides notice results
	$('#pager-nav').hide();						//hides pager
	
	$('#download-button').attr('disabled', 'disabled');	//disables download button until there are results to select and download
	initDownloadOption();						//function in the isesearch-download.js file which gets/ses cookie with the option for downloading (e.g. zip HTML/zip PDF/one PDF)
	
});

// at any time if text input field value is empty, hide the results
$(function() {
	$('#query').keyup(function() {
		if ($.trim($('#query').val()) == '') {
			$('#results-nav').hide();
			$('#notices-nav').hide();
			$('#pager-nav').hide();
			$('.ui-menu').hide();
		}
	});
});