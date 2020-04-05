package mupl

import java.nio.file.{Files, Path, Paths}

import org.scalatra._
import org.slf4j.LoggerFactory

import scala.jdk.CollectionConverters._

class MuplServlet extends ScalatraServlet {

  private val logger = LoggerFactory.getLogger("tryout")

  private val _muplDir: Path = Paths.get("/Users/wwagner4/prj/music/mupl/src/main/mupl")
  private var _selectedMuplFile = Option.empty[String]


  get("/") {
    contentType = "text/html"
    bodyCreate
  }
  

  get("/action/:file") {
    contentType = "text/html"
    val file = params("file")
    logger.info(s"Clicked on $file")
    _selectedMuplFile = Some(file)
    bodyCreate
  }
  
  post("/play") {
    contentType = "text/html"
    logger.info("Clicked the play button")
    val txt: String = params("mupl")
    logger.info(s"Clicked the play button\n$txt")
    bodyCreate
  }

  def bodyCreate: String =
    s"""
       |<html>
       |<head>
       |    <title>mupl player</title>
       |</head>
       |    <table>
       |        <tr>
       |            $tdMuplFiles
       |        </tr>
       |</table>
       |$pSelectedMuplFile
       |<form action="/play" method="post">
       |<textarea id="mupl" name="mupl" rows="20" cols="50">
       |$txtMupl
       |</textarea>
       |<p>
       |  <input type="submit" value="play"/>
       |</p>
       |</form>
       |</html>
       |""".stripMargin

  def pSelectedMuplFile: String = {
    _selectedMuplFile match {
      case None => """<p>No mupl file selected</p>"""
      case Some(f) => s"""<p>Selected mupl file $f</p>"""

    }
  }
  
  def txtMupl: String = {
    _selectedMuplFile match {
      case None => ""
      case Some(fn) =>       
        val fp =  _muplDir.resolve(fn)
        MuplUtil.fileToStr(fp)
    }
  }

  def tdMuplFiles: String = {
    Files
      .list(_muplDir)
      .iterator()
      .asScala.toList
      .map(f => f.getFileName.toString)
      .map(fn => s"""<td><a href="/action/$fn">$fn</a></td>""")
      .mkString("\n")
  }
}
