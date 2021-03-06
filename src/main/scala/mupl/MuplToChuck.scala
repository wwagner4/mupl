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
    VariableValidator.validate(vars)
    val sb = _convert(vars, new StringBuilder)
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
      case Nil => throw new IllegalArgumentException("At least one variable must be defined")  
      case _ :: rest => rest.map(argStarter).mkString("\n")
    }
  }


  def argStarter(name: String): String = {
    s"""   else if (arg == "$name") {$name();alldur::second => now;}"""
  }

  @scala.annotation.tailrec
  def _convert(vars: List[Variable], builder: StringBuilder): StringBuilder = {
    vars match {
      case Nil => builder
      case vari :: rest =>
        builder.append(str(vari))
        _convert(rest, builder)
    }
  }

  def strSym(sym: Symbol, name: String): String = {
    s"""
      |
      |fun void $name() {
      |    ${sym.name}();
      |}
      |""".stripMargin
  }

  def strMelo(melo: Melo, name: String): String = {

    def soundToString(sound: Sound): String = {

      def mapGain(gain: Option[GainVal]): String = {
        gain match {
          case None => "1.0"
          case Some(gv) => gv match {
            case GainVal.HH => "4.0"
            case GainVal.H => "2.0"
            case GainVal.M => "1.0"
            case GainVal.L => "0.5"
            case GainVal.LL => "0.25"
          }
        }
      }

      def mapPitch(pitch: Option[Int]): String = {
        pitch match {
          case Some(pi) => pi.toString
          case None => "44"
        }
      }

      sound match {
        case i: Inst =>
          val dur = i.dur
          val gain = mapGain(i.gain)
          val pitch = mapPitch(i.pitch)
          s"""pl($dur, $gain, $pitch)"""
        case b: Pause =>
          val dur = b.dur
          s"""pa($dur)"""
      }
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

  def strSeq(seq: Sequence, name: String): String = {
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
    
    val calls = fnam.zipWithIndex.map { case (nam, i) =>
      if (i == fnam.size - 1) s"""    $nam();"""
      else s"""    spork ~ $nam();"""
    }.mkString("\n")
    
    val cont = s"""
      |fun void $name() {
      |$calls
      |}
      |""".stripMargin
    
    sb.append(cont)
    
  }

  def strPar(par: Parallel, name: String): String = {
    val sb = new StringBuilder
    val fnams = parFunctions(par, name, sb)
    parFunction(fnams, name, sb)
    sb.toString()
  }

  def str(vari: Variable): String = {
    vari.chunk match {
      case c: Symbol => strSym(c, vari.name)
      case c: Melo => strMelo(c, vari.name)
      case c: Sequence => strSeq(c, vari.name)
      case c: Parallel => strPar(c, vari.name)
    }
  }
}
