package io.github.morgaroth.reactive.lab2.actors

import akka.actor.SupervisorStrategy.{Escalate, Restart, Stop}
import akka.actor._
import io.github.morgaroth.reactive.lab2.actors.NotifierActor.Notify
import io.github.morgaroth.reactive.lab2.actors.NotifierRequester.{ConnectionProblem, UnExpectedProblemOccurred}
import spray.can.Http.ConnectionAttemptFailedException
import spray.client.pipelining._
import spray.httpx.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.util.Random

object NotifierActor extends DefaultJsonProtocol {
  case class Notify(auction: String, winner: String, actualPrice: Double)
  implicit val NotifyJsonSerializer: RootJsonFormat[Notify] = jsonFormat3(Notify)
}

object NotifierRequester {
  case class UnExpectedProblemOccurred(message: String) extends Exception
  case class ConnectionProblem(message: String) extends Exception
  def props(notify: Notify, address: String) = Props(classOf[NotifierRequester], notify, address)
}

class NotifierRequester(notify: Notify, address: String) extends Actor with ActorLogging with SprayJsonSupport {

  import context.dispatcher

  val pipe = spray.client.pipelining.sendReceive

  self ! notify -> address

  /**
   * I tak niesamowicie wymuszone rzucanie wyjątku, spray client normalnie
   * sam ponawia wysłanie wiadomości jeśli napotka problemy, więc nie mam
   * pomysłu jak tu można naturalnie otrzymać wyjątek, więc jest wymuszony
   */
  override def receive: Receive = {
    case (notify: Notify, address: String) =>
      val unexpectedProblem = Random.nextInt()
      if (unexpectedProblem % 2 == 0) throw UnExpectedProblemOccurred(s"$unexpectedProblem unexpectedly isn't odd !")

      pipe(Post(s"$address/notify/", notify)).map { response =>
        log.info(s"posting notification ends with status ${response.status} and response ${response.entity.asString}")
      }.onFailure {
        // nie da się rzucić wyjątku stąd, bo wtedy nie ten aktor go rzuca,
        // bo to jest callback wykonywany przez kogoś innego,
        // przez jakiś inny wewnętrzny executor i nie obserwuję tego co chciałbym
        // ==== pamiętaj! fłuli ajsynkronołs ====
        case t: Throwable => self ! t
      }
    case t: Throwable =>
      // dlatego wysyłam aktorowi ten wyjątek i ten go dopiero rzuca :p
      throw t
  }
}

class NotifierActor(AuctionPublisher: String) extends Actor with ActorLogging {

  // przy czym jest to i tak niesamowicie wymuszone
  override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy(loggingEnabled = false) {
    case t: ConnectionAttemptFailedException =>
      log.error(s"child threw $t, stopping him")
      Stop
    case t: UnExpectedProblemOccurred =>
      log.error(s"child threw $t with message \"${t.message}\", Restarting him")
      Restart
    case t: Throwable =>
      log.error(s"child threw $t, escalating this problem")
      Escalate
  }

  override def receive: Actor.Receive = {
    case notify: Notify =>
      context actorOf NotifierRequester.props(notify, AuctionPublisher)
  }
}
