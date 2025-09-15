package com.udacity.webcrawler.profiler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A method interceptor that checks whether {@link Method}s are annotated with the {@link Profiled}
 * annotation. If they are, the interceptor records how long the method invocation took.
 */
final class ProfilingMethodInterceptor implements InvocationHandler {

    private final Clock clock;
    private final Object delegate;
    private final ProfilingState state;
    private final ZonedDateTime startTime;

    ProfilingMethodInterceptor(Clock clock, Object delegate, ProfilingState state, ZonedDateTime startTime) {
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
        this.state = Objects.requireNonNull(state, "state must not be null");
        this.startTime = Objects.requireNonNull(startTime, "startTime must not be null");
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // Handle Object#equals(Object) explicitly
        if (method.getDeclaringClass() == Object.class
                && method.getName().equals("equals")
                && method.getParameterCount() == 1) {
            return delegate.equals(args[0]);
        }

        boolean profiled = method.isAnnotationPresent(Profiled.class);
        Instant start = profiled ? clock.instant() : null;

        try {
            return method.invoke(delegate, args);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to invoke method: " + method.getName(), e);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } finally {
            if (profiled) {
                Duration duration = Duration.between(start, clock.instant());
                state.record(delegate.getClass(), method, duration);
            }
        }
    }
}
