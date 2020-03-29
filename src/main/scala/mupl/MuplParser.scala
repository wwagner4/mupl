package mupl

import mupl.GainVal.GainVal

import scala.util.parsing.combinator.RegexParsers

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

case class Sequence(chunks: List[Chunk]
                   ) extends Chunk

case class Parallel(sequences: List[Sequence]) extends Chunk

case class Sound(pitch: Int,
                 dur: Int,
                 gain: GainVal
                )

object MuplParser extends RegexParsers {

  def all: Parser[List[Variable]] = rep(variable) ^^ {variables => variables}

  def name: Parser[String] = """[a-zA-Z_][a-zA-Z0-9_]*""".r ^^ { nam => nam }

  def symbol: Parser[Symbol] = name ^^ {nam =>  Symbol(nam) }
  
  def melo: Parser[Melo] =
    name ~ "[" ~ rep(sound) ~ "]" ^^ { case name ~ _ ~ sounds ~ _ => Melo(name, sounds) }

  def variable: Parser[Variable] = name ~ "=" ~ chunk ^^ {
    case name ~ _ ~ chunk => Variable(name, chunk)
  }

  def sound: Parser[Sound] =
    "(" ~ """(0|[1-9]\d*)|\s*""".r ~ "|" ~ """(0|[1-9]\d*)|\s*""".r ~ "|" ~ """[A-Z]+|\s*""".r ~ ")" ^^ {
      case _ ~ a ~ _ ~ b ~ _ ~ c ~ _ => createSound(a, b, c)
    }

  def sequence: Parser[Sequence] = "[" ~ rep(chunk) ~ "]" ^^ {
    case _ ~ chunks ~ _ => Sequence(chunks)
  }

  def parallel: Parser[Parallel] = "{" ~ rep(sequence) ~ "}" ^^ {
    case _ ~ sequences ~ _ => Parallel(sequences)
  }

  def chunk: Parser[Chunk] = melo | symbol | sequence | parallel ^^ {chunk => chunk}

  def parseAll(str: String): List[Variable] = handleParseError(str, parse(all, str))

  def parseVariable(str: String): Variable = handleParseError(str, parse(variable, str))

  def parseSequence(str: String): Sequence = handleParseError(str, parse(sequence, str))

  def parseSound(str: String): Sound = handleParseError(str, parse(sound, str))

  def parseMelo(str: String): Melo = handleParseError(str, parse(melo, str))

  private def handleParseError[T](str: String, result: ParseResult[T]): T = {
    result match {
      case Success(out, _) => out
      case Failure(a, b) => throw new IllegalStateException(s"FAILURE could not parse '$str'. $a. ${b.pos})")
      case Error(a, b) => throw new IllegalStateException(s"ERROR could not parse '$str'. $a. ${b.pos})")
    }
  }

  private def createSound(a: String, b: String, c: String): Sound = {

    val input = s"($a|$b|$c)"

    def toPitch(value: String): Int = {
      val valt = value.trim
      try {
        val pitch = if (valt.isEmpty) 44 else valt.toInt
        if (pitch < 21) throw new IllegalArgumentException(s"$input does not contain a valid pitch. Must be greater 21")
        if (pitch > 108) throw new IllegalArgumentException(s"$input does not contain a valid pitch. Must be smaller 108")
        pitch
      } catch {
        case _: NumberFormatException =>
          throw new IllegalArgumentException(s"$input does not contain a valid pitch. Must be an integer")
      }
    }

    val validDurations = List(1, 2, 4, 8, 32, 64)

    def toDuration(value: String): Int = {
      val valt = value.trim
      try {
        val dur = if (valt.isEmpty) 1 else valt.toInt
        if (!validDurations.contains(dur)) {
          val durStr = validDurations.mkString(", ")
          throw new IllegalArgumentException(s"$input does not contain a valid duration. Must be one of $durStr")
        }
        dur
      } catch {
        case _: NumberFormatException =>
          throw new IllegalArgumentException(s"$input does not contain a valid duration. Must be an integer")
      }
    }

    def toGain(value: String): GainVal = {
      val valt = value.trim
      try {
        if (valt.isEmpty) GainVal.M
        else GainVal.withName(valt)
      } catch {
        case _: NoSuchElementException =>
          val gainVals = GainVal.values.mkString(", ")
          throw new IllegalArgumentException(s"$input does not contain a valid gain. Must be one of $gainVals")
      }
    }

    Sound(toPitch(a), toDuration(b), toGain(c))
  }

}
