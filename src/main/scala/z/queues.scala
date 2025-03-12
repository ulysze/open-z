package z

import zio._

trait LoadBalancer[A]:
        def submit(work: A): Task[A]
        def shutdown: Task[Unit]

object LoadBalancer:
        def RoundRobin[A]: Task[A] = 
                requireZ(workerCount > 0, "worker count must be positive") *> Queue.bounded[workerCount].flatMap:
                        q =>
                                ZIO.unit <&! ZIO.forEachPar((1 to workerCount))(_ => q.take.map(work => process(work)).forever) *> roundRobinBalancer(q)

case class roundRobinBalancer[A] private(q: Queue[A]) extends LoadBalancer[A]:
        override def submit(work: A): UIO[Boolean] = q.offer(work)
        override def shutdown: Task[Unit] = q.shutdown


trait rateLimiter:
        def acquire: ZIO[Any, FullRateLimiterException, Unit]

object rateLimiter:
        def Simple(rateLimit: Int, maxWaitingTime: Duration, context: RateLimitingContext) =
                def fill(q: Queue[Unit]): UIO[Unit] = q.size.map(size => q.offerAll(List.fill(q.capacity - size)(())).delay(1.second))

                requireZ(rateLimit > 0, "Rate Limit must be strictly positive") *> Queue.bounded[Unit](rateLimit).flatMap(q => (ZIO.unit <&! fill(q).forever).map:
                        _ => SimpleRateLimiter(rateLimit, maxWaitingTime, q, context))

case class SimpleRateLimiter(rateLimit: Int, maxWaitingTime: Duration, queue: Queue[Unit], context: RateLimitingContext):
        override def acquire: ZIO[Any, FullRateLimiterException, Unit] = queue.take.timeout(maxWaitingTime).someOrFail(new FullRateLimiterException(context))
        
        
        
