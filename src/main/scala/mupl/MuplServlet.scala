package mupl

import org.scalatra._

class MuplServlet extends ScalatraServlet {

  get("/") {
    contentType = "text/html"
    """
      |<html>
      |<head>
      |    <title>mupl player</title>
      |</head>
      |    <table>
      |        <tr>
      |            <td><a href="#">melody.ck</a></td>
      |        </tr>
      |        <tr>
      |            <td><a href="#">x.ck</a></td>
      |        </tr>
      |        <tr>
      |            <td><a href="#">y.ck</a></td>
      |        </tr>
      |</table>
      |</html>
      |""".stripMargin
  }

}
