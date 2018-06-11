import reftree.core._
import reftree.render._
import reftree.diagram._
import reftree.util.Reflection._

import scala.concurrent.duration._
import scala.collection.mutable
import scala.reflect.ClassTag

object Demo extends App {
  implicit def `ArrayDeque RefTree`[A <: AnyVal : ClassTag]: ToRefTree[mutable.ArrayDeque[A]] = ToRefTree {ds =>
    val array = ds.privateField[Array[AnyRef]]("array")
    val start = ds.privateField[Int]("start")
    val end = ds.privateField[Int]("end")

    val arrayRef = {
      val arrayFields = array.zipWithIndex map { case (a, i) =>
        val fieldName = i match {
          case `start` if start == end => s"↳$i↲"
          case `start` => s"↳$i"
          case `end` => s"$i↲"
          case _ => i.toString
        }
        val refTree = Option(a) match {
          case Some(c) => RefTree.Val(c.asInstanceOf[A]).withHighlight(true)
          case None => RefTree.Null().withHighlight(i == end)
        }
        refTree.toField.withName(fieldName)
      }
      val name = s"${implicitly[ClassTag[A]].runtimeClass.getName}[${array.length}]"
      RefTree.Ref(array, arrayFields).rename(name)
    }

    RefTree.Ref(ds, Seq(
      start.refTree.withHighlight(true).toField.withName("start"),
      end.refTree.withHighlight(true).toField.withName("end"),
      arrayRef.toField.withName("array")
    ) ++ ds.toArray.zipWithIndex.map({case (a, i) => a.refTree.toField.withName(i.toString)}))
  }

  val queue = mutable.ArrayDeque.empty[Char]
  var chars = Iterator.continually('A' to 'Z').flatten //Cycle A -> Z

  def append(n: Int) = Seq.fill(n) {
    val c = chars.next()
    Diagram(queue += c).withCaption(s"queue.append('$c')")
  }

  def prepend(n: Int) = Seq.fill(n) {
    val c = chars.next()
    Diagram(c +=: queue).withCaption(s"queue.prepend('$c')")
  }

  def removeHead(n: Int) = Seq.fill(n) {
    val str = queue.removeHeadOption()
    Diagram(queue).withCaption(s"queue.removeHeadOption()) //${str.map(c => s"'$c'")}")
  }

  def removeLast(n: Int) = Seq.fill(n) {
    val str = queue.removeLastOption()
    Diagram(queue).withCaption(s"queue.removeLastOption()) //${str.map(c => s"'$c'")}")
  }

  def clear(shrink: Boolean) = {
    if (shrink) queue.clearAndShrink() else queue.clear()
    Diagram(queue).withCaption(if (shrink) "queue.clearAndShrink()" else "queue.clear()")
  }

  val diagrams = Seq(
    Seq(Diagram(queue).withCaption("val queue = mutable.ArrayDeque.empty[Char]")),
    append(5),
    removeHead(3),
    prepend(2),
    append(4),
    removeLast(4),
    append(10),
    removeHead(3),
    prepend(5),
    append(5),
    Seq(clear(shrink = false), clear(shrink = true))
  ).flatten

  // Render
  val renderer = Renderer(
    animationOptions = AnimationOptions(interpolationDuration = 0.second, keyFrameDuration = 1.second)
  )
  import renderer._
  Animation(diagrams).render("ArrayDeque")
}
