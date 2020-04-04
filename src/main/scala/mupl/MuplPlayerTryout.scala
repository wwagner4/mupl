package mupl

import java.nio.file.Path

import org.slf4j.LoggerFactory

object MuplPlayerTryout extends App {

  val logger = LoggerFactory.getLogger("tryout")

  try {
    val dir =  Path.of("src", "main")
    val baseFile = dir.resolve("chuck").resolve("base.ck")
    val soundsFile = dir.resolve("chuck").resolve("sounds.ck")
    val muplFile = dir.resolve("mupl").resolve("p1.mupl")
    val p = new MuplPlayer
    val arg = "par"
    logger.info(s"Starting ${baseFile.toString} ${muplFile.toString}")
    val res = p.play(baseFile, soundsFile, muplFile, arg)
    res match {
      case Some(msg) => logger.info(msg)
      case None => logger.info("Finished successfull !!")
    }
  } catch {
    case e: Exception => logger.error(e.getMessage)
  }

}

