package z

import java.io.File

/**
 * Different modalities with their respective payloads.
 */
enum Modality{
        /**
         * A textual modality carrying a string payload.
         * @param payload The text content.
         */
        case Text(payload: String)

        /**
         * A voice modality carrying an audio file.
         * @param file The audio file containing the voice recording.
         */
        case Voice(file: File)

        /**
         * A sound modality using an MP4 file.
         * @param mp4 The multimedia file containing the sound.
         */
        case Sound(mp4: File)

        /**
         * A physical modality with a positional value.
         * @param pos The position in a given space.
         */
        case Physics(pos: Int)
}


/**
 * The state of a web crawling process, tracking visited URLs and encountered errors.
 *
 * @tparam E The type of errors collected during crawling.
 * @param errors A list of errors encountered during the crawling process.
 * @param visited A set of URLs that have already been visited.
 */
final case class CrawlState[+E](errors: List[E], visited: Set[URL]){
        
        /**
         * Marks a URL as visited and returns the updated crawl state.
         *
         * @param url The URL to mark as visited.
         * @return A new CrawlState with the updated visited set.
         */
        def visit(url: URL): CrawlState[E] =
                copy(visited = visited + url)

        /**
         * Marks multiple URLs as visited and returns the updated crawl state.
         *
         * @param urls The collection of URLs to mark as visited.
         * @return A new CrawlState with the updated visited set.
         */
        def visitAll(urls: Iterable[URL]): CrawlState[E] =
                copy(visited = visited ++ urls)

        /**
         * Logs an error encountered during crawling and returns the updated crawl state.
         *
         * @tparam E1 A supertype of the current error type, allowing error type widening.
         * @param error The error to log.
         * @return A new CrawlState with the error added to the list.
         */
        def logError[E1 >: E](error: E1): CrawlState[E1] =
                copy(errors = error :: errors)
}

/**
 * Companion object for [[CrawlState]], providing utility methods.
 */
object CrawlState{
        /**
         * An empty [[CrawlState]] instance with no errors and no visited URLs.
         */
        val empty: CrawlState[Nothing] =
                CrawlState(List.empty, Set.empty)
}

/**
 * Crawls a set of URLs concurrently using a specified routing and processing function.
 *
 * <p> This function initializes a crawl state and uses a concurrent queue to manage URLs to visit. It starts by seeding the crawler with the given URLs and the spawns 100 worker 
 * fibers to continuously fetch and process URLs. Each URL is fetched using a provided web client, and the resulting HTML content is processed using the provided processor function. 
 * The crawler tracks visited URLs and collect any errors encountered during processing. 
 * </p>
 *
 * @param seeds 
 *      the initial set of URLs to start crawling from.
 * @param router 
 *      a predicate function that determines whether a given URL should be processed. Only URLs for which this function returns true will be processed.
 * @param processor 
 *      a function that takes a URL (as a string) and returns an effectful computation that processes the content of the URL. Errors encountered during 
 *      processing are logged and collected.
 * @tparam R 
 *      the type of the environment required by the processor function.
 * @tparam E 
 *      the type of errors that may occur during URL processing. These errors are collected and returned as a list.
 * @return 
 *      a ZIO effect that, when executed, will perform the crawl and eventually yield a list of errors encountered during processing. The effect requires
 *      an environment of type R with Web and cannot fail (error type Nothing).
 */
def crawl[R, E](seeds: Set[URL], router: URL => Boolean, processor: URLString => ZIO[R, E, Unit]): ZIO[R with Web, Nothing, List[E]] = {
        Ref.make[CrawlState[E]](CrawlState.empty).flatMap{ crawlState =>
                Ref.make(0).flatMap{ ref =>
                        Promise.make[Nothing, Unit].flatMap{ promise =>
                                ZIO.acquireReleaseWith(Queue.unbounded[URL])(_.shutdown){ queue =>
                                        val onDone: ZIO[Any, Nothing, Unit] = ref.modify{ n => if (n == 1) then (queue.shutdown <* promise.succeed(()), 0) else (ZIO.unit, n - 1) 
                                        }.flatten
                                        val worker: ZIO[R with Web, Nothing, Unit] = queue.take.flatMap{ url =>
                                                web.getURL(url).flatMap{ html =>
                                                        val urls = extractURLs(url, html).filter(router)
                                                        for {
                                                                urls <- crawlState.modify{ state => (urls -- state.visited, state.visitAll(urls)) }
                                                                _     <- processor(url, html).catchAll{ e => crawlState.update(_.logError(e)) }
                                                                _     <- queue.offerAll(urls)
                                                                _     <- ref.update(_ + urls.size)
                                                        } yield ()
                                                }.ignore <* onDone
                                        }
                                        for {
                                                _     <- crawlState.update(_.visitAll(seeds))
                                                _     <- ref.update(_ + seeds.size)
                                                _     <- queue.offerAll(seeds)
                                                _     <- ZIO.collectAll(ZIO.replicate(100)(worker.forever.fork))
                                                _     <- promise.await
                                                state <- crawlState.get
                                        } yield state.errors
                                }
                        }
                }
        }
}