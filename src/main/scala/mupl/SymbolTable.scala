package mupl

case class SymbolTable(variables: List[Variable]) {

  val map: Map[String, Chunk] = variables.map(v => (v.name, v.chunk)).toMap
  validate()

  def chunk(nam: String): Chunk = map(nam)

  private def validate(): Unit = {
    val symbols = variables.map(_.name)
    val topSeq = Sequence(variables.map(_.chunk))
    val check = checkSymDefined(symbols) _
    MuplUtil.walk(check, topSeq)
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
