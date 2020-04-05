package mupl

import java.io.PrintWriter
import java.nio.file.Path

import scala.io.Source

object MuplUtil {

  def fileToStr(path: Path): String = {
    val src = Source.fromFile(path.toFile)
    try {
      src.getLines.mkString("\n")
    } finally {
      src.close()
    }
  }

  def resToStr(path: String): String = {
    val is = getClass.getClassLoader.getResourceAsStream(path)
    val src = Source.fromInputStream(is)
    try {
      src.getLines.mkString("\n")
    } finally {
      src.close()
    }
  }

  def writeToTmp(content: String): Path = {
    val tmpFile = Path.of(System.getProperty("java.io.tmpdir")).resolve("all.ck")
    writeToFile(content, tmpFile)
  }

  def writeToFile(content: String, file: Path): Path = {
    new PrintWriter(file.toFile) { write(content); close() }
    file
  }



  def walk(f: Chunk => Unit, chunk: Chunk, symbolMap: Option[Map[String, Chunk]] = None): Unit = {
    f(chunk)
    chunk match {
      case seq: Sequence => seq.chunks.foreach(c => walk(f, c, symbolMap))
      case par: Parallel => par.sequences.foreach(c => walk(f, c, symbolMap))
      case symbol: Symbol => symbolMap match {
        case Some(map) => walk(f, map(symbol.name), symbolMap)
        case None => // nothing to do  
      }
      case _ => // nothing to do  
    }
  }

}
