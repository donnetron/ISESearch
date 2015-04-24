//isesearch-advanced.js
//these functions toggle and correctly display the 'advanced' search options

//toggles the 'advanced' search options
$(function() {
	$('#advanced-nav-button').click(function() {
		$('#advanced-nav').toggle('blind', 300);
		$('#advanced-nav-caret').toggleClass('dropdown dropup');
	});
});

//SEARCH WITHIN COMPANY
//toggles the within-company search under advanced options, and also the input field
$(function() {
	$('#search-within-company-checkbox').click(function() {
		if ($('#search-within-company-checkbox:input:checkbox').is(':checked')) {
			$('#search-within-company-fieldset').removeAttr('disabled');
			$('#search-within-company-query').css('background-color', 'white');
			$('#search-within-company-label').fadeIn();
		} else {
			advancedOptionDisable('withinCompany');
		}
	});
});
//closes the search within-company badge on the blue results bar
$(function() {
	$('#search-within-company-label-remove').click(function() {
		advancedOptionDisable('withinCompany');
	});
});

//SEARCH DATE RANGE
//toggles the date range search under advanced options, and also the date input fields
$(function() {
	$('#search-date-checkbox').click(function() {
		if ($('#search-date-checkbox:input:checkbox').is(':checked')) {
			$('#search-date-fieldset').removeAttr('disabled');
			$('#search-date-label').fadeIn();
		} else {
			advancedOptionDisable('dateRange');
		}
	});
});
//close the search date badge on the blue results bar
$(function() {
	$('#search-date-label-remove').click(function() {
		advancedOptionDisable('dateRange');
	});
});

//DISABLE ADVANCED OPTIONS
function advancedOptionDisable(option) {
	//console.log('firing: ' + option);
	/* if (option == 'company') {
		$('#search-company-checkbox:input:checkbox').attr('Checked', false);
		$('#search-company').attr('disabled', true);
		$('#search-company-label').fadeOut();
	}
	else  */
	if (option == 'withinCompany') {
		$('#search-within-company-checkbox:input:checkbox').attr('Checked', false);
		$('#search-within-company-fieldset').attr('disabled', true);
		$('#search-within-company-query').css('background-color', '#eee');
		$('#search-within-company-query').val('');
		$('#search-within-company-label').fadeOut();		
	}
	else if (option == 'dateRange') {
		$('#search-date-checkbox:input:checkbox').attr('Checked', false);
		$('#search-date-fieldset').attr('disabled', true);
		$('#search-from-date').val('');
		$('#search-to-date').val('');
		$('#search-date-label').fadeOut();		
	}
}

//SEARCH COMPANY NAME
//DEPRECATED FUNCTION AS NO LONGER NEED SPECIAL SEARCH FOR COMPANY NAME ONLY
//toggles the company search under advanced options
/* $(function() {
	$('#search-company-checkbox').click(function() {
		if ($('#search-company-checkbox:input:checkbox').is(':checked')) {
			$('#search-company-label').fadeIn();
		} else {
			advancedOptionDisable('company');
		}
	});
}); */

//close the company badge on the blue results bar
/* $(function() {
	$('#search-company-label-remove').click(function() {
		advancedOptionDisable('company');
	});
}); */