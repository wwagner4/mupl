import GainVal.GainVal
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers

import scala.util.parsing.combinator._

object GainVal extends Enumeration {
  type GainVal = Value
  val LL, L, M, H, HH = Value
}

case class Sound(pitch: Int,
                 dur: Int,
                 gain: GainVal
                )

object MuplParser extends RegexParsers {


  def sound: Parser[Sound] = "(" ~ """(0|[1-9]\d*)|\s*""".r  ~ "|" ~ """(0|[1-9]\d*)|\s*""".r  ~ "|" ~ """[A-Z]+|\s*""".r  ~ ")"    ^^ {case _ ~ a ~  _ ~ b ~ _ ~  c ~ _ => createSound(a, b, c)}


  def parseSound(str: String): Sound = {
    parse(sound, str) match {
      case Success(sound, _) => sound
      case Failure(a, b) => throw new IllegalStateException(s"FAILURE could not parse '$str' ($a) ($b)")
      case Error(a, b) => throw new IllegalStateException(s"ERROR could not parse '$str' ($a) ($b)")
    }
  }

  def createSound(a: String, b: String, c: String): Sound = {

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
          throw new IllegalArgumentException(s"$input does not contain avalid duration. Must be one of $durStr")
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


class ParseSuite extends AnyFunSuite with Matchers {
  
  val dataValidSounds = List(
    ("(22|2|M)", Sound(22, 2, GainVal.M)),
    ("(21|64|LL)", Sound(21, 64, GainVal.LL)),
    ("(108|1|HH)", Sound(108, 1, GainVal.HH)),
    ("(108|1|H)", Sound(108, 1, GainVal.H)),
    ("(108|32|H)", Sound(108, 32, GainVal.H)),
    ("(67|32|H)", Sound(67, 32, GainVal.H)),
    ("(68|32|H )", Sound(68, 32, GainVal.H)),
    ("(68|32|  H )", Sound(68, 32, GainVal.H)),
    ("(68|32|)", Sound(68, 32, GainVal.M)),
    ("(67||H)", Sound(67, 1, GainVal.H)),
    ("(67| 2|H)", Sound(67, 2, GainVal.H)),
    ("(67| 2  |H)", Sound(67, 2, GainVal.H)),
    ("(|2|H)", Sound(44, 2, GainVal.H)),
    ("(66 |2|H)", Sound(66, 2, GainVal.H)),
    ("(  66 |2|H)", Sound(66, 2, GainVal.H)),
  )

  for ((in, should) <- dataValidSounds) {
    test(s"parse Sound $in") {
      MuplParser.parseSound(in).mustBe(should)
    }
  }

  val dataInvalidSounds = List(
    ("(22|2|LLL)", "Must be one of"),
    ("(22|2|LLM)", "Must be one of"),
    ("(22|21|LL)", "Must be one of"),
    ("(22|3|LL)", "Must be one of"),
    ("(22|888|LL)", "Must be one of"),
    ("(1|1|LL)", "Must be greater"),
    ("(200|1|LL)", "Must be smaller"),
  )

  for ((in, should) <- dataInvalidSounds) {
    test(s"parse invalid Sound $in") {
      val thrown = intercept[Exception] {
        MuplParser.parseSound(in)
      }
      val tm = thrown.getMessage
      if (!tm.contains(should)) fail(s"Message '$tm' must contain '$should'")
    }
  }


}