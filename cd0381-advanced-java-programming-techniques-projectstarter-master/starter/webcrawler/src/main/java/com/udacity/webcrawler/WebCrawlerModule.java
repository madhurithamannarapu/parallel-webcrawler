package com.udacity.webcrawler;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.binder.ScopedBindingBuilder;
import com.udacity.webcrawler.json.CrawlerConfiguration;
import com.udacity.webcrawler.parser.PageParser;
import com.udacity.webcrawler.parser.PageParserFactory;
import com.udacity.webcrawler.profiler.Profiler;

import javax.inject.Singleton;


/**
 * Guice module that sets up dependency injection for the WebCrawler.
 */
public final class WebCrawlerModule extends AbstractModule {

    private PageParserFactory PageParserFactory;

    public WebCrawlerModule(CrawlerConfiguration config) {
    }

    @Override
    protected void configure() {
        // Bind PageParser to use PageParserProvider
        ScopedBindingBuilder provider = bind(PageParser.class);
        // Bind Profiler class so it can be injected
        bind(Profiler.class).asEagerSingleton();
    }
    }