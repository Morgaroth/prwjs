package io.github.morgaroth.reactive.lab2.AuctionPublisher.services

import akka.actor.ActorSystem
import akka.event.Logging
import akka.util.Timeout
import io.github.morgaroth.reactive.lab2.actors.NotifierActor._
import spray.http.StatusCodes.Created
import spray.httpx.SprayJsonSupport
import spray.routing.{Directives, Route}

import scala.concurrent.duration._

class NotificationService(implicit actorSystem: ActorSystem) extends Directives with SprayJsonSupport {

  override val pathEnd = pathEndOrSingleSlash
  implicit val timeout: Timeout = 20.seconds

  val log = Logging(actorSystem, getClass.getCanonicalName)

  def route: Route =
    pathPrefix("") {
      pathEnd(
        get(complete("Hello from notification service")) ~
        post(handleWith { notification: Notify =>
          log.info(s"received notification $notification")
          Created -> "OK"
        })
      )
    }
}