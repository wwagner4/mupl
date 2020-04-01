package mupl

case class SymbolTable(variables: List[Variable]) {

  val map: Map[String, Chunk] = variables.map(v => (v.name, v.chunk)).toMap
  validate()

  def chunk(nam: String): Chunk = map(nam)

  private def validate(): Unit = {
    val symbols = variables.map(_.name)
    val topSeq = Sequence(variables.map(_.chunk))
    val check = checkSymDefined(symbols) _
    walk(check, topSeq)
    map.keys.foreach { k =>
      val checkSym = checkContainsSymbol(k) _
      walk(checkSym, chunk(k), deep = true)
    }
  }

  def checkContainsSymbol(symbol: String)(chunk: Chunk): Unit = {
    chunk match {
      case sym: Symbol if symbol == sym.name =>
        val sn = sym.name
        throw new IllegalArgumentException(s"Loop for symbol $sn")
      case _ => // Nothing to do  
    }
  }

  def checkSymDefined(symbols: List[String])(chunk: Chunk): Unit = {
    chunk match {
      case sym: Symbol => if (!symbols.contains(sym.name)) {
        val sn = sym.name
        val sstr = symbols.mkString(",")
        throw new IllegalArgumentException(s"Undfeined symbol $sn. must be one of $sstr")
      }
      case _ => // Nothing to do  
    }
  }

  private def walk(f: Chunk => Unit, chunk: Chunk, deep: Boolean = false): Unit = {
    f(chunk)
    chunk match {
      case seq: Sequence => seq.chunks.foreach(c => walk(f, c, deep))
      case par: Parallel => par.sequences.foreach(c => walk(f, c, deep))
      case symbol: Symbol if deep => walk(f, map(symbol.name), deep)
      case _ => // nothing to do  
    }
  }

}
