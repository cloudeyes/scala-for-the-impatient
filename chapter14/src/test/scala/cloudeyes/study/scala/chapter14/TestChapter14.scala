package cloudeyes.study.scala.chapter14

import org.scalatest.FunSuite
import org.scalatest.junit._
import org.scalatest.Assertions._
import org.junit.runner.RunWith
import org.junit.Assert._

/** 14장 패턴 매칭의 예제 테스트 */
@RunWith(classOf[JUnitRunner])
class TestChapter14 extends FunSuite {
  
  /** 14.2 가드 */
  def t02_가드 {

     // 모든 숫자를 매치하고 싶을 때
     def sign(ch:Char) = ch match{
       case '+' => 1
       case '-' => -1
       // 모든 숫자를 case '0' => case '1' => ... 할 필요 없이 가드로 해결한다.
       case _ if Character.isDigit(ch) => Character.digit(ch, 10)
       case _   => 0
     }

  }
  
  /** 14.4 타입 패턴 */
  def t04_타입_패턴 {
    
    def matchObj(obj:Object) = obj match{
      case m: Map[_, _]        => true // 제네릭 맵만 매치할 수 있다.
      // 매치는 런타임에 일어나고 제네릭 타입은 자바 가상 머신에서 지워진다. 
      // 따라서 특정 Map 타입을 매치할 수 없다.
      case m: Map[String, Int] => assert(false) // 매치 불가
      case a: Array[Int]       => true // 하지만 배열은 지워지지 않아 매치 가능하다.
    }
    
  }
  
  /** 14.5 배열, 리스트, 튜플을 매치하기 */
  def t05_배열_리스트_튜플_매치 {
    
    /** 배열 매치는 무결성(exhaustiveness) 체크를 하지는 않는다. */
    def matchArray(arr:Array[Int]) = arr match{
      case Array(0)     => "0" 
      case Array(x, y)  => x + " " + y 
      case Array(0, _*) => "0 ..."
      case _ => "something else"
    }
    
    /** 리스트 매치는 무결성 체크를 한다. 아래 예제는 Nil 케이스를 빠트렸다. */
    def matchList(list:List[Int]) = list match {
      case 0::Nil     => "0" 
      case x::y::Nil  => x + " " + y 
      case 0::tail    => "0 ..."
    }
    
    /** 튜플도 무결성 검사를 하지 않는다. */
    def matchTuple(tup:(Int, Int)) = tup match {
      case (0, _) => "0 ..."
      case (y, 0) => y + " 0"
    }
    
    intercept[MatchError] { matchTuple((1,1)) }
    
  }
  
  def t06_추출자 {
    // 정규 표현식도 추출자의 좋은 용법이다.
    val pattern = "([0-9]+) ([a-z]+)".r
    "99 bottles" match {
      case pattern(num, item) => assert(true)
      case _ => assert(false)
    }
  }
  
  def t07_변수_선언에서_패턴 {
    val (x, y) = (1, 2)
    val (q, r) = BigInt(10) /% 3
    val Array(first, second, _*) = Array(1,2,3,4,5)
  }
  
  def t08_for_표현식에서_패턴 {
    // for 컴프리헨션에서 매치 실패는 조용히 넘어간다.
    // 다음 루프는 빈 값을 가지는 모든 키를 출력한다.
    
    import collection.JavaConversions.propertiesAsScalaMap
    val keys1 = for ((k, "") <- System.getProperties) yield k
    val keys2 = for ((k, v)  <- System.getProperties if v == "") yield k
    assertEquals("sun.cpu.isalist", keys1.head)
    assertEquals(keys1, keys2)
  }
  
  /** case절에서 중위 표기법
   * 
   *  unapply 메소드가 쌍을 리턴할 때는 case 절에서 중위 표기법을 사용할 수 있다. 
   */
  def t11_case절_중위_표기법 {
    case object +/: {
      def unapply[T](input: List[T]) =
        if (input.isEmpty) None else Some((input.head, input.tail))
    }
    
    assertEquals(6, List(1,2,3,4,5) match {
      // 중위 표기법으로 패턴매칭 할 수 있다.
      case first +/: second +/: rest => first + second + rest.length
    })
  }
  
  def t12_중첩_구조_매치 {
    abstract class Item
    case class Article(desc:String, price:Double) extends Item
    case class Bundle(desc:String, discount:Double, items:Item*) extends Item
    
    val item = Bundle("Father's day special", 20, 
        Article("Scala for the Impatient" , 40),
        Bundle("Anchor Distillery Sampler", 10,
          Article("Old Poterero Straight Rye Whiskey", 80),
          Article("Junipero Gin", 33)),
        Article("Test", 1))
    
    // @ 표기법으로 중첩된 값을 변수에 바인드할 수 있다.
    item match {
      // rest가 하나인 경우
      case Bundle(_, _, art @ Article(_, _), rest)      => assert(false)
      // rest가 하나 이상인 경우
      case Bundle(_, _, art @ Article(_, _), rest @ _*) => assert(true)
    }
    
    def price(it:Item): Double = it match {
      case Article(_, p) => p
      case Bundle(_, disc, its @ _*) => its.map(price _).sum - disc
    }
    
    assert(124.0 ==  price(item))
    
  }
  
  /** 부분함수
   *  
   *  중괄호에 포함된 case 절들은 부분함수다. 이는 PartialFunction[A, B]의 인스턴스다.
   */
  def t14_14_부분함수 {
    
    val f:PartialFunction[Char, Int] = { case '+' => 1; case '-' => -1}
    assert(-1 == f('-'))
    assert(false == f.isDefinedAt('0'))
    intercept[MatchError] { f('0') }
    
    // 일부 메소드는 PartialFunction을 인자로 받는다.
    assert(Vector(-1, 1) == "-3+4".collect { case '+' => 1; case '-' => -1})
    
  }

  test("14.02 가드")     { t02_가드 }
  test("14.04 타입 패턴") { t04_타입_패턴 }
  test("14.05 배열, 리스트 튜플 매치") { t05_배열_리스트_튜플_매치 }
  test("14.06 추출자")   { t06_추출자 }
  test("14.08 for 표현식에서 패턴")   { t08_for_표현식에서_패턴 }
  test("14.11 case절에서 중위 표기법")   { t11_case절_중위_표기법 }
  test("14.12 중첩 구조 매치")   { t12_중첩_구조_매치 }
  test("14.14 부분함수")   { t14_14_부분함수 }
}
