package mupl

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers

class ParserTest extends AnyFunSuite with Matchers {

  test("all") {
    val in: String =
      """
        |a = []; 
        |d = e;
        |x1 = {[
        |       a A[(||)(||)(|LL|)(4||)]
        |     ]};
        |""".stripMargin
    val piece = MuplParser.parseAll(in)
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
        |m2 = sk[(||)];
        |""".stripMargin
    val piece = MuplParser.parseAll(in)
    piece.size mustBe 1
    piece(0).chunk.getClass.getName mustBe "mupl.Melo"
  }

  test("all one symbol") {
    val in: String =
      """
        |m2 = sk;
        |""".stripMargin
    val piece = MuplParser.parseAll(in)
    piece.size mustBe 1
    piece(0).chunk.getClass.getName mustBe "mupl.Symbol"
  }

  test("variable") {
    val v = MuplParser.parseVariable("a = {};")
    v.isInstanceOf[Variable] mustBe true
    val p = v.chunk.asInstanceOf[Parallel]
    p.sequences.isEmpty mustBe true    
  }

  test("parse sequence") {
    val seq: Sequence = MuplParser.parseSequence(
      """[asd [a b c] {
        |  [ A[(1||) ] 
        |    B[(2||) (2||) (8||33) ] 
        |    [a b c d e f g h] 
        |  ]
        |  []
        |}]""".stripMargin)
    seq.chunks.size.mustBe(3)
    val s2 = seq.chunks(1).asInstanceOf[Sequence]
    s2.chunks.size.mustBe(3)
    val p = seq.chunks(2).asInstanceOf[Parallel]
    p.sequences.size mustBe 2
  }

  test("parse melo") {
    val melo = MuplParser.parseMelo("m1 [ (2|M|55) (4|M|55) (2|LL|77) ]")
    melo.name.mustBe("m1")
    melo.sounds.size.mustBe(3)

    melo.sounds(0).pitch.mustBe(Some(55))
    melo.sounds(1).pitch.mustBe(Some(55))
    melo.sounds(2).pitch.mustBe(Some(77))

    melo.sounds(0).dur.mustBe(2)
    melo.sounds(1).dur.mustBe(4)
    melo.sounds(2).dur.mustBe(2)

    melo.sounds(0).gain.mustBe(Some(GainVal.M))
    melo.sounds(1).gain.mustBe(Some(GainVal.M))
    melo.sounds(2).gain.mustBe(Some(GainVal.LL))

  }

  val dataValidSounds = List(
    ("(2|M|22)", Sound(2, Some(GainVal.M), Some(22))),
    ("(64|LL|21)", Sound(64, Some(GainVal.LL), Some(21))),
    ("(1|HH|108)", Sound(1, Some(GainVal.HH), Some(108))),
    ("(1|H|108)", Sound(1, Some(GainVal.H), Some(108))),
    ("(32|H|108)", Sound(32, Some(GainVal.H), Some(108))),
    ("(32|H|67)", Sound(32, Some(GainVal.H), Some(67))),
    ("(32|H |68)", Sound(32, Some(GainVal.H), Some(68))),
    ("(32|  H |68)", Sound(32, Some(GainVal.H), Some(68))),
    ("(32||68)", Sound(32, None, Some(68))),
    ("(|H|67)", Sound(1, Some(GainVal.H), Some(67))),
    ("( 2|H|67)", Sound(2, Some(GainVal.H), Some(67))),
    ("( 2  |H|67)", Sound(2, Some(GainVal.H), Some(67))),
    ("(2|H|)", Sound(2, Some(GainVal.H), None)),
    ("(2|H|66 )", Sound(2, Some(GainVal.H), Some(66))),
    ("(2|H| 66 )", Sound(2, Some(GainVal.H), Some(66))),
  )

  for ((in, should) <- dataValidSounds) {
    test(s"parse mupl.Sound $in") {
      MuplParser.parseSound(in).mustBe(should)
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
    test(s"parse invalid mupl.Sound $in") {
      val thrown = intercept[Exception] {
        MuplParser.parseSound(in)
      }
      val tm = thrown.getMessage
      if (!tm.contains(should)) fail(s"Message '$tm' must contain '$should'")
    }
  }


}