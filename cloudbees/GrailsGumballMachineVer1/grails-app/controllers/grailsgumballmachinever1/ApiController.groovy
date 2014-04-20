package grailsgumballmachinever1

import grails.converters.XML
import grails.converters.JSON

class ApiController {

    def defaultAction = 'getXML'

	def index() {
		redirect(action: 'getXML')
	}
	
	def getXML() {
		if( params.serialNumber )
		{
		  def g = Gumball.findBySerialNumber(params.serialNumber)
		  if ( g )
		  {
			render g as XML
		  }
		  else {
			render( contentType:"text/xml")
			{
			  fault {
				code('00001')
				message('Machine Serial Number Not Found!')
			  }
			}
		  }
		}
		else
		{
		  def all = Gumball.list()
		  render all as XML
		}
	  }
	  
	  def putXML() {
		// dump out request object
		request.each { key, value ->
		  println( "request: $key = $value" )
		}
		
		//dump out params
		params?.each { key, value ->
		  println( "params: $key = $value" )
		}
		
		// print body
		println request.XML.count;
		println request.XML.message;
		
		if( params.serialNumber )
		{
		  def g = Gumball.findBySerialNumber(params.serialNumber)
		  if( g )
		  {
			try
			{
			  if (g.countGumballs>0)
			  {
				g.lock();
				g.countGumballs--;
				g.save(flush: true)
				render(contentType:"text/xml")
				{
				  success {
					code('00000')
					message('Gumball Purchase Successful.')
				  }
				}
			  }
			  else
			  {
				render(contentType:"text/xml")
				{
				  success {
					code('00005')
					message('Sorry, Out of Gumballs!')
				  }
				}
			  }
			}
			catch(e) {
			  println p.errors
			  println e
			  render(contentType:"text/xml")
			  {
				fault {
				  code('00009')
				  message('Service Update Error.')
				  errors {
					p.errors.allErrors.each {
					  error(it)
					}
				  }
				}
			  }
			}
		  }
		  else {
			render(contentType:"text/xml")
			{
			  fault {
				code('00001')
				message('Machine Serial Number Not Found!')
			  }
			}
		  }
		}
		else
		{
		  render(contentType:"text/xml")
		  {
			fault {
			  code('00002')
			  message('Invalid Request!')
			}
		  }
		}
	  }
	  
	  def postXML() {
		//dump out request object
		request.each { key, value ->
		  println( "request: $key = $value" )
		}
		
		//dump out params
		params?.each { key, value ->
		  println( "params: $key = $value" )
		}
		
		//parse request using xml parser
		println( request.XML.countGumballs )
		println( request.XML.modelNumber )
		println( request.XML.serialNumber )
		
		def g = new Gumball()
		g.countGumballs = request.XML.countGumballs.toInteger()
		g.modelNumber = request.XML.modelNumber
		g.serialNumber = request.XML.serialNumber
		
		try{
		  g.save(flush: true)
		  render(contentType:"text/xml")
		  {
			success {
			  code('00000')
			  message('Gumball Machine Registration Successful.')
			}
		  }
		}
		catch(e) {
		  println p.errors
		  println e
		  render(contextType:"text/xml")
		  {
			fault {
			  code('00011')
			  message('Gumball Machine Registration Error.')
			  errors {
				p.errors.allErrors.each {
				  error(it)
				}
			  }
			}
		  }
		}
	  }
	  
	  def deleteXML()
	  {
		//dump out request object
		request.each { key, value ->
		  println( "request: $key = $value" )
		}
		
		//dump out params
		params?.each { key, value ->
		  println( "params: $key = $value" )
		}
		
		if(params.serialNumber)
		{
		  def g = Gumball.findBySerialNumber(params.serialNumber)
		  
		  if(g)
		  {
			g.delete()
			render(contentType:"text/xml")
			{
			  success {
				code('20000')
				message('Gumball Machine deleted!')
			  }
			}
		  }
		  else
		  {
			render(content:"text/xml")
			{
			  fault {
				code('00003')
				message('Gumball Machine not found!')
			  }
			}
		  }
		}
	  }
}
