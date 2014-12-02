package io.github.morgaroth.reactive.lab2.AuctionPublisher.stack

import akka.actor.Props
import io.github.morgaroth.reactive.lab2.AuctionPublisher.services.NotificationService
import spray.routing.{Directives, Route, RouteConcatenation}

trait RESTApi extends RouteConcatenation with Directives {
  this: CoreActors with Core =>
  private implicit val _ = system.dispatcher

  val notificationService = new NotificationService()(system).route

  val routes: Route =
    pathPrefix("") {
      pathEndOrSingleSlash {
        get(complete("Hello from Your NotificationPublisher!"))
      }
    } ~
    pathPrefix("notify") {
      notificationService
    }

  val rootService = system.actorOf(Props(new RoutedHttpService(routes)))
}