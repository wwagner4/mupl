package mupl

import mupl.SoundLoader.getClass
import mupl.YamlTryout.getClass

import scala.io.Source
import net.jcazevedo.moultingyaml._

import scala.io.Source

object SoundYamlLoader {
  
  def loadChuckSounds(): List[ChuckSound] = {
    val is = getClass.getClassLoader.getResourceAsStream("sounds/base.yml")
    val ymlStr = Source.fromInputStream(is).mkString
    loadChuckSounds(ymlStr)
  }

  def loadChuckSounds(yml: String): List[ChuckSound] = {
    def toChuckSound(value: YamlValue): ChuckSound ={
      value match {
        case obj: YamlObject =>
          val tuple = obj.fields.toList(0)
          val name = tuple._1 match {
            case s: YamlString => s.value
            case _ => throw new IllegalStateException("Expected yaml string")
          }
          val obj1 = obj.getFields(YamlString(name))(0)
          obj1 match {
            case obj2: YamlObject =>
              val vals = obj2.getFields(YamlString("desc"), YamlString("chuckCode"))
              val desc = vals(0) match {
                case ys: YamlString => ys.value
                case _ => throw new IllegalStateException("Expected yaml string")
              }
              val cc = vals(1) match {
                case ys: YamlString => ys.value
                case _ => throw new IllegalStateException("Expected yaml string")
              }
              PlainFromMelody(name, desc, cc)
            case _ => throw new IllegalStateException("expected yaml object")
          }
        case _ => throw new IllegalStateException("Expected yaml object")
      }
    }

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
}

object SoundLoader {

  def loadSound(): String = {
    val csc = SoundYamlLoader.loadChuckSounds()
    SoundLoader(csc).loadSound()
  }

}

case class  SoundLoader(sounds: List[ChuckSound]) {

  def descs: SoundsDesc = {
    val sds = sounds.map{ cs => SoundDesc.of(cs.name, cs.desc)}
    SoundsDescImpl(sds)
  }

  def loadSound(): String = {
    sounds.map(s => s.chuckCode).mkString("\n\n")
  }

}
