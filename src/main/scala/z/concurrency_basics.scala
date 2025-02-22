package z

import zio._
import z.Wexecutors._
import java.net.URL

object app extends App:
        def main() = scala.Console.println("Hello")

trait RestService:
        def fetchUrl(url: URL): Task[String]

        def fetchAllUrlsPar(urls: Seq[URL]) = { 
                ZIO.partitionPar(urls) { url =>
                        (ZIO.succeed(url) <*> (fetchUrl(url))).mapError(t => (url, t))
                }
        }

        
