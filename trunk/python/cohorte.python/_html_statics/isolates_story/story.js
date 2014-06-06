// -------------------------------------------------------------------------
// Configuration variables

// Take 80% of the window
var svg_width = window.innerWidth * .8;
var svg_height = window.innerHeight * .8;

function draw_chart(container_id, data) {
	// -------------------------------------------------------------------------
	// Indexing

	/* Index names */
	var component_names = [];
	var all_isolate_names = [];
	var all_distributions = [];

	data.forEach(function(component) {
		component_names.push(component.name);
		component.scenes.forEach(function(scene) {
			if (all_isolate_names.indexOf(scene.isolate) == -1) {
				all_isolate_names.push(scene.isolate);
			}

			if (all_distributions.indexOf(scene.distribution) == -1) {
				all_distributions.push(scene.distribution);
			}
		});
	});

	/* Sort isolate names before indexing their names */
	all_isolate_names.sort();
	all_distributions.sort();

	// -------------------------------------------------------------------------
	// Preparing the drawing Area

	/* Drawing area */
	var margin = {
		top : 20,
		right : 20,
		bottom : 30,
		left : 100
	};
	var width = svg_width - margin.left - margin.right;
	var height = svg_height - margin.top - margin.bottom;

	/* Lines colors */
	var color = d3.scale.category20();

	/* Select the SVG element */
	var svg = d3.select(container_id).append("svg").attr("width",
			width + margin.left + margin.right).attr("height",
			height + margin.top + margin.bottom).append("g").attr("transform",
			"translate(" + margin.left + "," + margin.top + ")");

	// -------------------------------------------------------------------------
	// Preparing the axis

	/* Scales */
	var x = d3.scale.ordinal().rangePoints([ 0, width ]).domain(
			all_distributions);
	var y = d3.scale.ordinal().rangePoints([ height, 0 ]).domain(
			all_isolate_names);

	/* Axis labels */
	var xAxis = d3.svg.axis().scale(x).orient("bottom");
	var yAxis = d3.svg.axis().scale(y).orient("left");

	// -------------------------------------------------------------------------
	// Draw

	/* Create axis legends */
	svg.append("g").attr("class", "x axis").attr("transform",
			"translate(0," + height + ")").call(xAxis);

	svg.append("g").attr("class", "y axis").call(yAxis).append("text").attr(
			"transform", "rotate(-90)").attr("y", 6).attr("dy", ".71em").style(
			"text-anchor", "end").text("Isolate ID");

	/* Define a generic line */
	var line = d3.svg.line().x(function(d) {
		return x(d.distribution);
	}).y(function(d) {
		return y(d.isolate);
	}).interpolate("linear");

	/* Create a path per component */
	var svgContainer = svg.append("g");
	data.forEach(function(component) {
		// The line SVG Path we draw
		svgContainer.append("path").attr("d", line(component.scenes)).attr(
				"stroke", color(component_names.indexOf(component.name))).attr(
				"stroke-width", 2).attr("fill", "none");
	});
}
