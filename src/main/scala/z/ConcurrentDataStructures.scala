package z

import zio._

object ConcurrentDataStructure:
        def refTest = 
                for {
                        ref <- Ref.make(0)
                        _             <- ZIO.foreachPar((1 to 10_000)):
                                                _ =>
                                                        ref.update(_ + 1)
                        result        <- ref.up
                } yield result
                

