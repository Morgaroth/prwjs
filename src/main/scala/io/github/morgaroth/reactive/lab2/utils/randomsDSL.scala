package io.github.morgaroth.reactive.lab2.utils

import scala.util.Random

trait randomsDSL {
  implicit def wrapToRandomable(elem: (Int, Int)) = new {
    def random() = Random.nextInt(elem._2 - elem._1) + elem._1
  }

  implicit def wrapIntoRandomable(elem: Int) = new {
    def ~(end: Int) = elem -> end
  }

}
