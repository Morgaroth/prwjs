package io.github.morgaroth.reactive.lab2.actors

import akka.actor.{PoisonPill, Actor, ActorLogging, Props}
import io.github.morgaroth.reactive.lab2.actors.AuctionSearch.{Register, Unregister}
import io.github.morgaroth.reactive.lab2.actors.auction.AuctionActorMessages.AuctionEnds
import io.github.morgaroth.reactive.lab2.actors.auction.FSMAuctionActor
import io.github.morgaroth.reactive.lab2.utils.randomsDSL
import io.github.morgaroth.utils.random.randoms

import scala.concurrent.duration._

object Seller {
  case object Auctions
}

class Seller(items: List[String]) extends Actor with ActorLogging with randoms with randomsDSL {

  import context.dispatcher

  val auctionSearch = context.actorSelection("/user/AuctionSearch").resolveOne(10 seconds)

  var auctions = items.map(item => {
    val args = 30 ~ 50 random() seconds span
    val auction = context.actorOf(Props(classOf[FSMAuctionActor], item, args), s"""${item.replaceAll(" ", "_")}_auction""")
    auctionSearch.map(_.tell(Register(item), auction))
    auction -> item
  }).toMap

  override def receive: Receive = {
    case AuctionEnds(sold) =>
      sold match {
        case true => log.info(s"${sender().path.name} sold item")
        case false => log.info(s"${sender().path.name} didn't sold")
      }
      auctions -= sender
      auctionSearch.map(_.tell(Unregister(auctions(sender())), sender()))
      if (auctions.size == 0) {
        log.info(s"no auctions left, shutdown")
        self ! PoisonPill
      }
  }
}