package cloudeyes.study.scala.chapter16

import org.scalatest.FunSuite
import org.scalatest.junit._
import org.scalatest.Assertions._
import org.junit.runner.RunWith
import org.junit.Assert._
import scala.xml.NodeBuffer
import scala.xml.Atom
import scala.xml.Text
import scala.xml.PCData
import scala.xml.Group
import scala.xml.Attribute
import scala.xml.Null
import scala.xml.transform.RewriteRule
import scala.xml.transform.RuleTransformer


@RunWith(classOf[JUnitRunner])
class TestChapter16 extends FunSuite {
  import scala.xml.NodeSeq
  import scala.xml.Node
  import scala.xml.Elem

  /** 16.1~3 XML 리터럴, 노드, 어트리뷰트
   *  
   *  스칼라는 XML을 자체 지원한다.
   *  
   *  [[Node]] 클래스는 모든 XML노드 타입의 조상이다.
   *  [[Elem]] 클래스는 XML 엘리먼트를 나타낸다.
   *  다음은 xml 관련 클래스의 완전한 계층도를 나타낸다.
   *  <pre>
   *                      Seq[Node]
   *                          |
   *                      NodeSeq
   *               ___________|____________
   *               |                      |
   *            Document                  | 
   *                                     Node
   *     Iterable[Metadata]        _______|_______
   *             |                 |             |
   *          Metatdata-------<>-Elem       SpecialNode
   *                              _______________|______________
   *                              |       |          |         |           
   *                            Atom  EntityRef  ProcInstr  Comment 
   *                       _______|______
   *                       |      |     |
   *                     Text  PCData  Unparsed                              
   *  </pre>
   */
  def t01_XML리터럴_노드_어트리뷰트 {
    // doc의 타입은 Elem이다.
    val doc = <html><head><title>Hello World</title></head><body>...</body></html>
    // 같은 노드들이 반복될 경우 [[NodeBuffer]] 값이 된다.
    val nodes = <li>Sarang</li><li>John</li>
    val elem : Elem = <a href="http://scala-lang.org">The <em>Scala</em> language</a>

    assertEquals("a", elem.label)
    assertEquals("The ", elem.child(0).text)
    // 어트리뷰트로 얻는 값은 Option[String]이 아닌 Seq[Node]이다.
    val url:Seq[Node] = elem.attributes("href")
    assertEquals(1, url.size)
    assertEquals("http://scala-lang.org", url.mkString)
    
    // 노드 시퀀스는 Seq[Node]에 XPath와 유사한 연산자를 추가한 NodeSeq타입이다.
    val texts = (for (n <- elem.child) yield n.text).mkString
    assertEquals("The Scala language", texts)
    
    // 노드 시퀀스를 프로그래밍적으로 만드려면 ArrayBuffer[Node]의 서브클래스
    // NodeBuffer를 사용한다.
    
    val items = new NodeBuffer
    items += <li>Saran</li>
		items += <li>John</li>	
		  
		// NodeSeq로 암묵적으로 바꿀수 있지만, 변경 불가능한 시퀀스가 된다.
		val itemsImmutable:NodeSeq = items
		
		// 어트리뷰느는 엔티티 레퍼런스를 포함할 수 있기 때문에 문자열이 아닌 노드
		// 시퀀스를 준다.
		// 아래 예제에서 alt 텍스트가 3개의 노드 시퀀스로 나눠지는 이유는
		// &eacute; 엔티티를 XMl에서 일반적으로 문자열이라고 판단할 수 없기 때문.
		val image = <img alt="San Jos&eacute; State University Logo" src="..."/>
		val alt = image.attributes("alt")
		assertEquals(3, alt.size)
		assertEquals(Seq("San Jos", "&eacute;", " State University Logo"), 
		    alt.map(_.toString))
		// 하지만 XHTML의 경우는 문자열을 참조하므로 그냥 text로 변환해도 된다.
		assertEquals("San Jos&eacute; State University Logo", alt.text)
		
		// apply 메소드 호출 시 어트리뷰트가 없으면 null이 리턴된다.
		assertNull(image.attributes("null"))

		// apply 메소드 말고 get을 쓰면 Option 타입을 얻을 수 있다.
		val altOpt = image.attributes.get("alt")
		
		// 어트리뷰트 순회 방법
		val attrMap = (for (attr <- image.attributes) 
		  yield (attr.key, attr.value.text)).toMap
		  
		assertEquals(attrMap, image.attributes.asAttrMap)
   
  }
  
  def t04_내재_표현식 {
    val items = Seq("John", "Sarang")
    // 표현식의 계산 결과가 노드이면 XML에 단순히 추가된지만, 다른 타입이라면
    // T타입의 컨테이너인 Atom[T]로 변환되어 저장된다.
    val elem  = <ul><li>{items(0)}</li><li>{Text(items(1))}</li></ul>
    elem.child(0).child(0) match {
      case s:Text    => assert(false)
      case s:Atom[_] => assert(true)
    }
    // Text로 패턴매칭을 하려면. Text로 저장하는 것이 좋다.
    elem.child(1).child(0) match {
      case s:Text    => assert(true)
      case s:Atom[_] => assert(false)
    }
    
    // 내재 표현식에 XML 리터럴을 포함할 수 있다.
    val elem2 = <ul>{for (i <- items) yield <li>{i}</li>}</ul>
    assertEquals(elem2, elem)
    
  }
  
  def t06_특별한_노드_타입 {
    val js = <script><![CDATA[if (temp < 0) alert("Cold")]]></script>
    // xml 문자열을 파싱하면 파서는 텍스트가 CDATA로 마크없 된 사실을
    // 유지하지 않는다. 출력에서 CDATA를 원하면 PCData로 래핑해야 한다.
    val code = """if (temp < 0) alert("Cold")"""
    val js2 = <script>{PCData(code)}</script>
    assertEquals(js2, js)
    
    val items = Seq("John", "Sarang")
    val elem = for (i <- items) yield <li>{i}</li>
    // <xml:group> 리터럴은 노드 시퀀스를 하나의 그룹 노드로 모은다.
    val g1 = <xml:group>{elem}</xml:group>
    val g2 = Group(elem)
    assertEquals(g2, g1)
    
    val r1 = for (n <- elem) yield n
    // 그룹 노드를 순회하여 yield하면 NodeSeq를 만든다.
    val r2 = for (n <- <xml:group>elem</xml:group>) yield n
    
  }
  
  def t09_엘리멘트_어트리뷰트_수정 {
    val items = Seq("John", "Sarang")
    val list = <ul><li>{items(0)}</li><li>{Text(items(1))}</li></ul>
    // 레이블을 ul에서 ol로 바꾸기
    val list2 = list.copy(label = "ol")
    // 자식을 추가
    list.copy(child = list.child ++ <li>Another item</li>)
    // 어트리뷰트 수정
    val image = <img src="hamster.jpg"/>
    val image2 = image % Attribute(/*네임스페이스*/null, "alt", "An image of a hamster", 
        /*다음 메타데이터*/ Null)
    // 두 개 이상의 어트리뷰트를 더하기
    val image3 = image % Attribute(null, "alt", "An image of a frog",
        Attribute(null, "src", "frog.jpg", Null))
    assertEquals(<img src="frog.jpg" alt="An image of a frog"/>, image3)
    
  }
  
  /** 하나 이상의 RewriteRule 인스턴스를 노드와 그 자손들에 적용하는 RuleTransformer 클래스를 사용한다.
   * - 특정 조건을 만족하는 모든 자식을 다시 쓰는 방법.
   */
  def t10_XML변환 {
    val rule1 = new RewriteRule {
      override def transform(n: Node) = n match {
        case e @ <ul>{_*}</ul> => e.asInstanceOf[Elem].copy(label = "ol")
        case _ => n
      }
    }
    
    val root = <test><ul><li>hello</li><li>world</li></ul>instructions:<div><ul><li>first</li><li>second</li></ul></div></test>
    val transformed = new RuleTransformer(rule1).transform(root)
    assertEquals(<test><ol><li>hello</li><li>world</li></ol>instructions:<div><ol><li>first</li><li>second</li></ol></div></test> , 
        transformed)
  }
   
  test("16.01~3 XML 리터럴, 노드, 어트리뷰트") { t01_XML리터럴_노드_어트리뷰트 }
  test("16.04 내재 표현식") { t04_내재_표현식 }
  test("16.06 특별한 노드 타입") { t06_특별한_노드_타입 }
  test("16.09 엘리먼트/어트리뷰트 수정") { t09_엘리멘트_어트리뷰트_수정 }
  test("16.10 XML 변환") { t10_XML변환 }
}
