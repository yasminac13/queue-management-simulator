package businesslogic;

import model.Task;
import model.Server;
import gui.SimulationFrame;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class SimulationManager implements Runnable {

    private int timeLimit;
    private int maxProcessingTime;
    private int minProcessingTime;
    private int maxArrivalTime;
    private int minArrivalTime;
    private int numberOfServers;
    private int numberOfClients;
    private SelectionPolicy selectionPolicy;
    private Scheduler scheduler;
    private SimulationFrame frame;
    private List<Task> generatedTasks;
    private PrintWriter fileWriter;
    private int totalServiceTime = 0;
    private int totalWaitTime = 0;
    private int peakHour = 0;
    private int maxClientsAtPeak = 0;

    public SimulationManager(SimulationFrame frame) {
        this.frame = frame;
        this.generatedTasks = new ArrayList<>();

        try {
            fileWriter = new PrintWriter(new FileWriter("simulation_log.txt"));
        } catch (IOException e) {
            System.err.println("Failed to initialize log file: " + e.getMessage());
        }
    }
    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }
    public void setMinProcessingTime(int minProcessingTime) {
        this.minProcessingTime = minProcessingTime;
    }
    public void setMaxArrivalTime(int maxArrivalTime) {
        this.maxArrivalTime = maxArrivalTime;
    }
    public void setMinArrivalTime(int minArrivalTime) {
        this.minArrivalTime = minArrivalTime;
    }
    public void setNumberOfServers(int numberOfServers) {
        this.numberOfServers = numberOfServers;
    }
    public void setNumberOfClients(int numberOfClients) {
        this.numberOfClients = numberOfClients;
    }
    public void setSelectionPolicy(SelectionPolicy selectionPolicy) {
        this.selectionPolicy = selectionPolicy;
    }
    public void setMaxProcessingTime(int maxProcessingTime) {
        this.maxProcessingTime = maxProcessingTime;
    }

    public void setupSimulation() {
        this.scheduler = new Scheduler(numberOfServers, 100);
        this.scheduler.changeStrategy(selectionPolicy);
        generateNRandomTasks();
    }

    private void generateNRandomTasks() {
        Random random = new Random();
        for (int i = 0; i < numberOfClients; i++) {
            int processingTime = random.nextInt(maxProcessingTime - minProcessingTime + 1) + minProcessingTime;
            int arrivalTime = random.nextInt(maxArrivalTime - minArrivalTime + 1) + minArrivalTime;
            totalServiceTime += processingTime;
            generatedTasks.add(new Task(i + 1, arrivalTime, processingTime));
        }
        generatedTasks.sort(Comparator.comparingInt(Task::getArrivalTime));
    }

    private boolean isSimulationComplete(int currentTime) {
        if (currentTime >= timeLimit) {
            return true;
        }
        if (!generatedTasks.isEmpty()){ // all clients dispatched, all queues empty
            return false;
        }

        for (Server server : scheduler.getServers()) {//clients in checkoutline
            if (server.getTasks().length > 0) return false;
        }
        return true;
    }

    @Override
    public void run() {
        setupSimulation();
        int currentTime = 0;

        while (!isSimulationComplete(currentTime)) {
            dispatchClients(currentTime);
            pauseSafely(50);
            calculateRealTimeStats(currentTime);
            logCurrentSecond(currentTime);

            currentTime++;
            if (!pauseSafely(950)) break;
        }

        printFinalResults();
    }

    private void dispatchClients(int currentTime) {
        Iterator<Task> iterator = generatedTasks.iterator();
        while (iterator.hasNext()) {
            Task task = iterator.next();
            if (task.getArrivalTime() == currentTime) {
                scheduler.dispatchTask(task);
                iterator.remove();
            }
        }
    }

    private void calculateRealTimeStats(int currentTime) {
        int currentClientsInSystem = 0;
        for (Server server : scheduler.getServers()) {
            int queueLength = server.getTasks().length;
            currentClientsInSystem += queueLength;
            if (queueLength > 1) {
                totalWaitTime += (queueLength - 1);
            }
        }
        if (currentClientsInSystem > maxClientsAtPeak) {
            maxClientsAtPeak = currentClientsInSystem;
            peakHour = currentTime;
        }
    }

    private boolean pauseSafely(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private void logCurrentSecond(int currentTime) {
        String currentLog = buildLogString(currentTime);
        if (frame != null) frame.appendToLog(currentLog);
        if (fileWriter != null) {
            fileWriter.println(currentLog);
            fileWriter.flush();
        }
    }

    private void printFinalResults() {
        double avgService;
        if (numberOfClients > 0) {
            avgService = (double) totalServiceTime / numberOfClients;
        } else {
            avgService = 0;
        }

        double avgWait;
        if (numberOfClients > 0) {
            avgWait = (double) totalWaitTime / numberOfClients;
        } else {
            avgWait = 0;
        }

        String result = "\nSimulation Finished.\n" +
                "Average Waiting Time: " + String.format("%.2f", avgWait) + "\n" +
                "Average Service Time: " + String.format("%.2f", avgService) + "\n" +
                "Peak Hour: " + peakHour;

        if (frame != null) {
            frame.appendToLog(result);
            frame.enableStartButton();
        }
        if (fileWriter != null) {
            fileWriter.println(result);
            fileWriter.close();
        }
    }

    private String buildLogString(int time) {
        StringBuilder sb = new StringBuilder();
        sb.append("Time ").append(time).append("\nWaiting clients: ");

        if (generatedTasks.isEmpty()) sb.append("none");
        else {
            for (int i = 0; i < generatedTasks.size(); i++) {
                Task t = generatedTasks.get(i);
                sb.append("(").append(t.getID()).append(", ").append(t.getArrivalTime())
                        .append(", ").append(t.getServiceTime()).append(")");
                if (i < generatedTasks.size() - 1) sb.append("; ");
            }
        }
        sb.append("\n").append(buildServersLogString());
        return sb.toString();
    }

    private String buildServersLogString() {
        StringBuilder sb = new StringBuilder();
        List<Server> servers = scheduler.getServers();
        for (int i = 0; i < servers.size(); i++) {
            Server s = servers.get(i);
            sb.append("Queue ").append(i + 1).append(": ");
            Task[] currentTasks = s.getTasks();
            if (currentTasks.length == 0) sb.append("closed");
            else {
                for (int j = 0; j < currentTasks.length; j++) {
                    Task t = currentTasks[j];
                    sb.append("(").append(t.getID()).append(", ").append(t.getArrivalTime())
                            .append(", ").append(t.getServiceTime()).append(")");
                    if (j < currentTasks.length - 1) sb.append("; ");
                }
            }
            sb.append("\n");
        }
        sb.append("--------------------------------------------------");
        return sb.toString();
    }
}