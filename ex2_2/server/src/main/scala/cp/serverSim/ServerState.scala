package cp.serverSim
import scala.collection._
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Worker(
    tasks: mutable.Queue[String],
    content: AtomicReference[Array[String]]
) extends Thread {
  setDaemon(true)
  def poll() = tasks.synchronized {
    while (tasks.isEmpty) tasks.wait()
    // now using wait
    tasks.dequeue()
  }

  override def run() = while (true) {
    val task = poll()
    val args = task
      .split(" ")
      .map(_.trim)
      .filter(_.nonEmpty) // não separar por espaços desta forma
    var delay = 0
    if (args.size >= 4 && args(3) == "@") {
      delay = args(3).toInt
    }
    Thread.sleep(delay * 1000)

    val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
    val element =
      "[" ++ LocalDateTime.now().format(formatter) ++ "] " ++ args(1)
    content.updateAndGet { old =>
      old :+ element
    }

  }
}

class ServerState(n: Int) {
  // TODO: extend the state of the server

  val counter = new AtomicInteger(0)
  val content = new AtomicReference[Array[String]](Array.empty[String])

  val tasks = mutable.Queue[String]()
  (0 until n).map(i => {
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
