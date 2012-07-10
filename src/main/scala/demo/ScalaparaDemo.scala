package demo

import scalapara._
import scalapara.ParsedArgs
import scala.Some
import scalapara.AppArg

/*
* User: catayl2
* Date: 7/9/12
* Time: 11:55 PM
*/

object AppArgsDB {
  val schedulerName = AppArg("-s","Name designated for the task scheduler / coordinator actor system.")
  val schedulerPort = AppArg("-sp", "The port the Scheduler's actor system will be listening to and to which launchers can bind")
  val poolName = AppArg("-l","Name designated for the task launcher actor system.")
  val numWorkers = DefaultArg("-w", "The number of workers to which an instance of a launcher will route tasks", "3")
  val schedulerHost = DefaultArg("-sh", "The Host Ip Address the Scheduler is running on", "127.0.0.1")
  val poolStop = AppArg("-poolStop", "Stop a specificly named pool '-poolName' or view a menu of current running pools to stop [none, all, or $poolName]")
}

import AppArgsDB._
object CalculatorTaskScheduler extends CmdLineApp("CalculatorTaskScheduler", Array(schedulerName, schedulerHost, schedulerPort)) {
  def schedulerSystemName = appName
  override def description:String = "A CmdLineApp for bringing online a master task scheduler to which pools of workers supervised by launchers will communicate."
  def main(args: Array[String]) {
    parseAndValidateParamArgs(args) foreach{ paramArgs =>
      // do something with the user's parsed arguments
      println(paramArgs)
    }
  }
}

object RemoteCalculatorPoolApp extends CmdLineApp("RemoteCalculaterPoolApp", Array(poolName, numWorkers, schedulerName, schedulerHost, schedulerPort)) {
  override def description:String = "A CmdLineApp for bringing online a distributed pool of workers supervised by a RemoteWorkerPool Actor."
  def main(args: Array[String]) {
    parseAndValidateParamArgs(args) foreach{ appArgs:ParsedArgs =>

    /** Note that parseAndValidateParamArgs returns an Option[ParsedArgs].
     *  The code inside the 'foreach' block only gets executed when the user supplies a valid argument list, otherwise the user will see usage information **/

    val (pool, nWorkers, sName, sHost, sPort) =
      (appArgs(poolName), appArgs(numWorkers).toInt, appArgs(schedulerName), appArgs(schedulerHost), appArgs(schedulerPort).toInt)
      /** Note, the scalapara library does not currently concern itself with capturing the type information in CmdLineArgs.
       * This could easily be addressed in a later version but in practice a little exception handling seems to suffice. **/

      // Now do something meaningful with the user's arguments.
      println(List(pool, nWorkers, sName, sHost, sPort).mkString("\n"))
    }
  }
}


object RemoteApps extends CmdLineAppSuite("RemoteApps", List(CalculatorTaskScheduler, RemoteCalculatorPoolApp)) {
  def main (args:Array[String]) {
    if (args.isEmpty || args(0).equals("help")) println(printInfo)
    else {
      cmdLineApps.find{anApp:CmdLineApp =>  args(0).equals(anApp.appName)} match {
        case None =>
          printInfo
        case Some(choice) =>
          choice.main(args.tail)
      }
    }
  }
}