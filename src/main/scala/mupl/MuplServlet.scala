package mupl

import java.nio.file.{Files, Path, Paths}

import org.scalatra._
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.jdk.CollectionConverters._
import scala.concurrent._
import ExecutionContext.Implicits.global

class MuplServlet extends ScalatraServlet {

  private val logger = LoggerFactory.getLogger("servlet")

  private val _muplDir: Path = Paths.get("/Users/wwagner4/prj/music/mupl/src/main/mupl1")
  private var _selectedMuplFile = Option.empty[String]
  private val _player = new MuplPlayer

  get("/") {
    contentType = "text/html"
    htmlCreate(bodyCreate(None))
  }

  get("/action/:file") {
    contentType = "text/html" 
    val file = params("file")
    logger.info(s"Clicked on $file")
    _selectedMuplFile = Some(file)
    htmlCreate(bodyCreate(None))
  }

  get("/stop") {
    contentType = "text/html"
    _player.stop()
    htmlCreate(bodyCreate(None))
  }

  post("/play") {
    contentType = "text/html"
    try {
      _selectedMuplFile match {
        case None =>
          htmlCreate(bodyCreate(Some("No mupl file selected")))
        case Some(mf) =>
          logger.info("Clicked the play button")
          val mupl: String = params("mupl")
          MuplUtil.writeToFile(mupl, _muplDir.resolve(mf))
          var playResult = Option.empty[Option[String]]
          Future {
            playResult = Some(_player.play(mupl, "play"))
          }
          Thread.sleep(500)
          playResult match {
            case None => 
              htmlCreate(bodyCreatePlaying())
            case Some(pr) => pr match {
              case None => 
                htmlCreate(bodyCreate(None))
              case Some(msg) =>
                val m = s"Error in player:\n$msg"
                logger.info(m)
                htmlCreate(bodyCreate(Some(m)))
            }  
          }
      }
    } catch {
      case e: Exception =>
        logger.error(s"Error playing. ${e.getMessage}", e)
        htmlCreate(bodyCreate(Some(s"Error playing. ${e.getMessage}")))
    }
  }

  def htmlCreate(body: String): String = {
    s"""
       |<html>
       |<head>
       |    <title>mupl player</title>
       |    <style type="text/css">
       |        * { 
       |            font-family : "Lucida Console", Monospace;
       |            font-size : 15px;
       |            outline: none;
       |            background-color: #ffd401;}
       |        body { 
       |            margin : 0px 30px 0px 30px;}
       |        textarea {
       |            width: 100%;
       |            height: 70%; 
       |            box-sizing: border-box;
       |            background-color: yellow;
       |            border: none;
       |            padding: 5px;}
       |        .textp {
       |            width: 100%;
       |            height: 70%; 
       |            box-sizing: border-box;
       |            background-color: yellow;
       |            padding: 5px;}
       |        .texth {
       |            font-size: 70px;
       |            margin: 4px;}
       |    </style>
       |</head>
       |<body>
       |$body
       |</body
       |</html>
       |""".stripMargin
  } 
  
  def bodyCreate(msg: Option[String]): String =
    s"""
       |$pHeading
       |<table>
       |  <tr>
       |$tdMuplFiles
       |   </tr>
       |</table>
       |$pSelectedMuplFile
       |<form action="/play" method="post">
       |<textarea id="mupl" name="mupl" class="nosel">
       |$txtMupl
       |</textarea>
       |<p>
       |  <input type="submit"  accesskey="x" value="play"/>
       |</p>
       |</form>
       |${pMessage(msg)}
       |""".stripMargin

  
  def bodyCreatePlaying(): String =
    s"""
       |$pHeading
       |$pSelectedMuplFile
       |<p class="textp">
       |${htmlFormat(txtMupl)}
       |</p>
       |<a href="/stop">stop running</a>
       |""".stripMargin


  def pHeading: String = {
    """<p class="texth">m-u-p-l</p> """
  }

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
      .map(escapeBlanks)
      .map(l => s"$l<br/>")
      .mkString("\n")
  }

  def escapeBlanks(in: String): String = {
    in.toList
      .map(_.toString)
      .map(escapeBlank)
      .mkString("")
  }
  
  def escapeBlank(s: String): String = {
    if (s == " ") """&nbsp;"""
    else s
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
