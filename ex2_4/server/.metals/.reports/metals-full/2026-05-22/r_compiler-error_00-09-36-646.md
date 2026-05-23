error id: FECB4D22AA935EF6441A77B4A13ED5ED
file://<WORKSPACE>/src/main/scala/cp/serverSim/ServerState.scala
### java.lang.IndexOutOfBoundsException: -1

occurred in the presentation compiler.



action parameters:
offset: 531
uri: file://<WORKSPACE>/src/main/scala/cp/serverSim/ServerState.scala
text:
```scala
package cp.serverSim
import scala.collection._
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

class Worker(tasks: mutable.Queue[String]) extends Thread {
  setDaemon(true)
  def poll() = tasks.synchronized {
    while (tasks.isEmpty) tasks.wait()
    // now using wait
    tasks.dequeue()
  }
  override def run() = while (true) {
    val task = poll()
    val args = cmd.split(" ").map(_.trim).filter(_.nonEmpty)
    var delay = 0
    if (args.size == 3) {
      delay = args[@@]
    } 
  }
}

class ServerState(n: Int) {
  // TODO: extend the state of the server

  val tasks = mutable.Queue[String]()
  (0 until n).map(i => {
    val worker = new Worker(tasks)
    worker.start()
    worker
  })
  val counter = new AtomicInteger(0)
  val content = new AtomicReference[Array[String]](Array.empty[String])

  def toHtml: String =
    s"<p><strong>counter:</strong> ${counter.get()}</p>" +
      s"<p><strong>printContent:</strong> ${content.get()}</p>"
}

```


presentation compiler configuration:
Scala version: 3.3.7-bin-nonbootstrapped
Classpath:
<HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/scala3-library_3/3.3.7/scala3-library_3-3.3.7.jar [exists ], <HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/scala-library/2.13.16/scala-library-2.13.16.jar [exists ]
Options:





#### Error stacktrace:

```
scala.collection.LinearSeqOps.apply(LinearSeq.scala:129)
	scala.collection.LinearSeqOps.apply$(LinearSeq.scala:128)
	scala.collection.immutable.List.apply(List.scala:79)
	dotty.tools.dotc.util.Signatures$.applyCallInfo(Signatures.scala:244)
	dotty.tools.dotc.util.Signatures$.computeSignatureHelp(Signatures.scala:104)
	dotty.tools.dotc.util.Signatures$.signatureHelp(Signatures.scala:88)
	dotty.tools.pc.SignatureHelpProvider$.signatureHelp(SignatureHelpProvider.scala:46)
	dotty.tools.pc.ScalaPresentationCompiler.signatureHelp$$anonfun$1(ScalaPresentationCompiler.scala:498)
	scala.meta.internal.pc.CompilerAccess.withSharedCompiler(CompilerAccess.scala:149)
	scala.meta.internal.pc.CompilerAccess.withNonInterruptableCompiler$$anonfun$1(CompilerAccess.scala:133)
	scala.meta.internal.pc.CompilerAccess.onCompilerJobQueue$$anonfun$1(CompilerAccess.scala:210)
	scala.meta.internal.pc.CompilerJobQueue$Job.run(CompilerJobQueue.scala:153)
	java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1090)
	java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:614)
	java.base/java.lang.Thread.run(Thread.java:1474)
```
#### Short summary: 

java.lang.IndexOutOfBoundsException: -1