

var nodeColor = "#D8E7FF";
var nodeBorderColor = "#000000";

var circleSize = 26;
var wordSize = 11;
var levelSize = 12;

// set up SVG for D3
var width  = 1350,
    height = 500;

var svg = d3.select('body')
  .append('svg')
//  .attr('oncontextmenu', 'return false;')
  .attr('width', width)
  .attr('height', height)
  .style('background', "#ACACAC");

// set up initial nodes and links
//  - nodes are known by 'id', not by index in array.
//  - reflexive edges are indicated on the node (as a bold black circle).
//  - links are always source < target; edge directions are set by 'left' and 'right'.
var nodes = [
    {id: "I", reflexive: false},
    {id: "HAVE", reflexive: true },
    {id: "A", reflexive: false},
    {id: "MONKEY", reflexive: false}
  ],
  links = [
    {source: nodes[0], target: nodes[1], left: false, right: true },
    {source: nodes[1], target: nodes[2], left: false, right: true }
  ];
  
  var words = ["I", "I", "I", "a", "small", "I"];
  var newLinks = [
	  {source: nodes[2], target: nodes[3], left: false, right: true },
	  {source: nodes[2], target: nodes[0], left: false, right: true }
  ]
  
  words.forEach(timeout)
  
function timeout(item) {
	setTimeout(function(){ nodes.push({id: item, reflexive: false}); restart(); }, 500);
}

//setTimeout(function(){ nodes[0] = ({id: "NEW", reflexive: false}); restart(); }, 1000);

newLinks.forEach(timoutLinks)

function timoutLinks(item) {
	setTimeout(function(){ links.push(item); restart(); }, 1500);
}

// init D3 force layout
var force = d3.layout.force()
    .nodes(nodes)
    .links(links)
    .size([width, height])
    .linkDistance(150)
    .charge(-500)
    .on('tick', tick)

// handles to link and node element groups
var path = svg.append('svg:g').selectAll('path'),
    circle = svg.append('svg:g').selectAll('g');

// mouse event vars
var selected_node = null,
    selected_link = null;
//    mousedown_link = null;
//    mousedown_node = null,
//    mouseup_node = null;

// update force layout (called automatically each iteration)
function tick() {
  // draw directed edges with proper padding from node centers
  path.attr('d', function(d) {
    var deltaX = d.target.x - d.source.x,
        deltaY = d.target.y - d.source.y,
        dist = Math.sqrt(deltaX * deltaX + deltaY * deltaY),
        normX = deltaX / dist,
        normY = deltaY / dist,
        sourcePadding = d.left ? circleSize+2 : circleSize-5,
        targetPadding = d.right ? circleSize+2 : circleSize-5,
        sourceX = d.source.x + (sourcePadding * normX),
        sourceY = d.source.y + (sourcePadding * normY),
        targetX = d.target.x - (targetPadding * normX),
        targetY = d.target.y - (targetPadding * normY);
    return 'M' + sourceX + ',' + sourceY + 'L' + targetX + ',' + targetY;
  });

  circle.attr('transform', function(d) {
    return 'translate(' + d.x + ',' + d.y + ')';
  });
}

// update graph (called when needed)
function restart() {
  // path (link) group
  path = path.data(links);

//  // update existing links
//  path.classed('selected', function(d) { return d === selected_link; })
//    .style('marker-start', function(d) { return d.left ? 'url(#start-arrow)' : ''; })
//    .style('marker-end', function(d) { return d.right ? 'url(#end-arrow)' : ''; });
//
//
//  // add new links
//  path.enter().append('svg:path')
//    .attr('class', 'link')
//    .classed('selected', function(d) { return d === selected_link; })
//    .style('marker-start', function(d) { return d.left ? 'url(#start-arrow)' : ''; })
//    .style('marker-end', function(d) { return d.right ? 'url(#end-arrow)' : ''; })
//    .on('mousedown', function(d) {
//      if(d3.event.ctrlKey) return;
//
//      // select link
//      mousedown_link = d;
//      if(mousedown_link === selected_link) selected_link = null;
//      else selected_link = mousedown_link;
//      selected_node = null;
//      restart();
//    });

  // circle (node) group
  // NB: the function arg is crucial here! nodes are known by id, not by index!
  circle = circle.data(nodes, function(d) { return d.id; });

  // add new nodes
  var g = circle.enter().append('svg:g');

  g.append('svg:circle')
    .attr('class', 'node')
    .attr('r', circleSize)
    .style('fill', nodeColor)
    .style('stroke', nodeBorderColor);

  // print node name
  g.append('svg:text')
      .attr('x', 0)
      .attr('y', 2)
      .attr('class', 'id')
	  .style('font-size', wordSize)
      .text(function(d) { return d.id; });
	
	// print node level
	g.append('svg:text')
      .attr('x', 0)
      .attr('y', 16)
      .attr('class', 'id')
	  .style('font-size', levelSize)
	  .style('fill', "black")
      .text("1");

//  // remove old nodes
//  circle.exit().remove();

  // set the graph in motion
  force.start();
}

//function mousedown() {
//
//}
//
//function mousemove() {
//
//}
//
//function mouseup() {
//	}

// only respond once per keydown
var lastKeyDown = -1;

function keydown() {
  d3.event.preventDefault();

  if(lastKeyDown !== -1) return;
  lastKeyDown = d3.event.keyCode;

  // ctrl
  if(d3.event.keyCode === 17) {
    circle.call(force.drag);
    svg.classed('ctrl', true);
  }
}

function keyup() {
  lastKeyDown = -1;

  // ctrl
  if(d3.event.keyCode === 17) {
    circle
      .on('mousedown.drag', null)
      .on('touchstart.drag', null);
    svg.classed('ctrl', false);
  }
}

d3.select(window)
  .on('keydown', keydown)
  .on('keyup', keyup);
restart();