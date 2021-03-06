package mupl

import java.nio.file.Files

import org.scalatra._
import org.slf4j.LoggerFactory

import scala.jdk.CollectionConverters._

class MuplWebGuiServlet extends ScalatraServlet {

  private val logger = LoggerFactory.getLogger("servlet")
  private lazy val mconfig: MuplConfig = MuplUtil.config

  private var _selectedMuplFile = Option.empty[String]

  
  def createSoundLoader(): SoundLoader = {
    val sounds = SoundYamlLoader.loadChuckSounds(mconfig)
    SoundLoaderImpl(sounds)
  }

  val soundManager = new Resetable[SoundLoader](() => createSoundLoader(), "mupl sounds")

  private lazy val _player = MuplPlayer(mconfig, soundManager.value())
  
  get("/") {
    contentType = "text/html"
    htmlCreate(bodyCreate)
  }

  get("/load/:file") {
    contentType = "text/html"
    val file = params("file")
    logger.info(s"Clicked on $file")
    _selectedMuplFile = Some(file)
    htmlCreate(bodyCreate)
  }

  post("/play") {
    handle(() => {
      _selectedMuplFile match {
        case None =>
          "No mupl file selected"
        case Some(mf) =>
          logger.info("Clicked the play button")
          val mupl: String = request.body
          MuplUtil.writeToFile(mupl, mconfig.workDir.resolve(mf))
          val playResult = _player.play(mupl, soundManager.value().loadSound(), "play")
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

  post("/reset") {
    handle(() => {
      logger.info(s"Clicked on reset")
      _player.stop()
      soundManager.reset()
      "Reset. Sound will be reloaded on next play"
    })
  }

  private def handle(f: () => String): String = {
    try {
      f()
    } catch {
      case e: NullPointerException =>
        logger.error("Error on play. Null pointer exception", e)
        "A null pointer exception occurred. For details see server log"
      case e: YamlException =>
        logger.error("Yaml Error on play. " + e.getMessage, e)
        MuplUtil.codeToHtml(e.yamlString, s"Error parsing ${e.resourceDesc}. ${e.msg}");
      case e: PlayerException =>
        logger.error("Yaml Error on play. " + e.getMessage, e)
        MuplUtil.codeToHtml(e.code, e.msg)
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
       |        input {
       |            background-color: yellow;}
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
       |var rb = document.getElementById("reset-button");
       |rb.addEventListener("click", function(event) {
       |  var req = new XMLHttpRequest();
       |  req.onreadystatechange = function () {
       |    if (req.readyState==4 && req.status==200) {
       |      var m = document.getElementById("message");
       |      m.innerHTML = req.responseText;
       |    }
       |  };
       |  req.open('POST', '/reset', true);
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

  def bodyCreate: String =
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
       |  <input type="submit" name="action" value="reset" id="reset-button"/>
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
        val fp = mconfig.workDir.resolve(fn)
        MuplUtil.fileToStr(fp)
    }
  }

  def tdMuplFiles: String = {
    Files
      .list(mconfig.workDir)
      .iterator()
      .asScala.toList
      .map(f => f.getFileName.toString)
      .sorted
      .map(fn => s"""<td><a href="/load/$fn">$fn</a></td>""")
      .mkString("\n")
  }
}
