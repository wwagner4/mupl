package mupl

import javax.servlet.Servlet
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext
import org.slf4j.LoggerFactory

object MuplWebGuiMain extends App {
  
  val logger = LoggerFactory.getLogger("server")

  val server: Server = WebServiceBuilder.buildWebService(8080, classOf[MuplServlet])
  try {
    server.start()
  } catch{
    case e: Exception => 
      logger.error(s"Error staring server: ${e.getMessage}")
      server.stop()
  } 
}

object WebServiceBuilder {

  def buildWebService(port: Integer, webServiceClass: Class[_ <: Servlet]): Server = {
    val server = new Server(port)
    val context = new WebAppContext()
    context.setContextPath("/")
    context.setResourceBase("/tmp")
    context.addServlet(webServiceClass, "/*")
    server.setHandler(context)
    server
  }
}