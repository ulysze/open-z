package z

/**
 * Crawls a set of URLs concurrently using a specified routing and processing function.
 *
 * <p> This function initializes a crawl state and uses a concurrent queue to manage URLs to visit. It starts by seeding the crawler with the given URLs and then spawns 100 worker fibers to continuously 
 * fetch and process URLs. Each URL is fetched using a provided web client, and the resulting HTML content is processed using the provided processor function. The crawler tracks visited URLs and collects
 * any errors encountered during processing. </p>
 *
 * @param seeds 
 *      the initial set of URLs to start crawling from.
 * @param router 
 *      a predicate function that determines whether a given URL should be processed. Only URLs for which this function returns true will be processed.
 * @param processor 
 *      a function that takes a URL (as a string) and returns an effectful computation that processes the content of the URL. Errors encountered during processing are logged and collected.
 * @tparam R 
 *      the type of the environment required by the processor function.
 * @tparam E 
 *      the type of errors that may occur during URL processing. These errors are collected and returned as a list.
 * @return 
 *      a ZIO effect that, when executed, will perform the crawl and eventually yield a list of errors encountered during processing. The effect requires an environment of type R with Web and cannot 
 *      fail (error type Nothing).
 */
def crawl[R, E](seeds: Set[URL], router: URL => Boolean, processor: URLString => ZIO[R, E, Unit]): ZIO[R with Web, Nothing, List[E]] = {
        Ref.make[CrawlState[E]](CrawlState.empty).flatMap { crawlState =>
                Ref.make(0).flatMap { ref =>
                        Promise.make[Nothing, Unit].flatMap { promise =>
                                ZIO.acquireReleaseWith(Queue.unbounded[URL])(_.shutdown) { queue =>
                                        val onDone: ZIO[Any, Nothing, Unit] = ref.modify(n => if (n == 1) (queue.shutdown <* promise.succeed(()), 0) else (ZIO.unit, n - 1)).flatten
                                        val worker: ZIO[R with Web, Nothing, Unit] = queue.take.flatMap { url =>
                                                web.getURL(url).flatMap { html =>
                                                        val urls = extractURLs(url, html).filter(router)
                                                        for {
                                                                urls <- crawlState.modify { state => (urls -- state.visited, state.visitAll(urls)) }
                                                                _     <- processor(url, html).catchAll { e => crawlState.update(_.logError(e)) }
                                                                _     <- queue.offerAll(urls)
                                                                _     <- ref.update(_ + urls.size)
                                                        } yield ()
                                                }.ignore <* onDone
                                        }
                                        for {
                                                _     <- crawlState.update(_.visitAll(seeds))
                                                _     <- ref.update(_ + seeds.size)
                                                _     <- queue.offerAll(seeds)
                                                _     <- ZIO.collectAll { ZIO.replicate(100)(worker.forever.fork) }
                                                _     <- promise.await
                                                state <- crawlState.get
                                        } yield state.errors
                                }
                        }
                }
        }
}