package z

import zio._
import z.Preconditionz._
import z.Wexecutors._
import java.util.UUID

/**
 * A concurrency control mechanism that limits the rate at which operations can be performed.
 * For handling errors and using the Full Power of Scala. Use Pattern Matching to deconstruct the 
 * Exception and find the Context then compare with Types and value!
 * This is super powerful for handling toMuchRequest messages orient in locations manage logic etc. 
 * We recommand to create registries of RateLimiter and then use PAttern mathcing to decompose. 
 * Create différente categories of rateLimiters etc. This File provide a super powerful way to organize 
 * your Rate Limiting Logic
 * 
 * @param rateLimit   The maximum number of tokens available per time interval.
 * @param maxWaiting  The maximum number of requests that can wait in the queue.
 * @param queue       The internal queue used to hold tokens or pending requests.
 * @param context     The context in which rate limiting is applied.
 */
case class RateLimiter private(rateLimit: Int, maxWaiting: Int, queue: Queue[Unit], context: RateLimitingContext):

    /**
     * Attempts to acquire a token from the rate limiter.
     * If the queue is full, a [[FullRateLimiterException]] is thrown.
     * Otherwise, one token is removed from the queue.
     *
     * @return An IO effect that fails with [[FullRateLimiterException]] if the queue is full,
     *         or succeeds with `Unit` when a token is acquired.
     */
    def acquire: IO[FullRateLimiterException, Unit] = 
      for {
          q <- queue.isFull
          _ <- if q then ZIO.fail(new FullRateLimiterException(context))
               else queue.take
      } yield ()

/**
 * Companion object for [[RateLimiter]], providing a constructor method to create new instances.
 */
object RateLimiter:

    /**
     * Creates a new [[RateLimiter]] with the specified parameters, returning a ZIO effect.
     *
     * @param rateLimit  The maximum number of tokens available per time interval.
     * @param maxWaiting The maximum number of requests that can wait in the queue.
     * @param context    The context in which rate limiting is applied.
     * @return An IO effect that fails with [[IllegalArgumentException]] if `rateLimit` is not strictly positive,
     *         or succeeds with a [[RateLimiter]] instance once initialized.
     */
    def make(rateLimit: Int, maxWaiting: Int, context: RateLimitingContext): IO[IllegalArgumentException, RateLimiter] = 
        for {
            _ <- requireZ(rateLimit > 0, "Rate Limit must be strictly positive")
            q <- Queue.bounded[Unit](maxWaiting)
            _ <- ZIO.unit <&! q.offerAll((1 to rateLimit).map(i => ())).delay(1.second)
            uuid <- ZIO.randomWith(_.nextUUID)
        } yield new RateLimiter(rateLimit, maxWaiting, q, context)

/**
 * Defines the context in which rate limiting is applied.
 * 
 * Can be:
 *  - [[RateLimitingContext.Default]]: A default context with no additional location or region.
 *  - [[RateLimitingContext.Spatial]]: A specific region and location.
 */
enum RateLimitingContext:
    case Default
    case Spatial(region: Region, location: Location)

/**
 * Represents a geographical region for spatial rate limiting.
 */
enum Region:
    case Europe, Asia, USA

/**
 * Represents a specific city or location within a region.
 */
enum Location:
    case Paris, London, Tokyo, NewYork

/**
 * Base class for all errors related to rate limiting.
 */
abstract class RateLimiterError extends Throwable

/**
 * An exception indicating that the RateLimiter queue is full and cannot accept more requests.
 *
 * @param context The context in which the rate limiter is operating.
 */
case class FullRateLimiterException(context: RateLimitingContext) extends RateLimiterError:
    override def getMessage: String = s"Rate Limiter for $context: Maximum waiting requests reached!"