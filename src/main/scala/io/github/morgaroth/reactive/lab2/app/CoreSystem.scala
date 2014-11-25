package io.github.morgaroth.reactive.lab2.app

import akka.actor.ActorSystem

trait CoreSystem {
  implicit val system: ActorSystem
}

trait AppCoreSystem extends CoreSystem {
  override implicit val system = ActorSystem("Lydie")
}