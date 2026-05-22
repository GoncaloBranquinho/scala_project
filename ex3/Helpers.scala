package project.ex3

import scala.concurrent._

object Helpers {
  def log(msg: String): Unit = println(s"${Thread.currentThread.getName}: $msg")
  def thread[Unit](body: =>Unit): Thread = {
    val t = new Thread {
      override def run() = body
    }
    t.start()
    t
  }
}
