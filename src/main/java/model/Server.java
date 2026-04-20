package model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Server implements Runnable {
    private BlockingQueue<Task> tasks;
    private AtomicInteger waitingPeriod;
    private volatile Task currentTask;

    public Server() {
        this.tasks = new LinkedBlockingQueue<>();
        this.waitingPeriod = new AtomicInteger(0);
    }

    public void addTask(Task newTask) {
        tasks.add(newTask);
        waitingPeriod.addAndGet(newTask.getServiceTime());
    }

    @Override
    public void run() {
        while (true) {
            try {
                currentTask = tasks.take();
                int processingTime = currentTask.getServiceTime();
                for (int i = 0; i < processingTime; i++) {
                    Thread.sleep(1000);
                    currentTask.setServiceTime(currentTask.getServiceTime() - 1);
                    waitingPeriod.decrementAndGet();
                }
                currentTask = null;

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public Task[] getTasks() {
        List<Task> allTasks = new ArrayList<>();
        if (currentTask != null) {
            allTasks.add(currentTask);
        }
        allTasks.addAll(tasks);

        return allTasks.toArray(new Task[0]);
    }

    public AtomicInteger getWaitingPeriod() {
        return waitingPeriod;
    }
}