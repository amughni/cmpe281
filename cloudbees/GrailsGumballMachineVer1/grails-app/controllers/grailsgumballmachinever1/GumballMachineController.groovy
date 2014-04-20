package grailsgumballmachinever1

import groovyx.net.http.HTTPBuilder
import gumballstate.GumballMachine
import groovyx.net.http.Method
import groovyx.net.http.*
import static groovyx.net.http.ContentType.XML

class GumballMachineController implements Serializable{

    def String machineSerialNum = "1234998871109"
    def GumballMachine gumballMachine
    
    def index() {
		
		String VCAP_SERVICES = System.getenv('VCAP_SERVICES')
		if(request.method == "GET") {
			
				//search for gumball machine
	
				def modelNum = null;
				def http = new HTTPBuilder("http://localhost:8080/GrailsGumballMachineVer1/api/" + machineSerialNum);
				
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
					
					def gumball = Gumball.findBySerialNumber( machineSerialNum )
					if ( gumball )
					{
						// load machine
						gumballMachine = new GumballMachine(gumball.countGumballs)
						gumballMachine.setModelNumber(gumball.modelNumber)
						gumballMachine.setSerialNumber(gumball.serialNumber)
						System.out.println(gumballMachine.toString())
					}
					else{
						flash.message = "Error! Gumball Machine Not Found!";
						render(view: "index");
					}
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
				params?.each { key, value ->
					println( "params: $key = $value" )
				}
				
				//restore machine to client state
				def state = params?.state
				def modelNum = params?.model
				def gumball = Gumball.findBySerialNumber( machineSerialNum )
				if ( gumball )
				{
					// load machine
					gumballMachine = new GumballMachine(gumball.countGumballs)
					gumballMachine.setModelNumber(gumball.modelNumber)
					gumballMachine.setSerialNumber(gumball.serialNumber)
			
					if(state.contains("waiting for quarter"))
					{
						gumballMachine.setState(gumballMachine.getHasQuarterState());
					}
					else
					{
						gumballMachine.setState(gumballMachine.getSoldState());
					}
				
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
							def http = new HTTPBuilder("http://localhost:8080/GrailsGumballMachineVer1/api/" + machineSerialNum);
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
				else{
					flash.message = "Error! Gumball Machine Not Found!";
					render(view: "index");
				}
				 
			}
			
     }

}

