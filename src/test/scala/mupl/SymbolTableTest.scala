package mupl

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers

class SymbolTableTest extends AnyFunSuite with Matchers {

  test("all valid") {
    val all = MuplParser.parseAll(
      """
        |a = X[(||) (||) (||) (||) ]
        |b = {[a a a] [a a a]}
        |c = [a b]
        |""".stripMargin)

    val st = SymbolTable(all)
    val a = st.chunk("a")
    val b = st.chunk("b")
    val c = st.chunk("c")
    a.isInstanceOf[Melo] mustBe true
    b.isInstanceOf[Parallel] mustBe true
    c.isInstanceOf[Sequence] mustBe true
  }

  test("unknown symbol") {
    val all = MuplParser.parseAll(
      """
        |a = X[(||) (||) (||) (||) ]
        |b = {[a a a] [a x a]}
        |c = [a b]
        |""".stripMargin)
    val thrown = intercept[IllegalArgumentException] {
      val st = SymbolTable(all)
    }
    val tm = thrown.getMessage
    val should = "ndfeined symbol"
    if (!tm.contains(should)) fail(s"Message '$tm' must contain '$should'")

  }

  test("loop for symbol") {
    val all = MuplParser.parseAll(
      """
        |a = X[(||) (||) (||) (||) ]
        |b = {[a a a] [a c a]}
        |c = [a b]
        |""".stripMargin)
    val thrown = intercept[IllegalArgumentException] {
      val st = SymbolTable(all)
    }
    val tm = thrown.getMessage
    val should = "Loop for symbol"
    if (!tm.contains(should)) fail(s"Message '$tm' must contain '$should'")

  }

}
