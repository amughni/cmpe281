package grailsgumballmachinever1

import groovyx.net.http.*
import gumballstate.GumballMachine;
import static groovyx.net.http.ContentType.XML

class GumbulServiceController {

	def String machineSerialNum = "1234998871109"
	def GumballMachine gumballMachine
    def index() {
		
		if(request.method == "GET") {
		
			//search for gumball machine

			def modelNum = null;
			def http = new HTTPBuilder("http://localhost:8080/GrailsGumballMachineVer1/xml-api/" + machineSerialNum);
			
			http.request(Method.GET, XML) {
				
				headers.'Cache-Control' = 'no-store'
				
				response.success = 
				{
					resp, xml ->
					println "status: " + resp.status
					println "code: " + xml.code
					println "message: " + xml.message
					println "count: " + xml.countGumballs
					println "model: " + xml.modelNumber
					println "serial#: " + machineSerialNum
					modelNum = xml.modelNumber
				}
			}
			
			if(modelNum){
				//create the machine
				gumballMachine = new GumballMachine(modelNum, machineSerialNum);
				System.out.println(gumballMachine.toString());
			}
			else{
				flash.message = "Error! Gumball Machine Not Found!";
				render(view: "index");
			}
			
			//report a message to user
			flash.message = gumballMachine.toString();
			
			//send machine state to client
			flash.state = gumballMachine.getState();
			flash.model = modelNum;
			
			//display view
			render(view: "index");
		}
		else if (request.method == "POST") {
			//dump out request object
			request.each { key, value ->
				println( "request: $key = $value" )
			}
			
			// dump out params
			param?.each { key, value ->
				println( "params: $key = $value" )
			}
			
			//restore machine to client state
			def state = params?.state
			def modelNum = params?.model
			gumballMachine = new GumballMachine(modelNum, machineSerialNum);
			gumballMachine.setState(state);
			
			System.out.println(gumballMachine.toString())
			
			def respCode = ""
			def respMessage = ""
			if( params?.event == "Insert Quarter")
			{
				gumballMachine.insertQuarter();
			}
			if( params?.event == "Turn Crank" )
			{
				def before = gumballMachine.getCount() ;
                gumballMachine.turnCrank();
				def after = gumballMachine.getCount() ;
				
				if ( after != before )// Coin accepted state
				{
					def http = new HTTPBuilder("http://localhost:8080/GrailsGumballMachineVer1/xml-api/" + machineSerialNum);
					http.request(Method.PUT, XML)
					{
						body = "<root><count>10</count><message>Hello Update</message></root>";
						
						response.success = 
						{
							resp, xml ->
							println "status: " + resp.status
							println "code: " + xml.code
							println "message: " + xml.message
							respCode = ( xml.code!=null ? xml.code : " ")
							respMessage = ( xml.message!=null ? xml.message : " " )
						}
					}
				}
			}
			
			//report a message to user
			flash.message = gumballMachine.toString() + "\nCode: " + respCode + "\nMessage: " + respMessage
			
			// send machine state to client
			flash.state = gumballMachine.getState();
			flash.model = modelNum;
			
			//render view
			render(view: "index")
			 
		}
	}
}
