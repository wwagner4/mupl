package mupl

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers

class ParserTest extends AnyFunSuite with Matchers {
  
  test("parse melo") {
    val melo  = MuplParser.parseMelo("m1 [ (55|2|M) (55|4|M) (77|2|LL) ]")
    melo.name.mustBe("m1")
    melo.sounds.size.mustBe(3)
    
    melo.sounds(0).pitch.mustBe(55)
    melo.sounds(1).pitch.mustBe(55)
    melo.sounds(2).pitch.mustBe(77)

    melo.sounds(0).dur.mustBe(2)
    melo.sounds(1).dur.mustBe(4)
    melo.sounds(2).dur.mustBe(2)

    melo.sounds(0).gain.mustBe(GainVal.M)
    melo.sounds(1).gain.mustBe(GainVal.M)
    melo.sounds(2).gain.mustBe(GainVal.LL)

  }

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
    test(s"parse mupl.Sound $in") {
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
    test(s"parse invalid mupl.Sound $in") {
      val thrown = intercept[Exception] {
        MuplParser.parseSound(in)
      }
      val tm = thrown.getMessage
      if (!tm.contains(should)) fail(s"Message '$tm' must contain '$should'")
    }
  }


}