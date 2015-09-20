package cloudeyes.study.scala.chapter14

import org.scalatest.FunSuite
import org.scalatest.junit._
import org.scalatest.Assertions._
import org.junit.runner.RunWith
import org.junit.Assert._

@RunWith(classOf[JUnitRunner])
class TestClass extends FunSuite {

  test ("a simple test") {
    assertEquals("should", "fail")
  }
}
