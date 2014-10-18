package io.github.morgaroth.reactive.lab2.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.typesafe.config.ConfigFactory
import io.github.morgaroth.reactive.lab2.actors.Store.Auctions
import io.github.morgaroth.reactive.lab2.actors.auction.AuctionActorMessages.AuctionEnds
import io.github.morgaroth.reactive.lab2.actors.auction.FSMAuctionActor
import io.github.morgaroth.reactive.lab2.utils.randomsDSL
import net.ceedubs.ficus.Ficus._
import pl.morgaroth.utils.random.randoms

import scala.concurrent.duration._

object Store {
  case object Auctions
}


class Store extends Actor with ActorLogging with randoms with randomsDSL {

  val items = ConfigFactory.load().as[Option[List[String]]]("reactive.lab2.items").
    getOrElse(List("Bow", "Gun", "Shield", "Rod", "Mystic_sword"))

  var auctions: List[ActorRef] = items.map(item => {
    val args = 30 ~ 50 random() seconds span
    context.actorOf(Props(classOf[FSMAuctionActor], item, args), s"${item}_auction")
  })

  override def receive: Receive = {
    case Auctions =>
      sender ! auctions
    case AuctionEnds(sold) =>
      if (sold) {
        log.info(s"${sender.path.name} sold item")
      } else {
        log.info(s"${sender.path.name} didn't sold")
      }
      auctions = auctions.diff(sender :: Nil)
      if (auctions.length == 0) {
        log.info(s"no auctions left, shutdown")
        context.system.shutdown()
      }
  }
}
