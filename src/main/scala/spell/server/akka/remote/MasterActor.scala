package spell.server.akka.remote

import java.io.File
import akka.actor._
import com.typesafe.config.ConfigFactory
import spell.misc._

class MasterActor extends Actor {
  var games:List[ActorRef] = List()

  override def receive: Receive = {
    case _:Terminated =>
      println(s"#\t$sender disconnected")
      context stop sender

    case ListServers() =>
      println(s"#\tgames open: " + games.size)
      sender ! ServerList(games)

    case RequestJoin(game) =>
      val name = sender.path.name
      println(s"#\tplayer $name APPROVED to join " + game.path.name)
      sender ! RequestApproved(game)

    case RequestHost(servername) =>
    val name = sender.path.name
      println(s"#\t[$name] creating game...")
      sender ! createGame(servername)
  }

  def createGame(servername:String):GameMessage = {
    val game = context.system.actorOf(Props[GameServer], name=servername)
    games = game :: games
    RequestApproved(game)
  }
}

object MasterActor{
  def main(args: Array[String]) {
    val config = ConfigFactory.load()
    val system = ActorSystem("MasterSystem" , config)
    val game = system.actorOf(Props[GameServer], name="master")
    println("#\tmaster is ready")
  }
}
