package cloudeyes.study.scala.chapter13

import org.scalatest.FunSuite
import org.scalatest.junit._
import org.scalatest.Assertions._
import org.junit.runner.RunWith
import org.junit.Assert._

/** 13장 콜렉션의 예제 테스트 */
@RunWith(classOf[JUnitRunner])
class TestChapter13 extends FunSuite {
  import scala.math.pow

  /** 13.14 레이지 뷰
   *   
   *  레이지뷰는 스트림과 비슷하지만 어떤 값도 캐시하지 않든다. 
   *  매 인덱스에 접근할 때마다. 다시 계산된다.
   */
  def t14_레이지뷰 {
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
  def t15_자바_콜렉션_호환 {
    import collection.JavaConversions._
    /* 암시적 변환을 촉발하기 위해 타겟 값에 명시적인 타입을 준다. */
    val props: scala.collection.mutable.Map[String, String] = 
      System.getProperties()

    /* props는 실제 자바 객체가 아니라 Scala에서 쓰기 편한 래퍼로 변환된다
     * 값을 넣을때 put 메소드를 쓰지 않아도 된다.
     */
    props("hello") = "world"
    assertEquals("world", props("hello"))
    
  }
  
  /** 13.16 쓰레드 안전 콜렉션 / 13.17 병렬 콜렉션
   *  
   */
  def t16_17_쓰레드_안전_및_병렬_콜렉션 {
    import collection.mutable.ArrayBuffer
    import collection.JavaConversions.asScalaIterator
    import java.util.concurrent.ConcurrentLinkedQueue 
    val result = new ConcurrentLinkedQueue[Int]()
    // 병력 컬렉션 연산은 순서가 보장되지 않는다.
    val coll = (0 until 100)
    for (i <- coll.par) { result.add(i) }
    assertEquals(coll.size, result.size)
    assertNotEquals(coll, result) // 결과는 순서가 다르다.
    // 순서대로 정렬해보면 원하는 결과다.
    assertEquals(coll, result.iterator().toSeq.sorted)

    // 하지만 yield를 쓰면 결과를 순서대로 결합하여 리턴한다.
    assertEquals(coll.toSeq, for (i <- coll.par) yield i)
  }

  test("13.14 레이지 뷰")             { t14_레이지뷰 }
  test("13.15 자바 콜렉션과의 상호 호환") { t15_자바_콜렉션_호환 }
  test("13.16 쓰레드 안전 콜렉션 / 13.17 병렬 콜렉션") { t16_17_쓰레드_안전_및_병렬_콜렉션 }

}