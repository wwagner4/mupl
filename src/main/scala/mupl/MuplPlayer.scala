package mupl

import java.nio.file.{Files, Path, Paths}

import scala.sys.process._

class MuplPlayer {

  val chuckPath = "chuck"

  def play(path: Path): Unit = {
    if (!Files.exists(path)) {
      val wd = Paths.get(".")
      throw new IllegalArgumentException(s"file $path does not exist. workdir is ${wd.toAbsolutePath.toString}" )
    }
    val cmd = s"$chuckPath ${path.toString}"
    val stdout = new StringBuilder
    val stderr = new StringBuilder
    val r = cmd.!(ProcessLogger(stdout append _, stderr append _))
    if (r != 0)
      println(s"return value $r")
    pr(stdout)
    pr(stderr)
  }

  private def pr(valu: StringBuilder):Unit = {
    val v = valu.toString()
    if (v.nonEmpty)
      println(s"$v")
  }
}
