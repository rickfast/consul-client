package com.orbitz.consul.util;

import java.time.Duration;

public class Synchroniser {

    public static void pause(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
