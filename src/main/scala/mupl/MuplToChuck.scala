package mupl

import java.nio.file.Path

object MuplToChuck {

  def convertVars(vars: Seq[Variable]): String = {
    ???
  }

  def convert(path: Path): String = {
    val mupl = MuplUtil.fileToStr(path)
    val vars: Seq[Variable] = MuplParser.parseAll(mupl)
    convertVars(vars)
  }
}