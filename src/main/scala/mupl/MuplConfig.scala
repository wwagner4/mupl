package mupl

import java.nio.file.Path

trait MuplConfig {

  def chuckCall: String
  def workDir: Path
  def soundDir: Path

}
