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
    IO{HttpRoutes.of[IO] {

      // React to a "status" request
      case GET -> Root / "status" =>
        Ok(state.toHtml)
          .map(addCORSHeaders)
          .map(_.withContentType(org.http4s.headers.`Content-Type`(MediaType.text.html)))

          // React to a "reset" request
      case GET -> Root / "reset" =>
        state.counter.set(0)
        state.content.set(Array.empty[String])
        Ok("State reset!")
          .map(addCORSHeaders)

          // React to a "run-simulation" request
      case req@GET -> Root / "run-simulation" =>
        val cmdOpt = req.uri.query.params.get("cmd")
        val userIp = req.remoteAddr.getOrElse("unknown")

        //// printing to the terminal instead of a logging file
        //println(">>> got run-simulation!")
        //println(s">>> Cmd: ${cmdOpt}")
        //println(s">>> userIP: $userIp")

        cmdOpt match {
          case Some(cmd) =>
            // calling the `runProcess` method, which simulates running a process
            Ok(runProcess(cmd, userIp.toString))
              .map(addCORSHeaders)

          case None =>
            BadRequest("⚠️ Command not provided. Use /run-simulation?cmd=<your_commands>")
              .map(addCORSHeaders)
        }
    }
    }
  }


  def asynchr(s: String) = {
    state.tasks.synchronized {
      state.tasks.enqueue(s)
      // now notifying
      state.tasks.notify()
    }
  }


  /** Run a given process and collect its output. */
  /**
   * This method simulates running a process. It should be replaced with actual code
   * to simulate the process using a thread pool. The `Thread.sleep` is just mimicking
   * the time to process the comand, and should be removed.
   *
   * @param cmd the command to run, which can be a single command or multiple commands separated by ";"
   * @param userIp the IP address of the user who sent the request, used for logging purposes
   * @return a string confirming the received command and user IP, which will be sent back to the client as a response
   */

  private def runProcess(cmd: String, userIp: String): String = {
    val cnt = state.counter.incrementAndGet()
    val cmds = cmd.split(";").map(_.trim).filter(_.nonEmpty)
    cmds.foreach(asynchr)

    // Printing the received command and user IP to the logs
    logger.info(s"🔹 Starting processes (${cnt}) for user $userIp:" +
      s"${cmds.map("\n - "+_).mkString}")

    // TODO:Run process here. The `Thread.sleep` should be removed.
    val output: String = s"[${cnt}] Received request from $userIp: ${cmds.mkString(" | ")}"

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


