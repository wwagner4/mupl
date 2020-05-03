package mupl

import net.jcazevedo.moultingyaml._

import scala.io.Source

class YamlException(val msg: String, val yamlString: String, val resourceDesc: String, cause: Throwable) 
  extends Exception(msg, cause) 

object SoundYamlLoader {
  
  def loadChuckSounds(): List[ChuckSound] = {
    val is = getClass.getClassLoader.getResourceAsStream("sounds/base.yml")
    val ymlStr = Source.fromInputStream(is).mkString
    loadChuckSounds(ymlStr, s"Class path resource: sounds/base.yml")
  }

  def loadChuckSounds(yml: String, resourceDesc: String): List[ChuckSound] = {
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
    try {
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
    } catch {
      case e: Exception => 
        throw new YamlException(e.getMessage, yamlString = yml, resourceDesc = resourceDesc, e)
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
    sounds.map(s => s.chuckCode).mkString("\n")
  }

}
