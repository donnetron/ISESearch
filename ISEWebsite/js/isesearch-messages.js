//isesearch-messages.js
//these functions are used by various widgets to update the message bar and acknowledge query input
//they need to be pre-loaded before the widgets in index.html

//function to place a message in the message-nav bar
function setMessageNav(classes, strongMessage, message) {
	//construct HTML for message bar		
	var messageOutput  = '<div class="message-nav">																		';
		messageOutput += '	<div class="alert ' + classes + '">															';
		messageOutput += '		<strong>' + strongMessage + ' </strong>													';
		messageOutput += '		<span id="warning-nav-text">' + message + '</span>										';
		messageOutput += '		<a href="#" class="message-nav-close glyphicon glyphicon-remove alert-danger"></a>		';
		messageOutput += '	</div>																						';	
		messageOutput += '</div>																						';
	//insert into #message-nav bar
	$('#message-nav').append(messageOutput);
}

//generic function to close a message alert (onclick of a .message-nav-close class)
$(function() {
	$(document.body).on('click', '.message-nav-close', (function(e) {
		$this = $(e.target).closest('.message-nav');
		$this.fadeOut(200, function() { $this.remove() });
	}));
});

//function that flashes an input green for a few seconds to acknowledge input
function greenFade(input) {
		$(input).css('background-color', '#62c462');
		$(input).animate({backgroundColor: 'transparent'}, "medium");
}