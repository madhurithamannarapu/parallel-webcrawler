package com.udacity.webcrawler;

import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Provides static methods for sorting word counts.
 */
final class WordCounts {

    /**
     * Sorts the given word counts by popularity.
     *
     * @param counts           The map of word counts.
     * @param popularWordCount The number of top popular words to return.
     * @return A new map with the popular words and their counts.
     */
    public static Map<String, Integer> sort(Map<String, Integer> counts, int popularWordCount) {
        return counts.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(popularWordCount)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        java.util.LinkedHashMap::new
                ));
    }
}