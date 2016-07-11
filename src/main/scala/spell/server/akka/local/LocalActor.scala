package spell.server.akka.local

import java.io.File
import akka.actor.{Props, Actor, ActorSystem, ActorRef, FSM}
import com.typesafe.config.ConfigFactory
import spell.server.akka.misc._

/**
 * Local actor which listens on any free port
 */
class LocalActor(username:String) extends Actor {
  val ip = sys.env("SPELL_SERVER_EXT_IP")
  val port = sys.env("SPELL_SERVER_EXT_PORT")
  val master = context.actorSelection(s"akka.tcp://MasterSystem@$ip:$port/user/master")

  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    println(s"#\tremote is at: $ip:$port")

    print(s"#\tEnter mode (host/join): ")
    val mode = scala.io.StdIn.readLine()
    mode match {
      case "join" =>
        master ! ListServers()
      case "host" =>
        print(s"#\tEnter SERVER name: ")
        val game = scala.io.StdIn.readLine()
        println(s"#\t$username REQUESTS to host game: $game")
        master ! RequestHost(game)
      case _ => context stop self
    }
  }

  override def receive: Receive = {
    case PlayerConnected(player:ActorRef) =>
      val name = player.path.name
      println(s"#\t$name JOINED game " + sender.path.name)

    case RequestApproved(game) =>
      val gameActor = context.actorSelection(s"akka.tcp://MasterSystem@$ip:$port/user/"+game.path.name)
      println(s"#\trequest approved by " + game.path.name)
      gameActor ! Connect(self)

    case ServerList(list) =>
      println("#\tAvailable servers:".format(list.size))
      list.foreach(game => println(s"#\t\t~" + game.path.name))

      print(s"#\tSelect game(0-9): ")
      val index = scala.io.StdIn.readLine()
      val game = list(index.toInt)
      println(s"#\t$username REQUESTS to join " + game.path.name)
      sender ! RequestJoin(game)
      println("#\tjoining %s".format(game.path.name))
    }
  }

object LocalActor {
  def main(args: Array[String]) {
    val r = scala.util.Random
    val n = r.nextInt(1000)
    val configFile = getClass.getClassLoader.getResource("local_application.conf").getFile
    val config = ConfigFactory.parseFile(new File(configFile))
    val system = ActorSystem("ClientSystem",config)
    print(s"#\tEnter user name: ")
    val username = scala.io.StdIn.readLine()
    val localActor = system.actorOf(Props(classOf[LocalActor], username),  name=username)
  }
}
