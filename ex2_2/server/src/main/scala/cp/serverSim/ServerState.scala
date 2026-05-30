package cp.serverSim
import scala.collection._
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Worker(
    tasks: mutable.Queue[(String, Option[Int])],
    content: AtomicReference[List[String]]
) extends Thread {
  setDaemon(true)
  def poll() = tasks.synchronized {
    while (tasks.isEmpty) tasks.wait()
    // now using wait
    tasks.dequeue()
  }

  override def run() = while (true) {
    val (str, delayOpt) = poll()
    var delay = delayOpt match {
      case Some(d) => d
      case None    => 0
    }
    Thread.sleep(delay * 1000)

    val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
    val element =
      "[" ++ LocalDateTime.now().format(formatter) ++ "] " ++ str
    content.updateAndGet { old =>
      old :+ element
    }

  }
}

class ServerState(n: Int) {
  // TODO: extend the state of the server

  val counter = new AtomicInteger(0)
  val content = new AtomicReference[List[String]](List.empty[String])

  val tasks = mutable.Queue[(String, Option[Int])]()
  val workers = (0 until n).map(i => {
    val worker = new Worker(tasks, content)
    worker.start()
    worker
  })

  def toHtml: String = {
    val aux = content.get()
    s"<p><strong>counter:</strong> ${counter.get()}</p>" +
      s"<p><strong>content:</strong></p>${if (aux.isEmpty) ""
        else aux.mkString("<br>")}"

  }
}
