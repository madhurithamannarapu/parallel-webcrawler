package com.udacity.webcrawler.json;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class CrawlerConfiguration {

    private List<String> startPages;
    private List<Pattern> ignoredUrls;
    private List<Pattern> ignoredWords;
    private int parallelism = -1; // default as in test
    private String implementationOverride = "";
    private int maxDepth;
    private Duration timeout;
    private int popularWordCount;
    private String profileOutputPath = "";
    private String resultPath = "";

    // Getters
    public List<String> getStartPages() {
        return startPages;
    }

    public List<Pattern> getIgnoredUrls() {
        return ignoredUrls;
    }

    public List<Pattern> getIgnoredWords() {
        return ignoredWords;
    }

    public int getParallelism() {
        return parallelism;
    }

    public String getImplementationOverride() {
        return implementationOverride;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public int getPopularWordCount() {
        return popularWordCount;
    }

    public String getProfileOutputPath() {
        return profileOutputPath;
    }

    public String getResultPath() {
        return resultPath;
    }

    // Setters or builder pattern (if needed)
}
