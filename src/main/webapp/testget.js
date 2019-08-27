/**
 * 
 */

// Add your javascript here
function runit() {
	alert("running!");
	$
			.ajax(
					{
						type : "GET",

						url : "http://localhost:8080/irods-rest-4.0.2.1-SNAPSHOT/rest/server",
						beforeSend : function(xhr) {
							xhr.withCredentials = true;
							xhr.setRequestHeader("Authorization", "Basic "
									+ btoa("test1" + ":" + "test"));
							xhr.setRequestHeader("Access-Control-Allow-Origin",
									"*");
						},

						xhrFields : {
							withCredentials : true
						},
						crossDomain : true,
						processData : false,
						contentType : false,

					}).done(function(data) {
				$("#result").html(data);
			});
}

