{
	/*
	 * composition to launch the core  
	 */
	
	
	
	/*
	 * merge all composition_core file 
	 */
	"$merge":[
	          "compositions/composition_core*",
			  "../test/*/composition_core*"
		
	 ],
 

    "name": "CohorteIotPack",
    "root": {
    	
    	/* import here (in root) the content of the root json objects contained in the files */
    	
        "name": "CohorteIotPack-composition"
        
       
    }
}
