package spell.server.akka.remote

import java.io.File
import spell.server.akka.misc._
import akka.actor._
import com.typesafe.config.ConfigFactory

class RemoteActor extends Actor {
  var players:List[ActorRef] = List()

  override def receive: Receive = {

    case Disconnect(player) =>
      println(s"#\t$player disconnected")

    case _:Terminated =>
      println(s"#\t$sender disconnected")
      context stop sender

    case Connect(player:ActorRef) =>
      players = player :: players
      val serverName = self.path.name
      if(players.size == 1) {
        context.watch(player)
        val name = player.path.name
        println(s"#\t$name is being watched by $serverName")
      }

      println(s"#\t[$serverName] players: " + players.size)
      players.foreach(p => p ! PlayerConnected(player))
  }
}
