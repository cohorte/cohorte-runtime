/*
	 * my comment 
	 */
{
	/*
	 * my comment 
	 */
	// my comment 
	"$schema": "http://json-schema.org/draft-04/schema#",
	"definitions": {
		"allOf": [{// my comment 
			"$ref": "#/definitions/deviceTest"// my comment 
		}],// my comment 
		"device_iot": {
			"id": "/properties/_iot",
			"type": "object",
			"properties": {
				"cat": {
					"id": "/properties/_iot/properties/cat",// my comment 
					"type": "integer"
				},
				"cby": {
					"id": "/properties/_iot/properties/cby",
					"type": "string"
				},
				"tid": {
					"id": "/properties/_iot/properties/tid",
					"type": "string"
				}
			},/*
			 * my comment 
			 */
			"required": ["tid", "cby", "cat"]
		},
		/*
		 * my comment 
		 */
		"device_org_id": {
			"id": "/properties/org_id",
			"type": "object",
			"properties": {
				"ref": {
					"id": "/properties/org_id/properties/ref",
					"type": "string"
				}
			},
			"required": ["ref"]
		},
		"deviceTest": {
			"allOf": [{
				"$ref": "#/definitions/device"
			}, {
				"properties": {
					"test": {
						"type": "string"
					}
				}
			}],
			"type": "object"
		},
		/**
		 * my comment 
		 */
		"device_location": {// my comment 
			"id": "/properties/location",
			"type": "object",
			"properties": {
				"coordinates": {/**
					 * my comment 
					 */
					"id": "/properties/location/properties/coordinates",
					"type": "array",
					"items": {
						"id": "/properties/location/properties/coordinates/items",
						"type": "number"
					}
				},// my comment 
				"type": {// my comment 
					"id": "/properties/location/properties/type",
					"type": "string"
				}
				// my comment 
			},
			"required": ["type", "coordinates"]
		},
		"id": "http://example.com/example.json",
		"type": "object",
		"device": {
			"location": {
				"$ref": "#/definitions/device_location"
			},
			"type": "object",
			"properties": {
				"lbl": {
					"id": "/properties/lbl",
					"type": "string"
				},
				"id": {
					"id": "/properties/id",
					"type": "string"
				}
			}
		}/**
		 * my comment 
		 */
	}
}
/**
 * my comment 
 */