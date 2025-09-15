package com.udacity.webcrawler.main;

import com.udacity.webcrawler.json.ConfigurationLoader;
import com.udacity.webcrawler.json.CrawlerConfiguration;

import java.io.IOException;
import java.nio.file.Path;

public final class WebCrawlerMain {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: WebCrawlerMain <config-file>");
            System.exit(1);
        }

        // Load configuration from JSON file
        Path configPath = Path.of(args[0]);
        ConfigurationLoader loader = new ConfigurationLoader(configPath);
        CrawlerConfiguration config = loader.load();

        // Print configuration (debug)
        System.out.println("Loaded configuration:");
        System.out.println("Max Depth: " + config.getMaxDepth());
        System.out.println("Timeout: " + config.getTimeout());
        System.out.println("Start Pages: " + config.getStartPages());
        System.out.println("Popular Words: " + config.getPopularWordCount());


    }
}
