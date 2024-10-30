package com.deathmotion.antihealthindicator.schedulers;

import com.deathmotion.antihealthindicator.interfaces.Scheduler;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class FabricScheduler implements Scheduler {

    private final ScheduledExecutorService executorService;

    public FabricScheduler() {
        this.executorService = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void runAsyncTask(Consumer<Object> task) {
        executorService.submit(() -> task.accept(null));
    }

    @Override
    public void runAsyncTaskDelayed(Consumer<Object> task, long delay, TimeUnit timeUnit) {
        executorService.schedule(() -> task.accept(null), delay, timeUnit);
    }

    @Override
    public void runAsyncTaskAtFixedRate(@NotNull Consumer<Object> task, long delay, long period, @NotNull TimeUnit timeUnit) {
        executorService.scheduleAtFixedRate(() -> task.accept(null), delay, period, timeUnit);
    }
}
