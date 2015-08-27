/**
 * From http://bl.ocks.org/mbostock/4063530
 */
function draw_chart(json_data) {
	// Parse JSON arguments
	var root = JSON.parse(json_data);
	
	var diameter = 960;
	var format = d3.format(",d");

	var pack = d3.layout.pack().size([ diameter - 4, diameter - 4 ]).value(
			function(d) {
				return d.size;
			});

	var svg = d3.select("body").append("svg").attr("width", diameter).attr(
			"height", diameter).append("g").attr("transform", "translate(2,2)");

	var node = svg.datum(root).selectAll(".node").data(pack.nodes).enter()
			.append("g").attr("class", function(d) {
				return d.children ? "node" : "leaf node";
			}).attr("transform", function(d) {
				return "translate(" + d.x + "," + d.y + ")";
			});

	node.append("title").text(function(d) {
		return d.name + (d.children ? "" : ": " + format(d.size));
	});

	node.append("circle").attr("r", function(d) {
		return d.r;
	});

	node.filter(function(d) {
		return !d.children;
	}).append("text").attr("dy", ".3em").style("text-anchor", "middle").text(
			function(d) {
				return d.name.substring(0, d.r / 3);
			});

	node.filter(function(d) {
		return d.children;
	}).append("text").attr("dy", ".3em").style("text-anchor", "middle").style(
			"font-weight", "bold").text(function(d) {
		return d.name;
	});

	d3.select(self.frameElement).style("height", diameter + "px");
}
