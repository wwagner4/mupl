package mupl

object VariableValidator {

  def validate(variables: List[Variable]): Unit = {
    if (variables.isEmpty) throw new IllegalArgumentException("At least one variable must be defined")
    val map: Map[String, Chunk] = toMap(variables)
    val symbols = variables.map(_.name)
    val checkSym = checkSymDefined(symbols) _
    map.keys.foreach { k =>
      MuplUtil.walk(checkSym, map(k))
    }
    map.keys.foreach { k =>
      val checkSym = checkContainsSymbol(k) _
      MuplUtil.walk(checkSym, map(k), symbolMap = Some(map))
    }
  }

  private def toMap(variables: List[Variable]):Map[String, Chunk] = {
    val ret = variables.map(v => (v.name, v.chunk)).toMap
    if (ret.size != variables.size) {
      throw new IllegalArgumentException("Piece contains duplicate symbols")
    }
    ret
  }

  private def checkContainsSymbol(symbol: String)(chunk: Chunk): Unit = {
    chunk match {
      case sym: Symbol if symbol == sym.name =>
        val sn = sym.name
        throw new IllegalArgumentException(s"Loop for symbol $sn")
      case _ => // Nothing to do  
    }
  }

  private def checkSymDefined(symbols: List[String])(chunk: Chunk): Unit = {
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
