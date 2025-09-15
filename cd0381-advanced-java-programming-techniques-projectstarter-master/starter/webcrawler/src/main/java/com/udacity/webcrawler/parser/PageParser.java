package com.udacity.webcrawler.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface PageParser {

    Result parse();

    final class Result {
        private final Map<String, Integer> wordCounts;
        private final List<String> links;

        public Result(Map<String, Integer> wordCounts, List<String> links) {
            this.wordCounts = wordCounts;
            this.links = links;
        }

        public Map<String, Integer> getWordCounts() {
            return wordCounts;
        }

        public List<String> getLinks() {
            return links;
        }

        public static class Builder {
            private final Map<String, Integer> wordCounts = new HashMap<>();
            private final List<String> links = new ArrayList<>();

            public Builder addWord(String word) {
                if (word == null || word.isEmpty()) {
                    return this; // ignore invalid words
                }
                wordCounts.put(word, wordCounts.getOrDefault(word, 0) + 1);
                return this;
            }

            public Builder addLink(String link) {
                if (link == null || link.isEmpty()) {
                    return this; // ignore invalid links
                }
                links.add(link);
                return this;
            }

            public Result build() {
                // Return an immutable Result object with copies of current data
                return new Result(Map.copyOf(wordCounts), List.copyOf(links));
            }
        }
    }
}