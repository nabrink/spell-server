package spell.misc

import akka.actor._

case class Player(playerRef: ActorRef, score: Int, ready: Boolean)
case class PlayerStats(player: ActorRef, score: Int)
case class ServerSettings(host: ActorRef, name:String, maxPlayers:Int)

sealed trait GameState
case object Lobby extends GameState
case object GameRunning extends GameState
case object GameOver extends GameState


sealed trait GameData
case class LobbyData(players: Map[ActorRef, Player]) extends GameData
case class GameSessionData(players: Map[ActorRef, Player], engagedWords: Map[ActorRef, GlobalWord],
                        words: List[GlobalWord], spawner: ActorRef) extends GameData
case class Stats(players: Map[ActorRef, Player]) extends GameData
