package lantern

import scala.util.continuations._
import scala.util.continuations
import org.scala_lang.virtualized.virtualize
import org.scala_lang.virtualized.SourceContext

import scala.virtualization.lms._
import scala.virtualization.lms.common._
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.{Map => MutableMap}
import scala.math._

import scala.collection.mutable.ArrayBuffer

object Relay {

  type diff = cps[Unit]
  def RST(a: =>Unit @diff) = continuations.reset { a; () }
  var tape: Unit => Unit = (x: Unit) => ()

  class NumR(val x: Double, var d: Double) {
    def + (that: NumR) = {
      val y = new NumR(x + that.x, 0.0)
      tape = ((x: Unit) => this.d += y.d) andThen tape
      tape = ((x: Unit) => that.d += y.d) andThen tape
      y
    }
    def * (that: NumR) = {
      val y = new NumR(x * that.x, 0.0)
      tape = ((x: Unit) => this.d += y.d * that.x) andThen tape
      tape = ((x: Unit) => that.d += y.d * this.x) andThen tape
      y
    }
    override def toString() = (x, d).toString
  }

  def grad(f: NumR => NumR)(x: Double) = {
    val z = new NumR(x, 0.0)
    val y = f(z)
    y.d = 1.0
    tape()
    z
  }

  def f1(x: NumR) = x
  def f2(x: NumR) = x + x
  def f3(x: NumR) = x + x + x
  def f4(x: NumR) = x * x
  def f5(x: NumR) = x * x * x
  def f6(x: NumR) = x + x * x + x

  def main(args: Array[String]) {
    System.out.println("Hello")
    System.out.println(grad(f1)(5))
    System.out.println(grad(f2)(5))
    System.out.println(grad(f3)(5))
    System.out.println(grad(f4)(5))
    System.out.println(grad(f5)(5))
    System.out.println(grad(f6)(5))
  }
}
