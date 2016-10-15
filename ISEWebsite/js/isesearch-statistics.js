//jQuery document ready function
$(function($) {

	$('*').removeClass("highlight");

	$.getJSON('cache/statistics.json', function(data) { 
		
		//get jumbotron info
		var totalNotices = getTotalNotices(data.statistics.totalNotices.grouped.company_str);

		//data for a pie chart with top 20 companies by number of notices published
		var companyNotices = getCompanyValues(data.statistics.companyNotices.facet_counts.facet_fields.company_str);

		//data for a bar chart showing total notices per year 
		//super impose a line chart showing total notices as at time of month
		var yearNotices = getYearValues(data.statistics.yearNotices.facet_counts.facet_dates.datetime);
	
		//JavaScript counts months from 0 to 11. January is 0. December is 11.
		var lastMonth = new Date().getMonth();
	
		var yearNoticesToMonth = getYearValuesToMonth(data.statistics.monthNotices.facet_counts.facet_dates.datetime, lastMonth);

		//data for a filled line chart showing the average notices per month
		var monthNotices = getMonthValues(data.statistics.monthNotices.facet_counts.facet_dates.datetime);
		
		//data for a radar chart with AVERAGE notices on each day of the week (mon - fri)
		//var dayNotices = getDayValues(data.statistics.dayNotices.facet_counts.facet_dates.datetime);
		var aveDayNotices = getAveDayNotices(data.statistics.dayNotices.facet_counts.facet_dates.datetime);

		//data for a line chart with AVERAGE notices per hour
		//var hourNotices = getHourValues(data.statistics.hourNotices.facet_counts.facet_fields.hour);
		var aveHourNotices = getAveHourValues(data.statistics.hourNotices.facet_counts.facet_fields.hour, aveDayNotices.totalNonZeroNoticeDays);

		var directive12Notices = getDirective12Notices(data.statistics.directive12Notices.response);
		$('.totalNoticeDays').html(aveDayNotices.totalNonZeroNoticeDays.toLocaleString('en'));
		$('.averageNoticesPerDay').html(Math.round(totalNotices.notices/aveDayNotices.totalNonZeroNoticeDays).toLocaleString('en'));

		//create each of the charts
		setTotalNotices('.totalNotices', '#totalCompanies', totalNotices);
		createPieChart('#totalCompaniesChart', companyNotices);
		createBarLineChart('#yearChart', yearNotices, yearNoticesToMonth);
		createLineChart('#hourChart', aveHourNotices);
		createRadarChart('#dayChart', aveDayNotices);
		createBarChart('#monthChart', monthNotices);

		//hall of shame below
		$('#hos1').html(directive12Notices.mostRecent[0].name +
			' <span class="small">(on ' + getDatetimeDate(directive12Notices.mostRecent[0].datetime) + ')</span>');
		
		$('#hos2').html(directive12Notices.mostRecent[1].name +
			' <span class="small">(on ' + getDatetimeDate(directive12Notices.mostRecent[1].datetime) + ')</span>');

		$('#hos3').html(directive12Notices.mostRecent[2].name +
			' <span class="small">(on ' + getDatetimeDate(directive12Notices.mostRecent[2].datetime) + ')</span>');

		$('#totalEU12Notices').html(directive12Notices.total);
		$('#hallOfShame').fireworks();

	//end $.getJSON	
	});

//end $(function($)
});


/******************
*  TOTAL NOTICES  *
******************/
function setTotalNotices(id_notices, id_companies, input) {

	$(id_notices).hide();
	$(id_companies).hide();
	$(id_notices).html(Number(input.notices).toLocaleString('en')).fadeIn();
	$(id_companies).html(Number(input.companies).toLocaleString('en')).fadeIn();
}


/******************
*  COMPANIES PIE  *
******************/
function createPieChart(chart_id, input) {
	//console.log("creating pie chart");

	//console.log(chart_id);
	//console.log(input);
	//console.log("-----");

	var data = {
		labels: input.labels,
		datasets: [
			{
				data: input.data,
				backgroundColor:  [
					'rgba(66, 139, 202, 0.9)',
					'rgba(96, 99, 209, 0.9)',
					'rgba(170, 128, 216, 0.9)',
					'rgba(222, 163, 223, 0.9)',
					'rgba(230, 199, 215, 0.9)'
				],
				hoverBackgroundColor: [
					'rgba(66, 139, 202, 1)',
					'rgba(96, 99, 209, 1)',
					'rgba(170, 128, 216, 1)',
					'rgba(222, 163, 223, 1)',
					'rgba(230, 199, 215, 1)'
				]
			}]
	};

	var options = {
		responsive: true,
//		deferred: {           // enabled by default
//			xOffset: 150,     // defer until 150px of the canvas width are inside the viewport
//			yOffset: '50%',   // defer until 50% of the canvas height are inside the viewport
//			delay: 500        // delay of 500 ms after the canvas is considered inside the viewport
//		},
		legend: {
			display: true,
			position: 'right',
			labels: {
				fontColor: 'rgb(255, 99, 132)'
			}
		}
	};

	var ctx = $(chart_id);
	
	var myPieChart = new Chart(ctx,{
		type: 'pie',
		data: data,
		options: options
	});

}


/******************
*  HOURS LINE     *
******************/
function createLineChart(chart_id, input) {
	//console.log("creating line chart");

	//console.log(chart_id);
	//console.log(input);
	//console.log("-----");

	var data = {
		labels: input.labels,
		datasets: [
			{
				data: input.data,
				fill: true,
				pointBorderColor: 'rgba(255, 255, 255, 1)', 
				pointBackgroundColor: 'rgba(176, 176, 176, 1)', 
				backgroundColor: 'rgba(66, 139, 202, 0.5)',
				borderColor: 'rgba(66, 139, 202, 1)',
				lineTension: 0.5,
				steppedLine: false
			}]
	};

	var options = {
		responsive: true,
		deferred: {           // enabled by default
			xOffset: 150,     // defer until 150px of the canvas width are inside the viewport
			yOffset: '50%',   // defer until 50% of the canvas height are inside the viewport
			delay: 500        // delay of 500 ms after the canvas is considered inside the viewport
		},
		legend: {
			display: false,
			position: 'top',
			fullWidth: false,
			labels: {
				fontColor: 'rgb(255, 99, 132)'
			}
		}
	};

	var ctx = $(chart_id);
	
	var myLineChart = new Chart(ctx, {
		type: 'line',
		data: data,
		options: options
	});

}

/******************
*  DAYS RADAR     *
******************/
function createRadarChart(chart_id, input) {
	//console.log("creating radar chart");

	//console.log(chart_id);
	//console.log(input);
	//console.log("-----");

	var data = {
		//labels: input.labels.slice(1,6),
		labels: input.labels,
		datasets: [
			{
				//data: input.data.slice(1,6),
				data: input.data,
				backgroundColor: 'rgba(66, 139, 202, 0.2)',
				borderColor: 'rgba(66, 139, 202, 1)',
				pointBackgroundColor: "rgba(179,181,198,1)",
				pointBorderColor: "#fff",
				pointHoverBackgroundColor: "#fff",
				pointHoverBorderColor: "rgba(179,181,198,1)"
			}]
	};

	var options = {
		responsive: true,
		deferred: {           // enabled by default
			xOffset: 150,     // defer until 150px of the canvas width are inside the viewport
			yOffset: '50%',   // defer until 50% of the canvas height are inside the viewport
			delay: 500        // delay of 500 ms after the canvas is considered inside the viewport
		},
		legend: {
			display: false,
		},
		scale: {
		//	lineArc: true,
		//	reverse: true,
		//	ticks: {
		//		beginAtZero: true,
		//		min: 70,
		//		max: 110
		//	}
		}
	};

	var ctx = $(chart_id);
	
	var myPieChart = new Chart(ctx,{
		type: 'radar',
		data: data,
		options: options
	});

}

/******************
*  MONTH BAR      *
******************/
function createBarChart(chart_id, input) {
	//console.log("creating bar chart");

	//console.log(chart_id);
	//console.log(input);
	//console.log("-----");

	var data = {
		labels: input.labels,
		datasets: [
			{
				data: input.data,
				backgroundColor: 'rgba(66, 139, 202, 0.5)',
				hoverBackgroundColor: 'rgba(66, 139, 202, 0.8)',
				borderColor: 'rgba(66, 139, 202, 1)'
			}]
	};

	var options = {
		responsive: true,
		deferred: {           // enabled by default
			xOffset: 150,     // defer until 150px of the canvas width are inside the viewport
			yOffset: '50%',   // defer until 50% of the canvas height are inside the viewport
			delay: 500        // delay of 500 ms after the canvas is considered inside the viewport
		},
		legend: {
			display: false,
		},
		scales: {
			yAxes: [{
				ticks: {
				max: 4000,
				min: 0,
				}
			}]
		}
	};

	var ctx = $(chart_id);
	
	var myLineChart = new Chart(ctx, {
		type: 'bar',
		data: data,
		options: options
	});

}

/******************
*  YEAR BAR/LINE  *
******************/
function createBarLineChart(chart_id, barInput, lineInput) {
	//console.log("creating bar/line chart");

	//console.log(chart_id);
	//console.log(barInput);
	//console.log(lineInput);
	//console.log("-----");

	var data = {
		labels: barInput.labels,
		datasets: [
			{
				data: barInput.data,
				backgroundColor: 'rgba(66, 139, 202, 0.5)',
				hoverBackgroundColor: 'rgba(66, 139, 202, 0.8)',
				borderColor: 'rgba(66, 139, 202, 1)'
			},
			{
				type: 'line',
				//backgroundColor: 'rgba(66, 139, 202, 0.2)',
				borderColor: 'rgba(66, 139, 202, 1)',
				data: lineInput.data
			}
		]
	};

	var options = {
		responsive: true,
		deferred: {           // enabled by default
			xOffset: 150,     // defer until 150px of the canvas width are inside the viewport
			yOffset: '50%',   // defer until 50% of the canvas height are inside the viewport
			delay: 500        // delay of 500 ms after the canvas is considered inside the viewport
		},
		legend: {
			display: false,
		}
	};

	var ctx = $(chart_id);
	
	var myLineChart = new Chart(ctx, {
		type: 'bar',
		data: data,
		options: options
	});

}


/***************************
*  HELPER CHART FUNCTIONS  *
***************************/


//-------------------------
//getTotalNotices
//-------------------------
function getTotalNotices(json) {
	var totalNotices = {
		notices: 0,
		companies: 0
	};

	$.each(json, function(key, val) {
		if (key == 'matches') { 
			////console.log("key: " + key + ", value: " + val);
			totalNotices.notices = val;
			////console.log(totalNotices.notices);
		}
		
		if (key == 'ngroups') { 
			////console.log("key: " + key + ", value: " + val);
			totalNotices.companies = val;
			////console.log(totalNotices.companies);
		}
	});

	return totalNotices;
}


//-------------------------
//getCompanyValues
//-------------------------
function getCompanyValues(json) {
	var result = { labels: [], data: [] };

	//switch to iterate by 2 steps to allow for "[label, value, label, value, etc...]" in JSON construct
	var label = true;
	$.each(json, function(key, val) {
		////console.log("key: " + key + ", value: " + val);
		if (label == true) {
			result.labels.push(val);
			label = false;
		}
		else {
			result.data.push(val);
			label = true;
		}
	});

	//console.log(arguments.callee.toString().match(/function\s+([^\s\(]+)/) + "()");
	////console.log(result);
	return result;
}


//-------------------------
//getHourValues
//-------------------------
function getHourValues(json) {
	var result = { labels: [], data: [] };

	for (i=0; i<24; i++) {
		result.labels[i] = padTime(i);
		result.data[i] = 0;
	}
	
	var label = true;
	$.each(json, function(key, val) {
		////console.log("key: " + key + ", value: " + val);
		if (label == true) {
			index = parseInt(val);
			label = false
		}
		else {
			result.data[index] = val;
			label = true;
		}
	});

	result.labels = result.labels.slice(6,20);
	result.data = result.data.slice(6,20);

	//console.log(arguments.callee.toString().match(/function\s+([^\s\(]+)/) + "()");
	////console.log(result);
	return result;
}

//AveHourValues
function getAveHourValues(json, totalNonZeroNoticeDays) {
	var result = { labels: [], data: [] };

	for (i=0; i<24; i++) {
		result.labels[i] = padTime(i);
		result.data[i] = 0;
	}
	
	var label = true;
	$.each(json, function(key, val) {
		////console.log("key: " + key + ", value: " + val);
		if (label == true) {
			index = parseInt(val);
			label = false
		}
		else {
			result.data[index] = val;
			label = true;
		}
	});

	result.labels = result.labels.slice(6,20);
	result.data = result.data.slice(6,20);

	for (i=0; i<result.data.length; i++) {
		result.data[i] = Math.round(result.data[i]/totalNonZeroNoticeDays);
	}

	//console.log(arguments.callee.toString().match(/function\s+([^\s\(]+)/) + "()");
	////console.log(result);
	return result;
}

//-------------------------
//getDayValues
//-------------------------
function getDayValues(json) {
	var result = { labels: [], data: [] };

	$.each(json, function(key, val) {
		if (!isNaN(val)) {
			result.labels.push(key);
			result.data.push(val);
		}
	});

	//console.log(arguments.callee.toString().match(/function\s+([^\s\(]+)/) + "()");
	////console.log(result);
	return result;
}


//average notices published per day
//---------------------------------
function getAveDayNotices(json) {
	var result = { 
		labels: ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"],
		data: [0, 0, 0, 0, 0, 0, 0],
		totalDays: 0,
		totalNonZeroNoticeDays: 0,
		totalZeroNoticeDays: 0
	};

	var dayCount = [0, 0, 0, 0, 0, 0, 0];

	$.each(json, function(key, val) {
		if (!isNaN(val)) {
			//label/data values
			var day = new Date(key).getDay();
			dayCount[day]++;
			result.data[day] += val;

			//totalDays
			result.totalDays++;

			//totalNonZeroNoticeDays / totalZeroNoticeDays
			if (val > 0) { result.totalNonZeroNoticeDays++; }
			else { result.totalZeroNoticeDays++; }
		}
	});

	//calculate averages
	for (i=0; i<7; i++) {
		result.data[i] = Math.round(result.data[i]/dayCount[i]);
	}

	//console.log(arguments.callee.toString().match(/function\s+([^\s\(]+)/) + "()");
	////console.log(result);
	return result;
}


//return object with NON-zero day notices
//---------------------------------
function getNoticeDays(resultObject) {
	var noticeDayCount = 0;

	for (i=0; i<resultObject.data.length; i++) {
		if (resultObject.data[i] < 0) {
			noticeDayCount++;
		}
	}
	
	//console.log(arguments.callee.toString().match(/function\s+([^\s\(]+)/) + "()");
	////console.log(result);
	return noticeDayCount;
}

//return object with ZERO count day notices
//---------------------------------
function getZeroNoticeDays(resultObject) {
	var result = { labels: [], data: [] };

	for (i=0; i<resultObject.data.length; i++) {
		if (resultObject.data[i] == 0) {
			result.labels.push(resultObject.labels[i]);
			result.data.push(resultObject.data[i]);
		}
	}
	
	//console.log(arguments.callee.toString().match(/function\s+([^\s\(]+)/) + "()");
	////console.log(result);
	return result;
}

//-------------------------
//getMonthValues
//-------------------------
function getMonthValues(json) {
	var result = { labels: [], data: [] };

	result.labels = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"]
	result.data	= [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]

	yearCount = 0;
	year = 2003;

	$.each(json, function(key, val) {
		if (!isNaN(val)) {
			var currentYear = getDatetimeYear(key);
			var currentMonth = parseInt(getDatetimeMonth(key));

			if (currentYear != year && !isNaN(currentYear)) { 
				////console.log("Year: " + currentYear);
				yearCount++; 
				year = currentYear; 
			}
			
			////console.log("Month: " + currentMonth);
			result.data[currentMonth-1] = result.data[currentMonth-1] + val;
			////console.log("Total: " + result.data[currentMonth-1]);
		}
	});

	var denominator = 1;

	for (i=0; i<12; i++) {
		if (i => Date.getMonth()-1) { 
			denominator = yearCount-1; 
			//console.log(denominator)
		}
		else { denominator = yearCount; }
		
		result.data[i] = Math.round(result.data[i]/denominator);
	}

	//console.log(arguments.callee.toString().match(/function\s+([^\s\(]+)/) + "()");
	////console.log(result);
	return result;
}


//-------------------------
//getYearValues
//-------------------------
function getYearValues(json) {
	var result = { labels: [], data: [] };
		
	$.each(json, function(key, val) {
		if (!isNaN(val)) {
			result.labels.push(getDatetimeYear(key));
			result.data.push(val);
		}
	});

	//console.log(arguments.callee.toString().match(/function\s+([^\s\(]+)/) + "()");
	////console.log(result);
	return result;
}


//getYearValuesToMonth
//---------------------------------
function getYearValuesToMonth(json, month) {
	var result = { labels: [], data: [] };

	thisYear = new Date().getFullYear();
	
	for (i=2003; i<=thisYear; i++) { result.labels.push(i); }
	for (i=0; i<result.labels.length; i++) { result.data.push(0); } 

	yearCount = 0;
	year = 2003;

	$.each(json, function(key, val) {
		if (!isNaN(val)) {
			var currentYear = getDatetimeYear(key);
			var currentMonth = parseInt(getDatetimeMonth(key));

			if (currentYear != year && !isNaN(currentYear)) { 
				yearCount++; 
				year = currentYear; 
			}
			
			if (currentMonth <= month) {
				var index = result.labels.indexOf(parseInt(currentYear));
				result.data[index] = result.data[index] + val;
			}
		}
	});

	//console.log(arguments.callee.toString().match(/function\s+([^\s\(]+)/) + "()");
	////console.log(result);
	return result;
}


//-------------------------
//getDirective12Notices
//-------------------------
function getDirective12Notices(json) {
	var result = { 
		mostRecent: [],
		total: "[total]"
	};

	$.each(json, function(key, val) {
		
		if (key == "numFound") { result.total = val; }

		if (key == "docs") {	
			$.each(val, function(xkey, xval) {
				var company = {
					name: "[company]",
					datetime: "[datetime]",
					URL: "[url]"
				}

				company.name = xval.company;
				company.datetime = xval.datetime;
				company.URL = xval.url;

				result.mostRecent.push(company);
			});
		}
	});

	//console.log(arguments.callee.toString().match(/function\s+([^\s\(]+)/) + "()");
	//console.log(result);
	return result;
}


/*********************
*  HELPER FUNCTIONS  *
*********************/

function getDatetimeYear(datetime) {
	return datetime.substring(0,4);
}

function getDatetimeMonth(datetime) {
	return datetime.substring(5,7);
}

function getDatetimeDay(datetime) {
	return datetime.substring(0,10);
}

function getDatetimeDate(datetime) {
	var date = new Date(datetime);
	var monthNames = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];
	var day = date.getDate();
	var monthIndex = date.getMonth();
	var year = date.getFullYear();

	return day + " " + monthNames[monthIndex] + " " + year;
}

function padTime(n) {
    return (n < 10) ? ("0" + n + ":00") : n + ":00";
}