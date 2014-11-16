package io.github.morgaroth.reactive.lab2.actors

import akka.actor._
import akka.event.LoggingReceive
import io.github.morgaroth.reactive.lab2.actors.AuctionSearch._

import scala.collection.mutable

object AuctionSearch {
  case class Search(phase: String)
  case class SearchResults(auctions: List[ActorRef])
  case class Unregister(auctionName: String)
  case class Register(auctionName: String)
}

class AuctionSearch extends Actor with ActorLogging {

  val auctions = mutable.Map[String, ActorRef]()

  override def receive = LoggingReceive {
    case Search(searchPhase) =>
      log.info(s"received search for $searchPhase request for ${sender().path}")
      val phase = searchPhase.toLowerCase
      sender() ! SearchResults(auctions.filterKeys(_.contains(phase)).values.toList)
    case Unregister(auctionName) =>
      log.info(s"unregistering auction $auctionName by actor ${sender()}")
      if (auctions contains auctionName) {
        auctions -= auctionName
      }
    case Register(name) =>
      log.info(s"registering auction $name by actor ${sender()}")
      auctions += name.toLowerCase -> sender
  }

}
