error id: 3C0CFCD2D22507A8C6EAABCE607006D8
file://<WORKSPACE>/src/main/scala/cp/serverSim/ServerState.scala
### java.lang.NullPointerException: Cannot invoke "scala.reflect.internal.Symbols$Symbol.isModule()" because the return value of "scala.meta.internal.pc.completions.ArgCompletions$ArgCompletion.methodSym()" is null

occurred in the presentation compiler.



action parameters:
offset: 673
uri: file://<WORKSPACE>/src/main/scala/cp/serverSim/ServerState.scala
text:
```scala
package cp.serverSim
import scala.collection._
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Worker(
    tasks: mutable.Queue[(String, Option[Int], Int)],
    content: AtomicReference[List[String]],
    dependencies: AtomicReference[Map[String, List[Int]]],
    dependenciesCounter: AtomicReference[Map[String, Int]]
) extends Thread {
  setDaemon(true)
  def poll() = tasks.synchronized {
    while (tasks.isEmpty) tasks.wait()
    // now using wait
    val task = tasks.dequeue()
    task_counter = dependenciesCounter.get.getOrDefault(c@@)

  }

  override def run() = while (true) {
    poll().foreach(i => {
      val (str, delayOpt, cnt) = i
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
    })

  }
}

class ServerState(n: Int) {
  // TODO: extend the state of the server

  val counter = new AtomicInteger(0)
  val content = new AtomicReference[List[String]](List.empty[String])
  val dependencies = new AtomicReference[Map[String, List[String]]]
  val dependenciesCounter = new AtomicReference[Map[String, Int]]
  val tasks = mutable.Queue[(String, Option[Int], Int)]()
  (0 until n).map(i => {
    val worker = new Worker(tasks, content, dependencies, dependenciesCounter)
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

```


presentation compiler configuration:
Scala version: 2.12.20
Classpath:
<HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/scala-library/2.12.20/scala-library-2.12.20.jar [exists ]
Options:





#### Error stacktrace:

```
scala.meta.internal.pc.completions.ArgCompletions$ArgCompletion.methodsParams$lzycompute(ArgCompletions.scala:34)
	scala.meta.internal.pc.completions.ArgCompletions$ArgCompletion.methodsParams(ArgCompletions.scala:33)
	scala.meta.internal.pc.completions.ArgCompletions$ArgCompletion.allParams$lzycompute(ArgCompletions.scala:85)
	scala.meta.internal.pc.completions.ArgCompletions$ArgCompletion.allParams(ArgCompletions.scala:85)
	scala.meta.internal.pc.completions.ArgCompletions$ArgCompletion.params$lzycompute(ArgCompletions.scala:87)
	scala.meta.internal.pc.completions.ArgCompletions$ArgCompletion.params(ArgCompletions.scala:86)
	scala.meta.internal.pc.completions.ArgCompletions$ArgCompletion.isParamName$lzycompute(ArgCompletions.scala:94)
	scala.meta.internal.pc.completions.ArgCompletions$ArgCompletion.isParamName(ArgCompletions.scala:94)
	scala.meta.internal.pc.completions.ArgCompletions$ArgCompletion.isName(ArgCompletions.scala:100)
	scala.meta.internal.pc.completions.ArgCompletions$ArgCompletion.compare(ArgCompletions.scala:103)
	scala.meta.internal.pc.completions.Completions$$anon$1.compare(Completions.scala:256)
	scala.meta.internal.pc.completions.Completions$$anon$1.compare(Completions.scala:211)
	java.base/java.util.TimSort.countRunAndMakeAscending(TimSort.java:355)
	java.base/java.util.TimSort.sort(TimSort.java:234)
	java.base/java.util.Arrays.sort(Arrays.java:1230)
	scala.collection.SeqLike.sorted(SeqLike.scala:659)
	scala.collection.SeqLike.sorted$(SeqLike.scala:647)
	scala.collection.AbstractSeq.sorted(Seq.scala:45)
	scala.meta.internal.pc.CompletionProvider.completions(CompletionProvider.scala:83)
	scala.meta.internal.pc.ScalaPresentationCompiler.$anonfun$complete$1(ScalaPresentationCompiler.scala:236)
	scala.meta.internal.pc.CompilerAccess.withSharedCompiler(CompilerAccess.scala:148)
	scala.meta.internal.pc.CompilerAccess.$anonfun$withInterruptableCompiler$1(CompilerAccess.scala:92)
	scala.meta.internal.pc.CompilerAccess.$anonfun$onCompilerJobQueue$1(CompilerAccess.scala:209)
	scala.meta.internal.pc.CompilerJobQueue$Job.run(CompilerJobQueue.scala:152)
	java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1090)
	java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:614)
	java.base/java.lang.Thread.run(Thread.java:1474)
```
#### Short summary: 

java.lang.NullPointerException: Cannot invoke "scala.reflect.internal.Symbols$Symbol.isModule()" because the return value of "scala.meta.internal.pc.completions.ArgCompletions$ArgCompletion.methodSym()" is null