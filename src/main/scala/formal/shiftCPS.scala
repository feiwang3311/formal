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

object secOrder {

  type Cont = (Num => Unit) => Unit
  type diff = cps[Cont]

  class Num(val x: Double, var d: Double) {
    def + (that: Num) = {(k: Num => Unit) =>
      val y = new Num(x + that.x, 0.0); k(y)
      this.d += y.d; that.d += y.d
    }
    def * (that: Num) = {(k: Num => Unit) =>
      val y = new Num(x * that.x, 0.0); k(y)
      this.d += y.d * that.x; that.d += y.d * x
    }
    override def toString() = (x, d).toString
  }

  class NumR(val x: Num, var d: Num) {
    def + (that: NumR) = shift { (k: NumR => Cont) =>
      (p: Num => Unit) => (x + that.x) { t: Num =>
        val y = new NumR(t, new Num(0.0, 0.0))
        k(y){u: Num =>
          (this.d + y.d){u: Num =>
            this.d = u;
            (that.d + y.d){u: Num =>
              that.d = u
              p(that.d)
            }
          }
        }
      }
    }
    def * (that: NumR) = shift { (k: NumR => Cont) =>
      (p: Num => Unit) => (x * that.x) { t: Num =>
        val y = new NumR(t, new Num(0.0, 0.0))
        k(y){u: Num =>
          (y.d * that.x){u: Num =>
            (this.d + u){u: Num =>
              this.d = u
              (y.d * this.x){u: Num =>
                (that.d + u){u: Num =>
                  that.d = u
                  p(that.d)
                }
              }
            }
          }
        }
      }
    }
    override def toString() = (x.x, x.d, d.x, d.d).toString
  }

  def grad(f: NumR => NumR@diff)(x: Num) = {
    val z = new NumR(x, new Num(0.0, 0.0))
    continuations.reset{
      f(z).d = new Num(1.0, 0.0)
      (p: Num => Unit) => p(z.d)
    }
  }

  def grad(f: Num => (Num => Unit) => Unit)(x: Double) = {
    val z = new Num(x, 0.0)
    var res = 0.0
    f(z){r => res = r.x; r.d = 1.0}
    (res, z.d)
  }

  // def gradR(f: NumR => NumR@diff)(x: Double) = {
  //   val ff = (x: Num) => reset {
  //     val a = new Num(1.0, 0.0)
  //     f(new NumR(x, new Num(0.0, 0.0))).d = a
  //     (p: Num => Unit) => p(a)
  //   }
  //   val z = new Num(x, 0.0)
  //   ff(z){r => r.d = 1.0}
  //   z.d
  // }

  val input = new Num(5, 5)
  def f1(x: NumR) = x
  def f2(x: NumR) = x + x
  def f3(x: NumR) = x * x
  def f4(x: NumR) = x + x + x
  def f5(x: NumR) = x * x * x
  def f6(x: NumR) = x + x + x + x
  def f7(x: NumR) = x * x * x * x

  def main(args: Array[String]) {
    System.out.println("Hello")
    val a1: Num => (Num => Unit) => Unit = grad(f1)
    System.out.println(grad(a1)(5))
    val a2: Num => (Num => Unit) => Unit = grad(f2)
    System.out.println(grad(a2)(5))
    val a3: Num => (Num => Unit) => Unit = grad(f3)
    System.out.println(grad(a3)(5))
    val a4: Num => (Num => Unit) => Unit = grad(f4)
    System.out.println(grad(a4)(5))
    val a5: Num => (Num => Unit) => Unit = grad(f5)
    System.out.println(grad(a5)(5))
    val a6: Num => (Num => Unit) => Unit = grad(f6)
    System.out.println(grad(a6)(5))
    val a7: Num => (Num => Unit) => Unit = grad(f7)
    System.out.println(grad(a7)(5))
    // System.out.println(gradR(f1)(5))
    // System.out.println(gradR(f2)(5))
    // System.out.println(gradR(f3)(5))
    // System.out.println(gradR(f4)(5))
    // System.out.println(gradR(f5)(5))
    // System.out.println(gradR(f6)(5))
    // System.out.println(gradR(f7)(5))
  }
}
