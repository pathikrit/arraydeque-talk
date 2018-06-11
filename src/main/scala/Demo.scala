import reftree.core._
import reftree.render._
import reftree.diagram._
import reftree.util.Reflection._

import scala.concurrent.duration._
import scala.collection.mutable

object Demo extends App {
  implicit def `ArrayDeque RefTree`: ToRefTree[mutable.ArrayDeque[Char]] = ToRefTree {ds =>
    val array = ds.privateField[Array[AnyRef]]("array").map(c => Option(c).getOrElse('␀').asInstanceOf[Char])
    val start = ds.privateField[Int]("start")
    val end = ds.privateField[Int]("end")

    val arrayRef = {
      val arrayFields = array.zipWithIndex map { case (a, i) =>
        val name = i match {
          case `start` if start == end => s"start=end=$i"
          case `start` => s"start=$i"
          case `end` => s"end=$i"
          case _ => i.toString
        }
        a.refTree.withHighlight(i == start || i == end).toField.withName(name)
      }
      RefTree.Ref(array, arrayFields).rename(s"char[${array.length}]")
    }

    RefTree.Ref(ds, Seq(
      start.refTree.withHighlight(true).toField.withName("start"),
      end.refTree.withHighlight(true).toField.withName("end"),
      arrayRef.toField.withName("array")
    ) ++ ds.toArray.zipWithIndex.map({case (a, i) => a.refTree.toField.withName(i.toString)}))
  }

  val queue = mutable.ArrayDeque.empty[Char]
  var chars = Iterator.from('a').map(_.toChar)

  def append(n: Int) = Seq.fill(n) {
    val c = chars.next()
    Diagram(queue += c).withCaption(s"queue += '$c'")
  }

  def prepend(n: Int) = Seq.fill(n) {
    val c = chars.next()
    queue.prepend(c)
    Diagram(queue).withCaption(s"queue.prepend('$c')")
  }

  def removeHead(n: Int) = Seq.fill(n) {
    val str = queue.removeHeadOption().map(c => s"'$c'")
    Diagram(queue).withCaption(s"queue.removeHeadOption()) //$str")
  }

  def removeLast(n: Int) = Seq.fill(n) {
    val str = queue.removeLastOption().map(c => s"'$c'")
    Diagram(queue).withCaption(s"queue.removeLastOption()) //$str")
  }

  def clear(shrink: Boolean) = {
    if (shrink) queue.clearAndShrink() else queue.clear()
    Diagram(queue).withCaption(if (shrink) "queue.clearAndShrink()" else "queue.clear()")
  }

  val diagrams = Seq(
    Seq(Diagram(queue).withCaption("val queue = mutable.ArrayDeque.empty[Char]")),
    append(5),
    removeHead(3),
    append(2),
    removeLast(4),
    append(10),
    removeHead(3),
    prepend(5),
    Seq(clear(shrink = false), clear(shrink = true))
  ).flatten

  // Render
  val renderer = Renderer(
    animationOptions = AnimationOptions(interpolationDuration = 0.second, keyFrameDuration = 1.second)
  )
  import renderer._
  Animation(diagrams).render("ArrayDeque")
}
