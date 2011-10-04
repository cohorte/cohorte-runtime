<?php if(false){?>
<html>
<body>


<?php }?>

	<script>
	function changecss(aClassName,aAttribut,aValue) {
		//alert(aClassName+' { '+aAttribut+': '+aValue+'; }');
		
		var cssRules=null;
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
						    for (var prop in err) 
						    {  
						       vDebug += "property: "+ prop+ " value: ["+ err[prop]+ "]\n"; 
						    } 
						    vDebug += "toString(): " + " value: [" + err.toString() + "]"; 
							alert(vDebug);
						}
					}
			  }
	  	}
	}
	function setTopBarVisible(aTopDiv){
			var wValue = (aTopDiv.id=="hidden")?"0px":"-30px";
			changecss(".top_bar","top",wValue);
			aTopDiv.id = (wValue=="-30px")?"hidden":"visible";
	}
	</script>
	
	<div class="top_bar" onclick="setTopBarVisible(this);" id="hidden" >
		<div class="top_search">
			<div class="search_text">
				<a href="#">Advanced Search</a>
			</div>
			<input type="text" class="search_input" name="search"/> <input
				type="image"
				src="/<?php echo base_url(); ?>app_resources/images/search.gif"
				class="search_bt" />
			</div>

		<div class="languages">
			<div class="lang_text">Languages:</div>
			<a href="#" class="lang"><img
				src="/<?php echo base_url(); ?>app_resources/images/en.gif" alt=""
				title="" border="0" /> </a> <a href="#" class="lang"><img
				src="/<?php echo base_url(); ?>app_resources/images/de.gif" alt=""
				title="" border="0" /> </a>
		</div>

	</div>

	

<?php if(false){?>
</body>
</html>

<?php }?>