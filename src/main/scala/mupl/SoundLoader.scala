package mupl

import java.nio.file.{Files, Path}

import net.jcazevedo.moultingyaml._
import org.slf4j.LoggerFactory

import scala.jdk.CollectionConverters._
import scala.io.Source

class YamlException(val msg: String, val yamlString: String, val resourceDesc: String, cause: Throwable) 
  extends Exception(msg, cause) 

object SoundYamlLoader {

  def loadChuckSounds(mcfg: MuplConfig): List[ChuckSound] = {
    
    val exts = List("yml", "yaml")
    
    def isYaml(p: Path): Boolean = {
      val nam = p.toString.toLowerCase
      exts.exists(ext => nam.endsWith(ext))
    }
     
    Files.list(mcfg.soundDir)
      .iterator()
      .asScala
      .toList
      .filter(p => isYaml(p))
      .flatMap(p => loadChuckSoundsYaml(p))
  }
  
  def loadChuckSoundsYaml(yamlFile: Path): List[ChuckSound] = {
    val is = Files.newInputStream(yamlFile)
    val ymlStr = Source.fromInputStream(is).mkString
    loadChuckSounds(ymlStr, s"File: ${yamlFile.toString}")
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


trait SoundLoader {

  def descs(): SoundsDesc

  def loadSound(): String

}

case class SoundLoaderImpl(sounds: List[ChuckSound]) extends SoundLoader {
  
  def descs(): SoundsDesc = {
    val sds = sounds.map{ cs => SoundDesc.of(cs.name, cs.desc)}
    SoundsDescImpl(sds)
  }

  def loadSound(): String = {
    sounds.map(s => s.chuckCode).mkString("\n")
  }

}

class Resetable[T](init: () => T, desc: String) {

  private val logger = LoggerFactory.getLogger("resetable")
  
  private var _value = Option.empty[T]
  
  def reset(): Unit = {
    _value = None
  }

  def value(): T = {
    _value match {
      case Some(v) => v
      case None => 
        logger.info("Reloading " + desc)
        val v = init(); _value = Some(v); v
    }
  }  
}
