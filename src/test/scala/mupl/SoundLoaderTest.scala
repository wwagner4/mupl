package mupl

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers

class SoundLoaderTest extends AnyFunSuite with Matchers {

  test("Silence") {
    val yml =
      """sounds:
        |  - Silence:
        |      type: plainFromMelody
        |      desc: No sound at all
        |      chuckCode: >
        |        class SilentMelody extends Melody {
        |        }
        |
        |""".stripMargin
    val snds = SoundLoader.loadChuckSounds(yml)
    snds.size.mustBe(1)
    snds(0) match {
      case s: PlainFromMelody => 
        s.name.mustBe("Silence")
        s.desc.mustBe("No sound at all")
        s.chuckCode.mustBe(
          """class SilentMelody extends Melody { }
            |""".stripMargin)
    }
  }

}
