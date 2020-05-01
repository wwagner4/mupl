package mupl

import java.io.{FileReader, PrintWriter}
import java.nio.file.{Files, Path}
import java.util.Properties

import org.slf4j.LoggerFactory

import scala.io.Source

object MuplUtil {

  private val logger = LoggerFactory.getLogger("util")
  
  lazy val config: MuplConfig = {

    val homeDir = Path.of(System.getProperty("user.home"))

    def copyRes(resPath: String, resName: String, outDir: Path): Unit = {
      val is = getClass.getClassLoader.getResourceAsStream(resPath + resName)
      Files.copy(is, outDir.resolve(resName))
    }

    lazy val props: Properties = {

      val confPath = Path.of("mupl", "config")
      val confFileName = "mupl.properties"

      val confDir = homeDir.resolve(confPath)
      if (!Files.exists(confDir)) {
        Files.createDirectories(confDir)
      }
      val confFile = confDir.resolve(confFileName)
      if (!Files.exists(confFile)) {
        copyRes("", "mupl.properties", confDir)
      }
      val props = new Properties()
      logger.info("Read mupl config from " + confFile)
      props.load(new FileReader(confFile.toFile))
      props
    }

    def _chuckCall(): String = props.getProperty("mupl.chuck.call")

    def _workDir(): Path = {
      val pDirProp = Path.of(props.getProperty("mupl.piece.dir"))
      val pDir = if (pDirProp.isAbsolute) {
        pDirProp
      } else {
        homeDir.resolve(pDirProp)
      }
      if (!Files.exists(pDir)) {
        Files.createDirectories(pDir)
        copyRes("examples/", "p1.mupl", pDir)
        copyRes("examples/", "p2.mupl", pDir)
        copyRes("examples/", "soundtest.mupl", pDir)
      }
      logger.info("Mupl work dir is " + pDir)
      pDir
    }

    new MuplConfig {
      override val chuckCall: String = _chuckCall()

      override val workDir: Path = _workDir()
    }

  }


  def fileToStr(path: Path): String = {
    val src = Source.fromFile(path.toFile)
    try {
      src.getLines.mkString("\n")
    } finally {
      src.close()
    }
  }

  def resToStr(path: String): String = {
    val is = getClass.getClassLoader.getResourceAsStream(path)
    val src = Source.fromInputStream(is)
    try {
      src.getLines.mkString("\n")
    } finally {
      src.close()
    }
  }

  def writeToTmp(content: String): Path = {
    val tmpFile = Path.of(System.getProperty("java.io.tmpdir")).resolve("all.ck")
    writeToFile(content, tmpFile)
  }

  def writeToFile(content: String, file: Path): Path = {
    new PrintWriter(file.toFile) {
      write(content); close()
    }
    file
  }


  def walk(f: Chunk => Unit, chunk: Chunk, symbolMap: Option[Map[String, Chunk]] = None): Unit = {
    f(chunk)
    chunk match {
      case seq: Sequence => seq.chunks.foreach(c => walk(f, c, symbolMap))
      case par: Parallel => par.sequences.foreach(c => walk(f, c, symbolMap))
      case symbol: Symbol => symbolMap match {
        case Some(map) => walk(f, map(symbol.name), symbolMap)
        case None => // nothing to do  
      }
      case _ => // nothing to do  
    }
  }

}
