package spell.server.akka.remote

import java.io.File
import akka.actor._
import com.typesafe.config.ConfigFactory
import spell.server.akka.misc._

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
    val remote = context.system.actorOf(Props[RemoteActor], name=servername)
    games = remote :: games
    RequestApproved(remote)
  }
}

object MasterActor{
  def main(args: Array[String]) {
    val configFile = getClass.getClassLoader.getResource("remote_application.conf").getFile
    val config = ConfigFactory.parseFile(new File(configFile))
    val system = ActorSystem("MasterSystem" , config)
    val remote = system.actorOf(Props[MasterActor], name="master")
    println("#\tmaster is ready")
  }
}
