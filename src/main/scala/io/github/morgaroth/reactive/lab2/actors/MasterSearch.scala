package io.github.morgaroth.reactive.lab2.actors

import akka.actor._
import akka.event.LoggingReceive
import akka.routing._

object MasterSearch {
  def props(
             implicit underlyingCount: UnderlyingSearchers,
             runningLogic: MasterSearchRunningLogic
             ) = Props(new MasterSearch)
}

class MasterSearch(
                    implicit underlyingCount: UnderlyingSearchers,
                    runningLogic: MasterSearchRunningLogic
                    )
  extends Actor with ActorLogging {

  log.info(s"MasterSearch actor configured to use ${underlyingCount.value} actors and '${runningLogic.name}' running logic")

  val routees: Vector[ActorRefRoutee] = Vector.fill(5) {
    val r = context.actorOf(Props[AuctionSearch])
    context watch r
    ActorRefRoutee(r)
  }

  var registeringRouter = Router(runningLogic.registeringLogic, routees)
  var searchingRouter = Router(runningLogic.searchingLogic, routees)
  var unregisteringRouter = Router(BroadcastRoutingLogic(), routees)

  override def receive = LoggingReceive {
    case w: AuctionSearch.Search =>
      runningLogic.performSearch(w, sender(), searchingRouter)
    case r: AuctionSearch.Register =>
      registeringRouter.route(r, sender())
    case ur: AuctionSearch.Unregister =>
      unregisteringRouter.route(ur, sender())
    case Terminated(a) =>
      registeringRouter = registeringRouter.removeRoutee(a)
      val r = context.actorOf(Props[AuctionSearch])
      context watch r
      registeringRouter = registeringRouter.addRoutee(r)
  }
}
