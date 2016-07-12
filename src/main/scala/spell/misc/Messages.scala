package spell.misc

import akka.actor._
import java.util.UUID
import org.joda.time._

sealed trait GameMessage
case class ListServers() extends GameMessage
case class ServerList(list:List[ActorRef]) extends GameMessage
case class RequestHost(serverName:String) extends GameMessage
case class RequestJoin(game:ActorRef) extends GameMessage
case class RequestApproved(game:ActorRef) extends GameMessage
case class RequestDenied(game:ActorRef) extends GameMessage
case class Connect(player:ActorRef) extends GameMessage
case class Disconnect(player:ActorRef) extends GameMessage
case class PlayerConnected(player:ActorRef) extends GameMessage
case class PlayerDisconnected(player:ActorRef) extends GameMessage
case class StartGame() extends GameMessage
case class EndGame() extends GameMessage
case class EngagedWord(player: ActorRef, word: GlobalWord) extends GameMessage
case class FinishedWord(player: ActorRef, word: GlobalWord) extends GameMessage
case class Ready(player:ActorRef) extends GameMessage
case class UnReady(player:ActorRef) extends GameMessage

sealed trait GameEntity
case class GlobalWord(id:UUID, text:String) extends GameEntity

sealed trait GameEvent
case class SpawnWord(w:GlobalWord, timestamp:DateTime = DateTime.now) extends GameEvent
case class WordEngaged(player:ActorRef, word:GlobalWord, timestamp:DateTime = DateTime.now) extends GameEvent
case class WordDenied(player:ActorRef, word:GlobalWord, timestamp:DateTime = DateTime.now) extends GameEvent
case class WordWinner(player:ActorRef, word:GlobalWord, timestamp:DateTime = DateTime.now) extends GameEvent
case class GameEnded() extends GameEvent
case class PlayerReady(player: ActorRef, timestamp: DateTime = DateTime.now) extends GameEvent

/*
Client stuff
*/

sealed trait ServerMessage
case class RequestWord(delay: Int) extends ServerMessage
case class WordResponse(word: GlobalWord) extends ServerMessage
case class OutOfWords() extends ServerMessage
