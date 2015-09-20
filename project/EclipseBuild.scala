import sbt._
import Keys._
import com.typesafe.sbteclipse.core._
import com.typesafe.sbteclipse.core.EclipsePlugin._
import scala.xml.transform.RewriteRule
import scala.xml.Node
import scala.xml.Elem
import java.io.File

object EclipseBuild extends Build {
  val sep = if (File.separator ==  "\\") "\\\\" else File.separator
  def fixPath(s:String) = s.replaceAll("/", sep)
  def unfixPath(s:String) = s.replaceAll(sep, "/")
  val arch   = System.getProperty("os.arch")
  val ostype =
      (if (System.getProperty("os.name").startsWith("Win")) "win"
      else if (System.getProperty("os.name").startsWith("Mac")) "osx"
      else "linux") + "-" + arch

  def addChild(n: Node, newChild: Node) = n match {
    case Elem(prefix, label, attribs, scope, child @ _*) =>
      Elem(prefix, label, attribs, scope, child ++ newChild : _*)
    case _ => error("Can only add children to elements!")
  }

  def removeChild(n: Node, filterFunc: Elem => Boolean) = n match {
    case Elem(prefix, label, attribs, scope, child @ _*) =>
      val newchild = child.filter{ _ match {
          case e:Elem => {
            if (filterFunc(e)) false else true
          }
          case _ => true 
        }
      }
      Elem(prefix, label, attribs, scope, newchild : _*)
    case _ => error("Can only filter children to elements!")
  }


  /** 이클립스 프로젝트 생성 시  클래스패스를 수정한다.
   *  antlr4plugin이 grammar 파일에서 자동 생성한 Java 파일들을
   *  이클립스 프로젝트의 소스 폴더로 수정한다.
   */
  val sampleClasspathTransformer1 = 
    Seq(new EclipseTransformerFactory[RewriteRule] {
      override def createTransformer(project: sbt.ProjectRef, state: sbt.State)
      : Validation[RewriteRule] = {
        scalaz.Success(new RewriteRule {
          override def transform(n: Node): Seq[Node] = n match {
            case e:Elem if(e.label == "classpath") => {
              val n1 = removeChild(n, e => (e \ "@path" text) endsWith "parser")
              addChild(n1, <classpathentry output="target" kind="src" path="antlr4"/>)
            } 
            case _ => n
          }
        })
      }
    })

  /** 이클립스 프로젝트를 생성할때 클래스패스를 수정한다.
    *
    * JNI용 공유 라이브러리(DLL,dylib 등)를 참조하도록 수정한다.
    */
  def sampleClasspathTransformer2(baseDir:String) = 
    Seq(new EclipseTransformerFactory[RewriteRule] {
      override def createTransformer(project: sbt.ProjectRef, state: sbt.State)
      : Validation[RewriteRule] = {
        scalaz.Success(new RewriteRule {
          override def transform(n: Node): Seq[Node] = n match {
            case e:Elem if(e.label == "classpathentry") => {
              if ((n \ "@path" text) endsWith "scalaz3_2.11-2.1.jar") {
                addChild(n, <attributes>
      <attribute name="org.eclipse.jdt.launching.CLASSPATH_ATTR_LIBRARY_PATH_ENTRY"       
      value={baseDir + fixPath("/lib/") + ostype}/>
            </attributes>)
              } else n
            }
            case _ => n
          }
        })
      }
    })

  /** 이클립스 프로젝트 설정을 수정한다.
   *  antlr4plugin 자동 생성한 소스 폴더(target/generated-sources/antlr/parser)를
   *  이클립스 프로젝트의 소스 폴더로 링크한다.
   */
  def sampleProjectTransformer1(baseDir:String) = 
    Seq(new EclipseTransformerFactory[RewriteRule] {
      override def createTransformer(project: sbt.ProjectRef, state: sbt.State)
      : Validation[RewriteRule] = {
        scalaz.Success(new RewriteRule {
          override def transform(n: Node): Seq[Node] = n match {
            case e:Elem if(e.label == "projectDescription") =>
              addChild(n, 
                <linkedResources>
                  <link>
                    <name>antlr4</name>
                    <type>2</type>
                    <location>{unfixPath(baseDir + fixPath("/target/generated-sources/antlr4"))}</location>
                  </link>
                </linkedResources>)
            case e:Elem if(e.label == "buildSpec") => {
              val n1 = removeChild(n, _ => true)
              val n2 = addChild(n1, 
                <buildCommand>
                  <name>org.eclipse.xtext.ui.shared.xtextBuilder</name>
                  <arguments></arguments>
                </buildCommand>)
              val n3 = addChild(n2, 
                <buildCommand>
                  <name>org.eclipse.jdt.core.javabuilder</name>
                  <arguments></arguments>
                </buildCommand>)
              n3
            }
            case e:Elem if(e.label == "natures") =>
              addChild(n, 
                <nature>org.eclipse.xtext.ui.shared.xtextNature</nature>)
            case _ => n
          }
        })
      }
    })

  /** 이클립스 프로젝트 설정을 수정한다.
   *  리소스 필터를 추가한다
   */
  val sampleProjectTransformer2 =
    Seq(new EclipseTransformerFactory[RewriteRule] {
      override def createTransformer(project: sbt.ProjectRef, state: sbt.State)
      : Validation[RewriteRule] = {
        scalaz.Success(new RewriteRule {
          override def transform(n: Node): Seq[Node] = n match {
            case e:Elem if(e.label == "projectDescription") =>
              addChild(n, 
                <filteredResources>
                    <filter>
                        <id>1441261277082</id>
                        <name>src/main/resources</name>
                        <type>10</type>
                        <matcher>
                            <id>org.eclipse.ui.ide.multiFilter</id>
                            <arguments>1.0-name-matches-false-false-web</arguments>
                        </matcher>
                    </filter>
                </filteredResources>
            )
            case _ => n
          }
        })
      }
    })

}

