error id: 246AD5C699D6A7FFEB3C7E5647D9B9AC
file://<WORKSPACE>/src/main/scala/cp/serverSim/Routes.scala
### java.lang.StringIndexOutOfBoundsException: Range [4920, 4920 + -48) out of bounds for length 6084

occurred in the presentation compiler.



action parameters:
offset: 4871
uri: file://<WORKSPACE>/src/main/scala/cp/serverSim/Routes.scala
text:
```scala
package cp.serverSim

import cats.effect.IO
import org.http4s._
import org.http4s.dsl.io._
import org.slf4j.LoggerFactory

object Routes {
  // Logger object, printing to the file logs/logs.txt
  private val logger = LoggerFactory.getLogger(getClass)
  private val state = new ServerState(6)

  val routes: IO[HttpRoutes[IO]] = {
    IO {
      HttpRoutes.of[IO] {

        // React to a "status" request
        case GET -> Root / "status" =>
          Ok(state.toHtml)
            .map(addCORSHeaders)
            .map(
              _.withContentType(
                org.http4s.headers.`Content-Type`(MediaType.text.html)
              )
            )

        // React to a "reset" request
        case GET -> Root / "reset" =>
          var oldValue = state.counter.get
          var newValue = 0
          while (!state.counter.compareAndSet(oldValue, newValue)) {
            oldValue = state.counter.get
          }
          var oldValue1 = state.content.get
          var newValue1 = List.empty[String]
          while (!state.content.compareAndSet(oldValue1, newValue1)) {
            oldValue1 = state.content.get
          }
          Ok("State reset!")
            .map(addCORSHeaders)

        // React to a "run-simulation" request
        case req @ GET -> Root / "run-simulation" =>
          val cmdOpt = req.uri.query.params.get("cmd")
          val userIp = req.remoteAddr.getOrElse("unknown")

          //// printing to the terminal instead of a logging file
          // println(">>> got run-simulation!")
          // println(s">>> Cmd: ${cmdOpt}")
          // println(s">>> userIP: $userIp")

          cmdOpt match {
            case Some(cmd) =>
              // calling the `runProcess` method, which simulates running a process
              Ok(runProcess(cmd, userIp.toString))
                .map(addCORSHeaders)

            case None =>
              BadRequest(
                "⚠️ Command not provided. Use /run-simulation?cmd=<your_commands>"
              )
                .map(addCORSHeaders)
          }
      }
    }
  }

  def asynchr(s: (String, Option[Int], Int)) = {
    state.tasks.synchronized {
      state.tasks.enqueue(s)
      // now notifying
      state.tasks.notify()
    }
  }

  def parse(s: String): Option[((String, Option[Int]), Option[List[Int]])] = {
    var str = s
    val command = 0
    str = str.trim
    val print_command = str.takeWhile(c => c != ' ')
    if (print_command != "print") return None
    str = str.dropWhile(c => c != ' ')
    str = str.dropWhile(c => c == ' ')
    if (str.isEmpty || str(0) != '"') return None

    str = str.tail
    val word = str.takeWhile(c => c != '"')
    str = str.dropWhile(c => c != '"')
    if (str.isEmpty || str(0) != '"') return None

    str = str.tail.trim
    var delayOpt: Option[Int] = None
    if (str.length > 0 && str(0) == '@') {
      str = str.tail.trim
      val delay = str.takeWhile(n => n.isDigit)
      if (delay.isEmpty) return None

      delayOpt = Some(delay.toInt)
      str = str.dropWhile(n => n.isDigit).trim
    }

    var aftersOpt: Option[List[Int]] = None
    val after_command = str.takeWhile(c => c.isLetter)
    if (after_command != "after" && !after_command.isEmpty) return None
    str = str.dropWhile(c => c.isLetter)
    val afters = str.split(",").map(_.trim).filter(_.nonEmpty)
    if (!afters.forall(n => n.forall(m => m.isDigit))) return None
    aftersOpt = Some(afters.map(n => n.toInt).toList)
    Some(((word, delayOpt), aftersOpt))
  }

  /** Run a given process and collect its output. */
  /** This method simulates running a process. It should be replaced with actual
    * code to simulate the process using a thread pool. The `Thread.sleep` is
    * just mimicking the time to process the comand, and should be removed.
    *
    * @param cmd
    *   the command to run, which can be a single command or multiple commands
    *   separated by ";"
    * @param userIp
    *   the IP address of the user who sent the request, used for logging
    *   purposes
    * @return
    *   a string confirming the received command and user IP, which will be sent
    *   back to the client as a response
    */

  private def runProcess(cmd: String, userIp: String): String = {
    val cnt = state.counter.incrementAndGet()
    val cmds = cmd.split(";").map(_.trim).filter(_.nonEmpty)
    // perocrerr  todos os cmds uma vez para criar map de dependencias

    var id = 1
    val parse_results = cmds.foreach(i => {
      val parse_result = parse(i)
      val ((word, delay), afters) = parse_result
      afters.foreach { n =>
        {
          if (n >= 1 && n < id) {
            val key = cnt.toString + "_" + n.toString
            val toAdd = cnt.toString + "_" + id.toString
            var oldValue = state.dependencies.get
            var newValue = oldValue.updated(
              key,
              toAdd@@ :: oldValue.getOrElse(key, List.empty[String])
            )
            while (!state.counter.compareAndSet(oldValue, newValue)) {
              oldValue = state.counter.get
            }

          }
        }
      }
      id += 1
      parse_result
    })

    parse_results.foreach(i => {
      i match {
        case Some(((word, delay), afters)) => {
          asynchr((word, delay, cnt))
        }
        case None => {}
      }
    })

    // Printing the received command and user IP to the logs
    logger.info(
      s"🔹 Starting processes (${cnt}) for user $userIp:" +
        s"${cmds.map("\n - " + _).mkString}"
    )

    // TODO:Run process here. The `Thread.sleep` should be removed.
    val output: String =
      s"[${cnt}] Received request from $userIp: ${cmds.mkString(" | ")}"

    output
  }

  /** Add extra headers, required by the client. */
  def addCORSHeaders(response: Response[IO]): Response[IO] = {
    response.putHeaders(
      "Access-Control-Allow-Origin" -> "*",
      "Access-Control-Allow-Methods" -> "GET, POST, PUT, DELETE, OPTIONS",
      "Access-Control-Allow-Headers" -> "Content-Type, Authorization",
      "Access-Control-Allow-Credentials" -> "true"
    )
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

java.lang.StringIndexOutOfBoundsException: Range [4920, 4920 + -48) out of bounds for length 6084