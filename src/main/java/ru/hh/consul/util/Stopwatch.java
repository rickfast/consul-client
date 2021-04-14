package ru.hh.consul.util;

import java.time.Duration;
import java.util.function.LongSupplier;

public class Stopwatch {

  private final LongSupplier nanoClock;
  private boolean isRunning;
  private long elapsedNanos;
  private long startTick;

  public Stopwatch() {
    this.nanoClock = System::nanoTime;
  }

  public Duration elapsed() {
    return Duration.ofNanos(isRunning ? nanoClock.getAsLong() - startTick + elapsedNanos : elapsedNanos);
  }

  public boolean isRunning() {
    return isRunning;
  }

  public Stopwatch start() {
    if (isRunning) {
      throw new IllegalStateException("This stopwatch is already running.");
    }
    isRunning = true;
    startTick = nanoClock.getAsLong();
    return this;
  }

  public Stopwatch stop() {
    if (!isRunning) {
      throw new IllegalStateException("This stopwatch is already stopped.");
    }
    long tick = nanoClock.getAsLong();
    isRunning = false;
    elapsedNanos += tick - startTick;
    return this;
  }

  public Stopwatch reset() {
    elapsedNanos = 0;
    isRunning = false;
    return this;
  }
}
