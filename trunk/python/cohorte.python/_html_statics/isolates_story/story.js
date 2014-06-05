// -----------------------------------------------------------------------------
// Configuration variables

// Take 80% of the window
var svg_width = window.innerWidth * .8;
var svg_height = window.innerHeight * .8;

// -----------------------------------------------------------------------------
// Parsing

/* Parse data */
// var json = '[{"name": "compo_1", "scenes": [ {"distribution": 1,
// "isolate_id": 1}, {"distribution": 2, "isolate_id": 3} ] }, {"name":
// "compo_2", "scenes": [ {"distribution": 1, "isolate_id": 2} ] }]';
// var data = JSON.parse(json);
// The data for our line
var data = [ {
	"name" : "A",
	"scenes" : [ {
		"distribution" : 1,
		"isolate" : "central"
	}, {
		"distribution" : 2,
		"isolate" : "central"
	}, {
		"distribution" : 4,
		"isolate" : "auto04"
	} ]
}, {
	"name" : "B",
	"scenes" : [ {
		"distribution" : 1,
		"isolate" : "central"
	}, {
		"distribution" : 2,
		"isolate" : "auto03"
	}, {
		"distribution" : 3,
		"isolate" : "auto02"
	}, {
		"distribution" : 4,
		"isolate" : "auto04"
	} ]
} ];

// -----------------------------------------------------------------------------
// Indexing

/* Index names */
var component_names = [];
var all_isolate_names = [];

data.forEach(function(component) {
	component_names.push(component.name);
	component.scenes.forEach(function(scene) {
		if (all_isolate_names.indexOf(scene.isolate) == -1) {
			all_isolate_names.push(scene.isolate);
		}
	});
});

/* Sort isolate names before indexing their names */
all_isolate_names.sort();

// -----------------------------------------------------------------------------
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
var svg = d3.select("#chart").append("svg").attr("width",
		width + margin.left + margin.right).attr("height",
		height + margin.top + margin.bottom).append("g").attr("transform",
		"translate(" + margin.left + "," + margin.top + ")");

// -----------------------------------------------------------------------------
// Preparing the axis

/* Set axis domains */
var max_x = 0, min_x = 1;
var max_y = 0, min_y = 1;
data.forEach(function(component) {
	component.scenes.forEach(function(scene) {
		min_x = Math.min(min_x, scene.distribution);
		max_x = Math.max(max_x, scene.distribution);
		min_y = Math.min(min_y, scene.isolate);
		max_y = Math.max(max_y, scene.isolate);
	});
});

/* Scales */
var x = d3.scale.ordinal().rangePoints([ 0, width ]).domain(
		d3.range(min_x, max_x + 1));
var y = d3.scale.ordinal().rangePoints([ height, 0 ]).domain(all_isolate_names);

/* Axis labels */
var xAxis = d3.svg.axis().scale(x).orient("bottom");
var yAxis = d3.svg.axis().scale(y).orient("left");

// -----------------------------------------------------------------------------
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
