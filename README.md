# Scalapara

Scalapara is library for building cmd-line app-suites and for parsing their arguments into strings.  It is implemented as a collection of abstract classes that can be used to extend scala objects containing a main method.  

## The available abstract classes are:
* CmdLineApp - The constructor for the base class provides the derived class a way to name the CmdLineApp and establish a list of CmdLineArgs.
* CmdLineArg - This abstract base class is extended by (currently) two case classes (AppArg and DefaultArg).
* CmdLineAppSuite - This provides a way to compose a Suite of command line apps (like svn or git).
** ```CmdLineAppSuite(val multiAppName:String, val cmdLineApps:List[CmdLineApp])```


## How to get to input arg values
For an app suite with potentially reusable app-args, I typically define my arguments in an AppArgsDB object:

	object AppArgsDB {
	  val schedulerName = AppArg("-s","Name designated for the task scheduler / coordinator actor system.")
	  val schedulerPort = AppArg("-sp", "The port the Scheduler's actor system will be listening to and to which launchers can bind")
	  val poolName = AppArg("-l","Name designated for the task launcher actor system.")
	  val numWorkers = DefaultArg("-w", "The number of workers to which an instance of a launcher will route tasks", "3")
	  val schedulerHost = DefaultArg("-sh", "The Host Ip Address the Scheduler is running on", "127.0.0.1")
	  val poolStop = AppArg("-poolStop", "Stop a specificly named pool '-poolName' or view a menu of current running pools to stop [none, all, or $poolName]")
	}

	object AddSubtractRemoteWorkerPoolApp extends CmdLineApp("AddSubtractRemoteWorkerPoolApp", Array(poolName, numWorkers, schedulerName, schedulerHost, schedulerPort)) {

  	override def description:String = appName + " -- An CmdLineApp for bringing online a distributed pool of workers supervised by a RemoteWorkerPool Actor."

  	def main(args: Array[String]) {
	    parseAndValidateParamArgs(args) foreach{ appArgs =>
   	 	println(appArgs(numWorkers)) // Prints what the user passed in for the -numWorkers option
            }
  	}
       }

The ```parseAndValidParamArgs(args)``` is inherited from the CmdLineApp base class, and it gives back a container ```ParsedArgs``` as long as all required arguments are set by the user.  The values can be extracted by using the apply method of the ParsedArgs reference.



