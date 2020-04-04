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
