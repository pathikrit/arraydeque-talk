package scala.collection.mutable

import java.lang.System.nanoTime

import scala.collection.mutable

object ArrayDequeBenchmark extends App {
  val candidates = Seq(
    mutable.ArrayDeque.empty[Int],
    mutable.ArrayBuffer.empty[Int]
  )

  def benchmark[U](name: String, f: mutable.Buffer[Int] => U) = {
    def profile(buffer: mutable.Buffer[Int]) = {
      val t1 = nanoTime()
      f(buffer)
      (nanoTime() - t1)/1e6
    }
    println(s"===============[$name]=================")
    candidates.foreach(c => println(f"${c.getClass.getSimpleName}%12s: ${profile(c)}%8.2f ms"))
    candidates.sliding(2) foreach {case Seq(a, b) =>
      assert(a == b, s"Operations are not same [$name] for ${a.getClass.getName} and ${b.getClass.getName}")
    }
  }

  val range10m = (1 to 1e7.toInt).toArray

  benchmark("Insert lots of items", _ ++= range10m)
  benchmark("Drop some items from an head index", _.remove(5, 10000))
  benchmark("Drop some items from a tail index", b => b.remove(b.length - 10000, 10000))
  benchmark("Append lots of items one by one", b => range10m.foreach(b.+=))
  benchmark("Prepend few items one by one", b => (1 to 1000).foreach(_ +=: b))
  benchmark("Prepend lots of items at once", range10m ++=: _)
  benchmark("Insert items near head", _.insertAll(1000, range10m))
  benchmark("Reversal", _.reverse)
  benchmark("Insert items near tail", b => b.insertAll(b.size - 1000, range10m))
  benchmark("Sliding", _.sliding(size = 1000, step = 1000).size)
  benchmark("Random indexing", b => range10m.foreach(i => if (b.isDefinedAt(i)) b(i)))
  benchmark("toArray", _.toArray)
  benchmark("Clear lots of items", _.clear())
}