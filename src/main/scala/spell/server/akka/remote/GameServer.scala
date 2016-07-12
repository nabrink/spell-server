package spell.server.akka.remote

import java.io.File
import java.util.UUID
import spell.misc._
import akka.actor._
import com.typesafe.config.ConfigFactory
import scala.io.Source

class GameServer extends Actor {
  val spawnInterval: Int = 1000
  var players: List[ActorRef] = List()
  lazy val dict = readFile("src/main/res/words.txt")
  var words: List[GlobalWord] = List()
  var gameStarted: Boolean = false

  def readFile(fileName:String):List[String] = {
    var list: List[String] = List()
    for(line <- Source.fromFile(fileName).getLines){
      list = line.toUpperCase() :: list
    }
    list
  }

  def isUnique(s:String, list:List[GlobalWord]):Boolean = list match {
    case x::xs if x.text(0) equals s(0) => false
    case x::xs => isUnique(s, xs)
    case _ => true
  }

  def canSpawnWord():Boolean = {
    !dict.filter(w => isUnique(w, words)).isEmpty
  }

  def getRandomWord(): GlobalWord = {
    val list:List[String] = dict.filter(w => isUnique(w, words))
    val random = scala.util.Random
    if(list.isEmpty) null else GlobalWord(UUID.randomUUID(), list(random.nextInt(list.size)))
  }

  override def receive: Receive = {
    case StartGame =>
      gameStarted = true
      gameLoop()

    case EndGame =>
      gameStarted = false

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

  def gameLoop(): Unit = {
    while(gameStarted) {
      Thread.sleep(spawnInterval)
      if (canSpawnWord) {
        val word = getRandomWord()
        words = word :: words
        broadcastWord(word)
      }
    }
  }

  def broadcastWord(word: GlobalWord): Unit = {
    players.foreach(p => p ! SpawnWord(word))
  }
}
