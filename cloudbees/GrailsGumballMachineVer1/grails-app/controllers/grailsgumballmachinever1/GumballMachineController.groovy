package grailsgumballmachinever1

import gumballstate.GumballMachine


class GumballMachineController implements Serializable{

    def String machineSerialNum = "1234998871109"
    def GumballMachine gumballMachine
    
    def index() {
		
		String VCAP_SERVICES = System.getenv('VCAP_SERVICES')
		
        if (request.method == "GET") {

            // search db for gumball machine
            def gumball = Gumball.findBySerialNumber( machineSerialNum )
            if ( gumball )
            {
                // create a default machine
                gumballMachine = new GumballMachine(gumball.countGumballs)
                gumballMachine.setModelNumber(gumball.modelNumber)
                gumballMachine.setSerialNumber(gumball.serialNumber)
                System.out.println(gumballMachine)
            }
            else
            {
                // create a default machine
				gumballMachine = new GumballMachine(5);
				//set default serial number & model number
				gumball.modelNumber = "v2";
				gumball.serialNumber = "1234998871109";
				gumballMachine.setModelNumber(gumball.modelNumber)
				gumballMachine.setSerialNumber(gumball.serialNumber)
                System.out.println(gumballMachine)
            }

            // save in the session
            session.machine = gumballMachine
			
            // report a message to user
            flash.message = gumballMachine.toString() 

            // display view
            render(view: "index")

        }
        else if (request.method == "POST") {

            // dump out request object
            request.each { key, value ->
                println( "request: $key = $value")
            }

            // dump out params
            params?.each { key, value ->
                println( "params: $key = $value" )
            }

            // get machine from session
            gumballMachine = session.machine
            System.out.println(gumballMachine)
			
            if ( params?.event == "Insert Quarter" )
            {
                gumballMachine.insertQuarter()
            }
            if ( params?.event == "Turn Crank" )
            {	
				def before = gumballMachine.getCount() ;
                gumballMachine.turnCrank();
				def after = gumballMachine.getCount() ;
				
				if ( after != before )
				{
					def gumball = Gumball.findBySerialNumber( machineSerialNum )
					if ( gumball )
					{
						gumball.lock; //pessimistic lock
						// update gumball inventory
						gumball.countGumballs = after ;
						gumball.save(flush:true); // default optimistic lock
					}
				}
				
            }

			
            // report a message to user
            flash.message = gumballMachine.toString() 

            // render view
            render(view: "index")
        }
        else {
            render(view: "/error")
        }
    }

}

