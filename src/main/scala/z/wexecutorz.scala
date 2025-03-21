package z

import zio._

/** Ulysse E. O. Forest Execution Logic Language Extension with the Abstraction of Tasks/effects for Computers Systems. procedural and ; was cool... Time for an update
  */
object Wexecutors:
        extension [R, E, A](self: ZIO[R, E, A])
                /** Forks `that` in the background and immediately returns the result of `self`. The fiber launched by `that` is never joined, so it continues running in the background in Global Scope
                  */
                infix def <&![R1 <: R, E1 >: E, B](that: => ZIO[R1, E1, B])(using trace: Trace): ZIO[R1, E1, B] = that.forkDaemon *> self
                

                /** Forks `that` in the background and immediately returns the result of `self`. The fiber launched by `that` is never joined, so it continues running in the background 
                  */
                infix def &!>[R1 <: R, E1 >: E, B](that: => ZIO[R1, E1, B])(using trace: Trace): ZIO[R1, E1, B] = that.forkDaemon *> that
                

        def collectAllParResults[R, E, A](in: Iterable[ZIO[R, E, A]]) =
                ZIO.partitionPar(in)(identity)

                // Permettre une MapReduce. -> Mais c'est un cas particulier..
                // Fournir un controle sur les GROUPES, pour les Operations de chaque Layer, etc!
                // (la Map est surtout pour faire les Groupes) = Subdivisions utilisées ensuite, etc.

                def simple2DivAggregate[R1 <: R, E1 >: E, A1 >: A](i: Iterable[ZIO[R, E, A]])(nonAsscBinOp: (ZIO[R, E, A], ZIO[R, E, A]) => ZIO[R1, E1, A1])(
                        asscBinOp: (ZIO[R1, E1, A1], ZIO[R1, E1, A1]) => ZIO[R1, E1, A1]
                ) =

                        ?? // nonAsscBinOp is Concurrent (maximum). -> Done with subdivisions of size 2.
                        // then, asscBinOp is done fully concurrent for the different elements. -> Recursive Concurrent Max.
                        ???

                def aggregate = ???
                // taille des subdivions = 2. -> Full parralel avec les fibres.

