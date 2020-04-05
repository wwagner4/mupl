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

case class SoundsDesc(resPath: String,
                      descs: List[SoundDesc]
                     ) {
  def isValidId(id: String): Boolean = {
    descs.map(_.id).contains(id)
  }
  def validIds: String = {
    descs.map(_.id).mkString(", ")
  }
}

trait SoundDesc {
  def id: String

  def desc: String

}

object SoundDesc {

  private val idreg = """[a-zA-Z_][a-zA-Z0-9_]*""".r

  def of(id: String, desc: String): SoundDesc = {
    if (desc.isEmpty) throw new IllegalArgumentException("Decription must not be empty")
    if (!idreg.matches(id)) throw new IllegalArgumentException(s"Id must fulfill ${idreg.regex}")
    val _id = id
    val _desc = desc
    new SoundDesc {
      override def id: String = _id
      override def desc: String = _desc

    }

  }
}
