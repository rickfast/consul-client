package ru.hh.consul.util;

import java.util.Objects;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadFactoryBuilder {
  private Boolean daemon;
  private String nameTemplate;
  private Boolean needSequence;

  public ThreadFactory build() {
    Objects.requireNonNull(daemon);
    Objects.requireNonNull(nameTemplate);
    Objects.requireNonNull(needSequence);
    if (needSequence) {
      return new ThreadFactory() {
        final AtomicInteger counter = new AtomicInteger();
        @Override
        public Thread newThread(Runnable r) {
          Thread thread = new Thread(r, nameTemplate + counter.getAndIncrement());
          thread.setDaemon(daemon);
          return thread;
        }
      };
    } else {
      return r -> {
        Thread thread = new Thread(r, nameTemplate);
        thread.setDaemon(daemon);
        return thread;
      };
    }

  }

  public ThreadFactoryBuilder setDaemon(boolean daemon) {
    this.daemon = daemon;
    return this;
  }

  public ThreadFactoryBuilder setNameTemplate(String nameTemplate) {
    this.nameTemplate = nameTemplate;
    return this;
  }

  public ThreadFactoryBuilder setNeedSequence(boolean needSequence) {
    this.needSequence = needSequence;
    return this;
  }
}
