var Wifi = Parse.Object.extend("Wifis");

// Check if SSID is set, and enforce uniqueness based on the SSID column.
Parse.Cloud.beforeSave("Wifis", function(request, response) {
	if (!request.object.get("SSID")) {
		response.error('A Wifi must have a SSID.');
	} else {
		var query = new Parse.Query(Wifi);
		query.equalTo("SSID", request.object.get("SSID"));
		query.first({
			success : function(object) {
				if (object) {
					response.error("A Wifi with this SSID already exists.");
				} else {
					response.success();
				}
			},
			error : function(error) {
				response.success();

			}
		});
	}
});

Parse.Cloud.job("removeDuplicateItems", function(request, status) {
	Parse.Cloud.useMasterKey();
	var _ = require("underscore");

	var hashTable = {};

	function hashKeyForTestItem(testItem) {
		var fields = [ "SSID" ];
		var hashKey = "";
		_.each(fields, function(field) {
			hashKey += testItem.get(field) + "/";
		});
		return hashKey;
	}

	var testItemsQuery = new Parse.Query("Wifis");
	testItemsQuery.each(function(testItem) {
		var key = hashKeyForTestItem(testItem);

		if (key in hashTable) { // this item was seen before, so destroy this
			return testItem.destroy();
		} else { // it is not in the hashTable, so keep it
			hashTable[key] = 1;
		}

	}).then(function() {
		status.success("Migration completed successfully.");
	}, function(error) {
		status.error("Uh oh, something went wrong.");
	});
});