package mupl

import java.nio.file.{Files, Path, Paths}

import mupl.GainVal.GainVal

object GainVal extends Enumeration {
  type GainVal = Value
  val LL, L, M, H, HH = Value
}

sealed trait Chunk

case class Melo(name: String,
                sounds: List[Sound]
               ) extends Chunk

case class Variable(name: String,
                    chunk: Chunk
                   )

case class Symbol(name: String) extends Chunk

case class Sequence(chunks: List[Symbol]
                   ) extends Chunk

case class Parallel(sequences: List[Sequence]) extends Chunk

sealed trait Sound

case class Inst(dur: Int,
                gain: Option[GainVal],
                pitch: Option[Int],
               ) extends Sound

case class Pause(dur: Int,
               ) extends Sound

case class Globals(chuckCall: String = "chuck",
                   soundsFile: Path = Paths.get("src/main/chuck/sounds.ck"),
                   muplDir: Path = Paths.get("src/main/mupl"),
                   alldur: Int = 20,
                   globalSpeedFact: Double = 1.0,
                   globalGainFact: Double = 0.1
                  )

object Globals {

  def parse(tuples: List[(String, String)]): Globals = {
    _parse(tuples, Globals())
  }

  @scala.annotation.tailrec
  private def _parse(tuples: List[(String, String)], g: Globals): Globals = {
    def extend(key: String, strValue: String): Globals = {
      key match {
        case "chuckCall" => g.copy(chuckCall = strValue)
        case "soundsFile" =>
          val f = Paths.get(strValue)
          if (Files.notExists(f)) throw new IllegalArgumentException(s"Path to sounds file does not exist: $strValue")
          g.copy(soundsFile = f)
        case "muplDir" =>
          val f = Paths.get(strValue)
          if (Files.notExists(f)) throw new IllegalArgumentException(s"Mupl dir does not exist: $strValue")
          g.copy(muplDir = f)
        case "alldur" =>
          g.copy(alldur = strValue.toInt)
        case "globalSpeedFact" =>
          g.copy(globalSpeedFact = strValue.toDouble)
        case "globalGainFact" =>
          g.copy(globalGainFact = strValue.toDouble)
        case _ =>
          throw new IllegalArgumentException(s"Unknown key: $key")
      }
    }

    tuples match {
      case Nil => g
      case (key, valueStr) :: rest => _parse(rest, extend(key, valueStr))
    }
  }
}

case class Piece(globals: Globals,
                 variables: List[Variable]
                )