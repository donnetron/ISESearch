(function($) {

	AjaxSolr.ResultWidget = AjaxSolr.AbstractWidget.extend({
		start: 0,

		//beforeRequest returned (but after a request has been made)
		beforeRequest: function() {
			$(this.target).html($('<img>').attr('src', 'img/ajax-loader.gif')); //show the loading spinner
		},

		facetLinks: function(facet_field, facet_values) {
			var links = [];
			if (facet_values) {
				for (var i = 0, l = facet_values.length; i < l; i++) {
					if (facet_values[i] !== undefined) {
						links.push(
							$('<a href="#"></a>')
							.text(facet_values[i])
							.click(this.facetHandler(facet_field, facet_values[i]))
						);
					} else {
						links.push('no items found in current selection');
					}
				}
			}
			return links;
		},

		facetHandler: function(facet_field, facet_value) {
			var self = this;
			return function() {
				self.manager.store.remove('fq');
				self.manager.store.addByValue('fq', facet_field + ':' + AjaxSolr.Parameter.escapeValue(facet_value));
				self.doRequest();
				return false;
			};
		},

		//   <field name="UID" type="string" indexed="true" stored="true" required="true" multiValued="false" /> 
		//   <field name="company" type="text_general" indexed="true" stored="true" />
		//   <field name="datetime" type="date" indexed="true" stored="true" />
		//   <field name="title" type="string" indexed="true" stored="true" />
		//   <field name="url" type="text_general" indexed="true" stored="true" />
		//   <field name="content" type="text_general" indexed="true" stored="false" />

		afterRequest: function() {
			$(this.target).empty();
			if (this.manager.response.response.docs.length > 0) {
				$("#notices-nav").show();
			}
			else {
				$("#notices-nav").hide();
			}
			
			for (var i = 0, l = this.manager.response.response.docs.length; i < l; i++) {
				var doc = this.manager.response.response.docs[i];
				$(this.target).append(this.template(doc));

		/* 		var items = [];
				items = items.concat(this.facetLinks('company', doc.company));
				items = items.concat(this.facetLinks('title', doc.title));
				items = items.concat(this.facetLinks('datetime', doc.datetime));
				items = items.concat(this.facetLinks('url', doc.url)); */

				/* var $links = $('#links_' + doc.id);
				$links.empty();
				for (var j = 0, m = items.length; j < m; j++) {
					$links.append($('<li></li>').append(items[j]));
				} */
			}
		},

		template: function(doc) {
			var snippet = '';
			if (doc.notice.length > 300) {
				snippet += doc.notice.substring(0, 300);
				snippet += '<span style="display:none;">' + doc.notice.substring(300);
				snippet += '</span> <a href="#" class="more">more</a>';
			} else {
				snippet += doc.notice;
			}

			//uses external library 'moment.js' to prepare a formatted time
			var formattedDate = moment(doc.datetime).format("D MMM YYYY, HH:mm");
			var year = moment(doc.datetime).year();

			var output = '';
			output += '<div class="row">																';
			output += '	<div class="col-xs-1">															';
			output += '		<input id="' + doc.cachefile + '" class="ise-checkbox" type="checkbox">			';
			output += '	</div>																			';
			output += '	<div class="col-xs-2">															';
			output += '		<p class="notice-date">' + formattedDate + '</p>							';
			output += '	</div>																			';
			output += '	<div class="col-xs-2">															';
			output += '		<p class="notice-company">' + doc.company + '</p>							';
			output += '	</div>																			';
			output += '	<div class="col-xs-7">															';
			output += '		<p class="notice-title">' + doc.title + '</p>								';
			output += '		<p>	<a class="notice-url" target="_blank" href="' + doc.url + '">' + doc.url + '</a>		';
			output += '			<a class="notice-cached" target="_blank" href="cache/' + year + '/' + doc.cachefile + '.html"> (cached)</a>			';
			output += '		</p>																		';
			output += '		<p class="notice-text">' + snippet + ' ...</p>								';
			output += '	</div>																			';
			output += '</div>																			';
			output += '<hr>						 														';

			return output;
		},

		init: function() {
			$(document).on('click', 'a.more', function() {
				var $this = $(this),
					span = $this.parent().find('span');

				if (span.is(':visible')) {
					span.hide();
					$this.text('more');
				} else {
					span.show();
					$this.text('less');
				}

				return false;
			});
		}
	});

})(jQuery);