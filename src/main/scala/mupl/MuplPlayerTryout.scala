package mupl

import java.nio.file.Path

import org.slf4j.LoggerFactory

object MuplPlayerTryout extends App {

  val logger = LoggerFactory.getLogger("tryout")

  try {
    val dir =  Path.of("src", "main")
    val base = dir.resolve("chuck").resolve("base.ck")
    val play = dir.resolve("chuck").resolve("play.ck")
//    val play = dir.resolve("mupl").resolve("p1.mupl")
//    logger.info(s"Starting ${base.toString} ${play.toString}")
    val p = new MuplPlayer
    val arg = "m3"
    val res = p.play(base, play, arg)
    res match {
      case Some(msg) => logger.info(msg)
      case None => logger.info("Finished successfull !!")
    }
  } catch {
    case e: Exception => logger.error(e.getMessage)
  }

}

