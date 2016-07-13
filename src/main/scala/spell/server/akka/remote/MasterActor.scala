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

    case ShutdownServer(server: ActorRef) =>
      println(s"#\tmaster: Shutting down server ${server.path.name}")
      server ! PoisonPill
      games = games.filter(_ != server)

    case ListServers() =>
      println(s"#\tgames open: " + games.size)
      sender ! ServerList(games)

    case RequestHost(settings) =>
      println(s"#\t[${settings.name}] creating game...")
      sender ! createGame(settings)
  }

  def createGame(settings: ServerSettings):GameMessage = {
    val game = context.system.actorOf(Props(classOf[GameServer], settings, self), name=settings.name)
    games = game :: games
    RequestApproved(game)
  }
}

object MasterActor{
  def main(args: Array[String]) {
    val config = ConfigFactory.load()
    val system = ActorSystem("MasterSystem" , config)
    val game = system.actorOf(Props[MasterActor], name="master")
    println("#\tmaster is ready")
  }
}
