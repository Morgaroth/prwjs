package io.github.morgaroth.reactive.lab2.AuctionPublisher

import io.github.morgaroth.reactive.lab2.AuctionPublisher.stack.{CoreActors, BootedCore, BindedService, RESTApi}

object AuctionPublisherApp
  extends App
  with BootedCore
  with CoreActors
  with RESTApi
  with BindedService
