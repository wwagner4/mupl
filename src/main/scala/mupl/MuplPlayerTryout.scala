package mupl

import java.nio.file.Path

import org.slf4j.LoggerFactory

object MuplPlayerTryout extends App {

  val logger = LoggerFactory.getLogger("tryout")

  try {
    val dir =  Path.of("src", "main")
    val muplFile = dir.resolve("mupl").resolve("p1.mupl")
    val player = new MuplPlayer
    val arg = "play"
    logger.info(s"Starting ${muplFile.toString} $arg")
    val res = player.play(muplFile, arg)
    res match {
      case Some(msg) => logger.info(msg)
      case None => logger.info("Finished successfull !!")
    }
  } catch {
    case e: Exception => logger.error(e.getMessage)
  }

}

