package com.orbitz.consul;

import okhttp3.internal.Util;
import org.junit.Test;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

public class LifecycleTests extends BaseIntegrationTest {

    @Test
    public void shouldBeDestroyable() {
        Consul client = Consul.builder().withHostAndPort(defaultClientHostAndPort).build();
        client.destroy();
    }

    @Test
    public void shouldDestroyTheExecutorServiceWhenDestroyMethodIsInvoked() throws InterruptedException {
        ExecutorService executorService = mock(ExecutorService.class, (Answer) invocationOnMock -> {
            throw new UnsupportedOperationException("Mock Method should not be called");
        });

        doReturn(new ArrayList<>()).when(executorService).shutdownNow();

        Consul client = Consul.builder().withHostAndPort(defaultClientHostAndPort).withExecutorService(executorService).build();
        client.destroy();

        verify(executorService, times(1)).shutdownNow();
    }

    @Test
    public void shouldBeDestroyableWithCustomExecutorService() throws InterruptedException {
        ExecutorService executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
                new SynchronousQueue<>(), Util.threadFactory("OkHttp Dispatcher", false));

        executorService.execute(() -> {
            Thread currentThread = Thread.currentThread();
            System.out.println("This is a Task printing a message in Thread " + currentThread);
        });
        Consul client = Consul.builder().withHostAndPort(defaultClientHostAndPort).withExecutorService(executorService).build();
        client.destroy();
    }

    public static void main(String[] args) {
        ExecutorService executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 10, TimeUnit.SECONDS,
                new SynchronousQueue<>(), Util.threadFactory("OkHttp Dispatcher", false));

        // execute a task in order to force the creation of a Thread in the ThreadPool
        executorService.execute(() -> {
            Thread currentThread = Thread.currentThread();
            System.out.println("This is a Task printing a message in Thread " + currentThread);
        });

        Consul client = Consul.builder().withHostAndPort(defaultClientHostAndPort).withExecutorService(executorService).build();

        // do not destroy the Consul client
        // in order to verify that the Java VM does not terminate, and waits for some time (keepAliveTime parameter of the ThreadPoolExecutor)
        // if we call destroy() then the JVM terminates immediately

        //client.destroy();
    }

}
