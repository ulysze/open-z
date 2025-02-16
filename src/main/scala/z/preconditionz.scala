package z

/** Concurrently check weither a condition for an arguments is true
  * @param Condition
  * @param Exception
  *   message
  * @return
  *   an effect that will fail if the condition is false and will suceed otherwise.
  */
object Preconditionz:
   import zio._
   def requireZ(cond: Boolean, message: String): IO[IllegalArgumentException, Unit] =
      ZIO.when(!cond)(ZIO.fail(new IllegalArgumentException(message))) *> ZIO.unit
