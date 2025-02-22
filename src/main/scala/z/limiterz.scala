package z

import zio._
import z.Preconditionz._
import z.Wexecutors._
import java.util.UUID

/** A concurrency control mechanism that limits the rate at which operations can be performed. For handling errors and using the Full Power of Scala. Use
  * Pattern Matching to deconstruct the Exception and find the Context then compare with Types and value! This is super powerful for handling toMuchRequest
  * messages orient in locations manage logic etc. We recommand to create registries of RateLimiter and then use PAttern mathcing to decompose. Create
  * différente categories of rateLimiters etc. This File provide a super powerful way to organize your Rate Limiting Logic. The user can create is Own
  * Contexts... -> We give Examples/ Possible Implemenations. To implement a RateLimiter, the User Must Define a context... -> Le laisser abstrait alors..
  * Pattern Commun -> Peut-être utilisé trait ? -> A voir. TODO: Make it generic with the Context C -> Allows users to
  *
  * @param rateLimit
  *   The maximum number of tokens available per time interval.
  * @param maxWaiting
  *   The maximum number of requests that can wait in the queue.
  * @param queue
  *   The internal queue used to hold tokens or pending requests.
  * @param context
  *   The context in which rate limiting is applied.
  */
case class RateLimiter private (rateLimit: Int, maxWaitingTime: Duration, queue: Queue[Unit], context: RateLimitingContext):
        /** Attempts to acquire a token from the rate limiter. If the queue is full, a [[FullRateLimiterException]] is thrown. Otherwise, one token is
          * removed from the queue.
          *
          * @return
          *   An IO effect that fails with [[FullRateLimiterException]] if the queue is full, or succeeds with `Unit` when a token is acquired.
          */
        def acquire: ZIO[Any, FullRateLimiterException, Unit] = queue.take.timeout(maxWaitingTime).someOrFail(new FullRateLimiterException(context))

/** Companion object for [[RateLimiter]], providing a constructor method to create new instances.
  */
object RateLimiter:
        /** Creates a new [[RateLimiter]] with the specified parameters, returning a ZIO effect.
          *
          * @param rateLimit
          *   The maximum number of tokens available per time interval.
          * @param maxWaiting
          *   The maximum number of requests that can wait in the queue.
          * @param context
          *   The context in which rate limiting is applied.
          * @return
          *   An IO effect that fails with [[IllegalArgumentException]] if `rateLimit` is not strictly positive, or succeeds with a [[RateLimiter]] instance
          *   once initialized.
          */
        def make(rateLimit: Int, maxWaitingTime: Duration, context: RateLimitingContext) =
                def fill(q: Queue[Unit]): UIO[Unit] =
                        for {
                                size <- q.size
                                _ <- q.offerAll(List.fill(q.capacity - size)(())).delay(1.second)
                        } yield ()
                for {
                        _ <- requireZ(rateLimit > 0, "Rate Limit must be strictly positive")
                        q <- Queue.bounded[Unit](rateLimit)
                        _ <- ZIO.unit <&! fill(q).forever
                } yield new RateLimiter(rateLimit, maxWaitingTime, q, context)

/** Defines the context in which rate limiting is applied.
  *
  * Can be:
  *   - [[RateLimitingContext.Default]]: A default context with no additional location or region.
  *   - [[RateLimitingContext.Spatial]]: A specific region and location.
  */
enum RateLimitingContext:
        case Default
        case Spatial(region: Region, location: Location)

/** Represents a geographical region for spatial rate limiting.
  */
enum Region:
        case Europe,
                Asia,
                USA

/** Represents a specific city or location within a region.
  */
enum Location:
        case Paris,
                London,
                Tokyo,
                NewYork

/** Base class for all errors related to rate limiting.
  */
abstract class RateLimiterException extends Throwable

/** An exception indicating that the RateLimiter queue is full and cannot accept more requests.
  *
  * @param context
  *   The context in which the rate limiter is operating.
  * @tparam C
  *   The type of the context
  */
case class FullRateLimiterException(context: RateLimitingContext) extends RateLimiterException:
        override def getMessage: String = s"Rate Limiter for $context: Maximum waiting requests reached!"
