//this widget simply filters the query by company
//be careful with this - it was written to be generic but if there was ANOTHER widget also using the same filter (e.g. to search for companies only), it would clear that filter
// the clear() and remove(this.field) functions are the offendiner global filter wiper
//note this extends the AbstractFILTERWidget so/add/remove are for fq, not q
(function ($) {

	AjaxSolr.CompanySelectorWidget = AjaxSolr.AbstractFacetWidget.extend({
	
		constructor: function (attributes) {
			AjaxSolr.AbstractFacetWidget.__super__.constructor.apply(this, arguments);
			AjaxSolr.extend(this, {
				start: 0,
				//field: 'company',
				multivalue: false
			}, attributes);
		},
		
		init: function () {
			var self = this;
			//start the keybind
			$(self.input).bind('keydown', function(e) {
				if (e.which == 13 && ($.trim($(self.input).val()).length > 0)) {	//if you press ENTER and the within filter text is set
					greenFade($(self.input));										//greenfade visually acknolwledges choice
					if ($.trim($('#query').val()).length > 0) {						//only if there is ALSO text in the main #query will we do a search
						self.doRequest();
					}
				}
			});
		},
		
		beforeRequest: function() {
			this.clear();				//clear the fq
			this.remove(this.field);	//remove the fq for this field (passed in as parameter)
			//console.log('removed filter queries for: ' + this.field);
							
			if ($(this.target).find('input:checkbox').prop('checked')) { 	//expects the target to have a checkbox in it
				var fq = $(this.input).val();								//also, expects the id of an input to be passed in as a parameter
				if (fq != 0) {			//check the input actually had something in it
					this.add(fq);		//adds it (remember add for this abstract widget, automatically uses the this.field parameter
				}
				else {	//if the input had nothing in it, then set a warning
					var warningMessage = '<span id="noCompanyFilter"></span>Company name not specified (no filter applied).';
					if ($('#noCompanyFilter').length == 0) { setMessageNav('alert-danger', 'Warning!', warningMessage); }
				}
				//console.log('Added filter query: ' + this.field + '=' + fq);						
			}
			else {	//if the input had nothing in it, then set a warning
					$('#noCompanyFilter').closest('.message-nav').fadeOut(200, function() { $this.remove() });
				}
		}
	});
})(jQuery);