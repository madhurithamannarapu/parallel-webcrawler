package com.udacity.webcrawler;

import com.udacity.webcrawler.json.CrawlResult;
import com.udacity.webcrawler.parser.PageParser;
import com.udacity.webcrawler.parser.PageParserFactory;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * A {@link WebCrawler} that downloads and processes one page at a time sequentially.
 */
final class SequentialWebCrawler implements WebCrawler {

    private final Clock clock;
    private final PageParserFactory parserFactory;
    private final Duration timeout;
    private final int popularWordCount;
    private final int maxDepth;
    private final List<Pattern> ignoredUrls;

    @Inject
    SequentialWebCrawler(
            Clock clock,
            PageParserFactory parserFactory,
            @Timeout Duration timeout,
            @PopularWordCount int popularWordCount,
            @MaxDepth int maxDepth,
            @IgnoredUrls List<Pattern> ignoredUrls) {

        this.clock = clock;
        this.parserFactory = parserFactory;
        this.timeout = timeout;
        this.popularWordCount = popularWordCount;
        this.maxDepth = maxDepth;
        this.ignoredUrls = List.copyOf(ignoredUrls); // Defensive copy for immutability
    }

    @Override
    public CrawlResult crawl(List<String> startingUrls) {
        Instant deadline = clock.instant().plus(timeout);
        Map<String, Integer> counts = new HashMap<>();
        Set<String> visitedUrls = new HashSet<>();

        for (String url : startingUrls) {
            crawlInternal(url, deadline, maxDepth, counts, visitedUrls);
        }

        Map<String, Integer> sortedCounts = WordCounts.sort(counts, popularWordCount);

        return new CrawlResult.Builder()
                .setWordCounts(sortedCounts)
                .setUrlsVisited(visitedUrls.size())
                .build();
    }

    private void crawlInternal(
            String url,
            Instant deadline,
            int maxDepth,
            Map<String, Integer> counts,
            Set<String> visitedUrls) {

        if (maxDepth == 0 || clock.instant().isAfter(deadline)) {
            return;
        }

        if (ignoredUrls.stream().anyMatch(pattern -> pattern.matcher(url).matches())) {
            return;
        }

        if (!visitedUrls.add(url)) {
            return; // Already visited
        }

        PageParser.Result result = parserFactory.get(url).parse();

        result.getWordCounts()
                .forEach((word, count) -> counts.merge(word, count, Integer::sum));

        for (String link : result.getLinks()) {
            crawlInternal(link, deadline, maxDepth - 1, counts, visitedUrls);
        }
    }
}
