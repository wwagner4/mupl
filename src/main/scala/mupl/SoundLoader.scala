package mupl

import mupl.YamlTryout.getClass

import scala.io.Source
import net.jcazevedo.moultingyaml._

import scala.io.Source


object SoundLoader {

  private lazy val sounds: List[ChuckSound] = loadChuckSounds()

  def loadChuckSounds(): List[ChuckSound] = {
    val is = getClass.getClassLoader.getResourceAsStream("sounds/base.yml")
    val ymlStr = Source.fromInputStream(is).mkString
    loadChuckSounds(ymlStr)
  }

  def toChuckSound(value: YamlValue): ChuckSound ={
    value match {
      case obj: YamlObject => 
        val tuple = obj.fields.toList(0)
        val name = tuple._1 match {
          case s: YamlString => s.value
          case _ => throw new IllegalStateException("Expected yaml string")
        }
        val x = obj.getFields(YamlString(name))(0)
        x match {
          case obj1: YamlObject =>
            val vals = obj1.getFields(YamlString("desc"), YamlString("chuckCode"))
            val desc = vals(0) match {
              case ys: YamlString => ys.value
            }
            val cc = vals(1) match {
              case ys: YamlString => ys.value
            }
            PlainFromMelody(name, desc, cc)
          case _ => throw new IllegalStateException("expected yaml object")  
        } 
      case _ => throw new IllegalStateException("Expected yaml object")        
    }
  }

  def loadChuckSounds(yml: String): List[ChuckSound] = {
    val ast: YamlValue = yml.parseYaml()
    ast match {
      case YamlObject(fields) =>
        val sounds = fields(YamlString("sounds"))
        sounds match {
          case ya: YamlArray => 
            ya.elements.toList.map(ye => toChuckSound(ye))
          case _ => throw new IllegalStateException("sounds must be a list")  
        }
      case _ => throw new IllegalStateException("Sound yaml must start with an object")
    }
  }


  def descs: SoundsDesc = {
    ???
  }

  def loadSound(): String = {
    ???
  }

}
