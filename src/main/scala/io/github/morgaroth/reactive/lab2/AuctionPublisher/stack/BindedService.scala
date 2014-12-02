package io.github.morgaroth.reactive.lab2.AuctionPublisher.stack

import akka.io.IO
import com.typesafe.config.ConfigFactory
import spray.can.Http

trait BindedService extends ApplicationConfigPath {
  this: RESTApi with CoreActors with Core =>

  val port = {
    val conf = ConfigFactory.load()
    conf.getInt(pathOf("port"))
  }

  IO(Http)(system) ! Http.Bind(rootService, "0.0.0.0", port = port)
}