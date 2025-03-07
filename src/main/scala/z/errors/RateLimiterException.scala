
// also possible de define other base type of errors for our error system. (but need to define causes, etc) -> For more precise ZIO type aliases, etc. 

trait RateLimiterException extends Throwable

object RateLimiterException:
        enum Location:
                case Paris, London, Tokyo, NewYork

        enum Region:
                case Europe, Asia, USA

        enum RateLimitingContext:
                case Default
                case Spatial(region: Region, location: Location)


case class FullRateLimiterException(context: RateLimiterException.RateLimitingContext) extends RateLimiterException:
        override def getMessage(): String = s"Rate Limiter for $context: Maximum waiting requests reached!"
