package z

import zio._
import z.Preconditionz._
import scala.collection.immutable.Queue
import z.Counter.CounterService
import scala.util.Try
import z.Counter.SimpleCounter


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
        /** Unique identifier for a counter. */
        type CounterId = Int

        /**
         * Service for managing counters.
         */
        trait CounterManagerService:
                /**
                 * Increments the value of the counter identified by `id`.
                 * @param id Identifier of the counter.
                 * @return The new value of the counter after incrementing.
                 */
                def increment(id: CounterId): UIO[Int]

                /**
                 * Decrements the value of the counter identified by `id`.
                 * @param id Identifier of the counter.
                 * @return The new value of the counter after decrementing.
                 */
                def decrement(id: CounterId): UIO[Int]

                /**
                 * Retrieves the current value of the counter identified by `id`.
                 * @param id Identifier of the counter.
                 * @return The current value of the counter.
                 */
                def get(id: CounterId): UIO[Int]

                /**
                 * Resets the counter identified by `id` to zero.
                 * @param id Identifier of the counter.
                 */
                def reset(id: CounterId): UIO[Unit]

                /**
                 * Removes the counter identified by `id`.
                 * @param id Identifier of the counter.
                 */
                def remove(id: CounterId): UIO[Unit]

        /**
         * Simple implementation of CounterManager service.
         * @param countersRef Synchronized reference to an indexed sequence of counter services.
         */
        case class SimpleCounterManager private(countersRef: Ref.Synchronized[IndexedSeq[CounterService]]) extends CounterManagerService:
                /** Performs an operation on a specific counter based on the provided identifier. */
                private def actionOnSpecificCounter(op: CounterService => UIO[Int]) = 
                        countersRef.modifyZIO:
                                counters =>
                                        if counters.isDefinedAt(id) then op(counters(id)) <*> ZIO.succeed(counters) else ZIO.succeed(0) <*> SimpleCounter.make(0).catchAll:
                                                e => ()).map(counter => counters ++ counter))

                /** @inheritdoc */
                def increment(id: CounterId) = actionOnSpecificCounter(_.increment)

                /** @inheritdoc */
                def decrement(id: CounterId) = actionOnSpecificCounter(_.decrement)

                /** @inheritdoc */
                def get(id: CounterId) = actionOnSpecificCounter(_.get)

                /** @inheritdoc */
                def reset(id: CounterId) = actionOnSpecificCounter(_.reset)  

                /**
                 * Removes the counter identified by `id`.
                 * @param id Identifier of the counter to remove.
                 */
                def remove(id: CounterId) = 
                        countersRef.modifyZIO:
                                counters => 
                                        if counters.isDefinedAt(id) then ZIO.succeed(()) <*> ZIO.succeed(counters.remove(id)) else ZIO.succeed(0) <*> SimpleCounter.make(0).catchAll:
                                                e => ()).map(counter => counters ++ counter))

object SimpleCounterManager:
        /**
         * Creates a SimpleCounterManager with an initial sequence of counters.
         * @param counters Initial sequence of counters to manage.
         * @return A SimpleCounterManager or an error if no counters are provided.
         */
        def make(counters: IndexedSeq[CounterService]): IO[IllegalArgumentException, SimpleCounterManager] =
                for {
                        _ <- requireZ(!counters.isEmpty, "Should provide at least One counter!")
                        ref <- Ref.Synchronized.make(counters)
                } yield SimpleCounterManager(ref)



        

                

                
                               

                

        







