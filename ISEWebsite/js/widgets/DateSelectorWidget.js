//this widget filters the query to a specific date range
(function ($) {

	AjaxSolr.DateSelectorWidget = AjaxSolr.AbstractFacetWidget.extend({
	
		constructor: function (attributes) {
			AjaxSolr.AbstractFacetWidget.__super__.constructor.apply(this, arguments);
			AjaxSolr.extend(this, {
				start: 0,
				field: 'datetime',
				multivalue: false
			}, attributes);
		},
		
		init: function () {
			var self = this;
		},
		
		beforeRequest: function() {
			if ($('#search-date-checkbox').prop('checked')) {
				if (($('#search-from-date').val().length > 0) && ($('#search-to-date').val().length > 0)) {
					var startDate = new Date($('#search-from-date').datepicker('getDate'));
					var endDate = new Date($('#search-to-date').datepicker('getDate'));

					if (startDate <= endDate) {
						var fqDateRange = '[' + startDate.toISOString() + ' TO ' + endDate.toISOString() + ']';
					}
					else {
						//little quirk where if the start date is not equal to or before the end date, I switch them round so that they are
						var fqDateRange = '[' + endDate.toISOString() + ' TO ' + startDate.toISOString() + ']';
					}
					this.add(fqDateRange);
				}
				else {
					//if the date tickbox was checked but not dates were set, do nothing
				}
			}
			else {
				this.clear();
				//console.log("Date checker not ticked");
			}
		},
		
		afterRequest: function() {
			if ($('#search-date-checkbox').prop('checked')) {
				if (($('#search-from-date').val().length > 0) && ($('#search-to-date').val().length > 0)) {
					var startDate = new Date($('#search-from-date').datepicker('getDate'));
					var endDate = new Date($('#search-to-date').datepicker('getDate'));

					if (startDate > endDate) {
						var warningMessage = '<span id="swapDateRange"></span>[FROM] date (' + moment(startDate).format("DD/MM/YYYY") + ') was before [TO] date (' + moment(endDate).format("DD/MM/YYYY") + '), so I swapped them for you.';
						setMessageNav('alert-danger', 'Warning!', warningMessage);
						$('#search-from-date').datepicker('update', endDate);
						$('#search-to-date').datepicker('update', startDate);
						//console.log("Warning: Start date is after end date");
					}
				}
				else {
						var warningMessage = '<span id="noDateRange"></span>Date range not set (no filter applied).';
						if ($('#noDateRange').length == 0) { setMessageNav('alert-danger', 'Warning!', warningMessage); }
						//$('#search-date-checkbox').prop('checked', false);
						//console.log("Warning: dates were not selected");
				}
			}
			else {
				$('#swapDateRange').closest('.message-nav').fadeOut(200, function() { $this.remove() });
				$('#noDateRange').closest('.message-nav').fadeOut(200, function() { $this.remove() });
			}
		}
		
	});
})(jQuery);