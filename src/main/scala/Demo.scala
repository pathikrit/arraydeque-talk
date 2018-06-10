import reftree.core._
import reftree.render._
import reftree.diagram._
import reftree.util.Reflection._

import scala.concurrent.duration._
import scala.collection.mutable

object Demo extends App {
  implicit def `ArrayDeque RefTree`: ToRefTree[mutable.ArrayDeque[Char]] = ToRefTree {ds =>
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
  var chars = Iterator.from('a').map(_.toChar)

  def append() = {
    val c = chars.next()
    Diagram(queue += c).withCaption(s"queue += '$c'")
  }

  def removeHead() = {
    val str = queue.removeHeadOption().map(c => s"'$c'")
    Diagram(queue).withCaption(s"queue.removeHeadOption()) //$str")
  }

  def removeLast() = {
    val str = queue.removeLastOption().map(c => s"'$c'")
    Diagram(queue).withCaption(s"queue.removeLastOption()) //$str")
  }

  val diagrams = Seq(
    Diagram(queue).withCaption("val queue = mutable.ArrayDeque.empty[Char]"),
    append(),
    append(),
    append(),
    append(),
    append(),
    removeHead(),
    removeHead(),
    removeHead(),
    append(),
    append(),
    removeLast(),
    removeLast(),
    removeLast(),
  )

  // Render
  val renderer = Renderer(
    animationOptions = AnimationOptions(interpolationDuration = 0.second, keyFrameDuration = 1.second)
  )
  import renderer._
  Animation(diagrams).render("ArrayDeque")
}
