package spell.misc

import akka.actor._
import java.util.UUID

sealed trait GameMessage
case class ListServers() extends GameMessage
case class RequestHost(settings:ServerSettings) extends GameMessage
case class RequestJoin(game:ActorRef) extends GameMessage
case class RequestApproved(game:ActorRef) extends GameMessage
case class RequestDenied(game:ActorRef) extends GameMessage
case class Connect(player:ActorRef) extends GameMessage
case class Disconnect(player:ActorRef) extends GameMessage
case class StartGame() extends GameMessage
case class EndGame() extends GameMessage
case class EngagedWord(player:ActorRef, word:GlobalWord) extends GameMessage
case class FinishedWord(player:ActorRef, word:GlobalWord, keystrokes:Int) extends GameMessage
case class Ready(player:ActorRef) extends GameMessage
case class UnReady(player:ActorRef) extends GameMessage
case class GetStats() extends GameMessage
case class SendMessage(player: ActorRef, message: String) extends GameMessage
case class GetServerStatus() extends GameMessage
