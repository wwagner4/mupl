package mupl

import java.io.PrintWriter
import java.nio.file.{Files, Path, Paths}

import scala.sys.process._

class MuplPlayer {

  def strToPath(content: String): Path = {
    val tmpFile = Path.of(System.getProperty("java.io.tmpdir")).resolve("all.ck")
    new PrintWriter(tmpFile.toFile) { write(content); close() }
    tmpFile
  }
  
  private val soundsDesc = {
    val sl = List(
      SoundDesc.of("Silent", "Makes no sound. Should be used for pause only melodies"),
      SoundDesc.of("SK", "Harpsichord"),
      SoundDesc.of("GlotAhh", "Wooden sticks striking together. No pitch"),
    )
    SoundsDesc("sounds.ck", sl)
  }
  
  private val parser = MuplParser(soundsDesc)

  def play(playPath: Path, arg: String): Option[String] = {
    pathExists(playPath)
    val bstr = MuplUtil.resToStr("base.ck")
    val sstr = MuplUtil.resToStr(soundsDesc.resPath)
    val pstr = MuplUtil.fileToStr(playPath)
    val piece = parser.parsePiece(pstr)
    val chuckStr = MuplToChuck.convert(piece.variables)
    val chuckGlobals = MuplToChuck.convert(piece.globals)
    val code = chuckGlobals + bstr + "\n" + sstr + "\n" + chuckStr
    val allp = strToPath(code)
    val cmd = s"${piece.globals.chuckCall} ${allp.toString}:$arg"
    val stdout = new StringBuilder
    val stderr = new StringBuilder
    val status = cmd.!(ProcessLogger(stdout append _, stderr append _))
    message(stdout, stderr, status, code)
  }

  def pathExists(path: Path): Unit = {
    if (!Files.exists(path)) {
      val wd = Paths.get(".")
      throw new IllegalArgumentException(s"file $path does not exist. workdir is ${wd.toAbsolutePath.toString}")
    }
  }

  private def message(stdout: StringBuilder, stderr: StringBuilder, status: Int, code: String): Option[String] = {
    val sb = new StringBuilder
    if (status != 0)
      sb.append(s"return value $status ")
    sb.append(stdout.toString())
    sb.append(stderr.toString())
    if (sb.isEmpty) None 
    else {
      val lcode  = code.split("\n")
        .zipWithIndex
        .map{case (l, i) =>
          val i1 = i + 1
          f"$i1%5d $l"}
        .mkString("\n")

      Some(lcode + "\n\n" + sb.toString())
    }
  }

}
