package spell.misc

import akka.actor._

case class Player(playerRef: ActorRef, score: Int, ready: Boolean)
