<?php if(false){?>
<html>
<body>


<?php }?>

	<script>
	// Last Updated on July 4, 2011
	// documentation for this script at
	// http://www.shawnolson.net/a/503/altering-css-class-attributes-with-javascript.html
	function changecss(aClassName,aAttribut,aValue) {
		//alert(aClassName+' { '+aAttribut+': '+aValue+'; }');
		
		var cssRules="rules";
		var wStyleSheets=null;
		
		 for (var wI = 0; wI < document.styleSheets.length; wI++){
			 wStyleSheets = document.styleSheets[wI];
			 try{
				 wStyleSheets.insertRule(aClassName+' { '+aAttribut+': '+aValue+'; }',wStyleSheets[cssRules].length);
			 } catch(err){
			  		try{
			  			wStyleSheets.addRule(theClass,element+': '+value+';');
					}catch(err){
						try{
							if (wStyleSheets['rules']) {
								cssRules = 'rules';
							} else if (wStyleSheets['cssRules']) {
							 	cssRules = 'cssRules';
							} else {
								//no rules found... browser unknown
							}
							var wStyleSheetsRules = wStyleSheets[cssRules];
							var wMax =  wStyleSheetsRules.length;
							for (var wJ = 0; wJ < wMax; wJ++) {
								if (wStyleSheetsRules[wJ].selectorText == aClassName) {
									if(wStyleSheetsRules[wJ].style[aAttribut]){
										wStyleSheetsRules[wJ].style[aAttribut] = aValue;
										break;
									}
								}
							}
						} catch (err){
						    var vDebug = ""; 
						    for (var prop in err) {  
						       vDebug += "property: "+ prop+ " value: ["+ err[prop]+ "]\n"; 
						    } 
						    vDebug += "toString(): " + " value: [" + err.toString() + "]"; 
							alert(vDebug);
						}
					}
			  }
	  	}
	}
	function setTopBarVisible(aObject){
		//alert("setTopBarVisible" +aObject.toString() );
			var wValue = (aObject.id=="hidden")?"0px":"-30px";
			changecss(".top_bar","top",wValue);
			aObject.id = (wValue=="-30px")?"hidden":"visible";
			return true;
	}
	function launchRequest(aObject){
		var wSearch = document.getElementById("search").value;
		
		var wDetailUrl = "/<?php echo base_url(); ?>index.php/CHome/showDetails/"+ wSearch; 
		
		window.location.href= wDetailUrl;
		return true;
	}
	function setFocus(aObject){
		//alert("setFocus" +aObject.toString() );
		
		window.event.cancelBubble=true;
		return true;
	}
	</script>
	
	<div class="top_bar" onclick="setTopBarVisible(this);" id="hidden" >
		<div class="top_search">
			<div class="search_text">
				<a href="#">Advanced Search</a>
			</div>
			<input type="text" class="search_input" id="search" onclick="setFocus(this);" />
			<input type="image"
				src="/<?php echo base_url(); ?>app_resources/images/search.gif"
				class="search_bt" onclick="launchRequest(this);"/>
		</div>
		<div class="languages" onclick="setFocus(this);">
			<div class="lang_text">Languages:</div>
			<a href="#" class="lang" ><img
				src="/<?php echo base_url(); ?>app_resources/images/en.gif" alt=""
				title="" border="0" /> </a>
				
			<a href="#" class="lang"><img
				src="/<?php echo base_url(); ?>app_resources/images/de.gif" alt=""
				title="" border="0" /> </a>
		</div>
	</div>

	

<?php if(false){?>
</body>
</html>

<?php }?>