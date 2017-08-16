/**
 * Start configuration for a Top Composer (Python version)
 */
{
	/*
	 * Composer bundles
	 */
	"bundles" : [
	/* Parser */
	/*{
		"name" : "cohorte.composer.parser"
	},
	*/
	/* Top Composer components */
	{
		"name" : "cohorte.composer.top.composer"
	}, {
		"name" : "cohorte.composer.top.distributor"
	}, {
		"name" : "cohorte.composer.top.status"
	}, {
		"name" : "cohorte.composer.top.commander"
	}, {
		"name" : "cohorte.composer.top.store_handler"
	},

	/* Distributor criteria */
	{
		"name" : "cohorte.composer.top.criteria.distance.configuration"
	},

	/* Shell commands */
	{
		"name" : "cohorte.shell.composer_top"
	},
	/* admin REST API */
	/*{
		"name" : "admin.admin"	
	},*/
	/* debug REST API */
	{
		"name" : "debug.api"	
	},
	/* web admin */
	{
		"name" : "webadmin.webadmin"	
	} 
	] ,

	"composition" : [ {
			"factory" : "cohorte-composer-top-factory",
			"name" : "cohorte-composer-top",
			"properties" : {
				"autostart" : "True"
			}
		}, {
			"factory": "cohorte-admin-api-factory",
			"name" : "cohorte-admin-api",
			"properties" : {
				"username" : "admin",
				"password" : "hash:ee10c315eba2c75b403ea99136f5b48d",
				"session.timeout" : 240000
			}
			/*
				To generated a hashed password and put it in "password" property, 
				you can use the following python code:
				online: http://www.md5online.org/md5-encrypt.html
				python:
					import hashlib				
					hash_object = hashlib.md5(b'Hello World')
					print(hash_object.hexdigest())

			*/
		}
	 ]
/* All components of the Composer are automatically instantiated */
}
