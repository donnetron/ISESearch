//this widget is used to perform a separate side-search that is based on the main query search but ONLY asks for the UID of the results and populates the global array selectedDocIDs with the results
(function($) {
	AjaxSolr.SelectWidget = AjaxSolr.AbstractWidget.extend({

		//before a new request it resets the checkbox selection, this is just so you can re-run the tests each time the page is loaded
		//note the global selectedDocIDs array is NOT cleared under this widget because we don't want to reset it for 'any' new request
		//remember a new request will be made even if we are sorting or filtering the current results, so we use the submit widget (which works for a new query) to clear the global selectedDocIDs array
		beforeRequest: function() {
			var self = this;
	
			$('.ise-checkbox').prop('checked', false);
			$('#download-button').attr('disabled', 'disabled');
			$('#checkAll span').removeClass('glyphicon-check full-range-check').addClass('glyphicon-unchecked');
		},
		
		//after a request it populates the global selectedDocIDs array, but note the first condition below: ONLY if it is empty. It will only empty on a new query
		afterRequest: function() {
		
			var self = this;
			
			//if the global selectedDocIDs object is empty (in JavaScript an associative array is actually just an object with lots of properties)
			if ($.isEmptyObject(selectedDocIDs)) {
				//console.log("selectedDocIDs is empty object");
				
				//create and initialise a new parameter store, we can't use the main query parameter store because you need to do a different search (only getting the UIDs)
				var UIDstore = new AjaxSolr.ParameterStore();
				UIDstore.init();
				//Copy the main parameter store first however, as you want to capture the query string and any filters so you get the right set of UIDs
				UIDstore.parseString(self.manager.store.string());

				//Because we only want to get the UID, and we don't want just the first 10 (by default) we add the values.
				UIDstore.addByValue('fl', 'cachefile');		//Set the returns to Field List: CACHEFILE ***NOTE LAST MINUTE CHANGE: NOT USING THE UID AS ITS EASIER TO PASS AN ARRAY OF FILENAMES TO THE PHP DOWNLOADER ***
				UIDstore.addByValue('rows', '100000000');	//Set the number of results to be returned to be [100 million], an impossibly high number as there is no argument to return 'all' rows

				self.manager.executeRequest('select', UIDstore.string(), function(data) {	//execute a search request using this special parameter store
					for (var i = 0; i < data.response.docs.length; i++) {					//iterate through the manager return and populate the selectedDocIDs using the UID as the key, and 'unchecked' as the value. So we start with everything unchecked
						selectedDocIDs[data.response.docs[i].cachefile] = 'unchecked';
					}
				});
			}
			// if the selectedDocIDs is NOT EMPTY, it must not have been cleared as there was no new 'submit' query, so there must have been a request for some other reason like a new page of results, or a sort, so we just run the standard checkbox checks on the existing selectedDocIDs array
			else {
				//console.log("selectedDocIDs is NOT EMPTY OBJECT");
				checkISECheckboxes();
				checkCheckAllIcon();
			}
		}
	});

})(jQuery);