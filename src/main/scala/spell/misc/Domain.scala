package spell.misc

import akka.actor._
import java.util.UUID

case class Player(playerRef: ActorRef, score: Int, ready: Boolean)
case class PlayerStats(player: ActorRef, score: Int)
case class ServerSettings(host: ActorRef, name:String, maxPlayers:Int)

sealed trait GameEntity
case class GlobalWord(id: UUID, text:String, multiplier:Float) extends GameEntity

class Server(serverActor: ActorRef, players: List[ActorRef], maxPlayers: Int) {
  override def toString(): String = s"${serverActor.path.name}\t${players.size}/${maxPlayers}"
}

object Server {
  def apply(serverActor: ActorRef, players: List[ActorRef], maxPlayers: Int) = {
    new Server(serverActor, players, maxPlayers)
  }
}

sealed trait GameState
case object Lobby extends GameState
case object GameRunning extends GameState
case object GameOver extends GameState

sealed trait GameData
case class LobbyData(players: Map[ActorRef, Player]) extends GameData
case class GameSessionData(players: Map[ActorRef, Player], engagedWords: Map[ActorRef, GlobalWord],
                        words: List[GlobalWord], spawner: ActorRef) extends GameData
case class Stats(players: Map[ActorRef, Player]) extends GameData
