package cloudeyes.study.scala.chapter13

import org.scalatest.FunSuite
import org.scalatest.junit._
import org.scalatest.Assertions._
import org.junit.runner.RunWith
import org.junit.Assert._
import scala.math.pow

@RunWith(classOf[JUnitRunner])
class TestClass extends FunSuite {

  /** 13.14 레이지 뷰
   *   
   *  레이지뷰는 스트림과 비슷하지만 어떤 값도 캐시하지 않든다. 
   *  매 인덱스에 접근할 때마다. 다시 계산된다.
   */
  def test13_14 {
    import math._

    val powers = (0 until 1000).view.map(pow(10, _))
    val p1 = powers(100)
    assert(1.0E100 == p1)
    
    val power2 = (0 to 1000).map(pow(10, _)).map(1 / _)
    
    // 레이지뷰는 큰 중간 콜렉션을 만드는 것을 피할 수 있기 때문에 큰 콜렉션이
    // 여러 방법으로 변환될 때 유용할 수 있다.
    
    val power3 = (0 to 1000).view.map(pow(10, _)).map(1 / _)
    assert(1.0 == power3(0))
    assert(0.1 == power3(1))
   
    // .force로 연산이 강제될 때, 중간 컬렉션 없이 바로 계산한다.
  }

  
  /** 자바 콜렉션과의 상호 호환
   *  
   */
  def test13_15 {
    import scala.collection.JavaConversions._
    /* 암시적 변환을 촉발하기 위해 타겟 값에 명시적인 타입을 준다. */
    val props: scala.collection.mutable.Map[String, String] = 
      System.getProperties()

    /* props는 실제 자바 객체가 아니라 Scala에서 쓰기 편한 래퍼로 변환된다
     * 값을 넣을때 put 메소드를 쓰지 않아도 된다.
     */
    props("hello") = "world"
    assertEquals("world", props("hello"))
    
  }

  test("13.14 레이지 뷰") { test13_14 }
  test("13.15 자바 콜렉션과의 상호 호환") { test13_15 }

}