package z

import zio._

/** Ulysse E. O. Forest Execution Logic Language Extension with the Abstraction of Tasks/effects for Computers Systems. procedural and ; was cool... Time for
  * an update
  */
object Wexecutors:
        extension [R, E, A](self: ZIO[R, E, A])
                /** Forks `that` in the background and immediately returns the result of `self`. The fiber launched by `that` is never joined, so it
                  * continues running in the background in Global Scope
                  */
                infix def <&![R1 <: R, E1 >: E, B](that: => ZIO[R1, E1, B])(using trace: Trace) =
                        for {
                                _ <- that.forkDaemon
                                a <- self
                        } yield a

                /** Forks `that` in the background and immediately returns the result of `self`. The fiber launched by `that` is never joined, so it
                  * continues running in the background in Glo
                  */
                infix def &!>[R1 <: R, E1 >: E, B](that: => ZIO[R1, E1, B])(using trace: Trace): ZIO[R1, E1, B] =
                        for {
                                _ <- self.forkDaemon
                                b <- that
                        } yield b

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

                // nombre d'argument variables mais avec un certain type restreint.
                // agregate s'implémente possiblement avec une Queue qui divise le travail, ensuite on commence à combiner, etc.
                // -> Et permettre aussi de regler les paramètre en focntion de la Taille des Fonctions appliués ->
                // Détermine la taille des divisions pour les SeqOp. parce que la dernière combOp est séquentielle.
                // -> Naive, mais peut-être rapide -> Toutes le Seq sont des subdivisions de 2. !
                // -> Pas optimal si lourde charge de travail...
                // CELA EST Vraiment un pattern très général.
                // Même si on veut output une autre collection !
                // Et c'est concurrent car commence à combiner même si les seqOp ne sont pas fini, etc -> Utilise Queue, etc.
                // Implémentation assez avancée.
                // Et on veut pouvoir controler, pour chaque fonction, la taille des subdivisions -> Par défaut 2. -> Vraiment FULL
                // PARALLEL AVEC LES FIBRES.

                // Car fold En parralèle est Obligé de garder le même TYPE!
                // TODO: ZIO.aggregate(Seq[ZIO])(seqOp, combOp), ZIO.reduceAllPar, etc.
                // seqOp eq reduce. et ensuite comOp -> Assez complexe sur les types mais SUPER UTILE!
                // We reason in term of ITERABLE ZIO.
                // ZIO.fold Also , ZIO.foldLeft, foldRight, etc. -> Super Important quand on travail avec des types.

                // reduce -> fold can change Types -> aggregate allow to fold in parralel + Change types 2 times also..
                // fold can be used with collectAll.
