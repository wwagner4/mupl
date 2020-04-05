package mupl

import java.nio.file.{Files, Path, Paths}
import java.util.concurrent.TimeUnit

import org.scalatra._
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.jdk.CollectionConverters._
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

class MuplServlet extends ScalatraServlet {

  private val logger = LoggerFactory.getLogger("servlet")

  private val _muplDir: Path = Paths.get("/Users/wwagner4/prj/music/mupl/src/main/mupl1")
  private var _selectedMuplFile = Option.empty[String]
  private val _player = new MuplPlayer
  

  get("/") {
    contentType = "text/html"
    bodyCreate(None)
  }


  get("/action/:file") {
    contentType = "text/html"
    val file = params("file")
    logger.info(s"Clicked on $file")
    _selectedMuplFile = Some(file)
    bodyCreate(None)
  }

  get("/stop") {
    contentType = "text/html"
    _player.stop()
    bodyCreate(None)
  }

  post("/play") {
    contentType = "text/html"
    try {
      _selectedMuplFile match {
        case None => bodyCreate(Some("No mupl file selected"))
        case Some(mf) =>
          logger.info("Clicked the play button")
          val mupl: String = params("mupl")
          MuplUtil.writeToFile(mupl, _muplDir.resolve(mf))
          var playResult = Option.empty[Option[String]]
          Future {
            playResult = Some(_player.play(mupl, "play"))
          }
          Thread.sleep(1000)
          playResult match {
            case None => bodyCreatePlaying()
            case Some(pr) => pr match {
              case None => bodyCreate(None)
              case Some(msg) =>
                val m = s"Error in player:\n$msg"
                logger.info(m)
                bodyCreate(Some(m))
            }  
          }
      }
    } catch {
      case e: Exception =>
        logger.error(s"Error playing. ${e.getMessage}", e)
        bodyCreate(Some(s"Error playing. ${e.getMessage}"))
    }
  }

  def bodyCreate(msg: Option[String]): String =
    s"""
       |<html>
       |<head>
       |    <title>mupl player</title>
       |</head>
       |<body>
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
       |${pMessage(msg)}
       |</body
       |</html>
       |""".stripMargin

  def bodyCreatePlaying(): String =
    s"""
       |<html>
       |<head>
       |    <title>mupl player</title>
       |</head>
       |<body>
       |$pSelectedMuplFile
       |<p>
       |${htmlFormat(txtMupl)}
       |</p>
       |<a href="/stop">stop running</a>
       |</body>
       |</html>
       |""".stripMargin

  def pMessage(msg: Option[String]): String = {
    msg match {
      case None => ""
      case Some(m) =>
        s"""
           |<p>${htmlFormat(m)}</p>
           |""".stripMargin
    }
  }

  def htmlFormat(str: String): String = {
    str
      .split("\n")
      .map(l => s"$l<br/>")
      .mkString("\n")
  }

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
        val fp = _muplDir.resolve(fn)
        MuplUtil.fileToStr(fp)
    }
  }

  def tdMuplFiles: String = {
    Files
      .list(_muplDir)
      .iterator()
      .asScala.toList
      .map(f => f.getFileName.toString)
      .sorted
      .map(fn => s"""<td><a href="/action/$fn">$fn</a></td>""")
      .mkString("\n")
  }
}
