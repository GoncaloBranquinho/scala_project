error id: CC11AC2D9A238BD4E1A5CA9437B8FAD6
file://<WORKSPACE>/src/main/scala/cp/serverSim/ServerState.scala
### java.lang.StringIndexOutOfBoundsException: Range [1825, 1825 + -48) out of bounds for length 2942

occurred in the presentation compiler.



action parameters:
offset: 1724
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
    tasks: mutable.Queue[(String, Option[Int], String)],
    content: AtomicReference[List[String]],
    dependencies: AtomicReference[Map[String, List[String]]],
    dependenciesCounter: AtomicReference[Map[String, Int]]
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
      dependencies.get
        .get(cnt)
        .foreach(key => {
          var oldValue = dependencies.get
          var newValue = @@oldValue.updated(
            key,
            toAdd :: oldValue.getOrElse(key, List.empty[String])
          )
          while (!state.dependencies.compareAndSet(oldValue, newValue)) {
            oldValue = state.dependencies.get
            newValue = oldValue.updated(
              key,
              toAdd :: oldValue.getOrElse(key, List.empty[String])
            )
          }
        })

    })
  }
}

class ServerState(n: Int) {
  // TODO: extend the state of the server

  val counter = new AtomicInteger(0)
  val content = new AtomicReference[List[String]](List.empty[String])
  val dependencies =
    new AtomicReference[Map[String, List[String]]](
      Map.empty[String, List[String]]
    )
  val dependenciesCounter =
    new AtomicReference[Map[String, Int]](Map.empty[String, Int])
  val tasks = mutable.Queue[(String, Option[Int], String)]()
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
java.base/jdk.internal.util.Preconditions$1.apply(Preconditions.java:55)
	java.base/jdk.internal.util.Preconditions$1.apply(Preconditions.java:52)
	java.base/jdk.internal.util.Preconditions$4.apply(Preconditions.java:213)
	java.base/jdk.internal.util.Preconditions$4.apply(Preconditions.java:210)
	java.base/jdk.internal.util.Preconditions.outOfBounds(Preconditions.java:98)
	java.base/jdk.internal.util.Preconditions.outOfBoundsCheckFromIndexSize(Preconditions.java:118)
	java.base/jdk.internal.util.Preconditions.checkFromIndexSize(Preconditions.java:397)
	java.base/java.lang.String.checkBoundsOffCount(String.java:4925)
	java.base/java.lang.String.rangeCheck(String.java:318)
	java.base/java.lang.String.<init>(String.java:314)
	scala.tools.nsc.interactive.Global.typeCompletions$1(Global.scala:1231)
	scala.tools.nsc.interactive.Global.completionsAt(Global.scala:1254)
	scala.meta.internal.pc.SignatureHelpProvider.$anonfun$treeSymbol$1(SignatureHelpProvider.scala:462)
	scala.Option.map(Option.scala:230)
	scala.meta.internal.pc.SignatureHelpProvider.treeSymbol(SignatureHelpProvider.scala:460)
	scala.meta.internal.pc.SignatureHelpProvider$MethodCall$.unapply(SignatureHelpProvider.scala:255)
	scala.meta.internal.pc.SignatureHelpProvider$MethodCallTraverser.visit(SignatureHelpProvider.scala:366)
	scala.meta.internal.pc.SignatureHelpProvider$MethodCallTraverser.traverse(SignatureHelpProvider.scala:360)
	scala.meta.internal.pc.SignatureHelpProvider$MethodCallTraverser.$anonfun$visit$5(SignatureHelpProvider.scala:418)
	scala.meta.internal.pc.SignatureHelpProvider$MethodCallTraverser.$anonfun$visit$5$adapted(SignatureHelpProvider.scala:373)
	scala.collection.TraversableLike$WithFilter.$anonfun$foreach$1(TraversableLike.scala:985)
	scala.collection.immutable.List.foreach(List.scala:431)
	scala.collection.TraversableLike$WithFilter.foreach(TraversableLike.scala:984)
	scala.meta.internal.pc.SignatureHelpProvider$MethodCallTraverser.$anonfun$visit$3(SignatureHelpProvider.scala:373)
	scala.meta.internal.pc.SignatureHelpProvider$MethodCallTraverser.$anonfun$visit$3$adapted(SignatureHelpProvider.scala:372)
	scala.collection.TraversableLike$WithFilter.$anonfun$foreach$1(TraversableLike.scala:985)
	scala.collection.immutable.List.foreach(List.scala:431)
	scala.collection.TraversableLike$WithFilter.foreach(TraversableLike.scala:984)
	scala.meta.internal.pc.SignatureHelpProvider$MethodCallTraverser.visit(SignatureHelpProvider.scala:372)
	scala.meta.internal.pc.SignatureHelpProvider$MethodCallTraverser.traverse(SignatureHelpProvider.scala:360)
	scala.meta.internal.pc.SignatureHelpProvider$MethodCallTraverser.traverse(SignatureHelpProvider.scala:325)
	scala.reflect.internal.Trees.$anonfun$itraverse$1(Trees.scala:1273)
	scala.reflect.api.Trees$Traverser.atOwner(Trees.scala:2515)
	scala.reflect.internal.Trees.traverseMemberDef$1(Trees.scala:1267)
	scala.reflect.internal.Trees.itraverse(Trees.scala:1392)
	scala.reflect.internal.Trees.itraverse$(Trees.scala:1264)
	scala.reflect.internal.SymbolTable.itraverse(SymbolTable.scala:28)
	scala.reflect.internal.SymbolTable.itraverse(SymbolTable.scala:28)
	scala.reflect.api.Trees$Traverser.traverse(Trees.scala:2483)
	scala.meta.internal.pc.SignatureHelpProvider$MethodCallTraverser.visit(SignatureHelpProvider.scala:422)
	scala.meta.internal.pc.SignatureHelpProvider$MethodCallTraverser.traverse(SignatureHelpProvider.scala:360)
	scala.meta.internal.pc.SignatureHelpProvider$MethodCallTraverser.traverse(SignatureHelpProvider.scala:325)
	scala.reflect.api.Trees$Traverser.$anonfun$traverseTrees$1(Trees.scala:2492)
	scala.reflect.api.Trees$Traverser.traverseTrees(Trees.scala:2492)
	scala.reflect.internal.Trees.traverseComponents$1(Trees.scala:1298)
	scala.reflect.internal.Trees.itraverse(Trees.scala:1394)
	scala.reflect.internal.Trees.itraverse$(Trees.scala:1264)
	scala.reflect.internal.SymbolTable.itraverse(SymbolTable.scala:28)
	scala.reflect.internal.SymbolTable.itraverse(SymbolTable.scala:28)
	scala.reflect.api.Trees$Traverser.traverse(Trees.scala:2483)
	scala.meta.internal.pc.SignatureHelpProvider$MethodCallTraverser.visit(SignatureHelpProvider.scala:422)
	scala.meta.internal.pc.SignatureHelpProvider$MethodCallTraverser.traverse(SignatureHelpProvider.scala:360)
	scala.meta.internal.pc.SignatureHelpProvider$MethodCallTraverser.traverse(SignatureHelpProvider.scala:325)
	scala.reflect.internal.Trees.$anonfun$itraverse$3(Trees.scala:1393)
	scala.reflect.api.Trees$Traverser.atOwner(Trees.scala:2515)
	scala.reflect.internal.Trees.itraverse(Trees.scala:1393)
	scala.reflect.internal.Trees.itraverse$(Trees.scala:1264)
	scala.reflect.internal.SymbolTable.itraverse(SymbolTable.scala:28)
	scala.reflect.internal.SymbolTable.itraverse(SymbolTable.scala:28)
	scala.reflect.api.Trees$Traverser.traverse(Trees.scala:2483)
	scala.meta.internal.pc.SignatureHelpProvider$MethodCallTraverser.visit(SignatureHelpProvider.scala:422)
	scala.meta.internal.pc.SignatureHelpProvider$MethodCallTraverser.traverse(SignatureHelpProvider.scala:360)
	scala.meta.internal.pc.SignatureHelpProvider$MethodCallTraverser.$anonfun$visit$5(SignatureHelpProvider.scala:418)
	scala.meta.internal.pc.SignatureHelpProvider$MethodCallTraverser.$anonfun$visit$5$adapted(SignatureHelpProvider.scala:373)
	scala.collection.TraversableLike$WithFilter.$anonfun$foreach$1(TraversableLike.scala:985)
	scala.collection.immutable.List.foreach(List.scala:431)
	scala.collection.TraversableLike$WithFilter.foreach(TraversableLike.scala:984)
	scala.meta.internal.pc.SignatureHelpProvider$MethodCallTraverser.$anonfun$visit$3(SignatureHelpProvider.scala:373)
	scala.meta.internal.pc.SignatureHelpProvider$MethodCallTraverser.$anonfun$visit$3$adapted(SignatureHelpProvider.scala:372)
	scala.collection.TraversableLike$WithFilter.$anonfun$foreach$1(TraversableLike.scala:985)
	scala.collection.immutable.List.foreach(List.scala:431)
	scala.collection.TraversableLike$WithFilter.foreach(TraversableLike.scala:984)
	scala.meta.internal.pc.SignatureHelpProvider$MethodCallTraverser.visit(SignatureHelpProvider.scala:372)
	scala.meta.internal.pc.SignatureHelpProvider$MethodCallTraverser.traverse(SignatureHelpProvider.scala:360)
	scala.meta.internal.pc.SignatureHelpProvider$MethodCallTraverser.fromTree(SignatureHelpProvider.scala:329)
	scala.meta.internal.pc.SignatureHelpProvider.$anonfun$signatureHelp$3(SignatureHelpProvider.scala:33)
	scala.Option.flatMap(Option.scala:271)
	scala.meta.internal.pc.SignatureHelpProvider.$anonfun$signatureHelp$2(SignatureHelpProvider.scala:31)
	scala.Option.flatMap(Option.scala:271)
	scala.meta.internal.pc.SignatureHelpProvider.signatureHelp(SignatureHelpProvider.scala:29)
	scala.meta.internal.pc.ScalaPresentationCompiler.$anonfun$signatureHelp$1(ScalaPresentationCompiler.scala:434)
	scala.meta.internal.pc.CompilerAccess.withSharedCompiler(CompilerAccess.scala:148)
	scala.meta.internal.pc.CompilerAccess.$anonfun$withNonInterruptableCompiler$1(CompilerAccess.scala:132)
	scala.meta.internal.pc.CompilerAccess.$anonfun$onCompilerJobQueue$1(CompilerAccess.scala:209)
	scala.meta.internal.pc.CompilerJobQueue$Job.run(CompilerJobQueue.scala:152)
	java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1090)
	java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:614)
	java.base/java.lang.Thread.run(Thread.java:1474)
```
#### Short summary: 

java.lang.StringIndexOutOfBoundsException: Range [1825, 1825 + -48) out of bounds for length 2942