package mupl

import java.io.PrintWriter
import java.nio.file.{Files, Path, Paths}

import scala.sys.process._

class MuplPlayer {

  val chuckPath = "chuck"

  def strToPath(content: String): Path = {
    val tmpFile = Path.of(System.getProperty("java.io.tmpdir")).resolve("all.ck")
    new PrintWriter(tmpFile.toFile) { write(content); close() }
    tmpFile
  }

  def play(basePath: Path, playPath: Path): Option[String] = {
    pathExists(basePath)
    pathExists(playPath)
    val bstr = MuplUtil.fileToStr(basePath)
    val pstr = MuplUtil.fileToStr(playPath)
//    val pstr = MuplToChuck.convert(playPath)
//    val all = bstr + "\n" + pstr
    val code = bstr + "\n" + pstr
    println(code)
    val allp = strToPath(code)
    val cmd = s"$chuckPath ${allp.toString}"
    val stdout = new StringBuilder
    val stderr = new StringBuilder
    val status = cmd.!(ProcessLogger(stdout append _, stderr append _))
    message(stdout, stderr, status)
  }

  def pathExists(path: Path): Unit = {
    if (!Files.exists(path)) {
      val wd = Paths.get(".")
      throw new IllegalArgumentException(s"file $path does not exist. workdir is ${wd.toAbsolutePath.toString}")
    }
  }

  private def message(stdout: StringBuilder, stderr: StringBuilder, status: Int): Option[String] = {
    val sb = new StringBuilder
    if (status != 0)
      sb.append(s"return value $status ")
    sb.append(stdout.toString())
    sb.append(stderr.toString())
    if (sb.isEmpty) None else Some(sb.toString())
  }

}
