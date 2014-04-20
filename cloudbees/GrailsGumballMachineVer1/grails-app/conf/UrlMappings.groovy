class UrlMappings {

	static mappings = {
		
		"/api/$serialNumber?"(controller: "api", parseRequest: true) {
			action = [GET: "getXML", PUT: "putXML", DELETE: "deleteXML", POST: "postXML"]
		}
		
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

		"/"(view:"/index")
		"500"(view:'/error')
	}
}
