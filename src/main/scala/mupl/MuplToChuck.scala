package mupl

import mupl.GainVal.GainVal

object MuplToChuck {

  def convert(globals: Globals): String = {
    s"""
       |${globals.alldur} => int alldur;
       |${globals.globalSpeedFact} => float globalSpeedFact;
       |${globals.globalGainFact} => float globalGainFact;
       |
       |""".stripMargin

  }

  def convert(vars: List[Variable]): String = {
    val st = SymbolTable(vars)
    val sb = _convert(vars, new StringBuilder, st)
    val names = vars.map { v => v.name }
    sb.append(
      s"""
         |if (me.args() != 1) {
         |    <<<"one argument must be defined" >>>;
         |} else {
         |   me.arg(0) => string arg;
         |   if (arg == "${names(0)}") {${names(0)}();alldur::second => now;}
         |${argStarters(names)}
         |   else {<<<"unknown argument", arg>>>;}
         |}
         |
         |""".stripMargin)
    sb.toString()
  }

  def argStarters(names: List[String]): String = {
    names match {
      case _ :: rest => rest.map(argStarter).mkString("\n")
    }
  }


  def argStarter(name: String): String = {
    s"""   else if (arg == "$name") {$name();alldur::second => now;}"""
  }

  @scala.annotation.tailrec
  def _convert(vars: List[Variable], builder: StringBuilder, st: SymbolTable): StringBuilder = {
    vars match {
      case Nil => builder
      case vari :: rest =>
        builder.append(str(vari, st))
        _convert(rest, builder, st)
    }
  }

  def strSym(sym: Symbol, name: String, st: SymbolTable): String = {
    s"""
      |
      |fun void $name() {
      |    ${sym.name}();
      |}
      |""".stripMargin
  }

  def strMelo(melo: Melo, name: String, st: SymbolTable): String = {

    def soundToString(sound: Sound): String = {

      def mapGain(gain: Option[GainVal]): String = {
        gain match {
          case None => "1.0"
          case Some(gv) => gv match {
            case GainVal.HH => "1.7"
            case GainVal.H => "1.3"
            case GainVal.M => "1.0"
            case GainVal.L => "0.7"
            case GainVal.LL => "0.3"
          }
        }
      }

      def mapPitch(pitch: Option[Int]): String = {
        pitch match {
          case Some(pi) => pi.toString
          case None => "44"
        }
      }


      val dur = sound.dur
      val gain = mapGain(sound.gain)
      val pitch = mapPitch(sound.pitch)
      s"""pl($dur, $gain, $pitch)"""
    }

    def melos: String = {
      melo.sounds
        .map(soundToString)
        .mkString(",")
    }

    s"""
       |class ${name}Melody extends ${melo.name}Melody {
       |    fun Sound[] sounds() {
       |        return [
       |            nop(), $melos
       |        ];
       |    }
       |}
       |fun void $name() {
       |    ${name}Melody m;
       |    m.play();
       |}
       |""".stripMargin
  }

  def strSeq(seq: Sequence, name: String, st: SymbolTable): String = {
    val calls = seq.chunks.map(s => s"""${s.name}();""").mkString(" ")
    s"""
       |fun void $name() {
       |    $calls
       |}
       |""".stripMargin
  }

  def parFunctions(par: Parallel, name: String, sb: StringBuilder): List[String] = {
    def fnam(nam: String, i: Int): String = f"$nam$i%03d"
    
    def calls(se: Sequence): String = 
      se.chunks.map(s => s"""${s.name}();""").mkString(" ")
    
    def func(seq: Sequence, i: Int): String =
      s"""
         |fun void ${fnam(name, i)}() {
         |    ${calls(seq)}
         |    1::second => now;
         |}
         |""".stripMargin
    
    val funcs = 
      par.sequences
      .zipWithIndex
      .map { case (seq, i) => (func(seq, i), fnam(name, i)) }
    
    sb.append(funcs.map(_._1).mkString("\n"))
    funcs.map(_._2)
  }

  def parFunction(fnam: List[String], name: String, sb: StringBuilder): Unit = {
    
    val calls = fnam.map{nam => s"""    spork ~ $nam();"""}.mkString("\n")
    
    val cont = s"""
      |fun void $name() {
      |$calls
      |    1::second => now;
      |}
      |""".stripMargin
    
    sb.append(cont)
    
  }

  def strPar(par: Parallel, name: String, st: SymbolTable): String = {
    val sb = new StringBuilder
    val fnams = parFunctions(par, name, sb)
    parFunction(fnams, name, sb)
    sb.toString()
  }

  def str(vari: Variable, st: SymbolTable): String = {
    vari.chunk match {
      case c: Symbol => strSym(c, vari.name, st)
      case c: Melo => strMelo(c, vari.name, st)
      case c: Sequence => strSeq(c, vari.name, st)
      case c: Parallel => strPar(c, vari.name, st)
    }
  }
}
