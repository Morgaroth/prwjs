package io.github.morgaroth.reactive.lab2.actors.auction.utils

import scala.util.Random

trait LoremIpsum {
  val lorem = ("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur efficitur nisl ipsum, eget rutrum turpis congue non. Etiam sed neque hendrerit lorem lacinia eleifend eu id purus. Vestibulum consectetur hendrerit dolor, sit amet auctor dolor gravida eu. Sed et suscipit risus, id elementum erat. Aliquam gravida accumsan ipsum sed commodo. Duis ut arcu aliquet, tincidunt velit vel, viverra arcu. Nullam vitae sagittis nulla. Nullam non placerat mi, ut congue sem. Maecenas tempor fermentum eros, ut euismod ipsum vestibulum eget. Morbi dapibus risus tortor, vel tincidunt nibh cursus at. Maecenas dictum odio ac nibh tempus, a mollis metus interdum. Nunc ultricies sollicitudin nibh ut tristique. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Ut lobortis nisl at vulputate faucibus. Nunc ac massa nulla. Nam vel neque sed ex mollis tempor et a ante." +
    "Aliquam ultrices nec tortor ac tempus.Fusce vehicula nisi id bibendum condimentum.Lorem ipsum dolor sit amet, consectetur adipiscing elit.Praesent eget elit sit amet nunc posuere auctor ac eget erat.Maecenas nulla nulla, aliquet eu turpis nec, malesuada suscipit libero.Sed nunc metus, tincidunt eu luctus in, imperdiet non tellus.Nunc tempus vulputate ligula et rhoncus.In viverra tellus at diam posuere.").split(' ').toList

  def loremIpsumSentences(wordsInOne: Int, count: Int): List[String] = {
    List.fill(count) {
      Random.shuffle(lorem).take(wordsInOne).mkString(" ")
    }
  }

  def loremIpsumWord: String = lorem(Random.nextInt(lorem.size - 1))
}
