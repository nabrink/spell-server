package spell.misc

import akka.actor._
import java.util.UUID
import org.joda.time._

sealed trait GameEvent
case class SpawnWord(w:GlobalWord, timestamp:DateTime = DateTime.now) extends GameEvent
case class WordEngaged(player:ActorRef, word:GlobalWord, timestamp:DateTime = DateTime.now) extends GameEvent
case class WordDenied(player:ActorRef, word:GlobalWord, timestamp:DateTime = DateTime.now) extends GameEvent
case class WordWinner(player:ActorRef, word:GlobalWord, timestamp:DateTime = DateTime.now) extends GameEvent
case class GameStarted() extends GameEvent
case class GameEnded() extends GameEvent
case class PlayerReady(player:ActorRef, timestamp:DateTime = DateTime.now) extends GameEvent
case class PlayerUnready(player:ActorRef, timestamp:DateTime = DateTime.now) extends GameEvent
case class ScoreUpdated(player:ActorRef, score:Int, timestamp:DateTime = DateTime.now) extends GameEvent
case class PlayerConnected(player:ActorRef) extends GameEvent
case class PlayerDisconnected(player:ActorRef) extends GameEvent
case class PlayerList(players:List[ActorRef]) extends GameEvent
case class ConnectionGranted(message: String) extends GameEvent
case class ConnectionRefused(reason: String) extends GameEvent
case class ServerList(servers:List[ActorRef]) extends GameEvent
case class StatsList(list:List[PlayerStats]) extends GameEvent
case class ServerShutdown(reason: String) extends GameEvent
case class MessageReceived(player: ActorRef, message: String) extends GameEvent
case class ServerStatus(server: ActorRef, players: List[ActorRef], status: String, maxPlayers: Int) extends GameEvent
