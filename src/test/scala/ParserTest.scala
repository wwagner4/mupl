import GainVal.GainVal
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers

object GainVal extends Enumeration {
  type GainVal = Value
  val LL, L, M, H, HH = Value
}

case class Sound(pitch: Int,
                 dur: Int,
                 gain: GainVal
                )


class ParseSuite extends AnyFunSuite with Matchers {

  val dataValidSounds = List(
    ("(22|2|M)", Sound(22, 2, GainVal.M)),
    ("(21|64|LL)", Sound(21, 64, GainVal.LL)),
    ("(108|1|HH)", Sound(108, 1, GainVal.HH)),
    ("(108|1|H)", Sound(108, 1, GainVal.H)),
    ("(108|32|H)", Sound(108, 32, GainVal.H)),
    ("(67|32|H)", Sound(67, 32, GainVal.H)),
  )

  for ((in, should) <- dataValidSounds) {
    test(s"parse Sound $in") {
      parseSound(in).mustBe(should)
    }
  }

  val dataInvalidSounds = List(
    ("(22|2|M1)", "Must be one of"),
    ("(22|2|hallo)", "Must be one of"),
    ("(22|2|123123)", "Must be one of"),
    ("(22|2|LLL)", "Must be one of"),
    ("(22|21|LL)", "Must be one of"),
    ("(22|3|LL)", "Must be one of"),
    ("(22|888|LL)", "Must be one of"),
    ("(22|hallo|LL)", "Must be an integer"),
    ("(hallo|1|LL)", "Must be an integer"),
    ("(1|1|LL)", "Must be greater"),
    ("(200|1|LL)", "Must be smaller"),
  )

  for ((in, should) <- dataInvalidSounds) {
    test(s"parse invalid Sound $in") {
      val thrown = intercept[Exception] {
        parseSound(in)
      }
      val tm = thrown.getMessage
      if (!tm.contains(should)) fail(s"Message '$tm' must contain '$should'")
    }
  }


  def parseSound(input: String): Sound = {

    def toPitch(value: String): Int = {
      try {
        val pitch = value.toInt
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
      try {
        val dur = value.toInt
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
      try {
        GainVal.withName(value)
      } catch {
        case _: NoSuchElementException =>
          val gainVals = GainVal.values.mkString(", ")
          throw new IllegalArgumentException(s"$input does not contain a valid gain. Must be one of $gainVals")
      }
    }

    val SoundRegex = """\((.*)\|(.*)\|(.*)\)""".r
    input match {
      case SoundRegex(a, b, c) => Sound(toPitch(a), toDuration(b), toGain(c))
      case _ => throw new IllegalArgumentException(s"$input does not confirm $SoundRegex")
    }
  }

}