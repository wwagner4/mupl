package mupl

import mupl.GainVal.GainVal

import scala.util.parsing.combinator.RegexParsers

case class MuplParser(soundsDesc: SoundsDesc) extends RegexParsers {
  
  def piece: Parser[Piece] = globals ~ variables ~ ".*".r ^^ {case g ~ v ~ rest => 
    if (!rest.isEmpty) throw new IllegalArgumentException(s"Syntax error after: $rest")
    Piece(g, v)}

  def globals: Parser[Globals] = keyValue.* ^^ {ts => Globals.parse(ts)}
  
  def keyValue: Parser[(String, String)] = name ~ "<=" ~ ".*".r ^^ {case key ~ _ ~ value => (key, value)}
  
  def variables: Parser[List[Variable]] = rep(variable) 

  def name: Parser[String] = """[a-zA-Z_][a-zA-Z0-9_]*""".r ^^ { nam => nam }

  def symbol: Parser[Symbol] = name ^^ { nam => Symbol(nam) }

  def melo: Parser[Melo] =
    name ~ "[" ~ rep(sound) ~ "]" ^^ { case name ~ _ ~ sounds ~ _ => 
      if (!soundsDesc.isValidId(name)) throw new IllegalArgumentException(s"$name is not a valid sound id. ${soundsDesc.validIds}")
      Melo(name, sounds) }

  def variable: Parser[Variable] = name ~ "=" ~ chunk ^^ {
    case name ~ _ ~ chunk => Variable(name, chunk)
  }

  def inst: Parser[Sound] =
    "(" ~ """(0|[1-9]\d*)|\s*""".r ~ "|" ~ """[A-Z]+|\s*""".r ~ "|" ~ """(0|[1-9]\d*)|\s*""".r ~ ")" ^^ {
      case _ ~ a ~ _ ~ b ~ _ ~ c ~ _ => createInst(a, b, c)
    }

  def pause: Parser[Sound] =
    "#(" ~ """(0|[1-9]\d*)|\s*""".r ~ ")" ^^ {
      case _ ~ a ~ _ => createPause(a)
    }
  
  def sound: Parser[Sound] = pause | inst

  def sequence: Parser[Sequence] = "[" ~ symbol.* ~ "]" ^^ {
    case _ ~ chunks ~ _ => Sequence(chunks)
  }

  def parallel: Parser[Parallel] = "{" ~ rep(sequence) ~ "}" ^^ {
    case _ ~ sequences ~ _ => Parallel(sequences)
  }

  def chunk: Parser[Chunk] = melo | symbol | sequence | parallel ^^ { chunk => chunk }


  def parsePiece(str: String): Piece = handleParseError(str, parse(piece, str))

  def parseVariables(str: String): List[Variable] = handleParseError(str, parse(variables, str))

  def parseGlobals(str: String): Globals = handleParseError(str, parse(globals, str))

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

  private def createInst(a: String, b: String, c: String): Inst = {

    val input = s"($a|$b|$c)"

    def toPitch(value: String): Option[Int] = {
      val valt = value.trim
      try {
        if (valt.isEmpty) {
          None
        } else {
          val ival = valt.toInt
          if (ival < 21) throw new IllegalArgumentException(s"$input does not contain a valid pitch. Must be greater 21")
          if (ival > 108) throw new IllegalArgumentException(s"$input does not contain a valid pitch. Must be smaller 108")
          Some(ival)
        }
      } catch {
        case _: NumberFormatException =>
          throw new IllegalArgumentException(s"$input does not contain a valid pitch. Must be an integer")
      }
    }

    def toGain(value: String): Option[GainVal] = {
      val valt = value.trim
      try {
        if (valt.isEmpty) None
        else Some(GainVal.withName(valt))
      } catch {
        case _: NoSuchElementException =>
          val gainVals = GainVal.values.mkString(", ")
          throw new IllegalArgumentException(s"$input does not contain a valid gain. Must be one of $gainVals")
      }
    }

    Inst(toDuration(a, input), toGain(b), toPitch(c))
  }

  private def createPause(a: String): Pause = {

    val input = s"#($a)"

    Pause(toDuration(a, input))
  }

  private val validDurations = List(1, 2, 4, 8, 32, 64)

  private def toDuration(value: String, input: String): Int = {
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



}
