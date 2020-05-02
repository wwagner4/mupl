package mupl

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers

class SoundDescTest  extends AnyFunSuite with Matchers {

  test("all ok") {
    val sds = List(
      SoundDesc.of("kasjdhkasjd", "Some Desc")
    )
    val ssds = SoundsDescImpl(sds)

    ssds.isValidId("asdas") mustBe false
    ssds.isValidId("kasjdhkasjd") mustBe true
  }

  test("invalid id") {
    val thrown = intercept[IllegalArgumentException] {
      SoundDesc.of("kasj dhkasjd", "Some Desc")
    }
    thrown.getMessage.contains("must fulfill") mustBe true
  }

  test("invalid desc") {
    val thrown = intercept[IllegalArgumentException] {
      SoundDesc.of("kasjdhkasjd", "")
    }
    thrown.getMessage.contains("must not be empty") mustBe true
  }

}
