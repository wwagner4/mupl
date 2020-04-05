package mupl

import javax.servlet.Servlet
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext

object ChuckGuiMain extends App {
  val server: Server = WebServiceBuilder.buildWebService(8080, classOf[MuplServlet])
  server.start()
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