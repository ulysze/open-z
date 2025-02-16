package z

import zio._

/** Ulysse E. O. Forest Execution Logic Language Extension with the Abstraction of Tasks/effects for Computers Systems. procedural
  * and ; was cool... Time for an update
  */
object Wexecutors:
   extension [R, E, A](self: ZIO[R, E, A])
      /** Forks `that` in the background and immediately returns the result of `self`. The fiber launched by `that` is never
        * joined, so it continues running in the background in Global Scope
        */
      infix def <&![R1 <: R, E1 >: E, B](that: => ZIO[R1, E1, B])(using trace: Trace): ZIO[R1, E1, A] =
         for {
            _ <- that.forkDaemon
            a <- self
         } yield a

      /** Forks `that` in the background and immediately returns the result of `self`. The fiber launched by `that` is never
        * joined, so it continues running in the background in Glo
        */
      infix def &!>[R1 <: R, E1 >: E, B](that: => ZIO[R1, E1, B])(using trace: Trace): ZIO[R1, E1, B] =
         for {
            _ <- self.forkDaemon
            b <- that
         } yield b
