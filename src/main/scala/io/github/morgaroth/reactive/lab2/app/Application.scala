package io.github.morgaroth.reactive.lab2.app

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import io.github.morgaroth.reactive.lab2.actors.SoulsReaper.WatchHim
import io.github.morgaroth.reactive.lab2.actors.{AuctionSearch, Buyer, Seller, SoulsReaper}
import io.github.morgaroth.reactive.lab2.utils.MapOfArraysConfigReader
import net.ceedubs.ficus.Ficus._
import akka.event.{Logging,LoggingAdapter}

import scala.concurrent.duration._

object Application extends MapOfArraysConfigReader {

  class Reaper extends SoulsReaper {
    // Derivations need to implement this method.  It's the
    override def allSoulsReaped(): Unit = context.system.shutdown()
  }


  def main(args: Array[String]) {
    val system = ActorSystem("Lydie")
    import system.dispatcher

    val log = Logging(system,"APP")

    val reaper = system.actorOf(Props[Reaper],"reaper")

    val buyers = readMapStringAtPath("reactive.lab2.buyers")

    val sellers = readMapListStringAtPath("reactive.lab2.sellers").map { x =>
      val (name, items) = x
      val seller = system.actorOf(Props(classOf[Seller], items), s"seller_$name")
      reaper ! WatchHim(seller)
      seller
    }

    val auctionSearchActor = system.actorOf(Props[AuctionSearch], "AuctionSearch")

    println(auctionSearchActor.path)

    system.scheduler.scheduleOnce(2 seconds) {
      buyers.map { x =>
        val (name, target) = x
        val buyer = system.actorOf(Props(classOf[Buyer], target), name)
        reaper ! WatchHim(buyer)
      }
    }

    system.scheduler.scheduleOnce(10 minutes)(system.shutdown())
  }
}
