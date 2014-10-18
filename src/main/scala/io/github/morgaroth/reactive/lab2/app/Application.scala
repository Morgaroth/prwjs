package io.github.morgaroth.reactive.lab2.app

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import io.github.morgaroth.reactive.lab2.actors.{Buyer, Store}

import scala.concurrent.duration._
import net.ceedubs.ficus.Ficus._

object Application {
  def main(args: Array[String]) {
    val system = ActorSystem("Lydie")
    import system.dispatcher

    val storeActor = system.actorOf(Props[Store], "Store")

    val buyers = ConfigFactory.load().as[Option[List[String]]]("reactive.lab2.buyers").
      getOrElse(List("Zbyszek", "Staszek", "Jozek", "Zdzisek", "Mietek", "Franciszek"))

    buyers.map {
      name => system.actorOf(Props(classOf[Buyer], storeActor), name)
    }

    system.scheduler.scheduleOnce(3 minutes)(system.shutdown())
  }
}
