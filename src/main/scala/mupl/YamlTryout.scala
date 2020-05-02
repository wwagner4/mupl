package mupl

import net.jcazevedo.moultingyaml._

import scala.io.Source 

sealed trait Snd

case class PlainFromMelody(name: String, desc: String, chuckCode: String) extends Snd



object YamlTryout extends App {
  val is = getClass.getClassLoader.getResourceAsStream("sounds/base.yml")
  val ymlStr = Source.fromInputStream(is).mkString
  print(ymlStr)
  val ast = ymlStr.parseYaml()
  print(ast)
}
