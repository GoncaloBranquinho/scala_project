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
          state.strChannel1.str = ""
          state.strChannel2.str = ""
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

  def asynchr(s: (String, Option[Int], String, Int)) = {
    state.tasks.synchronized {
      state.tasks.enqueue(s)
      // now notifying
      state.tasks.notify()
    }
  }

  def parse(s: String): Option[(((String, Option[Int]), Option[List[Int]]), Int)] = {
    var str = s
    val command = 0
    str = str.trim
    val print_command = str.takeWhile(c => c != ' ')
    if (print_command != "print" && print_command != "channel1" && print_command != "channel2") return None
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
    val channel = if (print_command == "print") 0 else if (print_command == "channel1") 1 else if (print_command == "channel2") 2 else 3
    Some((((word, delayOpt), aftersOpt), channel))
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
    val parse_results = cmds.map(i => {
      val parse_result = parse(i)
      if (parse_result.isDefined) {
        val (((word, delay), aftersOpt), channel) = parse_result.get
        aftersOpt.foreach { aftersList =>
          aftersList.foreach { n =>
            {
              if (n >= 1 && n < id) {
                val key = cnt.toString + "_" + n.toString
                val toAdd = cnt.toString + "_" + id.toString
                var oldValue = state.dependencies.get
                var newValue = oldValue.updated(
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

                var oldValue1 = state.dependenciesCounter.get
                var newValue1 = oldValue1.updated(
                  toAdd,
                  oldValue1.getOrElse(toAdd, 0) + 1
                )
                while (
                  !state.dependenciesCounter.compareAndSet(oldValue1, newValue1)
                ) {
                  oldValue1 = state.dependenciesCounter.get
                  newValue1 = oldValue1.updated(
                    toAdd,
                    oldValue1.getOrElse(toAdd, 0) + 1
                  )
                }

              }
            }
          }
        }
      }
      id += 1
      parse_result
    })
    println(state.dependencies.get)
    println(state.dependenciesCounter.get)
    id = 1
    parse_results.foreach(i => {
      i match {
        case Some((((word, delay), afters), channel)) => {
          asynchr((word, delay, cnt.toString + "_" + id.toString, channel))
        }
        case None => {}
      }
      id += 1
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
