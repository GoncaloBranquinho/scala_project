package cp.serverSim
import scala.collection._
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class StrRef(@volatile var str: String)

class Worker(
  tasks: mutable.Queue[(String, Option[Int], String, Int)],
  content: AtomicReference[List[String]],
  dependencies: AtomicReference[Map[String, List[String]]],
  dependenciesCounter: AtomicReference[Map[String, Int]],
  var strChannel1: StrRef,
  var strChannel2: StrRef
) extends Thread {
  setDaemon(true)
  def poll() = tasks.synchronized {
    while (tasks.isEmpty) tasks.wait()
    // now using wait
    val task = tasks.dequeue()
    val id = task._3
    val oldValue = dependenciesCounter.get
    val task_counter = if (oldValue.contains(id)) {
      oldValue.get(id).get
    } else {
      0
    }
    if (task_counter > 0) {
      tasks.enqueue(task)
      None
    } else {
      var oldValue = dependenciesCounter.get
      var newValue = oldValue - id
      while (!dependenciesCounter.compareAndSet(oldValue, newValue)) {
        oldValue = dependenciesCounter.get
        newValue = oldValue - id
      }
      Some(task)
    }

  }

  override def run() = while (true) {
    poll().foreach(i => {
      val (str, delayOpt, cnt, channel) = i
      var delay = delayOpt match {
        case Some(d) => d
        case None    => 0
      }
      Thread.sleep(delay * 1000)
      var str1 = str
      if (channel == 1) {
        strChannel1.str = str
        str1 = strChannel2.str
      } else if (channel == 2) {
        strChannel2.str = str
        str1 = strChannel1.str
      }
      val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
      val element =
        "[" ++ LocalDateTime.now().format(formatter) ++ "] " ++ str1
      content.updateAndGet { old =>
        old :+ element
      }
      dependencies.get
        .getOrElse(cnt, List.empty[String])
        .foreach(key => {
          var oldValue = dependenciesCounter.get
          var newValue = oldValue.updated(key, oldValue(key) - 1)
          while (!dependenciesCounter.compareAndSet(oldValue, newValue)) {
            oldValue = dependenciesCounter.get
            newValue = oldValue.updated(key, oldValue(key) - 1)
          }
        })
      var oldValue = dependencies.get
      var newValue = oldValue - cnt
      while (!dependencies.compareAndSet(oldValue, newValue)) {
        oldValue = dependencies.get
        newValue = oldValue - cnt
      }

    })
  }
}

class ServerState(n: Int) {
  // TODO: extend the state of the server
  val strChannel1 = new StrRef("")
  val strChannel2 = new StrRef("")

  val counter = new AtomicInteger(0)
  val content = new AtomicReference[List[String]](List.empty[String])
  val dependencies =
    new AtomicReference[Map[String, List[String]]](
      Map.empty[String, List[String]]
    )
  val dependenciesCounter =
    new AtomicReference[Map[String, Int]](Map.empty[String, Int])
  val tasks = mutable.Queue[(String, Option[Int], String, Int)]()
  (0 until n).map(i => {
    val worker = new Worker(tasks, content, dependencies, dependenciesCounter, strChannel1, strChannel2)
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

