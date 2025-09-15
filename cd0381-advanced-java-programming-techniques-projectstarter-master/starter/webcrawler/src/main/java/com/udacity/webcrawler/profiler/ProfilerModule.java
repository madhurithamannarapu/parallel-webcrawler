package com.udacity.webcrawler.profiler;

import com.google.inject.AbstractModule;

/**
 * Guice module for setting up the Profiler dependency injection.
 */
public final class ProfilerModule extends AbstractModule {

    @Override
    protected void configure() {
        // You can add custom bindings here if required.
        bind(Profiler.class).asEagerSingleton();
    }
}