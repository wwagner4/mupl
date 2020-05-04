package mupl

import java.nio.file.{Path, Paths}

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers

class SoundLoaderTest extends AnyFunSuite with Matchers {
  
  private val testCfg = new MuplConfig {
    override def chuckCall: String = ""

    override def workDir: Path = Paths.get("/")

    override def soundDir: Path = Paths.get("/")
  }

  test("Load Silence") {
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
    val snds = SoundYamlLoader.loadChuckSounds(yml, "Test string")
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

  test("Descs Silence") {
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
    val snds = SoundYamlLoader.loadChuckSounds(yml, "Test string")
    val descs = SoundLoaderImpl(snds).descs()
    descs.validIds.mustBe("Silence")
    descs.isValidId("Silence").mustBe(true)
    descs.isValidId("X").mustBe(false)
  }

  test("loadSound Silence X") {
    val yml =
      """sounds:
        |  - Silence:
        |      type: plainFromMelody
        |      desc: No sound at all
        |      chuckCode: >
        |        class SilentMelody extends Melody {
        |        }
        |  - X:
        |      type: plainFromMelody
        |      desc: X ??
        |      chuckCode: >
        |        class SilentMelody extends Melody {
        |        }
        |""".stripMargin
    val snds = SoundYamlLoader.loadChuckSounds(yml, "Test string")
    val sl = SoundLoaderImpl(snds)
    val chuck = sl.loadSound()
    chuck.mustBe(
      """class SilentMelody extends Melody { }
        |
        |class SilentMelody extends Melody { }
        |""".stripMargin)
  }

}
