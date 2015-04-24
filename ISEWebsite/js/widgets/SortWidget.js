(function($) {

	//TODO include option parameter so that we can set asc for A-Z, but desc for date

	AjaxSolr.SortWidget = AjaxSolr.AbstractWidget.extend({
		init: function() {
			var self = this;
			var order = 'desc';
			$(this.target).on('click', function() {
				self.manager.store.remove('sort');
				self.manager.store.addByValue('sort', self.field + ' ' + self.order);
				self.doRequest();
			});
		},

		afterRequest: function() {
			if (this.order == 'asc') {
				this.order = 'desc';
				//console.log("Setting order to: " + this.order);
			} else {
				this.order = 'asc';
				//console.log("Setting order to: " + this.order);
			}
		}

	});

})(jQuery);