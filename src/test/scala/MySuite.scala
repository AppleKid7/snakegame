// For more information on writing tests, see
// https://scalameta.org/munit/docs/getting-started.html

import zio._
import zio.test._


object MySuite extends ZIOSpecDefault {
  val spec = test("example test that succeeds") {
    val obtained = 42
    val expected = 42
    assert(obtained)(Assertion.equalTo(expected))
  }
}
