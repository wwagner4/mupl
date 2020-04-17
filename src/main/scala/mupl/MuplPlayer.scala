package mupl

import java.util.concurrent.TimeUnit

import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future, TimeoutException}
import scala.sys.process._

class MuplPlayer {

  private val logger = LoggerFactory.getLogger("player")

  private val soundsDesc = {
    val sl = List(
      SoundDesc.of("Silent", "Makes no sound. Should be used for pause only melodies"),
      SoundDesc.of("SK", "Harpsichord"),
      SoundDesc.of("GlotAhh", "Wooden sticks striking together. No pitch"),
      SoundDesc.of("HM", "FM HevyMetl"),
      SoundDesc.of("M1", "Moog 1"),
      SoundDesc.of("M2", "Moog 2"),
    )
    SoundsDesc("sounds.ck", sl)
  }

  private val parser = MuplParser(soundsDesc)
  private var _process = Option.empty[Process]

  def stop(): Unit = {
    _process match {
      case None => logger.info("No process is running")
      case Some(p) =>
        p.destroy()
        logger.info("process stopped")
    }
  }

  def play(mupl: String, arg: String): Option[String] = {
    _process match {
      case Some(p) =>
        p.destroy()
        _play(mupl, arg)
      case None =>
        _play(mupl, arg)
    }
  }

  private def _play(mupl: String, arg: String): Option[String] = {
    try {
      val bstr = MuplUtil.resToStr("base.ck")
      val sstr = MuplUtil.resToStr(soundsDesc.resPath)
      val piece = parser.parsePiece(mupl)
      val chuckStr = MuplToChuck.convert(piece.variables)
      val chuckGlobals = MuplToChuck.convert(piece.globals)
      val code = chuckGlobals + bstr + "\n" + sstr + "\n" + chuckStr
      logger.info(code)
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
        case e: TimeoutException => Some("Music is playing") // Music is playing nothing to do
      }
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
    else {
      val lcode = code.split("\n")
        .zipWithIndex
        .map {
          case (l, i) =>
            val i1 = i + 1
            f"$i1%5d $l"
        }
        .mkString("</br>")
      val msg = lcode + "</br></br>" + sb.toString()
      logger.warn("Execution of chuck went twrong " + msg)
      Some(msg)
    }
  }

}
