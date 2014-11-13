package io.github.morgaroth.reactive.lab2.app

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import io.github.morgaroth.reactive.lab2.actors.SoulsReaper.WatchHim
import io.github.morgaroth.reactive.lab2.actors.{AuctionSearch, Buyer, Seller, SoulsReaper}
import io.github.morgaroth.reactive.lab2.utils.MapOfArraysConfigReader
import net.ceedubs.ficus.Ficus._
import akka.event.{Logging, LoggingAdapter}

import scala.concurrent.duration._

object Application extends MapOfArraysConfigReader {

  class Reaper extends SoulsReaper {
    override def allSoulsReaped(): Unit = context.system.shutdown()
  }

  def main(args: Array[String]) {
    val system = ActorSystem("Lydie")
    import system.dispatcher

    val reaper = system.actorOf(Props[Reaper], "reaper")

    readMapListStringAtPath("reactive.lab2.sellers").map { x =>
      val (name, items) = x
      val seller = system.actorOf(Props(classOf[Seller], items), s"seller_$name")
      reaper ! WatchHim(seller)
    }

    system.actorOf(Props[AuctionSearch], "AuctionSearch")

    system.scheduler.scheduleOnce(1 seconds) {
      readMapStringAtPath("reactive.lab2.buyers").map { x =>
        val (name, target) = x
        val buyer = system.actorOf(Props(classOf[Buyer], target), name)
        reaper ! WatchHim(buyer)
      }
    }

    system.scheduler.scheduleOnce(10 minutes)(system.shutdown())
  }
}
