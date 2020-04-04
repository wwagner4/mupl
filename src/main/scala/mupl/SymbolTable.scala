package mupl

case class SymbolTable(variables: List[Variable]) {

  val map: Map[String, Chunk] = toMap
  validate()

  def chunk(nam: String): Chunk = map(nam)
  
  def toMap:Map[String, Chunk] = {
    val ret = variables.map(v => (v.name, v.chunk)).toMap
    if (ret.size != variables.size) {
      throw new IllegalArgumentException("Piece contains duplicate symbols")
    }
    ret
  }

  private def validate(): Unit = {
    val symbols = variables.map(_.name)
    val check = checkSymDefined(symbols) _
    map.keys.foreach { k =>
      MuplUtil.walk(check, chunk(k))
    }
    map.keys.foreach { k =>
      val checkSym = checkContainsSymbol(k) _
      MuplUtil.walk(checkSym, chunk(k), symbolMap = Some(map))
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

}
