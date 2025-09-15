package com.udacity.webcrawler;

import com.udacity.webcrawler.json.CrawlResult;
import com.udacity.webcrawler.parser.PageParser;
import com.udacity.webcrawler.parser.PageParserFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.regex.Pattern;


/**
 * A concrete implementation of {@link WebCrawler} that runs multiple threads on a
 * {@link ForkJoinPool} to fetch and process multiple web pages in parallel.
 */
final class ParallelWebCrawler implements WebCrawler {

    private final Clock clock;
    private final Duration timeout;
    private final int popularWordCount;
    private final ForkJoinPool pool;
    private final List<Pattern> ignoredUrls;
    private final int maxDepth;
    private final PageParserFactory parserFactory;

    @Inject
    ParallelWebCrawler(
            Clock clock,
            PageParserFactory pageParserFactory,
            @Timeout Duration timeout,
            @PopularWordCount int popularWordCount,
            @TargetParallelism int threadCount,
            @IgnoredUrls List<Pattern> ignoredUrls,
            @MaxDepth int maxDepth,
            PageParserFactory parserFactory) {

        this.clock = clock;
        this.timeout = timeout;
        this.popularWordCount = popularWordCount;
        this.pool = new ForkJoinPool(Math.min(threadCount, getMaxParallelism()));
        this.ignoredUrls = List.copyOf(ignoredUrls);
        this.maxDepth = maxDepth;
        this.parserFactory = parserFactory;
    }

    @Override
    public CrawlResult crawl(List<String> startingUrls) {
        Instant deadline = clock.instant().plus(timeout);

        ConcurrentMap<String, Integer> wordCounts = new ConcurrentHashMap<>();
        ConcurrentSkipListSet<String> visitedUrls = new ConcurrentSkipListSet<>();

        startingUrls.forEach(url -> pool.invoke(new CrawlTask(url, deadline, maxDepth, wordCounts, visitedUrls)));

        Map<String, Integer> sortedWordCounts = WordCounts.sort(wordCounts, popularWordCount);

        return new CrawlResult.Builder()
                .setWordCounts(sortedWordCounts)
                .setUrlsVisited(visitedUrls.size())
                .build();
    }

    private static CrawlResult emptyResult() {
        return new CrawlResult.Builder()
                .setWordCounts(Map.of())
                .setUrlsVisited(0)
                .build();
    }

    private class CrawlTask extends RecursiveTask<Boolean> {
        private final String url;
        private final Instant deadline;
        private final int depth;
        private final ConcurrentMap<String, Integer> wordCounts;
        private final Set<String> visitedUrls;

        CrawlTask(String url, Instant deadline, int depth,
                  ConcurrentMap<String, Integer> wordCounts, Set<String> visitedUrls) {
            this.url = url;
            this.deadline = deadline;
            this.depth = depth;
            this.wordCounts = wordCounts;
            this.visitedUrls = visitedUrls;
        }

        @Override
        protected Boolean compute() {
            if (depth == 0 || clock.instant().isAfter(deadline)) {
                return false;
            }

            if (ignoredUrls.stream().anyMatch(pattern -> pattern.matcher(url).matches())) {
                return false;
            }

            // Avoid revisiting URLs
            if (!visitedUrls.add(url)) {
                return false;
            }

            PageParser.Result result;
            try{
                result = parserFactory.get(url).parse();
            }catch (Exception e){
                return false;
            }

            result.getWordCounts().forEach((word, count) -> wordCounts.merge(word, count, Integer::sum));

            List<CrawlTask> subtasks = result.getLinks().stream()
                    .map(link -> new CrawlTask(link, deadline, depth - 1, wordCounts, visitedUrls))
                    .toList();

            invokeAll(subtasks);

            return true;
        }
    }

    @Override
    public int getMaxParallelism() {
        return Runtime.getRuntime().availableProcessors();
    }

}
