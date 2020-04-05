package mupl

import java.nio.file.Path

import org.slf4j.LoggerFactory

object MuplPlayerTryout extends App {

  val logger = LoggerFactory.getLogger("tryout")

  val dir = Path.of("src", "main")
  val muplFile = dir.resolve("mupl").resolve("p2.mupl")
  val player = new MuplPlayer
  val arg = "play"
  logger.info(s"Starting ${muplFile.toString} $arg")
  val mupl = MuplUtil.fileToStr(muplFile)
  val res = player.play(mupl, arg)
  res match {
    case Some(msg) => logger.info(msg)
    case None => logger.info("Finished successfull !!")
  }

}

