package mupl

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers

class VariableValidatorTest extends AnyFunSuite with Matchers {

  private val soundsDesc = {
    val sl = List(
      SoundDesc.of("m1", "test"),
      SoundDesc.of("X", "test"),
    )
    SoundsDescImpl(sl)
  }

  private val parser = MuplParser(soundsDesc)

  test("all valid") {
    val all = parser.parseVariables(
      """
        |a = X[(||) (||) (||) (||) ]
        |b = {[a a a] [a a a]}
        |c = [a b]
        |""".stripMargin)

    VariableValidator.validate(all)
  }

  test("unknown symbol") {
    val all = parser.parseVariables(
      """
        |a = X[(||) (||) (||) (||) ]
        |b = {[a a a] [a x a]}
        |c = [a b]
        |""".stripMargin)
    val thrown = intercept[IllegalArgumentException] {
      VariableValidator.validate(all)
    }
    val tm = thrown.getMessage
    val should = "ndfeined symbol"
    if (!tm.contains(should)) fail(s"Message '$tm' must contain '$should'")

  }

  test("duplicate symbol") {
    val all = parser.parseVariables(
      """
        |a = X[(||) (||) (||) (||) ]
        |b = {[a a a] [a x a]}
        |a = [a b]
        |""".stripMargin)
    val thrown = intercept[IllegalArgumentException] {
      VariableValidator.validate(all)
    }
    val tm = thrown.getMessage
    val should = "uplicate symbols"
    if (!tm.contains(should)) fail(s"Message '$tm' must contain '$should'")

  }

  test("loop for symbol") {
    val all = parser.parseVariables(
      """
        |a = X[(||) (||) (||) (||) ]
        |b = {[a a a] [a c a]}
        |c = [a b]
        |""".stripMargin)
    val thrown = intercept[IllegalArgumentException] {
      VariableValidator.validate(all)
    }
    val tm = thrown.getMessage
    val should = "Loop for symbol"
    if (!tm.contains(should)) fail(s"Message '$tm' must contain '$should'")

  }

}
