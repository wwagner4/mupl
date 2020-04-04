package mupl

import java.nio.file.Path

import scala.io.Source

object MuplUtil {

  def fileToStr(path: Path): String = {
    val src = Source.fromFile(path.toFile)
    try {
      src.getLines.mkString
    } finally {
      src.close()
    }
  }

}
