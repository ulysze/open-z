package z

import zio.test._
import zio.test.Assertion._
import zio._

/** The CacheSpec object defines unit tests for the Cache data structure using ZIO Test. These tests ensure that items stored in
  * the cache can be retrieved before their expiration time, and that they are unavailable after their expiration time.
  */
object CacheSpec extends ZIOSpecDefault:
    /** The 'spec' method aggregates all tests for the Cache functionality:
      *
      * 1) "availible before expiration": Ensures that an item added to the cache can still be retrieved before the expiration
      * period ends. 2) "unavailible after expiration": Confirms that an item is no longer available once its expiration time has
      * passed.
      */
    def spec: Spec[TestEnvironment & Scope, Any] =
        /** This test stores an item in the cache with a 30-minute expiration time and attempts to retrieve it immediately to
          * assert successful completion, verifying the cache works correctly before expiration.
          */
        test("availible before expiration"): for {
            cache <- Cache.make[Int, String](30.minutes)
            _ <- cache.add(3, "Password")
            _ <- cache.retrieve(3)
        } yield assertCompletes

        /** This test stores an item in the cache with a 2-hour expiration time, advances the test clock by 2 hours, and then
          * checks if the item is still in the cache. As it should be expired, the test asserts the item is no longer available.
          */
        test("unavailible after expiration"): for {
            cache <- Cache.make[Int, String](2.hours)
            _ <- cache.add(12, "Personal Informations")
            _ <- TestClock.adjust(2.hours)
            b <- cache.contains(12)
        } yield assertTrue(!b)
