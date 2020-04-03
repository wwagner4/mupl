package mupl

import java.nio.file.Path

import org.slf4j.LoggerFactory

object MuplPlayerTryout extends App {

  val logger = LoggerFactory.getLogger("tryout")

  try {
    val dir =  Path.of("src", "main", "chuck")
    val base = dir.resolve("base.ck")
    val play = dir.resolve("play01.ck")
    logger.info(s"Starting ${base.toString} ${play.toString}")
    val p = new MuplPlayer
    val res = p.play(base, play)
    res match {
      case Some(msg) => logger.info(msg)
      case None => logger.info("Finished successfull !!")
    }
  } catch {
    case e: Exception => logger.error(e.getMessage)
  }

}
