package mupl

import java.nio.file.Path

object MuplPlayerTryout extends App {
  
  val f = Path.of("src", "main", "chuck", "base.ck")
  val p = new MuplPlayer
  p.play(f)

}
