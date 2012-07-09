package scalapara

/*
* User: catayl2
* Time: 3:43 PM
*/

abstract class CmdLineArg(val argName:String, val description:String)

case class AppArg (override val argName:String,
                   override val description:String) extends CmdLineArg(argName, description)

case class DefaultArg (override val argName:String, override val description:String, defaultVal:String)
  extends CmdLineArg(argName, description)

case class ParsedArg(paramName:String, paramVal:String) {
  def apply(paramName:CmdLineArg):String = paramVal

  override def toString:String = List(paramName, paramVal).mkString(" ")
}

case class ParsedArgs(nameValList:Array[ParsedArg]) {
  def apply(parsedArgName:CmdLineArg):String = {
    nameValList.find(_.paramName.equals(parsedArgName.argName)).flatMap(pArg => Some(pArg(parsedArgName))).getOrElse(null)
  }

  def size:Int = nameValList.size

  def find(argName:String):Option[ParsedArg] = nameValList.find(_.paramName.equals(argName))

  override def toString:String = nameValList.mkString("\n")
}

abstract class CmdLineApp(val appName:String, val appParams:Array[CmdLineArg]) {

  def usage = "Usage: " + appName + " " + appParams.map(_.argName).mkString(" ")

  def description:String

  def parseAndValidateParamArgs(argsUnpaired: Array[String]): Option[ParsedArgs] = {
    val groupedArgs = argsUnpaired.grouped(2).toList

    def foundCmdLineArg(ps:Array[CmdLineArg], userArg:Array[String]):Boolean = {
      ps.exists{p => p.argName.equals(userArg(0))}
    }

    val parsedArgs:ParsedArgs =
      if(argsUnpaired.contains("help")) {ParsedArgs(Array.empty) }
      else {
        ParsedArgs(groupedArgs.filter(grpd => foundCmdLineArg(appParams, grpd)).map(foundTups =>
          ParsedArg(foundTups(0), foundTups(1))).toArray)
      }

    if (parsedArgs.nameValList.length != appParams.length) {
      val usrParamNames = parsedArgs.nameValList.map(_.paramName)

      val parsedWithDefs = ParsedArgs(parsedArgs.nameValList ++:
        appParams.collect{case defArg:DefaultArg => ParsedArg(defArg.argName, defArg.defaultVal)}.filterNot{pArg =>
        usrParamNames.contains(pArg.paramName)})

      if (parsedWithDefs.size == appParams.size) {
        Some(parsedWithDefs)
      } else {
        println("\n"+usage+"\nParameter Info: ")
        val unspecifiedArgs = appParams.filter(reqArg => parsedArgs(reqArg) == null)

        unspecifiedArgs.collect{case a:AppArg => a}.foreach{invalidArg =>
          println("\n"+invalidArg.argName +" ==> " + invalidArg.description)
        }

        println("\nParameter with Defaults:")
        unspecifiedArgs.collect{case d:DefaultArg => d}.foreach{invalidArg =>
          println("\n"+invalidArg.argName +" ==> " + invalidArg.description + ": DefaultValue="+invalidArg.defaultVal+"\n")
        }

        None
      }
    } else Option(parsedArgs)
  }

  def main(args:Array[String])
}

abstract class CmdLineAppSuite(val multiAppName:String, val cmdLineApps:List[CmdLineApp]) {

  def printInfo:String = {
    List("Usage: task " + cmdLineApps.map(_.appName).mkString(" [ ", " | ", " ] "),
      cmdLineApps.map(a => a.appName + " -- " + a.description).mkString("\n\t", "\n\n\t", "")
    ).mkString("\n", "\n","\n")
  }
}

