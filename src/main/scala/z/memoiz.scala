package z

import zio._
import scala.collection._
import z.Wexecutors._
import z.Preconditionz._

/** A generic cache with a time-to-live (TTL) mechanism. This cache stores key-value pairs along with their insertion timestamp. Items can be added, retrieved, and checked for
  * existence. Expired items are automatically removed by a background expiration daemon.
  *
  * @param ttl
  *   The time-to-live duration for items in the cache.
  * @tparam K
  *   The type of keys stored in the cache.
  * @tparam V
  *   The type of values stored in the cache.
  */
case class Cache[K, V] private (ttl: Duration):
        private val map = mutable.Map.empty[K, V]

        /** Adds an item to the cache with the specified key.
          *
          * @param key
          *   The key associated with the item.
          * @param value
          *   The value to store in the cache.
          * @return
          *   An asynchronous task that performs the insertion.
          */
        def add(key: K, value: V): Task[Unit] =
                for {
                        now <- Clock.instant()
                        _ <- ZIO.attempt(map.addOne((key, value)))
                        deamon = ZIO.fromOption(map.remove(key)).orElseFail(new NoSuchElementException).delay(ttl)
                        _ <- ZIO.unit <&! deamon
                } yield ()

        /** Retrieves an item from the cache by its key.
          *
          * @param key
          *   The key of the item to retrieve.
          * @return
          *   A task containing the associated value if it exists, or an error if the key is not found.
          */
        def retrieve(key: K): Task[V] = ZIO.fromOption(map.get(key)).catchAll(_ => ZIO.fail(new NoSuchElementException))

        /** Checks if a key exists in the cache.
          *
          * @param key
          *   The key to check.
          * @return
          *   A task containing a boolean indicating whether the key exists.
          */
        def contains(key: K): UIO[Boolean] = ZIO.succeed(map.contains(key))

/** Companion object for the Cache class. Provides a factory method to create a cache instance with a specified time-to-live (TTL) duration.
  */
object Cache:

        /** Creates a new cache instance with the given time-to-live duration.
          *
          * @param ttl
          *   The time-to-live duration for stored items. Must be greater than or equal to 5 seconds.
          * @tparam K
          *   The type of keys stored in the cache.
          * @tparam V
          *   The type of values stored in the cache.
          * @return
          *   A task containing an initialized cache instance.
          * @throws IllegalArgumentException
          *   if the duration is less than 5 seconds.
          */
        def make[K, V](ttl: Duration): Task[Cache[K, V]] =
                for {
                        _ <- requireZ(ttl >= 5.seconds, "duration should be greather than 5 seconds")
                } yield new Cache[K, V](ttl)
