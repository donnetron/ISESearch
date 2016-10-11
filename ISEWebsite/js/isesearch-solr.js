//isesearch-solr.js
//main file to implements ajax-solr and manages all the search 'widgets'. 
//widgets are various items on the page that query and deal with searches.

//jQuery document ready function - sets all the widgets up on load
(function($) {

	$(function() {
		//manager object to make requests to the solr server
		Manager = new AjaxSolr.Manager({
			solrUrl: 'http://debian-vm:8080/solr/'
	});


		///////////////////////
		// MODIFIED WIDGETS: //
		///////////////////////

		//makes pop-up of suggestions for autocomplete when typing into search box
		Manager.addWidget(new AjaxSolr.AutocompleteTermWidget({
			id: 'search',
			target: '#search',
			field: ['text']
		}));

		//makes pop-up of suggestions for autocomplete when typing into WITHIN COMPANY search box
		Manager.addWidget(new AjaxSolr.AutocompleteTermWidget({
			id: 'withinCompany',
			target: '#search-within-company-selector',
			field: ['company']
		}));
		
		//Result widget to list the results from a query (e.g. a table of notices)
		Manager.addWidget(new AjaxSolr.ResultWidget({
			id: 'result',
			target: '#docs'
		}));

		//Pager widget has two functions: 
		//	(i) if more than 10 results, it will plug in a page navigator at the bottom of the page
		//	(ii) it will display [X] results for '[search term]' in a chosen location (here it is the blue results bar)
		Manager.addWidget(new AjaxSolr.PagerWidget({
			id: 'pager',
			target: '#pager-nav',
			prevLabel: '&lt;',
			nextLabel: '&gt;',
			innerWindow: 1,
			renderHeader: function(perPage, offset, total) {
				var filter = '';
				var company = $('#search-within-company-query').val();			//these will check if there was a narrow filter for company
				var startDate = $('#search-from-date').val();
				var endDate = $('#search-to-date').val();
				
				if (company.length > 0) { filter += ' in \'' + company + '\''; }												//adds in the company filter
				if ((startDate.length > 0) && (endDate.length > 0)) { filter += ' between ' + startDate + ' and ' + endDate; } 	//adds in the date range filter
				
				//actually prints the value in the blue results bar
				$('#results-nav-text').html($('<span></span>').text(total + ' results for \'' + $('#query').val() + '\'' + filter));
			}
		}));


		///////////////////////
		//    NEW WIDGETS:   //
		///////////////////////

		//A new widget designed as a catch-all to 'submit' a search for multiple actions on the query box, e.g. enter pressed or magnifying class clicked
		//It also sets the selectedDocIDs array to be empty as we only want to repopulate for a NEW search. Since requests are also done if sorting or filtering, we don't always want to clear this array (which would have been the case if a before request in the selector widget)
		Manager.addWidget(new AjaxSolr.SubmitWidget({
			id: 'submit',
			target: '#search',
			//field: [ 'text' ]
		}));

		//A new widget that does a separate (sneaky) request to get JUST the IDs of all the results for the main query
		//after the side request is done, it populates the selectedDocIDs array with all of the IDs so they can be set to checked or unchecked for download later on
		Manager.addWidget(new AjaxSolr.SelectWidget({
			id: 'select',
			target: '#checkAll'
		}));

		//A new widget (time 3) to sort the current search
			//1. DATE sort
			Manager.addWidget(new AjaxSolr.SortWidget({
				id: 'sortDate',
				target: '#sort_date',
				field: 'datetime',
				startOrder: 'asc'
			}));

			//2. COMPANY (a-z) sort
			Manager.addWidget(new AjaxSolr.SortWidget({
				id: 'sortCompany',
				target: '#sort_company',
				field: 'company',
				startOrder: 'asc'
			}));

			//3. NOTICE TITLE (a-z) sort
			Manager.addWidget(new AjaxSolr.SortWidget({
				id: 'sortNotice',
				target: '#sort_notice',
				field: 'title',
				startOrder: 'asc'
			}));

		//filter query when only searching by company
		/* Manager.addWidget(new AjaxSolr.CompanySelectorWidget({
			id: 'companySelector',
			target: '#company-selector',
			input: '#query',
			field: 'company'
		})); */
		
		Manager.addWidget(new AjaxSolr.CompanySelectorWidget({
			id: 'withinCompanySelector',
			target: '#search-within-company-selector',
			input: '#search-within-company-query',
			field: 'company'
		}));

		//filter query when searching between date range
		Manager.addWidget(new AjaxSolr.DateSelectorWidget({
			id: 'searchDateSelector',
			target: '#search-date-selector',
		}));


		///////////////////////
		//    INITIALISE:    //
		///////////////////////

		//Below to set up back-button history (To be completed)
		//pressing back button redoes the solr search but it fucks up the array of results, and doesn't update the querybox and result summary properly... 
		//Manager.setStore(new AjaxSolr.ParameterHistoryStore());
		//Manager.store.exposed = [ 'fq', 'q'];

		//initialise the manager object
		Manager.init();
		Manager.store.addByValue('q', '*:*');

	});

})(jQuery);
