package z

import zio._
import z.Preconditionz._
import scala.collection.immutable.Queue
import z.Counter.CounterService
import scala.util.Try
import z.Counter.SimpleCounter

/** 
 * Et permet surtout de pouvoir effectuer la construction de SimpleCounter en parralèle sur des fibres possiblement ! (en plus de l'aspect fonctionel  * pure, erreur channel, etc!) -> If the construction of the Counter fail, -> erroe channel handled By FLATMAPS, etc!! + Substitution Model, etc -> The * ONLY way to model, either Stateful or non stateful Data-Structures! -> If State -> Internal + Clear!!!! -> Permet l'ecriture d'un code *toujours pur * car tout est utilisé dans le même Contexte de fonctions, etc! -> C'est ici que nous Utilisons les Instances d'un certain Type,etc. ============>    * Bien sur, possible ensuite de Combiner les Data-Structures (en appelant d'autres effets dans le constructeur, etc -> TOUT EST PURE ET CONCURRENT!)
 * Note : Recursions are authorized and encouraged. But they Must be tailrec.
 * We recommand also using assertions for invariant in the functinons, ensuring, theorem based proving (stainless), etc! -> Ultra-safe Code. Synchronized Ref block the retries as we perform effects ! = Effectful Transaction... -> SUPER POWERFUL! (allows to perform atomic side effectful operations because NO retries!)*/

object LeetCode:
        def twoSum(nums: Array[Int], target: Int) =
                nums.zipWithIndex.foldLeft(Map.empty[Int, Int], List.empty[(Int, Int)]):
                        case ((complements, result), (value, index)) =>
                                complements.get(target - value) match 
                                        case Some(complementIndex) => (complements,(complementIndex, index) :: result)
                                        case None => (complements + (value -> index),result)
                        ._2.reverse.map(tuple => List(tuple._1, tuple._2)).flatten.toArray
        
        def addTwoNumbers(l1: List[Int], l2: List[Int]) =
                l1.zip(l2).reverse.foldLeft((List.empty[Int], Int)):
                        case ((result, carry),(x, y)) =>
                                val sum = x + y
                                (sum % 10, if sum >= 10 then 1 else 0)
                ._1.reverse

object Counter:
        trait CounterService extends Counter:
                def increment: UIO[Int]
                def decrement: UIO[Int]
                def get: UIO[Int]
                def reset: UIO[Int]

        private case class SimpleCounter private(start: Long, ref: Ref[Int]) extends CounterService:
                def increment: UIO[Int] = ref.incrementAndGet
                def decrement: UIO[Int] = ref.decrementAndGet
                def get: UIO[Int] = ref.get
                def reset: UIO[Int] = ref.updateAndGet(_ => start)
        
        object SimpleCounter:
                def make(start: Int): IO[IllegalArgumentException, SimpleCounter] =
                        requireZ(start > 0, "Start value must be strictly positive") *> Ref.make(start).map(_ => SimpleCounter(start, _))

object BoundedQueue:
        trait BoundedQueueService[A] extends BoundedQueue:
                def enqueue(a: A): UIO[Boolean]
                def dequeue: UIO[Option[A]]
                def size: UIO[Int]
                def capacity: UIO[Int]

        case class SimpleBoundedQueue[A] private(size: Int, ref: Ref[Queue[A]]) extends BoundedQueueService[A]:
                def isFull(q: Queue[A]): Boolean = q.size == size
                def enqueue(a: A): UIO[Boolean] = 
                        ref.modify:
                                q =>
                                        (isFull(q), if !isFull(q) then q.enqueue(a) else q)

                def dequeue: UIO[Option[A]] = 
                        ref.modify:
                                q => 
                                        (isFull(q), if !isFull(q) then Some(q.dequeue._1) else None)

                def size: UIO[Int] = ref.get.map(q => q.size)
                def capacity: UIO[Int] = ref.get.map(q => size - q.size)

        object SimpleBoundedQueue:
                def make[A](size: Int): Task[IllegalArgumentException, SimpleBoundedQueue] =
                        requireZ(size > 0, "Size must be strictly positive") *> Ref.make(Queue.empty[A]).map(_ => SimpleBoundedQueue(size, _))

object CounterManager:
        type CounterId = Int
        trait CounterManagerService:
                def increment(id: CounterId): UIO[Int]
                def decrement(id: CounterId): UIO[Int]
                def get(id: CounterId): UIO[Int]
                def reset(id: CounterId): UIO[Unit]
                def remove(id: CounterId): UIO[Unit]
        
        case class SimpleCounterManager private(countersRef: Ref.Synchronized[IndexedSeq[CounterService]]) extends CounterManagerService:
                // show how powerful currying is!
                private def actionOnSpecificCounter(op: CounterService => => UIO[Int | Unit]): UIO[Int | Unit] = 
                        countersRef.modifyZIO:
                                counters => 
                                        if counters.isDefinedAt(id) then op(counters(id)) <*> ZIO.succeed(counters)
                                        else ZIO.succeed(0) <*> SimpleCounter.make(0).catchAll(e => ()).map:
                                                counter => counters ++ counter

                def increment(id: CounterId) = actionOnSpecificCounter(_.increment)
                def decrement(id: CounterId) = actionOnSpecificCounter(_.decrement)
                def get(id: CounterId) = actionOnSpecificCounter(_.get)
                def reset(id: CounterId) = actionOnSpecificCounter(_.reset)  
                def remove(id: CounterId) = 
                        countersRef.modifyZIO:
                                counters => 
                                        if counters.isDefinedAt(id) then op(counters(id)) <*> ZIO.succeed(counters.remove(id))
                                        else ZIO.succeed(0) <*> SimpleCounter.make(0).catchAll(e => ()).map:
                                                counter => 
                                                        counters ++ counter
                                                        
        object SimpleCounterManager:
                def make(counters: IndexedSeq[CounterService]): IO[IllegalArgumentException, SimpleCounterManager] = 
                        for {
                                _ <- requireZ(!counters.isEmpty, "Should provide at least One counter!")
                                ref <- Ref.Synchronized.make(counters)
                        } yield SimpleCounterManager(ref)
                

                
                               

                

        







