package project.ex3

import Helpers._
import akka.actor._
import akka.event.Logging

object TicketOfficeTest extends App {

  case class ToSell (n : Int)
  case class Buy (n : Int)
  // ...

  class SellerActor() extends Actor {
    val log = Logging(context.system, this)
    var tickets = 0
      // ...
    def receive: Actor.Receive = {
      case ToSell(n) if n <= tickets =>
        tickets = tickets - n
        log.info(s"Sold ${n} tickets")
      case ToSell(n) => log.info (s"Failed to sell ${n} tickets, as I only have ${tickets} tickets")
      case Buy(n) =>
        tickets = tickets + n
        log.info(s"Bought ${n} tickets, now I have ${tickets} tickets")
      case _ =>
        log.info(s"Unexpected input: I neither got ToSell nor Buy, closing the seller")
        context.stop(self)
    }
  }
  // Testing the system

  val sys = ActorSystem("TicketSys")
  val ticketOffice = sys.actorOf(Props[SellerActor], "main_office")

  ticketOffice ! ToSell(2000)

  for (x <- 0 until 101) ticketOffice ! Buy(20)

  log(s"Tried to buy many ${20*101} tickets.")
  ticketOffice ! "Bye"
  Thread.sleep(3000)
  sys.terminate()
}
