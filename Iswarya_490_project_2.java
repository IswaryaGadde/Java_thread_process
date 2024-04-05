package com.mycompany.iswarya_490_project_2;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


/**
 *
 * @author Iswarya gadde
 */
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

class Process implements Comparable<Process> {
    int processID;
    String name;
    int millisecondsToRun;
    String processClass;
    int priority;

    public Process(int processID, String name, int millisecondsToRun, String processClass, int priority) {
        this.processID = processID;
        this.name = name;
        this.millisecondsToRun = millisecondsToRun;
        this.processClass = processClass;
        this.priority = priority;
    }

    public void runProcess() {
        // printing a line when the process begins
        System.out.println("BEGIN " + getCurrentTime() + "\t" + processID + "\t" + name);
        try {
            // simulating the process running for a specified time    
            Thread.sleep(millisecondsToRun);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // printing a line when the process ends
        System.out.println("END " + getCurrentTime() + "\t" + processID + "\t" + name);
    }

    private String getCurrentTime() {
        // current time with milliseconds
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"));
    }

    @Override
    public int compareTo(Process other) {
        // comparing method to sort processes in the queue based on priority and process
        // class
        if (this.processClass.equals("RT") && !other.processClass.equals("RT")) {
            return -1;
        } else if (!this.processClass.equals("RT") && other.processClass.equals("RT")) {
            return 1;
        } else if (this.priority != other.priority) {
            return Integer.compare(this.priority, other.priority);
        } else {
            return Integer.compare(this.processID, other.processID);
        }
    }
}

public class Iswarya_490_project_2 {
   private static PriorityQueue<Process> priorityQueue = new PriorityQueue<>();
   private static final AtomicInteger processCounter = new AtomicInteger(234);
    public static ExecutorService executorService = Executors.newFixedThreadPool(5);
    private static volatile boolean shutdown = false;
    private static final Object lock = new Object();

    public static void main(String[] args) {
        startProcess();
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println(
                    "PROCESS {Milliseconds to Run} {RT|NM} {Priority} {Process Name} or SHUTDOWN to exit");
            while (true) {
                String line = scanner.nextLine();
                // if line is \n, continue
                if (line.equals("")) {
                    continue;
                }
                if (line.equals("SHUTDOWN")) {
                    shutdown = true;
                    shutdown();
                    break;
                }

                // splitting the line into 5 tokens
                String[] tokens = line.split(" ", 5);
                // adding process to the queue and notify threads
                addProcessToQueue(Integer.parseInt(tokens[1]), tokens[4], Integer.parseInt(tokens[1]), tokens[2],
                        Integer.parseInt(tokens[3]));
            }
        }
    }

    public static void startProcess() {
        // creating and starting threads to simulate the processor
        System.out.println("Creating and starting threads to simulate the processor");
        Thread[] threads = new Thread[5];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                while (true) {
                    if (shutdown == true) {
                        break;
                    }
                    synchronized (priorityQueue) {
                        if (!priorityQueue.isEmpty() && !shutdown) {
                            Process process = priorityQueue.poll();
                            if (process != null) {
                                CompletableFuture.runAsync(() -> {
                                    if (shutdown == false) {
                                        process.runProcess();
                                    }
                                }, executorService);
                            }
                        }
                    }
                }
            });
            threads[i].start();
        }
    }

    public static void addProcessToQueue(int processID, String name, int millisecondsToRun, String processClass,
            int priority) {
        processID=processCounter.incrementAndGet();
        addProcessToQueue(new Process(processID, name, millisecondsToRun, processClass, priority));
    }

    public static void addProcessToQueue(Process process) {
        // adding process to the queue and notify threads
        synchronized (priorityQueue) {
            priorityQueue.add(process);
            priorityQueue.notify();
        }
    }

    public static void shutdown() {
        synchronized (lock) {
            shutdown = true;
        }
        executorService.shutdown();
        try {
            // wait for running tasks to finish
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Process scheduler shut down.");
    }
}
