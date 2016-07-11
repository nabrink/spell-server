package spell.server.akka.misc

import akka.actor._

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
