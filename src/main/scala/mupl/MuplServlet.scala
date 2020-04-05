package mupl

import java.nio.file.{Files, Paths}

import org.scalatra._
import org.slf4j.LoggerFactory
import org.slf4j.impl.SimpleLoggerFactory

import scala.jdk.CollectionConverters._

class MuplServlet extends ScalatraServlet {
  
  private val logger = LoggerFactory.getLogger("tryout")

  private val muplDir = Paths.get("/Users/wwagner4/prj/music/mupl/src/main/mupl")
  private var _selectedMuplFile = Option.empty[String]
  

  get("/") {
    contentType = "text/html"
    body
  }

  get("/action/:file") {
    contentType = "text/html"
    val file = params("file")
    logger.info(s"Clicked on $file")
    _selectedMuplFile = Some(file)
    body
  }

  def body: String =
    s"""
      |<html>
      |<head>
      |    <title>mupl player</title>
      |</head>
      |    <table>
      |        <tr>
      |            $muplFiles
      |        </tr>
      |</table>
      |$selectedMuplFile
      |</html>
      |""".stripMargin

  def selectedMuplFile: String = {
    _selectedMuplFile match {
      case None => """<p>No mupl file selected</p>"""
      case Some(f) => s"""<p>Selected mupl file $f</p>"""
      
  }
  }
  
  def muplFiles: String = {
    Files
      .list(muplDir)
      .iterator()
      .asScala.toList
      .map(f => f.getFileName.toString)
      .map(fn => s"""<td><a href="/action/$fn">$fn</a></td>""")
      .mkString("\n")
  }
}
