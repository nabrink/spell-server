package spell.misc

import akka.actor._

case class Player(playerRef: ActorRef, score: Int, ready: Boolean)

sealed trait GameState
case class Started() extends GameState
case class Lobby() extends GameState
