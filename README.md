# Scalapara

Scalapara is a library for building cmd-line app-suites and for parsing their arguments.  It is implemented as a collection of abstract classes that can be used to extend scala objects containing a main method.  I was motivated to write it because I found myself writing a bunch of CmdLine app "suites" similar in spirit to __git__ and __svn__.  I wanted a way to help users understand the use-cases for the variety of sub-tasks in a suite of apps, yet also receive help on the various parameters available for a given individual subApp. 

## The Scalapara library let's an command line application developer build a hierarchal model of an appsuite:
* I typically write a simple shell script that forwards its arguments to the top level app suite. (e.g. 'git').  If the user supplies no additional arguments, the list of apps and their descriptions are displayed.
* If the user supplies a recognized __CmdLineApp__ name as the first arg to the shell script and nothing else, the list of arguments and their descriptions will be displayed for the chosen app (e.g. 'git add' would show options available for the git add command).
* If the user supplies an appName, some but not all required arguments, the missing requirements and their descriptions will be displayed for the user.

## Sample demo app.
For an app suite with potentially reusable app arguments, I typically define my arguments in an __AppArgsDB__ scala object of sorts:

	object AppArgsDB {
		val schedulerName = AppArg("-s","Name designated for the task scheduler / coordinator actor system.")
		val schedulerPort = AppArg("-sp", "The port the Scheduler's actor system will be listening to and to which launchers can bind")
		val poolName = AppArg("-l","Name designated for the task launcher actor system.")
		val numWorkers = DefaultArg("-w", "The number of workers to which an instance of a launcher will route tasks", "3")
		val schedulerHost = DefaultArg("-sh", "The Host Ip Address the Scheduler is running on", "127.0.0.1")
		val poolStop = AppArg("-poolStop", "Stop a specificly named pool '-poolName' or view a menu of current running pools to stop [none, all, or $poolName]")
	}

With the arguments we anticipate needing, we can proceed to very simply create a couple of __CmdLineApps__ which reference those __CmdLineArgs__.

	object CalculatorTaskScheduler extends CmdLineApp("CalculatorTaskScheduler", Array(schedulerName, schedulerHost, schedulerPort)) {
		def schedulerSystemName = appName
		override def description:String = appName + " -- An executable for bringing online a master task scheduler to which pools of workers supervised by launchers will communicate."
		def main(args: Array[String]) {
			parseAndValidateParamArgs(args) foreach{ paramArgs =>
				// do something with the user's parsed arguments
			}
		}
	}

	object RemoteCalculatorPoolApp extends CmdLineApp("RemoteCalculaterPoolApp", Array(poolName, numWorkers, schedulerName, schedulerHost, schedulerPort)) {
  		override def description:String = appName + " -- An CmdLineApp for bringing online a distributed pool of workers supervised by a RemoteWorkerPool Actor."
		def main(args: Array[String]) {
			parseAndValidateParamArgs(args) foreach{ appArgs:ParsedArgs => 
				
				/** Note that parseAndValidateParamArgs returns an Option[ParsedArgs].  
				 *  The code inside the 'foreach' block only gets executed when the user supplies a valid argument list, otherwise the user will see usage information **/
				
				val (pool, nWorkers, sName, sHost, sPort) = (appArgs(poolName), appArgs(numWorkers).toInt, appArgs(schedulerName), appArgs(schedulerHost), appArgs(schedulerPort).toInt)
				
				/** Note, the scalapara library does not currently concern itself with capturing the type information in CmdLineArgs.  
				  * This could easily be addressed in a later version but in practice a little exception handling seems to suffice. **/
				
				// Now do something meaningful with the user's arguments.
			}
		}
	}

The ```parseAndValidParamArgs(args)``` is inherited from the CmdLineApp base class, and it returns an ```Option[ParsedArgs]``` as long as all required arguments are set by the user.  The values can be extracted by using the apply method of the ParsedArgs reference.

## We have thus far created: 
1. A list of reusable arguments and their descriptions.
2. A CmdLineApp theoretically allowing us to start a CalculatorTaskScheduler.
3. Another CmdLineApp theoretically useful for binding to the scheduler and telling it that a pool of 'CalculatorWorkers' are ready for business.

## We could now offer this up as an application suite by simply doing the following: 
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

All that remains is to define a user-friendly shell script:
## ./cluster-admin.sh 
	#!/bin/bash
	java $JAVA_OPTS -cp ./:akka-calculator-example.jar sample.remote.RemoteApps $1 $2 $3 $4 $5 $6 $7 $8 $9 ${10} ${11} ${12}

%> ./cluster-admin

	Usage: cluster-admin  [ CalculatorTaskScheduler | RemoteCalculatorPoolApp ] 

	CalculatorTaskScheduler -- An executable for bringing online a master task scheduler to which pools of workers supervised by launchers will communicate.

	RemoteCalculatorPoolApp -- An CmdLineApp for bringing online a distributed pool of workers supervised by a RemoteWorkerPool Actor.

----  OR  ----

%> ./cluster-admin RemoteCalculatorPoolApp 

	Usage: CalculatorTaskScheduler -s -sh -sp

	Parameter Info: 

		-s ==> Name designated for the task scheduler / coordinator actor system.

		-sp ==> The port the Scheduler's actor system will be listening to and to which launchers can bind

	Parameter with Defaults:

		-sh ==> The Host Ip Address the Scheduler is running on: DefaultValue=127.0.0.1

----  OR EVEN ----

%> ./cluster-admin RemoteCalculatorPoolApp -s Scheduler_001

	Usage: CalculatorTaskScheduler -s -sh -sp

	Parameter Info: 

		-sp ==> The port the Scheduler's actor system will be listening to and to which launchers can bind

	Parameter with Defaults:

		-sh ==> The Host Ip Address the Scheduler is running on: DefaultValue=127.0.0.1


Notice how '-s' no longer appears in the ```Parameter Info```.  This is because the user has supplied a valid value for that argument.  ```Scalapara``` will keep printing info about which options/params the user hasn't properly supplied until the user properly supplies them.  When that moment occurs, the control flow will fall into the code block within the ```parseAndValidateParamArgs(args) foreach{ appArgs:ParsedArgs =>``` statement.  This assures the user that all required arguments have been set and so the application may proceed with initialization.

