package spell.misc

import akka.actor._

sealed trait GameMessage extends Serializable

@SerialVersionUID(100L)
case class ListServers() extends GameMessage

@SerialVersionUID(101L)
case class ServerList(list:List[ActorRef]) extends GameMessage

@SerialVersionUID(102L)
case class RequestHost(serverName:String) extends GameMessage

@SerialVersionUID(103L)
case class RequestJoin(game:ActorRef) extends GameMessage

@SerialVersionUID(104L)
case class RequestApproved(game:ActorRef) extends GameMessage

@SerialVersionUID(105L)
case class RequestDenied(game:ActorRef) extends GameMessage

@SerialVersionUID(106L)
case class Connect(player:ActorRef) extends GameMessage

@SerialVersionUID(107L)
case class Disconnect(player:ActorRef) extends GameMessage

@SerialVersionUID(108L)
case class PlayerConnected(player:ActorRef) extends GameMessage

@SerialVersionUID(109L)
case class PlayerDisconnected(player:ActorRef) extends GameMessage
