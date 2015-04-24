(function($) {

	AjaxSolr.SubmitWidget = AjaxSolr.AbstractTextWidget.extend({
		
		//most of the work is done in 'init' which sits and listens for an ENTER or Submit click
		// then performs the request after setting Q and initialised the selectedDocIDs and does the request
		init: function() {
			var self = this;
			
			self.clear();
			self.manager.store.addByValue('sort', 'datetime desc')
			//this block deals with keydowns in the input box, and if you press enter (and #query has - text) it will set Q to the value of #query (the text box ID) and do a request
			$(this.target).find('#query').bind('keydown', function(e) {
				if (e.which == 13) {
					
					var q;
					
					if  ($.trim($('#query').val()).length === 0) { 	//if the main query is empty BUT we are searching within a company, set Q to *:* (and fq will narrow to that company)
						if ($.trim($('#search-within-company-query').val()).length > 0) {
							q = '*:*';
						}
					}
					else {											//but if the main query IS set, we'll set that as Q
						q = $(this).val();
					}
					
					if (q != null) {
						//console.log('q = ' + q);
						greenFade(this);

						selectedDocIDs = {};	//clear the selectedDocIDs array for the new query
						//checkCheckAllIcon();
						self.set(q);
						self.doRequest();
					}
				}
			});


			//this block deals with clicking the submit button, it checks the #query text box (the search input id) isn't 0 length and will set Q to the value of that and do a request
			$(this.target).find('#submit').click(function() {
				if ($.trim($("#query").val()).length > 0) {
					var q = $('#query').val();
					//console.log('q = ' + q);
					greenFade('#query');

					selectedDocIDs = {};	//clear the selectedDocIDs array for the new query
					//checkCheckAllIcon();
					self.set(q);
					self.doRequest();
				}
			});
		},
		
		afterRequest: function() { 			//after request used to stop autocomplete popping up again
			$(".ui-autocomplete").hide();
			$("#results-nav").show();
		}

	});
})(jQuery);