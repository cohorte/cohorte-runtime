// my comment 1
{ // my comment 2
	"$schema": "http://json-schema.org/draft-04/schema#",
	"definitions": {
		"allOf": [{
			"$ref": "#/definitions/deviceTest"
		}],
		"device_iot": {
			"id": "/properties/_iot",
			"type": "object",
			"properties": {
				"cat": {
					"id": "/properties/_iot/properties/cat",
					"type": "integer"
				},
				"cby": {
					"id": "/properties/_iot/properties/cby",
					"type": "string"
				},
				// my coment 3
				"tid": {
					"id": "/properties/_iot/properties/tid",
					"type": "string"
				}
			},
			"required": ["tid", "cby", "cat"]
		},
		"device_org_id": {
			"id": "/properties/org_id",
			"type": "object",
			"properties": {
				"ref": {
					"id": "/properties/org_id/properties/ref",
					"type": "string"
				}
			},
			// my comment 4
			"required": ["ref"] // my comment 5
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
		"device_location": {
			"id": "/properties/location",
			"type": "object",
			"properties": {
				"coordinates": {
					"id": "/properties/location/properties/coordinates",
					"type": "array",
					"items": {
						"id": "/properties/location/properties/coordinates/items",
						"type": "number"
					}
				},
				"type": {
					"id": "/properties/location/properties/type",
					"type": "string"
				}
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
		}
	}// my comment 6
// my comment 7
}


// my comment 8