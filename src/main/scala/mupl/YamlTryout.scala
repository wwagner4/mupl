package mupl

import net.jcazevedo.moultingyaml._
import net.jcazevedo.moultingyaml.DefaultYamlProtocol._

import scala.io.Source 

object YamlTryout extends App {
  val is = getClass.getClassLoader.getResourceAsStream("sounds/base.yml")
  val ymlStr = Source.fromInputStream(is).mkString
  print(ymlStr)
  val ast = ymlStr.parseYaml()
  print(ast)
}
