package spell.server.akka.remote

import java.io.File
import spell.misc._
import akka.actor._
import com.typesafe.config.ConfigFactory
import scala.io.Source

class GameServer extends Actor {
  var players:List[ActorRef] = List()
  lazy val dict = readFile("src/main/res/words.txt")

  def readFile(fileName:String):List[String] = {
    var list:List[String] = List()
    for(line <- Source.fromFile(fileName).getLines){
      list = line.toUpperCase() :: list
    }
    list
  }

  override def preStart(): Unit = {
    
  }

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
