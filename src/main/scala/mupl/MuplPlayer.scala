package mupl

import scala.sys.process._

class MuplPlayer {

  private val soundsDesc = {
    val sl = List(
      SoundDesc.of("Silent", "Makes no sound. Should be used for pause only melodies"),
      SoundDesc.of("SK", "Harpsichord"),
      SoundDesc.of("GlotAhh", "Wooden sticks striking together. No pitch"),
    )
    SoundsDesc("sounds.ck", sl)
  }
  
  private val parser = MuplParser(soundsDesc)

  def play(mupl: String, arg: String): Option[String] = {
    try {
      val bstr = MuplUtil.resToStr("base.ck")
      val sstr = MuplUtil.resToStr(soundsDesc.resPath)
      val piece = parser.parsePiece(mupl)
      val chuckStr = MuplToChuck.convert(piece.variables)
      val chuckGlobals = MuplToChuck.convert(piece.globals)
      val code = chuckGlobals + bstr + "\n" + sstr + "\n" + chuckStr
      val allp = MuplUtil.writeToTmp(code)
      val cmd = s"${piece.globals.chuckCall} ${allp.toString}:$arg -p"
      val stdout = new StringBuilder
      val stderr = new StringBuilder
      val status = cmd.!(ProcessLogger(stdout append _, stderr append _))
      message(stdout, stderr, status, code)
    } catch {
      case e: Exception => Some(e.getMessage)
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
