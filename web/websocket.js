//window.onload = init;
var socket = new WebSocket("ws://localhost:8080/artificial-neural-network-datasets/actions");
socket.onmessage = onMessage;

//var nodeColor = "#FFFFFF";
var nodeBorderColor = "#000";
var circleSize = 7;
var wordSize = 14;
var levelSize = 8;
var pathId = 1;

//COEFF
var coeffSize = 12;
var coeffWordSize = 10;
var coeffColor = "#50FF73";

var lineDefColor = "#000";
var lineActiveColor = "#FFF700";

var radius = 25;

// set up SVG for D3
var width = window.screen.availWidth-12,
//    height = window.screen.availHeight-210;
    height = window.screen.availHeight-275;
var svg = d3.select('body')
    .append('svg')
    .attr('width', width)
    .attr('height', height)
    .style('background', "#ACACAC");


var index = 9;
var action = "add";

var nodes = [],
    links = [];

// handles to link and node element groups
var path = svg.append('svg:g').selectAll('path'),
    circle = svg.append('svg:g').selectAll('g'),
	coeff = svg.append('svg:g').selectAll('c');

// mouse event vars
var selected_node = null,
    selected_link = null;
//    mousedown_link = null;
//    mousedown_node = null,
//    mouseup_node = null;

var currentNode;

force = d3.layout.force()
        .nodes(nodes)
        .links(links)
        .size([width, height])
        .linkDistance(100)
		.linkStrength(0.0003)
        .charge(-8)
//        .charge(-200)
		.gravity(0.0003)
        .on('tick', tick);

function refresh() {
//	path = svg.append('svg:g').selectAll('path');
//	circle = svg.append('svg:g').selectAll('g');
	
	    force = d3.layout.force()
        .nodes(nodes)
        .links(links)
        .size([width, height])
        .linkDistance(100)
		.linkStrength(0.0003)
        .charge(-8)
//        .charge(-200)
		.gravity(0.0003)
        .on('tick', tick);

    restart("add");
}

function onMessage(event) {
	var object = JSON.parse(event.data);
	currentNode = object;
	
    switch (object.action) {
        case "add":
			refresh();
            printNodeElement(object);
            break;
        case "addSentence":
            printNewSentenceElement(object);
            break;
		case "updateSentence":
            printAddWordToSentence(object);
            break;
        case "removeSentence":
			printRemoveSentence(object);
//			document.getElementById(object.id).remove();
//			object.parentNode.removeChild(object);
            break;
        case "update":
            printUpdatedNode(object);
            break;
		case "addLines":
			printAddLines(object);
			break;
		case "updateLines":
			printUpdateLines(object);
			break;
		case "activeNeuron":
			printActiveNeuron(object);
			break;
		case "resetNodes":
			restart("resetNodes");
			break;
		case "updateBestLines":
			printUpdateBestLines(object);
			break;
		case "resetLines":
			printResetLines(object);
    }
}

function startCreatingGraphMonkey() {
    var StartAction = {
        action: "start",
		name: "monkey",
        neurons: document.getElementById("neurons").value,
		speed: document.getElementById("speed").value,
		speedActive: document.getElementById("speedActive").value,
		objectParameters: document.getElementById("objectParameters").value
    };
    socket.send(JSON.stringify(StartAction));
}

function startCreatingGraphFromLog() {
	    var StartAction = {
        action: "startFromLog",
		name: "monkey",
        neurons: document.getElementById("neurons").value,
		speed: document.getElementById("speed").value,
		speedActive: document.getElementById("speedActive").value,
		objectParameters: document.getElementById("objectParameters").value
    };
    socket.send(JSON.stringify(StartAction));
}

function startCreatingGraphTest() {
    var StartAction = {
        action: "start",
		name: "test",
        neurons: document.getElementById("neurons").value,
		speed: document.getElementById("speed").value,
		speedActive: document.getElementById("speedActive").value,
    };
    socket.send(JSON.stringify(StartAction));
}

function updateGraph() {
    var UpdateAction = {
        action: "update",
		word: document.getElementById("speed").value,
		neurons: document.getElementById("neurons").value,
		speed: document.getElementById("speed").value,
		speedActive: document.getElementById("speedActive").value
    };
    socket.send(JSON.stringify(UpdateAction));
}

function updateMode() {
    var UpdateAction = {
        action: "updateMode",
		mode: document.getElementById("selectedMode").value,
		speed: document.getElementById("speed").value
    };
    socket.send(JSON.stringify(UpdateAction));
}

function resetPage() {
    location.reload();
}

function submitData() {
	var SubmitData = {
		action: "submitData",
		mode: document.getElementById("selectedMode").value,
		speed: document.getElementById("speed").value,
		objectParameters: document.getElementById("objectParameters").value
	};
	alert("Dane wprowadzone");
	socket.send(JSON.stringify(SubmitData));
}

function resetLines() {
	var ResetLinesAction = {
        action: "resetLines"
    };
    socket.send(JSON.stringify(ResetLinesAction));
}

function stopAndStart() {
	alert("STOP");
	var stopAndStart = {
        action: "stopAndStart"
    };
    socket.send(JSON.stringify(stopAndStart));
}

function printNodeElement(node) {
    nodes.push(
        {id: node.id, name: node.name, level: node.level, attribute: node.attribute}
    );
	
    restart("add");
}

function isInArray(source, target, array) {
  	for (var k = 0; k<array.length; k++) {
		if (array[k].source===source && array[k].target===target) {
			return true;
		}
	}
	return false;
}

function printAddLines(node) {
	var neigh = node.neighbours.split(" ");
	var coeff = node.coeff.split(" ");
//	console.log(currentNode.name);
//	console.log(neigh);
//	console.log(coeff);
	
	
	for (i=1; i<neigh.length; i++) {

		var lookup = {};
		for (var k = 0; k<nodes.length; k++) {
			lookup[nodes[k].name] = nodes[k];
		}	
		
		if(!isInArray(lookup[node.name],lookup[neigh[i]],links)) {
			links = links.concat(
				{id:lookup[node.name].name+lookup[neigh[i]].name, name:lookup[node.name].name, source: lookup[node.name], target: lookup[neigh[i]], coeff: coeff[i],left: false, right: true}
			);
//			console.log(lookup[node.name].name+lookup[neigh[i]].name);
			restart("addLine", lookup[node.name].name+lookup[neigh[i]].name)
		}
	}

//restart("addLine");
}

function printResetLines(nodes) {
	restart("resetLines");
}

function printUpdateLines(node) {
	restart("updateLine");
}

function printUpdateBestLines() {
	restart("updateBestLine");
}

function printNewSentenceElement(sentence) {

    //Remove old sentence if exist
    if (document.getElementById(sentence.id - 1) !== null) {
        document.getElementById(sentence.id - 1).remove();
    }

    var sentenceHTML = document.getElementById("sentenceHTML");

    var sentenceDiv = document.createElement("div");
    sentenceDiv.setAttribute("id", sentence.id);
    sentenceHTML.appendChild(sentenceDiv);

    var sentenceName = document.createElement("span");
    sentenceName.setAttribute("class", "sentenceName");
    sentenceName.innerHTML = sentence.name;
    sentenceName.style.fontSize = "12px";
    sentenceName.style.fontFamily = "Comic Sans MS";
    sentenceName.style.color = currentNode.color;
//    sentenceName.className = "animationSentence";
    sentenceDiv.appendChild(sentenceName);
}

function printAddWordToSentence(word) {
    var sentenceHTML = document.getElementById("sentenceHTML");

//    var sentenceDiv = document.createElement("div");
//    sentenceDiv.setAttribute("id", word.id);
//    sentenceHTML.appendChild(sentenceDiv);

	var sentenceDiv = document.getElementById(word.id);
	sentenceHTML.appendChild(sentenceDiv);
	
    var sentenceName = document.createElement("span");
    sentenceName.setAttribute("class", "sentenceName");
    sentenceName.innerHTML = word.name + " | ";
    sentenceName.style.fontSize = "10px";
    sentenceName.style.fontFamily = "Comic Sans MS";
    sentenceName.style.color = currentNode.color;
//    sentenceName.style.color = "red";
    sentenceName.className = "animationSentence";
    sentenceDiv.appendChild(sentenceName);
}

function printRemoveSentence(sentence) {
	//Remove old sentence if exist
    if (document.getElementById(sentence.id) !== null) {
        document.getElementById(sentence.id).remove();
    }
}

function printUpdatedNode(node) {
    restart("update",node);
}

function printActiveNeuron(node) {
	
	var mainPart = document.getElementById("nodeMain"+node.name);
	if (node.attribute==="CLASS" || node.attribute==="") {
		mainPart.style.fill="yellow";
	} else {
		mainPart.style.fill="#00FF33";
	}
}

function tick() {
    path.attr('d', function (d) {
        var deltaX = d.target.x - d.source.x,
            deltaY = d.target.y - d.source.y,
            dist = Math.sqrt(deltaX * deltaX + deltaY * deltaY),
            normX = deltaX / dist,
            normY = deltaY / dist,
            sourcePadding = d.left ? circleSize + 2 : circleSize - 5,
            targetPadding = d.right ? circleSize + 2 : circleSize - 5,
            sourceX = d.source.x + (sourcePadding * normX),
            sourceY = d.source.y + (sourcePadding * normY),
            targetX = d.target.x - (targetPadding * normX),
            targetY = d.target.y - (targetPadding * normY);
        return 'M' + sourceX + ',' + sourceY + 'L' + targetX + ',' + targetY;
    });

	if (document.getElementById("selectedMode").value==="CHAOS") {
		circle.attr("cx", function(d) { return d.x = Math.max(radius, Math.min(width - radius, d.x)); })
        .attr("cy", function(d) { return d.y = Math.max(radius, Math.min(height - radius, d.y)); });
	} else if (document.getElementById("selectedMode").value==="ORDER") {
		circle.attr("cx", function(d) { 
		if(d.attribute==="CLASS") {
			return d.x = Math.max(3*width/7+5, Math.min(4*width/7, d.x)); 
		} else if (d.attribute==="") {
			return d.x = Math.max(radius, Math.min(width - radius, d.x));
		} else if (d.attribute==="LL") {
			return d.x = Math.max(radius, Math.min(3*width/7 - radius, d.x));
		} else if (d.attribute==="LW") {
			return d.x = Math.max(4*width/7 + radius, Math.min(width - radius, d.x));
		} else if (d.attribute==="PL") {
			return d.x = Math.max(radius, Math.min(width/2 - radius, d.x));
		} else if (d.attribute==="PW") {
			return d.x = Math.max(width/2 + radius, Math.min(width - radius, d.x));
		}
		})
        .attr("cy", function(d) { 
		if(d.attribute==="CLASS") {
			return d.y = Math.max(radius, Math.min(radius, d.y));
		} else if (d.attribute==="") {
			return d.y = Math.max(height/2, Math.min(height/2, d.y));
		} else if (d.attribute==="LL") {
			return d.y = Math.max(radius, Math.min(radius, d.y));
		} else if (d.attribute==="LW") {
			return d.y = Math.max(radius, Math.min(radius, d.y));
		} else if (d.attribute==="PL") {
			return d.y = Math.max(height-radius, Math.min(height-radius, d.y));
		} else if (d.attribute==="PW") {
			return d.y = Math.max(height-radius, Math.min(height-radius, d.y));
		}
	});
	}

//	circle.attr("cx", function(d) { return d.x = Math.max(radius, Math.min(width - radius, d.x)); })
//        .attr("cy", function(d) { return d.y = Math.max(radius, Math.min(height - radius, d.y)); });
	

//	coeff.attr("cx", function(d) { return d.x; })
//		.attr("cy", function(d) { return d.y; });
	coeff.attr("cx", function(d) { return d.x; })
		.attr("cy", function(d) { return d.y; });
	
    path.attr("x1", function(d) {  return d.source.x; })
        .attr("y1", function(d) {  return d.source.y; })
        .attr("x2", function(d) {  return d.target.x; })
        .attr("y2", function(d) {  return d.target.y; });

    circle.attr('transform', function (d) {
        return 'translate(' + d.x + ',' + d.y + ')';
	});
	
	coeff.attr('transform', function (d) {
//		return 'translate(' + d.x + ',' + d.y + ')';
		return 'translate(' + (d.source.x*1.2+d.target.x*0.8)/2 + ',' + (d.source.y*1.2+d.target.y*0.8)/2 + ')';
    });
//	}
}

function isInSingleArray(coeffX, coeffY, array) {
	for (var k = 0; k<array.length; k++) {
		if (array[k].x===coeffX && array[k].y===coeffY) {
			return false;
		}
	}
	return true;
}

function isInArray(source, target, array) {
  	for (var k = 0; k<array.length; k++) {
		if (array[k].source===source && array[k].target===target) {
			return true;
		}
	}
	return false;
}

// update graph (called when needed)
function restart(action, idPath) {
	
	if(action === "addLine") {
		
				var name = '';
		if (currentNode!==undefined) {
			name = currentNode.name;
		}
		
		// path (link) group
    path = path.data(links);

	var ccc = 0;
	
    // add new links
    path.enter().append('svg:path')
        .attr('class', 'link')
		.attr('id', 'path'+idPath)
		.style('stroke', lineDefColor)
//		.style('stroke-width', 2)
        .classed('selected', function (d) {
            return d === selected_link;
        })
        .style('marker-start', function (d) {
            return d.left ? 'url(#start-arrow)' : '';
        })
        .style('marker-end', function (d) {
            return d.right ? 'url(#end-arrow)' : '';
        })
				.text(function(d) { 
					ccc = d.coeff; 
		})
        .on('mousedown', function (d) {
            if (d3.event.ctrlKey) return;

            // select link
            mousedown_link = d;
            if (mousedown_link === selected_link) selected_link = null;
            else selected_link = mousedown_link;
            selected_node = null;
        });
	
	}
    
	// circle (node) group
    // NB: the function arg is crucial here! nodes are known by id, not by index!
    circle = circle.data(nodes, function (d) {
        return d.id;
    });
	
    // add new nodes
    if (action === "add") {
		
        var g = circle.enter().append('svg:g');
		
		var name = '';
		var attribute = '';
		if (currentNode!==undefined) {
			name = currentNode.name;
			attribute = currentNode.attribute;
		}
		
		var cSize = circleSize + 10;
		var nodeColor="#FFF";
		switch(attribute) {
			case "LL":
				nodeColor="#FFBD4A";
				break;
			case "LW":
				nodeColor="#D0FFD6";
				break;
			case "PL":
				nodeColor="#73BEFF";
				break;
			case "PW":
				nodeColor="#FFCCFF";
				break;
			case "CLASS":
				nodeColor="#A0A0A0";
				cSize = cSize+10;
				break;	
			case "":
				nodeColor="#FF9999";
				break;
			defaulty:
				nodeColor="#FFF";
				break;
		}
		
		
		
		if (currentNode.attribute==="") {
			g.append('svg:circle')
			.attr('id', "nodeMain" + name)
            .attr('class', 'node')
            .attr('r', cSize)
            .style('fill', nodeColor)
            .style('stroke', nodeBorderColor);
		} else {
			g.append('svg:circle')
			.attr('id', "nodeMain" + name)
            .attr('class', 'node')
            .attr('r', cSize)
            .style('fill', nodeColor)
            .style('stroke', nodeBorderColor);
		}
        

		var mainPart = document.getElementById("nodeMain"+name);

        // print node name
        g.append('svg:text')
			.attr('id', "nodeWord" + name )
            .attr('x', 0)
            .attr('y', 4)
            .attr('class', 'id')
            .style('font-size', wordSize)
            //      .text(nodes[0].name);
            .text(name.replace(attribute,""));
//            .text(function(d) { return d.name; });

        // print node attribute
        g.append('svg:text')
			.attr('id', "nodeAttribute" + attribute)
            .attr('x', 0)
            .attr('y', 12)
            .attr('class', 'id')
            .style('font-size', levelSize)
            .style('fill', "black")
                  .text(function(d) { return d.attribute; });

		
		


//  // remove old nodes
        circle.exit().remove();

        // set the graph in motion
        force.start();

    } else if (action === "update") {

//		var pathName = document.getElementById("path"+name);
//		pathName.style.stroke = lineActiveColor;
		
		var name = '';
		if (currentNode!==undefined) {
			name = currentNode.name;
		}
		
		var mainPart = document.getElementById("nodeMain"+name);
		
		var wordPart = document.getElementById("nodeWord"+name);
		
		var levelPart = document.getElementById("nodeAttribute"+attribute);
		
		var multCS = 4;
		var multWS = 2;
		var paramY = levelPart.getAttribute("y");
		
		switch(currentNode.attribute) {
			case "ll":
				mainPart.style.fill="#FFBD4A";
				break;
			case "lw":
				mainPart.style.fill="#73FF86";
//				mainPart.setAttribute("r",circleSize+multCS*1);
//				wordPart.style.font = (wordSize+multWS)+"px arial";
//				levelPart.style.font = (levelSize+multWS)+"px arial";
//				for(var k = 0; k<multWS*1; k++) {
//					paramY++;
//				}
//				levelPart.setAttribute("y",paramY);
				break;
			case "pl":
				mainPart.style.fill="#AFD8FC";
//				wordPart.style.font = (wordSize+multWS)+"px arial";
//				levelPart.style.font = (levelSize+multWS)+"px arial";
//				for(var k = 0; k<multWS*1; k++) {
//					paramY++;
//				}
//				levelPart.setAttribute("y",paramY);
				break;
			case "pw":
				mainPart.style.fill="#FCAFD8";
//				wordPart.style.font = (wordSize+multWS)+"px arial";
//				levelPart.style.font = (levelSize+multWS)+"px arial";
//				for(var k = 0; k<multWS*1; k++) {
//					paramY++;
//				}
//				levelPart.setAttribute("y",paramY);
				break;
			case "a":
				mainPart.style.fill="#F9FF36";
//				wordPart.style.font = "bold " +(wordSize+multWS)+"px arial";
//				levelPart.style.font = (levelSize+multWS)+"px arial";
//				for(var k = 0; k<multWS*1; k++) {
//					paramY++;
//				}
//				levelPart.setAttribute("y",paramY);
				break;
			case "class":
				mainPart.style.fill="#FFA4A4";
//				mainPart.setAttribute("r",circleSize+multCS);
//				wordPart.style.font = "bold " +(wordSize+multWS)+"px arial";
//				levelPart.style.font = (levelSize+multWS)+"px arial";
//				for(var k = 0; k<multWS*1; k++) {
//					paramY++;
//				}
//				levelPart.setAttribute("y",paramY);
				break;	
			defaulty:
				mainPart.style.fill="#FFFFFF";
//				mainPart.setAttribute("r",circleSize+multCS);
//				wordPart.style.font = (wordSize+multWS)+"px arial";
//				levelPart.style.font = (levelSize+multWS)+"px arial";
//				for(var k = 0; k<multWS*1; k++) {
//					paramY++;
//				}
//				levelPart.setAttribute("y",paramY);
				break;
		}
		
		var textnode = document.getElementById("nodeAttribute"+attribute);
		textnode.textContent = currentNode.level;
		
		
		
        // set the graph in motion
//        force.start();
		
//		refresh();
    } else if (action==="updateLine") {
		
		var name = '';
		if (currentNode!==undefined) {
			name = currentNode.name;
		}
			
			var neigh = currentNode.neighbours.split(" ");
//			console.log(neigh);
			for(var i=1; i<neigh.length; i++) {
//				var singlePath = document.getElementById("path" + paths[i].id);
//				console.log(neigh[i]);
				var singlePath = document.getElementById("path" + neigh[i] + currentNode.name);
				singlePath.style.stroke=currentNode.color;
//				singlePath.style.stroke="000";
				if (currentNode.color==="red") {
					singlePath.style['stroke-width']="2px";
				} else {
					singlePath.style['stroke-width']="1px";
				}
				
//				(function(capturedI) {
//					setTimeout(function(){ 
//						var sPath = document.getElementById("path" + neigh[capturedI] + currentNode.name);
////						
////						console.log(neigh[capturedI] + currentNode.name);
//						sPath.style.stroke="#000";
//						sPath.style['stroke-width']="2px";}, 1500);
//				
//				})(i);
			
			}
			
			
	} else if (action === "updateBestLine") {
				var name = '';
		if (currentNode!==undefined) {
			name = currentNode.name;
		}
			
			var neigh = currentNode.neighbours.split(" ");
//			console.log(neigh);
			for(var i=1; i<neigh.length; i++) {
//				var singlePath = document.getElementById("path" + paths[i].id);
//				console.log(neigh[i]);
				var singlePath = document.getElementById("path" + neigh[i] + currentNode.name);
//				var singlePath = document.getElementById("pathVERYCLEVER");
				singlePath.style.stroke="red";
				singlePath.style['stroke-width']="8px";
				(function(capturedI) {
					setTimeout(function(){ 
						var sPath = document.getElementById("path" + neigh[capturedI] + currentNode.name);
//						console.log(neigh[capturedI] + currentNode.name);
						sPath.style.stroke="#000";
						sPath.style['stroke-width']="2px";}, 5000);
				
				})(i);
			
			}
	} else if(action==="resetLines") {
			var result = links.filter(function( obj ) {
				return obj.name;

			});

			for(var i=0; i<result.length; i++) {
				var singlePath = document.getElementById("path" + result[i].id);
				singlePath.style.stroke="#000";
				singlePath.style['stroke-width']="1px";
			}
	} else if(action==="resetNodes") {
		var singleNode = document.getElementById("nodeMain" + currentNode.name);
		
		var nodeColor="#FFF";
		console.log(currentNode);
		console.log(currentNode.attribute);
		switch(currentNode.attribute) {
			case "LL":
				nodeColor="#FFBD4A";
				break;
			case "LW":
				nodeColor="#D0FFD6";
				break;
			case "PL":
				nodeColor="#73BEFF";
				break;
			case "PW":
				nodeColor="#FFCCFF";
				break;
			case "CLASS":
				nodeColor="#A0A0A0";
				break;	
			case "":
				nodeColor="#FF9999";
				break;
			defaulty:
				nodeColor="#FFF";
				break;
		}
		singleNode.style.fill=nodeColor;
	}
}

// only respond once per keydown
var lastKeyDown = -1;

function keydown() {
    d3.event.preventDefault();

    if (lastKeyDown !== -1) return;
    lastKeyDown = d3.event.keyCode;

    // ctrl
    if (d3.event.keyCode === 17) {
        circle.call(force.drag);
        svg.classed('ctrl', true);
    }
	
	if (d3.event.keyCode === 82) {
		refresh();
	}
}

function keyup() {
    lastKeyDown = -1;

    // ctrl
    if (d3.event.keyCode === 17) {
//        circle
//            .on('mousedown.drag', null)
//            .on('touchstart.drag', null);
//        svg.classed('ctrl', false);
        circle.call(force.drag);
        svg.classed('ctrl', true);
    }
	
	if (d3.event.keyCode === 82) {
		refresh();
	}
}

d3.select(window)
//    .on('keydown', keydown)
  .on('keyup', keyup);
//restart("add");
//refresh();

