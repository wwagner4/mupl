package mupl

import java.nio.file.Paths

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers

class ParserTest extends AnyFunSuite with Matchers {

  private val soundsDesc = {
    val sl = List(
      SoundDesc.of("m1", "test"),
      SoundDesc.of("SK", "test"),
    )
    SoundsDescImpl(sl)
  }

  private val parser = MuplParser(soundsDesc)

  test("all") {
    val in: String =
      """
        |a = [] 
        |d = e
        |x1 = {[
        |       a y z
        |     ]}
        |""".stripMargin
    val piece = parser.parseVariables(in)
    piece.size mustBe 3
    piece.forall(_.isInstanceOf[Variable]) mustBe true
    val e1 = piece(0)
    e1.name mustBe "a"
    e1.chunk.isInstanceOf[Sequence]

    val e2 = piece(1)
    e2.name mustBe "d"
    e2.chunk.isInstanceOf[Symbol]

    val e3 = piece(2)
    e3.name mustBe "x1"
    e3.chunk.isInstanceOf[Parallel]
  }

  test("all one melody") {
    val in: String =
      """
        |m1 = SK[(||)]
        |""".stripMargin
    val piece = parser.parseVariables(in)
    piece.size mustBe 1
    piece(0).chunk.getClass.getName mustBe "mupl.Melo"
  }

  test("all one symbol") {
    val in: String =
      """
        |m2 = sk
        |""".stripMargin
    val piece = parser.parseVariables(in)
    piece.size mustBe 1
    piece(0).chunk.getClass.getName mustBe "mupl.Symbol"
  }

  test("variable") {
    val v = parser.parseVariable("a = {}")
    v.name mustBe "a"
    v.chunk.getClass.getName mustBe "mupl.Parallel"
  }

  test("parse sequence") {
    val seq: Sequence = parser.parseSequence(
      """[a b c d e f g h] 
        |""".stripMargin)
    seq.chunks.size.mustBe(8)
  }

  test("parse melo") {
    val melo = parser.parseMelo("m1 [ (2|M|55) (4|M|55) (2|LL|77) ]")
    melo.name.mustBe("m1")
    melo.sounds.size.mustBe(3)

    melo.sounds(0).asInstanceOf[Inst].pitch.mustBe(Some(55))
    melo.sounds(1).asInstanceOf[Inst].pitch.mustBe(Some(55))
    melo.sounds(2).asInstanceOf[Inst].pitch.mustBe(Some(77))

    melo.sounds(0).asInstanceOf[Inst].dur.mustBe(2)
    melo.sounds(1).asInstanceOf[Inst].dur.mustBe(4)
    melo.sounds(2).asInstanceOf[Inst].dur.mustBe(2)

    melo.sounds(0).asInstanceOf[Inst].gain.mustBe(Some(GainVal.M))
    melo.sounds(1).asInstanceOf[Inst].gain.mustBe(Some(GainVal.M))
    melo.sounds(2).asInstanceOf[Inst].gain.mustBe(Some(GainVal.LL))

  }

  val dataValidSounds = List(
    ("(2|M|22)", Inst(2, Some(GainVal.M), Some(22))),
    ("(64|LL|21)", Inst(64, Some(GainVal.LL), Some(21))),
    ("(1|HH|108)", Inst(1, Some(GainVal.HH), Some(108))),
    ("(1|H|108)", Inst(1, Some(GainVal.H), Some(108))),
    ("(32|H|108)", Inst(32, Some(GainVal.H), Some(108))),
    ("(32|H|67)", Inst(32, Some(GainVal.H), Some(67))),
    ("(32|H |68)", Inst(32, Some(GainVal.H), Some(68))),
    ("(32|  H |68)", Inst(32, Some(GainVal.H), Some(68))),
    ("(32||68)", Inst(32, None, Some(68))),
    ("(|H|67)", Inst(1, Some(GainVal.H), Some(67))),
    ("( 2|H|67)", Inst(2, Some(GainVal.H), Some(67))),
    ("( 2  |H|67)", Inst(2, Some(GainVal.H), Some(67))),
    ("(2|H|)", Inst(2, Some(GainVal.H), None)),
    ("(2|H|66 )", Inst(2, Some(GainVal.H), Some(66))),
    ("(2|H| 66 )", Inst(2, Some(GainVal.H), Some(66))),
    ("#(2)", Pause(2)),
    ("#(8)", Pause(8)),
  )

  for ((in, should) <- dataValidSounds) {
    test(s"parse mupl.Inst $in") {
      parser.parseSound(in).mustBe(should)
    }
  }

  val dataInvalidSounds = List(
    ("(2|LLL|22)", "Must be one of"),
    ("(2|LLM|22)", "Must be one of"),
    ("(21|LL|22)", "Must be one of"),
    ("(3|LL|22)", "Must be one of"),
    ("(888|LL|22)", "Must be one of"),
    ("(1|LL|1)", "Must be greater"),
    ("(1|LL|200)", "Must be smaller"),
  )

  for ((in, should) <- dataInvalidSounds) {
    test(s"parse invalid mupl.Inst $in") {
      val thrown = intercept[Exception] {
        parser.parseSound(in)
      }
      val tm = thrown.getMessage
      if (!tm.contains(should)) fail(s"Message '$tm' must contain '$should'")
    }
  }

  test("globals simple") {
    val g = parser.parseGlobals("chuckCall <= /opt/bin/chuck")
    g.chuckCall mustBe "/opt/bin/chuck"
  }
  test("globals multi") {
    val g = parser.parseGlobals(
      """
        |chuckCall <= /opt/bin/chuck
        |globalGainFact <= 2.2
        |
        |""".stripMargin)
    g.chuckCall mustBe "/opt/bin/chuck"
    g.globalGainFact mustBe 2.2
    
  }
  
  test("piece") {
    val p = parser.parsePiece(
      """
        |chuckCall <= /opt/bin/chuck
        |globalGainFact <= 2.2
        |
        |a = [] 
        |d = e
        |x1 = {[
        |       a y z
        |     ]}
        |""".stripMargin)
    
    p.globals.globalGainFact mustBe 2.2
    p.globals.globalSpeedFact mustBe 1.0
    p.globals.chuckCall mustBe "/opt/bin/chuck"
    
    p.variables.size mustBe 3
  }
}