package io.github.morgaroth.reactive.lab2.actors

import akka.actor.{Actor, ActorLogging}
import io.github.morgaroth.reactive.lab2.actors.NotifierActor.Notify
import spray.client.pipelining._
import spray.client._
import spray.httpx.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

object NotifierActor extends DefaultJsonProtocol {
  case class Notify(auction: String, winner: String, actualPrice: Double)
  implicit val NotifyJsonSerializer: RootJsonFormat[Notify] = jsonFormat3(Notify)
}

class NotifierActor(AuctionPublisher: String) extends Actor with ActorLogging with SprayJsonSupport {

  import context.dispatcher

  val pipe = spray.client.pipelining.sendReceive

  override def receive: Receive = {
    case notify: Notify =>
      pipe(Post(s"$AuctionPublisher/notify/", notify)).map { response =>
        log.info(s"posting notification ends with status ${response.status} and response ${response.entity.asString}")
      }
  }
}

