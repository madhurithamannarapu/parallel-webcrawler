package com.udacity.webcrawler.profiler;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Objects;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

/**
 * Concrete implementation of the {@link Profiler}.
 */
final class ProfilerImpl implements Profiler {

    private final Clock clock;
    private final ProfilingState state = new ProfilingState();
    private final ZonedDateTime startTime;

    ProfilerImpl(Clock clock) {
        this.clock = Objects.requireNonNull(clock);
        this.startTime = ZonedDateTime.now(clock);
    }

    private boolean isProfiledClass(Class<?> klass) {
        return Arrays.stream(klass.getDeclaredMethods())
                .anyMatch(m -> m.isAnnotationPresent(Profiled.class));
    }


    @Override
    @SuppressWarnings("unchecked")
    public <T> T wrap(Class<T> klass, T delegate) {
        Objects.requireNonNull(klass);

        if (!isProfiledClass(klass)) {
            throw new IllegalArgumentException(klass.getName() + " doesn't have profiled methods.");
        }

        InvocationHandler handler = new ProfilingMethodInterceptor(clock, delegate, state, startTime);

        return (T) Proxy.newProxyInstance(
                klass.getClassLoader(),    // use target class loader
                new Class<?>[]{klass},     // proxy only for this interface
                handler
        );
    }


    @Override
    public void writeData(Path path) {
        Objects.requireNonNull(path);

        try (Writer writer = Files.newBufferedWriter(
                path,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,      // create if not exists
                StandardOpenOption.APPEND       // append if exists
        )) {
            writeData(writer);
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to write profiling data", ex);
        }
    }


    @Override
    public void writeData(Writer writer) throws IOException {
        writer.write("Run at " + RFC_1123_DATE_TIME.format(startTime));
        writer.write(System.lineSeparator());
        state.write(writer);
        writer.write(System.lineSeparator());
    }
}
