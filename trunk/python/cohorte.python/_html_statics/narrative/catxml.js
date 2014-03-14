
var comics_dir = "https://dl.dropbox.com/u/10053530/comics/";
//var comics_dir = "/Users/nancy/Documents/Fall2012/URA/code/project/comics/";
var transitions_xml = comics_dir + "data/transitions.xml";
var tags_xml_path = comics_dir + "data/tags.xml";

function get_xml(path) {
    var request = new XMLHttpRequest();
    request.open('GET', path, false);
    request.send(); // because of "false" above, will block until the request is done 
    // and status is available. Not recommended, however it works for simple cases.

    
    if (request.status === 200) {
	//console.log("read " + path);
	//console.log(request.responseText);
	return request.responseXML;
    } else {
	alert("ERROR: Couldn't retrieve xml at " + path + "; error status: " + request.status);
    }
    
}

function get_attribute(element, attr_name, def_value) {
    var value = element.getAttribute(attr_name);
    if (value == null) return def_value
    return value
}

// Returns the name of a comic page whose number is page_number, 
// taking into account the padding of the page number and if the basename
// of the comic is prepended or appended to the number to create the name.
function number_to_name(page_number, comic_settings) {
    var num_str = page_number.toString();
    var num_length = num_str.length;
    while (num_length < comic_settings.pad_to) {
	num_str = "0" + num_str;
	num_length += 1;
    }
    var page_name = num_str;
    if (comic_settings.path_creation == "prepend") {
	page_name = comic_settings.basename + page_name;
    } else if (comic_settings.path_creation == "append") {
	page_name = page_name + comic_settings.basename;
    }
    return page_name
}

// class ComicOptions
function ComicOptions(folder_path) {
    var options_xml = get_xml(folder_path + "/options.xml");
    var el = options_xml.getElementsByTagName("folder-options")[0];
    this.basename = el.attributes.getNamedItem("basename").nodeValue;
    this.pad_to = el.attributes.getNamedItem("number-padding").nodeValue;
    this.path_creation = el.attributes.getNamedItem("path-creation").nodeValue;
    this.first_page = el.attributes.getNamedItem("first-page").nodeValue;
    this.ext = el.attributes.getNamedItem("ext").nodeValue;
}

// class Transition
function Transition(trans_xml) {
    //console.log(trans_xml);
    this.from = parseInt(trans_xml.attributes.getNamedItem("from-id").nodeValue);
    this.to = parseInt(trans_xml.attributes.getNamedItem("to-id").nodeValue);
    this.type = parseInt(trans_xml.attributes.getNamedItem("type").nodeValue);
}

// class Vertex
function Vertex(x, y) {
    this.x = x;
    this.y = y;
}

// class Character
function Character(char_xml) {
    //console.log(char_xml);
    this.name = char_xml.getAttribute("name");
    this.id = parseInt(char_xml.getAttribute("id"));
    this.group = parseInt(char_xml.getAttribute("group"));
}

// class TagDef
function TagDef(tag_xml) {
    this.name = tag_xml.getAttribute("name");
    this.id = parseInt(tag_xml.getAttribute("id"));
    this.description = get_attribute(tag_xml, "description", "");
}

// class Tag
function Tag(tag_xml) {
    this.id = parseInt(tag_xml.getAttribute("id"));
    this.confidence = parseInt(get_attribute(tag_xml, "confidence", 10));
    this.intensity = parseInt(get_attribute(tag_xml, "intensity", 10));
}

// class Panel
function Panel(panel_xml) {
    //console.log("panel xml");
    // Vertices
    this.vertices = [];
    var v_elms = panel_xml.getElementsByTagName("vertex");
    for (var i = 0; i < v_elms.length; i++) {
	x = parseFloat(v_elms[i].attributes.getNamedItem("x").nodeValue);
	y = parseFloat(v_elms[i].attributes.getNamedItem("y").nodeValue);
	this.vertices[i] = new Vertex(x, y);
    }

    this.char_count = parseInt(get_attribute(panel_xml, "charcount", 0));
    this.depth = parseInt(get_attribute(panel_xml, "depth", 0));
    this.number = parseInt(get_attribute(panel_xml, "number", -1));

    // Tags
    this.tags = [];
    var tag_elms = panel_xml.getElementsByTagName("tag");
    for (var i = 0; i < tag_elms.length; i++) {
	this.tags[i] = new Tag(tag_elms[i]);
    }

    // Characters
    this.characters = [];
    var char_elm = panel_xml.getElementsByTagName("characters")[0];
    if (char_elm != undefined) {
	var char_str = char_elm.textContent;
	//console.log("chars: " + char_str);
	//var b = false;
	if (char_str) {
	    //console.log("if chars: " + b);
	    //console.log("length: " + char_str.length);
	    var char_list = char_str.split(",");
	    for (var i = 0; i < char_list.length; i++) {
		var c = parseInt(char_list[i]);
		if (c != NaN) {
		    this.characters[i] = c;
		    //console.log("char " + i + ": " + this.characters[i]);
		} else {
		    break;
		}
	    } // for
	} // if
    } // if
    //console.log(this);
}

// class ComicPage
function ComicPage(page_path) {
    //console.log("page xml");
    var page_xml = get_xml(page_path);
    
    // Panels
    this.panels = [];
    var panel_elements = page_xml.getElementsByTagName("panel");
    for (var i = 0; i < panel_elements.length; i++) {
	this.panels[i] = new Panel(panel_elements[i]);
    }

    // Transitions
    this.transitions = [];
    var trans_elements = page_xml.getElementsByTagName("transition");
    console.log(trans_elements);
    for (var i = 0; i < trans_elements.length; i++) {
	this.transitions[i] = new Transition(trans_elements[i]);
    }

    // Sort the transitions in ascending order according to the 
    // "from" panel
    this.transitions.sort(function(a, b) { return a.from - b.from; });
    //console.log(page_path + " created.");
    //this.path = page_path;
}

// class ComicBook
function ComicBook(name, folder_path, from_page, to_page) {
    //console.log("comic xml");
    console.log(name);
    this.name = name;
    // Options
    this.options = new ComicOptions(folder_path);
    this.options.from_page = from_page;
    this.options.to_page = to_page;
    
    // Pages
    this.pages = [];
    for (var i = from_page; i <= to_page; i++) {
	console.log("Page " + i);
	page_name = number_to_name(i, this.options);
	page_path = folder_path + "/" + page_name + ".xml";
	this.pages[i - from_page] = new ComicPage(page_path);
    }

    this.count_transitions = function(types) {
	var previous = -1;
	//console.log(name + ", types.length = " + types.length);
	this.transition_matrix = [];
	for (var i = 0; i < types.length; i++) {
	    this.transition_matrix[i] = [];
	    for (var j = 0; j < types.length; j++) {
		this.transition_matrix[i][j] = 0;
	    }
	}
	// types.length rows and types.length columns

	for (var i = 0; i < this.pages.length; i++) {
	    //console.log(this.pages[i].page_path);
	    for (var j = 0; j < this.pages[i].transitions.length; j++) {
		var current = this.pages[i].transitions[j].type;
		if (previous == -1) {
		    previous = current;
		    continue;
		}
		this.transition_matrix[previous][current] += 1;
		previous = current;
	    } // for
	} // for

	// Remove the transition types that occur 0 times
	this.index_id_map = [];
	this.id_index_map = [];
	var deletions = 0;
	for (var i = 0; i < this.transition_matrix.length; i++) {
	    var sum = 0;
	    for (var j = 0; j < this.transition_matrix[i].length; j++) {
		sum += this.transition_matrix[i][j];
	    }
	    if (sum == 0) {
		this.transition_matrix.splice(i, 1);
		for (var k = 0; k < this.transition_matrix.length; k++) {
		    this.transition_matrix[k].splice(i, 1);
		}
		//console.log("Deleted row " + i);
		this.id_index_map[i+deletions] = -1;
		//console.log("id_index_map " + (i+deletions) + "= " + -1);
		i--;
		deletions += 1;
	    } else {
		this.index_id_map[i] = i + deletions;
		this.id_index_map[i+deletions] = i;
		//console.log("id_index_map " + (i+deletions) + "= " + i);

	    }
	} // for

	/*
	for (var i = 0; i < this.transition_matrix.length; i++) {
	    for (var j = 0; j < this.transition_matrix[i].length; j++) {
		if (this.id_index_map[j] == -1) {
		    this.transition_matrix[i].splice(j, 1);
		    j--;
		}
	    }
	}
	*/
    } // count_transitions

    
    // Characters
    var chars_xml = get_xml(folder_path + "/characters.xml");
    var char_elms = chars_xml.getElementsByTagName("character");
    this.characters = [];
    for (var i = 0; i < char_elms.length; i++) {
	this.characters[i] = new Character(char_elms[i]);
    }
    
} // ComicBook

// class TransitionType
function TransitionType(name, id_str, color) {
    this.name = name;
    this.id = parseInt(id_str);
    this.color = color;
}

function read_transitions() {
    // Read definition of transitions
    var trans_xml = get_xml(transitions_xml);
    var trans_elements = trans_xml.getElementsByTagName("transition");
    //console.log(trans_elements);
    var transitions = [];
    for (var i = 0; i < trans_elements.length; i++) {
	name = trans_elements[i].attributes.getNamedItem("name").nodeValue;
	id_str = trans_elements[i].attributes.getNamedItem("id").nodeValue;
	color = trans_elements[i].attributes.getNamedItem("color").nodeValue;
	transitions[i] = new TransitionType(name, id_str, color);
    }
    return transitions
}

function read_tag_defs() {
    // Read the definition of tags
    var tags_xml = get_xml(tags_xml_path);
    var tag_elms = tags_xml.getElementsByTagName("tag");
    var tag_defs = [];
    for (var i = 0; i < tag_elms.length; i++) {
	tag_defs[i] = new TagDef(tag_elms[i]);
    }
    return tag_defs;
}

function read_comics(names, from_page, to_page) {
    //var transition_types = read_transitions();

    //var names = [ "sandman1", "luckyluke6", "drmcninja25" ];
    var comics = [];
    for (var i = 0; i < names.length; i++) {
	comics[i] = new ComicBook(names[i], comics_dir + names[i], from_page[i], to_page[i]);
	//comics[i].count_transitions(transition_types);
	//console.log(comics[i].transition_matrix);
    }
    return comics
}

function read_chars(xml) {
    //var chars_xml = get_xml(folder_path + "/characters.xml");
    var char_elms = xml.getElementsByTagName("character");
    characters = [];
    for (var i = 0; i < char_elms.length; i++) {
	characters[i] = new Character(char_elms[i]);
    }
    return characters
}