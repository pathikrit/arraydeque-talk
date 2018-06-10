import reftree.core._
import reftree.render._
import reftree.diagram._
import reftree.util.Reflection._

import scala.concurrent.duration._
import scala.collection.mutable

object Demo extends App {
  implicit def arrayDequeRefTree: ToRefTree[mutable.ArrayDeque[Char]] = ToRefTree {ds =>
    val array = ds.privateField[Array[AnyRef]]("array")
    RefTree.Ref(ds, Seq(
      ds.privateField[Int]("start").refTree.withHighlight(true).toField.withName("start"),
      ds.privateField[Int]("end").refTree.withHighlight(true).toField.withName("end"),
      array.map(c => Option(c).getOrElse('␀').asInstanceOf[Char]).refTree.toField.withName("array"),
      RefTree.Val(ds.size).toField.withName("size"),
      RefTree.Val(array.length).toField.withName("array.length")
    ))
  }

  val queue = mutable.ArrayDeque.empty[Char]

  def append(c: Char) = Diagram(queue += c).withCaption(s"queue += '$c'")

  def removeHead() = {
    val str = queue.removeHeadOption().map(c => s"'$c'")
    Diagram(queue).withCaption(s"queue.removeHeadOption()) //$str")
  }

  val diagrams = Seq(
    Diagram(queue).withCaption("val queue = mutable.ArrayDeque.empty[Char]"),
    append('a'),
    append('b'),
    append('c'),
    append('d'),
    append('e'),
    removeHead(),
    removeHead(),
    removeHead()
  )

  // Render
  val renderer = Renderer(
    animationOptions = AnimationOptions(interpolationDuration = 0.second, keyFrameDuration = 1.second)
  )
  import renderer._
  Animation(diagrams).render("ArrayDeque")
}
