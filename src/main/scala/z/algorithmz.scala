package z

import zio._

object Algorithmz:
        def calculateSumSequential(n: Int): UIO[Int] =
                ZIO.succeed((1 to n).foldLeft(0)(_ + _))

        def calculateSumClever(n: Int) = 
                ZIO.succeed((n * (n + 1)) / 2)

        def insertionSort(input: List[Int]) =
                def insert(sorted: List[Int], n: Int): List[Int] = 
                        sorted match
                                case x :: xs => if n <= x then n :: sorted else x :: insert(xs, n)
                                case Nil => List(n)

                input.foldLeft(List.empty[Int])((acc, x) => insert(acc, x))

        def parSearch[A](a: Seq[A], target: A): UIO[Option[Int]] = 
                Queue.bounded[Option[A]](1).map:
                        q => q.take.race:
                                ZIO.foreachPar(a.zipWithIndex)(case (x, i) => if x == target then q.offer(Some(i)) else ZIO.unit)
                                .withParralelismInfinite *> ZIO.succeed(None)  
        
        def linearSearch[A](a: Seq[A], target: A): UIO[Option[Int]] = 
                @tailrec
                def indexedSearch(za: List[(A, Int)]): UIO[Option[Int]] = 
                        za match
                                case Nil => ZIO.succeed(None)
                                case (x, i) :: zs => if x == target then ZIO.succeed(Some(i)) else indexedSearch(zs)

                indexedSearch(a.zipWithIndex.toList)
                 



        

                                               

