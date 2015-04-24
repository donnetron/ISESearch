//isesearch-globalVars.js
//contains global variables used by various functions to track the state of the page
//loaded before all the other js files which use them

var Manager; 			//global variable used by all the widgets to access the solr manager object
var selectedDocIDs = {};//global variable which is an associative array where each [key] is the ID of a notice result, and the [value] is the status of whether it is 'checked' or 'unchecked'
var downloadOption;		//global variable to choose between PDF/HTML/ZIP options