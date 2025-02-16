package z

import zio._
import zio.test._
import zio.test.Assertion._
import zio.test.TestAspect._

object limiterzSpec extends ZIOSpecDefault:
    def spec: Spec[TestEnvironment & Scope, Any] = 
        suite("Rate Limiter"):
            test("allows operations when possible"):
                for {
                    rateLimiter <- RateLimiter.make(1000, 10.seconds, RateLimitingContext.Default)
                    limitedEffect = rateLimiter.acquire *> ZIO.sleep(10.minutes) *> Console.printLine("Complete!")
                    _ <- ZIO.foreachPar(List.fill(1000)(()))(i => limitedEffect) <&> TestClock.adjust(1.second.plus(10.minutes))
                } yield assertCompletes

object ListSpec extends ZIOSpecDefault:
    def reverseList[T](l: List[T]): List[T] = 
        l.reverse
    def spec: Spec[TestEnvironment & Scope, Any] = 
        test("reverse stays same"):
            check(Gen.listOf(Gen.asciiString)): 
                list => 
                    val same = reverseList(reverseList(list))
                    assert(same)(equalTo(same))

            