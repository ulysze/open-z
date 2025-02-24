package z

import zio._

object Preconditionz:
        /** Concurrently check weither a condition for an arguments is true
          * @param Condition
          * @param Exception message
          * @return an effect that will fail if the condition is false and will suceed otherwise.
          */
        def requireZ(cond: Boolean, message: String): ZIO[Any, IllegalArgumentException, Unit] =
                ZIO.when(!cond)(ZIO.fail(new IllegalArgumentException(message))) *> ZIO.unit

        
        