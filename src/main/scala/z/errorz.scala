package z

import zio._

object Errorz:
   
   def failWithMessage(string: String) =
      ZIO.succeed(new Error(string))