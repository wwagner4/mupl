package mupl

import java.nio.file.{Files, Path, Paths}

import org.scalatra._
import org.slf4j.LoggerFactory

import scala.jdk.CollectionConverters._

class MuplWebGuiServlet extends ScalatraServlet {

  private val logger = LoggerFactory.getLogger("servlet")

  private val _muplDir: Path = Paths.get("/Users/wwagner4/prj/music/mupl/src/main/mupl1")
  private var _selectedMuplFile = Option.empty[String]
  private val _player = new MuplPlayer

  get("/") {
    contentType = "text/html"
    htmlCreate(bodyCreate(None))
  }

  get("/load/:file") {
    contentType = "text/html"
    val file = params("file")
    logger.info(s"Clicked on $file")
    _selectedMuplFile = Some(file)
    htmlCreate(bodyCreate(None))
  }

  post("/play") {
    handle(() => {
      _selectedMuplFile match {
        case None =>
          "No mupl file selected"
        case Some(mf) =>
          logger.info("Clicked the play button")
          val mupl: String = request.body
          MuplUtil.writeToFile(mupl, _muplDir.resolve(mf))
          val playResult = _player.play(mupl, "play")
          playResult match {
            case None =>
              "playing"
            case Some(msg) =>
              msg
          }
      }
    })
  }

  post("/stop") {
    handle(() => {
      _player.stop()
      "stopped"
    })
  }

  private def handle(f: () => String): String = {
    try {
      f()
    } catch {
      case e: Exception =>
        logger.error("Error on play. " + e.getMessage, e)
        e.getMessage
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
       |            height: 60%; 
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
       |<script>
       |function action(event) {
       |  if (event.keyCode === 13 && event.ctrlKey) {
       |   event.preventDefault();
       |   document.getElementById("play-button").click();
       |  }
       |  else if (event.keyCode === 83 && event.ctrlKey) {
       |   event.preventDefault();
       |   document.getElementById("stop-button").click();
       |  }
       |}
       |function noaction(event) {
       |  if (event.keyCode === 13 && event.ctrlKey) {
       |   event.preventDefault();
       |  }
       |  else if (event.keyCode === 83 && event.ctrlKey) {
       |   event.preventDefault();
       |  }
       |}
       |var pb = document.getElementById("play-button");
       |pb.addEventListener("click", function(event) {
       |  var req = new XMLHttpRequest();
       |  req.onreadystatechange = function () {
       |    if (req.readyState==4 && req.status==200) {
       |      var m = document.getElementById("message");
       |      m.innerHTML = req.responseText;
       |    }
       |  };
       |  req.open('POST', '/play', true);
       |  var ta = document.getElementById("mupl");
       |  req.send(ta.value);
       |});
       |var sb = document.getElementById("stop-button");
       |sb.addEventListener("click", function(event) {
       |  var req = new XMLHttpRequest();
       |  req.onreadystatechange = function () {
       |    if (req.readyState==4 && req.status==200) {
       |      var m = document.getElementById("message");
       |      m.innerHTML = req.responseText;
       |    }
       |  };
       |  req.open('POST', '/stop', true);
       |  req.send("");
       |});
       |var ta = document.getElementById("mupl");
       |ta.addEventListener("keyup", action);
       |ta.addEventListener("keydown", noaction);
       |ta.addEventListener("keypressed", noaction);
       |</script>
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
       |<textarea id="mupl" name="mupl" class="nosel">
       |$txtMupl
       |</textarea>
       |<p>
       |  <input type="submit" name="action" value="play (ctrl + return)" id="play-button"/>
       |  <input type="submit" name="action" value="stop (ctrl + 's')" id="stop-button"/>
       |</p>
       |<p id="message"></p>
       |""".stripMargin


  def pHeading: String = {
    """<p class="texth">m-u-p-l</p> """
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
      .map(fn => s"""<td><a href="/load/$fn">$fn</a></td>""")
      .mkString("\n")
  }
}
