package io.github.morgaroth.reactive.lab2.AuctionPublisher.stack

trait ApplicationConfigPath {
  val pathPrefix = "reactive.lab6.auction-publisher"
  def pathOf(postfix: String) = s"$pathPrefix.$postfix"
}
