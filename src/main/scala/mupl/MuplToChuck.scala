package mupl

import java.nio.file.Path

object MuplToChuck {

  def convertVars(vars: Seq[Variable]): String = {
    """
      |class SKMelody01 extends SKMelody {
      |    fun Sound[] sounds() {
      |        return [
      |            nop(), pl(1, 0.3, 55),pl(2, 1.0, 58),pl(2, 1.0, 56),pl(1, 0.3, 59),pl(2, 0.3, 58),pl(2, 0.3, 58),
      |            pl(1, 0.3, 55),pl(2, 1.0, 58),pl(2, 1.0, 56),pl(1, 0.3, 59),pl(1, 0.3, 58)
      |        ];
      |    }
      |}
      |fun void SKFunMelody01() {SKMelody01 m; m.play(); }
      |
      |SKFunMelody01();
      |1::second => now;
      |
      |""".stripMargin
  }

  def convert(path: Path): String = {
    val mupl = MuplUtil.fileToStr(path)
    val vars: Seq[Variable] = MuplParser.parseAll(mupl)
    convertVars(vars)
  }
}