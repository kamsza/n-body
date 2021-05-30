import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import scala.collection.mutable
import com.github.tototoshi.csv._
import java.io._
import scala.collection.mutable.ListBuffer

class Simulation(bodiesCount: Int) extends Actor {
  var counter: Int = 0
  var startTime: Long = 0
  var endTime: Long = 0
  var step: Int = 0
  var bodies: mutable.Map[Int, Data] = mutable.Map()
  var positions = new ListBuffer[List[Double]]()

  val f = new File("result.csv")
  val writer = CSVWriter.open(f)

  def receive: Receive = {
    case StartSimulation => {
      context.system.actorSelection("/user/*") ! StartSimulation(context.self)
    }

    case Data(id, msgId, mass, position, velocity) => {
      bodies += (id -> Data(id, msgId, mass, position, velocity))
      if(this.bodies.size == bodiesCount) {
        if(msgId % 1000 == 0) {
          print("+")
        }
        if(msgId % 10000 == 0) {
          println(msgId / 10000 + " ")
        }

        this.bodies.values.foreach(b => positions += List(b.id, b.msgId, b.position.x, b.position.y, b.velocity.x, b.velocity.y))
        this.bodies.clear()
      }
    }

    case "finished" => {
      counter += 1
      if(counter == bodiesCount) {
        println("finished")
        writer.writeAll(this.positions.toList)
        writer.close()
        context.system.terminate()
      }
    }
  }
}
