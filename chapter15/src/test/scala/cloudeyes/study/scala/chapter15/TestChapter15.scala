package cloudeyes.study.scala.chapter15

import org.scalatest.FunSuite
import org.scalatest.junit._
import org.scalatest.Assertions._
import org.junit.runner.RunWith
import org.junit.Assert._
import org.junit.Test
import scala.beans.BeanProperty
import javax.validation.constraints.NotNull
import javax.persistence.Id
import javax.persistence.Entity
import java.io.IOException
import javax.inject.Named

/** 15장 어노테이션의 예제 테스트 
 */
@RunWith(classOf[JUnitRunner])
class TestChapter15 extends FunSuite {

  def t02_어노테이트_가능한_항목들 {
    @Entity class Credentials
    @Test def testSomeFeature() { assert(false) }
    @BeanProperty var username = "john"
    def doSomething(@NotNull message: String) {}
    
    // 여러 어노테이션을 적용할 수 있다. 순서는 중요하지 않다.
    @BeanProperty @Id var greetings = "hello world!"
    
    // 표현식을 어노테이션 할 수 있다. 어노테이션 앞에 콜론을 붙인다.
    // 다음 예제는 None에 대한 케이스가 없어 무결성 경고가 발생해야 하지만
    // @unchecked로 표시 안되도록 했다.
    def f(x: Option[String]) = (x: @unchecked) match { case Some(y) => y }
    
    // 타입 인자를 어노테이트할 수 있다.
    //class MyContainer[@specialized T]
    
    // 실제 타입에 대한 어노테이션은 다음과 같이 타입 뒤에 온다.
    // scala 컴파일러 옵션에 `-P:continuations:enable`를 추가해야 한다.
    // continuation 플러그인은 지원이 중단되었다. 대신 async 라이브러리를 쓰기를 권장한다.
    //import scala.util.continuations._
    //val a : String @cps[Unit] = "string1"
  }
  
  def t03_어노테이션_인자 {
    @Entity class Credentials
    // 이름 있는 인자.
    @Test(timeout = 100, expected = classOf[IOException]) def test1 {} 

    // 인자 이름이 value이면 이름을 생략할 수 있다.
    @Named("creds") var credentials: Credentials = null 
  }
  
  def t04_어노테이션_구현 {
    // 어노테이션은 Annotation 트레이트를 확장해야 한다.
    // 어노테이션 클래스는 StaticAnnotation 이나 ClassfileAnotation 트레이트를
    // 선택적으로 확장할 수 있다.jko
    
    // 스칼라에서 필드 정의는 자바에서 여러 기능이 되는데 이 모든 것들이
    // 암시적으로 어노테이트 된다.
    // 이 엔티티 클래스에서 @NotNull이 어노테이트 되는 항목은 6개나 된다.
    // 생성자 인자, 비밀 인스턴스 필드, 접근 메소 username, 뮤테이터 메소드 username_=
    // 빈 접근자 getUsername, 빈 뮤테이터 setUsername
    
    @Entity class Credentials(@NotNull @BeanProperty var username: String)
    val cred = new Credentials(null)
    cred.username = null
  }
  
  def t06_최적화_어노테이션 {
    
    // 제너릭 함수는 타입에 대한 박싱/언박싱이 매번 일어나 비효율적이다.
    def allDifferentGeneric[T](x:T, y:T, z:T) = x != y && x != z && y != z
    
    // 제너릭 함수에서 기본 타입 값들을 싸고 푸는대신 기본 타입(Int, String 등)
    // 에 대해 미리 오버로드된 함수를 만들어 놓는다.
    def allDifferent[@specialized T](x:T, y:T, z:T) = x != y && x != z && y != z
    
    // 특수화를 타입의 부분집합으로 제한할 수도 있다.
    def allDifferentLimited[@specialized(Long, Double) T](x:T, y:T, z:T) = 
      x != y && x != z && y != z
  }
  
  def t07_오류와_경고_어노테이션 {
    @deprecated(message = "Use factorial(n: BigInt) instead")
    def factorial(n: Int): Int = ???
    
    // sz는 size로 변경된 후 ㅍ
    def draw(@deprecatedName('sz) size:Int, style:Int = 0) = 
      println(s"draw with style $style")
      
    // sz
    draw(sz = 12)

  }

  test("15.2 어노테이션 가능한 항목들") { t02_어노테이트_가능한_항목들 }
  test("15.4 어노테이션 구현") { t04_어노테이션_구현 }
}
