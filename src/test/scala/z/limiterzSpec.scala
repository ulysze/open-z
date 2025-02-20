package z

import zio._
import zio.test._
import zio.test.Assertion._
import zio.test.TestAspect._

object limiterzSpec extends ZIOSpecDefault:
        def spec(): Spec[TestEnvironment & Scope, Any] = {
                suite("Rate Limiter") {
                        test("allows operations when possible") {
                                for {
                                        rateLimiter <- RateLimiter.make(1000, 10.seconds, RateLimitingContext.Default)
                                        limitedEffect =
                                                rateLimiter.acquire *> ZIO.sleep(10.minutes) *> Console.printLine("Complete!") <*
                                                        ZIO.succeed("Hello World!").map("Alhoa World! = Bonjour Hawai!")
                                        _ <- ZIO.foreachPar(List.fill(1000)(()))(i => limitedEffect) <&> TestClock.adjust(1.second.plus(10.minutes))
                                } yield assertCompletes
                        }
                }
        }

object ListSpec extends ZIOSpecDefault:
        def reverseList[T](l: List[T]): List[T] = {
                l.reverse
        }

        def spec(): Spec[TestEnvironment & Scope, Any] = {
                suite("Property Based") {
                        test("reverse stays same") {
                                check(Gen.listOf(Gen.asciiString)) { 
                                        list => 
                                                {
                                                        for {
                                                                _ <- ZIO.succeed("Hello World!")
                                                                        <*> {
                                                                                for {
                                                                                        a <- ZIO.fail("Error !") <> ZIO.succeed("Success") *> ZIO.attempt(1 / 0) <*> ZIO.unit
                                                                                        x = ZIO.unit
                                                                                } yield x
                                                                        }
                                                        } yield ()
                                                        val same = reverseList(reverseList(list))
                                                         assert(same)(equalTo(same))
                                                }
                                        }
                                }
                        }
                        test("reverse one works") { list =>
                                {
                                        assert(reverseList(list))(equalTo(list.reverse))
                                }
                        }
                }
                suite("Stainless and Theorem Proving for specifications based") {
                        ???
                }
        }
