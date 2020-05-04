package mupl

import java.util.concurrent.TimeUnit

import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future, TimeoutException}
import scala.sys.process._

case class MuplPlayer(muplConfig: MuplConfig, soundLoader: SoundLoader) {

  private val logger = LoggerFactory.getLogger("player")

  private val parser = MuplParser(soundLoader.descs())
  private var _process = Option.empty[Process]

  def stop(): Unit = {
    _process match {
      case None => logger.info("No process is running")
      case Some(p) =>
        p.destroy()
        logger.info("process stopped")
    }
  }

  def play(mupl: String, soundsCode: String, arg: String): Option[String] = {
    _process match {
      case Some(p) =>
        p.destroy()
        _play(mupl, soundsCode, arg)
      case None =>
        _play(mupl, soundsCode, arg)
    }
  }

  private lazy val baseChuckCode = MuplUtil.resToStr("base.ck")

  private def _play(mupl: String, chuckSoundsCode: String, arg: String): Option[String] = {
    val piece = parser.parsePiece(mupl)
    val chuckStr = MuplToChuck.convert(piece.variables)
    val chuckGlobals = MuplToChuck.convert(piece.globals)
    val code = chuckGlobals + baseChuckCode + "\n" + chuckSoundsCode + "\n" + chuckStr
    val allp = MuplUtil.writeToTmp(code)
    val stdout = new StringBuilder
    val stderr = new StringBuilder

    val launcher = Process("chuck", Seq(s"${allp.toString}:$arg", "-p"))
    val f = Future {
      val process = launcher.run(ProcessLogger(stdout append _, stderr append _))
      try {
        _process = Some(process)
        val status = process.exitValue()
        message(stdout, stderr, status, code)
      } finally {
        _process = None
      }
    }
    try {
      Await.result(f, Duration.create(1000, TimeUnit.MILLISECONDS))
    } catch {
      case _: TimeoutException => Some("Music is playing") // Music is playing nothing to do
    }
  }

  private def message(
                       stdout: StringBuilder,
                       stderr: StringBuilder,
                       status: Int,
                       code: String): Option[String] = {
    val sb = new StringBuilder
    if (status != 0)
      sb.append(s"return value $status ")
    sb.append(stdout.toString())
    sb.append(stderr.toString())
    if (sb.isEmpty) None
    else if (status == 143) Some("stopped")
    else {
      throw new PlayerException(s"Error executing chuck. ${sb.toString()}", code)
    }
  }
  

}

class PlayerException(val msg: String, val code: String) extends Exception(msg)
