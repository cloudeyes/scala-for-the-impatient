package cloudeyes.study.scala.chapter17

import org.scalatest.FunSuite
import org.scalatest.junit._
import org.scalatest.Assertions._
import org.junit.runner.RunWith
import org.junit.Assert._
import scala.reflect.ClassTag

@RunWith(classOf[JUnitRunner])
class TestChapter17 extends FunSuite {
  
  def t03_타입_바운드 {
    /** 상위 바운드 예제 */
    class Pair[T <: Comparable[T]](val first: T, val second: T) {
      /** first compareTo 메소드를 가지도록 제한하기 위해 상위 바운드 제한이 필요하다. */
      def smaller = if (first.compareTo(second) < 0) first else second
    }

    /** 하위 바운드 예제 */
    class Pair2[T](val first: T, val second: T) {
      /** 첫 번째 요소를 다른 값으로 치환하고 싶을 때. */
      def replaceFirst(newFirst: T) = new Pair2[T](newFirst, second)
      /** 첫 번째 요소가 두번째 요소와 꼭 같은 타입일 필요는 없다. T의 슈퍼타입으로 제한한다. */
      def replaceFirst2[R >: T](newFirst: R) = new Pair2[R](newFirst, second)
    }

    class Person(val name: String)
    class Student(val studentId: Int, override val name:String) 
      extends Person(name: String)
    
    val p1 : Pair2[Student] = new Pair2(new Student(1, "Sammy"), new Student(2, "John"))
    val p2 : Pair2[Person]  = p1.replaceFirst2(new Person("Sarang"))
    
    assertEquals("John", new Pair("John", "Sarang").smaller)
    /** 뷰 바운드 예제
     *  
     *  T 는 암시적 변환에 의해 Comparable을 구현하는 객체로 변환되기만 해도 된다. 
     * `<%`는 뷰 바운드로서 암시 변환에 의해 바운드 됨을 의미한다.  
     */
    class Pair3[T <% Comparable[T]](val first: T, val second: T) {
      // Int 는 Comparable을 구현하지 않는데, 상식적으로 Int 비교가 되지 않는것은 말이 안된다.
      // val p2 = new Pair(4, 2)  // 오류 발생
      def smaller = if (first.compareTo(second) < 0) first else second
    }
    
    val p3 = new Pair3(4, 2) // 암시 변환에 의 Int -> RichInt로 변환된다.
    
    /** 콘텍스트 바운드 예제 
     * 
     *  콘텍스트 바운드는 `T : M`의 형태를 가지는데, `M`은 다른 제네릭 타입이다. 
     *  이는 `T[M]`의 *암묵 값*이 있음을 요구한다.  
     */
    class Pair4[T : Ordering](val first: T, val second: T) {
      /** 암묵값을 사용하는 메소드를 선언할 때, *암묵 인자*를 추가해야 한다. */
      def smaller(implicit ord: Ordering[T]) = 
        if (ord.compare(first, second) < 0) first else second
    }
    
    val p4 = new Pair4(4, 2)
    
    assertEquals(p3.smaller, p4.smaller)
    
    /** 매니페스트 콘텍스트 바운드
      *
      * 스칼라와는 달리 배열은 자바에서 별도 취급한다. Array[T]에서 T가 Int라면
      * 가상머신에서 int[]를 원할 것이다.
      * 가상머신에서는 제너릭 타입이 제거되기 때문에, 제너릭 타입 정보를 보존하기 위한 트릭이다.
      */
    def makePair[T : ClassTag](first: T, second: T) = {
      val r = new Array[T](2); r(0) = first; r(1) = second; r
    }
    
    // 컴파일러는 암묵 ClassTag[Int]를 찾아서 실제로는 makePair(4, 9)(intClassTag)를
    // 를 호출한다. 
    val p5 = makePair(4, 2)
  }
  
  def t07_다중_바운드 {
    class Person(val name: String)
    class Student(val studentId: Int, name: String) extends Person(name) 
    class Graduate(val graduateYear: Int, studentId: Int, name:String) 
      extends Student(studentId, name)
 
    // 타입 변수는 상위와 하위 바운드 모두 가질 수 있다.
    class CampusCouple[T >: Graduate <: Student](s1: T, s2: T)
    // cc1은 생성 불가 Person이 Upper 바운드인 Student 보다 수퍼클래스이기 때문.
    // val cc1 = new CampusCouple(new Person("Bob"), new Student(1, "John")) // 
    val cc2 = new CampusCouple(new Student(2, "Kim"), new Graduate(2014, 1, "John"))
    
    // 여러 상위 / 하위 바운드를 가질 수는 없지만, 타입이 여러 트레이트를 구현하게끔 요구할 수 있다.
    class Pair[T <: Comparable[T] with Serializable with Cloneable]
      (val first:T, val second:T)
      
    // 하나 이상의 뷰 바운드를 가질 수도 있다.
    class StringPair[T <% Comparable[T] <% String](val first:T, val second:T)
    val p1 = new StringPair("hello", "world")

    import scala.language.implicitConversions
    implicit def intToString(n:Int) = n.toString
    // (123, 456) 의 각 숫자를 문자열로 변환하는 암시적 변환이 정의되어 있어야 한다.
    val p2 = new StringPair(123, 456)
    
    // 하나 이상의 콘텍스트 바운드를 가질 수도 있다.
    def makePair[T : Ordering : Manifest](first: T, second: T) = {
      val r = new Array[T](2); r(0) = first; r(1) = second; r
    } 

    val p3 = makePair(1, 2)
    // 아래의 경우 Person이 Ordering타입에 속하지 않기 때문에 실패한다.
    // val p4 = makePair(new Person("Kim"), new Person("John"))
  }
  
  /** 타입 제한자는 타입을 제한하는 다른 방법을 제공한다.
   *  - `T =:= U` : T 와 U 가 같은 타입
   *  - `T <:< U` : T 가 U 의 서브타입
   *  - `T <%< U` : U는 T로 뷰 변환 가능
   */
  def t08_타입_제한자 {
    
    /** T 가 Comparable[T]의 서브타입이라는 암묵 증거(implicit evidence) 인자를 준다. 
     *  이 경우에는 별 효용이 없다.
     */
    class Pair[T](val first: T, val second: T)(implicit ev: T <:< Comparable[T])
    
    //val p1 = new Pair(1, 2) // Cannot prove that Int <:< Comparable[Int]
    val p1 = new Pair("123", "456")
    
    /** 타입 제한자는 제네릭 클래스가 특정 상황에서만 사용 가능한 메소드를 제공할 수 있다. */
    class Pair1[T](val first: T, val second: T) {
      
      /** smaller 메소드는 T 가 Ordered[T]의 서브타입일 경우만 사용 가능하다. */
      def smaller(implicit ev: T <:< Ordered[T]) = 
        if (first.compareTo(second) < 0) first else second
    }
    
    val p2 = new Pair1(123, 456)
    val v1 = p2.smaller
  }
  
  test("t03_타입_변수_바운드") { t03_타입_바운드 }
  test("t07_다중_바운드")     { t07_다중_바운드 }

}